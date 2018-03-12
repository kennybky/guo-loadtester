package edu.csula.cs594.client;

import edu.csula.cs594.client.dao.RealTimeData;
import edu.csula.cs594.client.dao.QueueItem;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Response callback handler for Ning HTTP Client - measures web service response time and adds it to the
 * DatabaseConsumer instance in TestContext
 */
public class LocalAsyncHandler implements AsyncHandler<Long> {

    private static final Logger logger = LoggerFactory.getLogger(LocalAsyncHandler.class);
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private final TestContext testContext;
    private final long handlerStartTime;
    private int statusCode;
    private final boolean warmUp;
    private final RealTimeData realTimeData;

    public LocalAsyncHandler(TestContext testContext, boolean warmUp, RealTimeData realTimeData) {
        this.handlerStartTime = System.currentTimeMillis();
        this.testContext = testContext;
        this.warmUp = warmUp;
        this.realTimeData = realTimeData;
    }

    @Override
    public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
        statusCode = status.getStatusCode();
        if (!warmUp) {
            if (statusCode >= 200 && statusCode < 300) {
                realTimeData.getSuccesses().incrementAndGet();
                testContext.getConsumed().incrementAndGet();
            } else {
                realTimeData.getFailures().incrementAndGet();
                testContext.getFailedCalls().incrementAndGet();
            }
        }
        return STATE.CONTINUE;
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        bytes.write(bodyPart.getBodyPartBytes());
        return STATE.CONTINUE;
    }

    @Override
    public void onThrowable(Throwable t) {
        
        statusCode = 504;
        realTimeData.getFailures().incrementAndGet();
        testContext.getFailedCalls().incrementAndGet();
        
        logger.error("A client-side error has occured and we cannot initiate outgoing HTTP requests", t);
        try {
            /*
             * I'm finding that onThrowable is invoked during a "connection to refuse" error, such as when
             * I test my own local web service and it's not turned on. in that case, I need to still record the response
             * as a failure, so that's why onCompleted is called. I asssume statusCode = 504 but not sure since no status
             * code is actually received.
             */
            logEventComplete();
        } catch (Exception e) {
            logger.error("Unable to log event duration", e);
        }
    }

    @Override
    public STATE onHeadersReceived(HttpResponseHeaders headers) throws Exception {
        return STATE.CONTINUE;
    }

    @Override
    public Long onCompleted() throws Exception {
        // Will be invoked once the response has been fully read or a ResponseComplete exception
        // has been thrown.
        // NOTE: should probably use Content-Encoding from headers
        final long eventDuration = logEventComplete();
        return eventDuration;
    }
    
    private long logEventComplete() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - handlerStartTime;
        long testStartDelta = endTime - testContext.getStartTime();
        QueueItem item = new QueueItem();
        item.setStatusCode(statusCode);
        item.setDuration(duration);
        item.setUri(testContext.getUri());
        item.setTestStartDelta(testStartDelta);
        item.setRequests(realTimeData.getRequests());
        item.setRelativeTimeBucket(realTimeData.getRelativeTimeBucket());
        item.setProjectid(testContext.getProjectId());
        if (!warmUp) {
            realTimeData.getCumRspTimes().getAndAdd((int) duration);
            testContext.getQueue().add(item);
        }
        return duration;  
    }
}
