package edu.csula.cs594.client;

import edu.csula.cs594.client.dao.RealTimeData;
import edu.csula.cs594.client.dao.CummulativeData;
import edu.csula.cs594.client.dao.QueueItem;
import edu.csula.cs594.client.loadfunctions.*;
import edu.csula.cs594.client.loadfunctions.LoadFactory.Type;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a specific test instance. Also encapsulates logic to initialize all types of tests.
 */
public class CliClient implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CliClient.class);
    private final TestContext testContext;
    private LoadFactory.Type loadFunctionType;
    private Thread thread;
    private volatile boolean shutdown = false;

    public CliClient(TestContext testContext) {
        this.testContext = testContext;
    }

    public CliClient(TestContext testContext, LoadFactory.Type loadFunctionType) {
        this.testContext = testContext;
        this.loadFunctionType = loadFunctionType;
    }

    public void setThread(Thread t) {
        this.thread = t;
    }

    public Thread getThread() {
        return thread;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    private boolean isDone(long maxDurationMs) {
        long currentDuration = System.currentTimeMillis() - testContext.getStartTime();
        boolean done = currentDuration >= maxDurationMs;
        double failedPercentage = testContext.getFailedCalls().get() * 1.00 / (testContext.getConsumed().get() + testContext.getFailedCalls().get());
        boolean errorsExceeded = failedPercentage > (1.00) * testContext.getFailuresPermitted();
        
        logger.info("done=" + done + "; failedPercentage=" + failedPercentage + "; errorsExceeded=" + errorsExceeded + "; totalCalls=" + testContext.getConsumed().get());
        
        return done || errorsExceeded;
    }

    private long getRelativeTime() {
        return System.currentTimeMillis() - testContext.getStartTime();
    }

    public void shutdown() {
        shutdown = true;
    }

    @Override
    public void run() {
        // Use try-with-resources here so that we always clean-up after we're done
        try (HttpClient client = new HttpClient(testContext)) {
            switch (testContext.getTestType()) {
                case SCHEDULED:
                    startScheduledTest(client);
                    break;
                case PERFORMANCE:
                    startPerformanceTest(client);
                    break;
                case CAPACITY:
                    LoadFunction loadFunction = LoadFactory.getLoadFunction(loadFunctionType);
                    loadFunction.setLoad(testContext.getInitialUserCount(), testContext.getStepCount(), false);
                    loadFunction.setStepDuration(testContext.getStepDuration());
                    loadFunction.setStepCount(testContext.getStepCount());
                    loadFunction.setTotalDuration(testContext.getTestDuration());
                    warmUp(client);
                    testContext.setStartTime(System.currentTimeMillis());
                    if (loadFunctionType == Type.CAPACITY) {
                        startCapacityTest(client, loadFunction);
                    } else if (loadFunctionType != Type.UNKNOWN) {
                        startScalabilityTest(client, loadFunction);
                    } else {
                        logger.error("Unknown load function type.");
                    }
                    break;
                default:
                    logger.error("Couldn't determine valid test type.");
            }
        } finally {
            logger.info("Test complete!");
        }
    }

    /**
     * Starts a scheduled test. This tests a specific uri once every time interval, until the user shuts it down.
     *
     * @param client
     */
    private void startScheduledTest(HttpClient client) {
        testContext.setStartTime(System.currentTimeMillis());
        testContext.startConsumer();
        while (!shutdown) {
            RealTimeData realTimeData = new RealTimeData(getRelativeTime(), 1);
            
            // make a single async request and don't shutdown if requests fail:
            makeRequest(client, testContext, realTimeData, true, false);

            sleepAndShutdownIfInterrupted(testContext.getTestInterval());
        }
        testContext.getRunning().set(false);
    }

    /**
     * Starts a performance test. Make requests nonstop until user shuts CliClient down.
     *
     * @param client
     */
    private void startPerformanceTest(HttpClient client) {
        testContext.setStartTime(System.currentTimeMillis());
        testContext.startConsumer();
        
        RealTimeData realTimeData = initRTD(getRelativeTime(), 0);
        
        // loop making sync requests and shutdown if any requests fail:
        makeUnlimitedRequests(client, testContext, realTimeData, false, true);
        
        testContext.getRunning().set(false);
    }

    /**
     * Start a capacity test. Keep ramping up requests, with stepDuration pauses between ramps, until we get a failure.
     *
     * @param client
     * @param loadFunction
     */
    private void startCapacityTest(HttpClient client, LoadFunction loadFunction) {
        logger.info("Starting capacity test.");
        testContext.startConsumer();
        logger.info("Max duration is: " + testContext.getTestDuration() + " seconds");
        int requests = testContext.getInitialUserCount();
        while (!isDone(testContext.getTestDurationMs()) && !shutdown) {
            long relativeTimeBucket = getRelativeTime();
            logger.info("0 failures...Making " + requests + " requests until failure at time " + relativeTimeBucket + " ms.");

            RealTimeData realTimeData = initRTD(relativeTimeBucket, requests);
            //testContext.startConsumer();
            // loop making sync requests and shutdown if any requests fail:
            makeFiniteRequests(client, testContext, realTimeData, false, true);
            sleepAndShutdownIfInterrupted(testContext.getStepDuration());
            
            requests = loadFunction.getUserCountForTimeMs(0);
            logger.info("New request count: " + requests);
        }
        testContext.getRunning().set(false);
    }
    
    /**
     * Starts a scalability test. Make a specific number of requests (decided by the loadFunction) at every
     * relativeTimeBucket until user shuts down or the test duration is over.
     *
     * @param client
     * @param loadFunction
     */
    private void startScalabilityTest(HttpClient client, LoadFunction loadFunction) {
        logger.info("Starting scalability test.");
        testContext.startConsumer();
        logger.info("Max duration is: " + testContext.getTestDuration() + " seconds");

        while (!isDone(testContext.getTestDurationMs()) && !shutdown) {
            long relativeTimeBucket = getRelativeTime();
            int requests = loadFunction.getUserCountForTimeMs(relativeTimeBucket);
            logger.info(" - number of requests to be made = " + requests + " for time " + relativeTimeBucket + "ms");

            /* If 0 requests, still record that at time bucket x, 0 requests were made. */
            if (requests == 0) {
                QueueItem item = new QueueItem();
                item.setDuration(0);
                item.setUri(testContext.getUri());
                item.setRequests(0);
                item.setRelativeTimeBucket(relativeTimeBucket);
                item.setProjectid(testContext.getProjectId());
                if (null != testContext.getQueue()) {
                    testContext.getQueue().add(item);
                }
            }

            RealTimeData realTimeData = initRTD(relativeTimeBucket, requests);
            makeFiniteRequests(client, testContext, realTimeData, true, false);
            sleepAndShutdownIfInterrupted(testContext.getStepDuration());
        }
        testContext.getRunning().set(false);
    }
    
    private void sleepAndShutdownIfInterrupted(long millis) {
        try {
            logger.info("Sleeping for step duration, " + testContext.getStepDuration() + "ms...");
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            String error = "Test was interrupted while sleeping... Ending test.";
            logger.error(error, ex);
            testContext.setFinishMessage(error);
            shutdown = true;
        }
    }

    private void makeUnlimitedRequests(HttpClient client, TestContext testContext, RealTimeData realTimeData, boolean async, boolean failOnError) {
        while (!shutdown) {
            makeRequest(client, testContext, realTimeData, async, failOnError);
        }
    }

    private void makeFiniteRequests(HttpClient client, TestContext testContext, RealTimeData realTimeData, boolean async, boolean failOnError) {
        final int requests = realTimeData.getRequests();
        for (int i = 0; i < requests && !shutdown; i++) {
            makeRequest(client, testContext, realTimeData, async, failOnError);
        }
    }

    private void makeRequest(HttpClient client, TestContext testContext, RealTimeData realTimeData, boolean async, boolean failOnError) {
        final AtomicInteger successfulCalls = realTimeData.getSuccesses();
        final AtomicInteger failedCalls = realTimeData.getFailures();
        try {
            if (testContext.getMethod().equals("POST")) {

                client.makeHttpPostCall(realTimeData, async);
            } else {
                client.makeHttpCall(realTimeData, async);
            }
        } catch (IOException | SQLException | ExecutionException e) {
            logger.error(loadFunctionType + " load test: Couldn't make synchronous http call.", e);
        } catch (InterruptedException e) {
            String error = "Test was interrupted during an HTTP call... Ending test.";
            logger.error(error, e);
            testContext.setFinishMessage(error);
            shutdown = true;
        }
        if (successfulCalls.get() % 10000 == 0) {
            logger.info("... " + successfulCalls.get() + " calls successful...");
        }
        if (failOnError && failedCalls.get() > 0) {
            testContext.setFinishMessage("Test terminated with one or more failed service calls.");
            shutdown = true;
        }
    }

    /**
     * Makes a single warm up call to the service, then blocks the thread for warmUpDurationMs. We could change this
     * instead to make warm up calls non-stop until warmUpDurationMs expires, doesn't matter to me.
     *
     * @param client
     */
    private void warmUp(HttpClient client) {
        try {
            client.makeWarmUpCall();
        } catch (ExecutionException e) {
            logger.error("Couldn't make the warm up call.", e);
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted during warmUp. Ending test.", e);
            shutdown = true;
        }

        sleepAndShutdownIfInterrupted(testContext.getWarmUpDurationMs());
    }

    private RealTimeData initRTD(long relativeTimeBucket, int requests) {
    	if(testContext.isRetest()) {
    		initCumData(testContext.getProjectId());
    	}
        RealTimeData realTimeData = new RealTimeData(relativeTimeBucket, requests);
        testContext.getRealTimeDatas().add(realTimeData);
        int newReqCount = testContext.getRequests() + requests;
        testContext.setRequests(newReqCount); //incr the number of requests that the whole test will have made.
        return realTimeData;
    }

	private void initCumData(int id) {
		try {
			int[] result = testContext.getDbClient().getPerformanceDelta(id);
			if(result == null) {
				return;
			}
			CummulativeData cumdata = new CummulativeData(result);
			testContext.setCummulativeData(cumdata);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
