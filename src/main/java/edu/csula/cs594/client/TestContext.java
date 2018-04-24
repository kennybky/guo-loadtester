package edu.csula.cs594.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.csula.cs594.client.dao.RealTimeData;
import edu.csula.cs594.client.dao.CummulativeData;
import edu.csula.cs594.client.dao.QueueItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Represents the current state/context of a test instance
 */
public class TestContext {

    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);

    public String getRequestBody() {
        String jsonContent = "";
            final ObjectMapper mapper = new ObjectMapper();
        try {
            jsonContent = mapper.writeValueAsString(this.getParams());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonContent;
    }

    public enum Type {
        SCHEDULED, PERFORMANCE, CAPACITY
    }
        
    @Context
    private ServletContext context;

    private final DatabaseClient dbClient;
    private final BlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>();
    private final ExecutorService consumers;

    private final int projectId;
    private final String uri;
    private int initialUserCount;
    private long startTime = 0;
    private int warmUpDuration = 0;
    private long warmUpDurationMs = 0;
    private int testDuration = 0;
    private long testDurationMs = 0;
    private int failuresPermitted = 0;
    private int stepDuration;
    private int stepCount;
    private long testInterval;
    private Type testType;
    private DataConsumer consumer;
    private List<RealTimeData> realTimeDatas = new ArrayList<>();
    private CummulativeData cummulativeData = null;
    private int requests = 0;
    private final AtomicInteger consumed = new AtomicInteger(0);
    private final AtomicInteger failedCalls = new AtomicInteger(0);

    public double getAvgResponse() {
        return avgResponse;
    }

    public void setAvgResponse(double avgResponse) {
        this.avgResponse = avgResponse;
    }

    private final AtomicBoolean running;
    private String finishMessage;
    private boolean retest = false;
    private Map<String, String> params;
    private double avgResponse;



    private String method;

    public TestContext(int projectId, String uri, Type testType, DatabaseClient dbClient, ExecutorService consumers) {
        this.projectId = projectId;
        this.uri = uri;
        this.testType = testType;
        this.dbClient = dbClient;
        this.consumers = consumers;
        this.running = new AtomicBoolean(true);
        this.finishMessage = "Test finished without errors.";
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Start consuming data when responses consume.
     */

    public void startConsumer() {
        logger.info("Submitting the consumer to the consumer thread pool.");
        consumers.execute(consumer);
    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    public BlockingQueue<QueueItem> getQueue() {
        return queue;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getUri() {
        return uri;
    }

    public int getInitialUserCount() {
        return initialUserCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getWarmUpDuration() {
        return warmUpDuration;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public int getStepDuration() {
        return stepDuration;
    }

    public int getStepCount() {
        return stepCount;
    }

    public DataConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(DataConsumer consumer) {
        this.consumer = consumer;
    }

    public AtomicInteger getFailedCalls() {
        return failedCalls;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRequests() {
        return requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }

    public long getTestInterval() {
        return testInterval;
    }

    public void setTestInterval(long testInterval) {
        this.testInterval = testInterval;
    }

    public Type getTestType() {
        return testType;
    }

    public void setTestType(Type testType) {
        this.testType = testType;
    }

    public ExecutorService getConsumers() {
        return consumers;
    }

    public List<RealTimeData> getRealTimeDatas() {
        return realTimeDatas;
    }

    public void setRealTimeDatas(List<RealTimeData> realTimeDatas) {
        this.realTimeDatas = realTimeDatas;
    }

    public CummulativeData getCummulativeData() {
		return cummulativeData;
	}

	public void setCummulativeData(CummulativeData cummulativeData) {
		this.cummulativeData = cummulativeData;
	}

	public DatabaseClient getDbClient() {
        return dbClient;
    }

    public void setInitialUserCount(int initialUserCount) {
        this.initialUserCount = initialUserCount;
    }

    public void setWarmUpDuration(int warmUpDuration) {
        this.warmUpDuration = warmUpDuration;
        this.warmUpDurationMs = TimeUnit.MILLISECONDS.convert(this.warmUpDuration, TimeUnit.SECONDS);
    }

    public void setTestDuration(int testDuration) {
        this.testDuration = testDuration;
        this.testDurationMs = TimeUnit.MILLISECONDS.convert(this.testDuration, TimeUnit.SECONDS);
    }

    public void setStepDuration(int stepDuration) {
        this.stepDuration = stepDuration;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public AtomicInteger getConsumed() {
        return consumed;
    }

    public long getWarmUpDurationMs() {
        return warmUpDurationMs;
    }

    public long getTestDurationMs() {
        return testDurationMs;
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public String getFinishMessage() {
        return finishMessage;
    }

    public void setFinishMessage(String finishMessage) {
        this.finishMessage = finishMessage;
    }

    @Override
    public String toString() {
        // TODO: use Jackson to serialize the object as a json string
        return "[ projectId=" + projectId + ", uri=" + uri + "]";
    }

    public int getFailuresPermitted() {
        return failuresPermitted;
    }

    public void setFailuresPermitted(int failuresPermitted) {
        this.failuresPermitted = failuresPermitted;
    }

	public boolean isRetest() {
		return retest;
	}

	public void setRetest(boolean retest) {
		this.retest = retest;
	}
}
