package edu.csula.cs594.client;


import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import edu.csula.cs594.client.dao.RealTimeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private final AsyncHttpClient asyncHttpClient;
    private final TestContext testContext;

    public HttpClient(TestContext testContext) {

        Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setCompressionEnforced(false)
                .setAllowPoolingConnections(true)
                .setRequestTimeout(30000)
                .build();

        asyncHttpClient = new AsyncHttpClient(builder.build());
        this.testContext = testContext;
        logger.info("Max connections: " + asyncHttpClient.getConfig().getMaxConnections());
    }

    public Future<Long> makeWarmUpCall() throws ExecutionException, InterruptedException {
        LocalAsyncHandler asyncHandler = new LocalAsyncHandler(testContext, true, new RealTimeData(0, 0));
        Future<Long> f = asyncHttpClient.prepareGet(testContext.getUri()).execute(asyncHandler);
        return f;
    }

    public Future<Long> makeAsyncHttpCall(RealTimeData realTimeData) throws InterruptedException, ExecutionException, IOException, SQLException {
        LocalAsyncHandler asyncHandler = new LocalAsyncHandler(testContext, false, realTimeData);
        Future<Long> f = asyncHttpClient.prepareGet(testContext.getUri()).execute(asyncHandler);
        return f;
    }

    public Long makeSyncHttpCall(RealTimeData realTimeData) throws InterruptedException, ExecutionException, IOException, SQLException {
        LocalAsyncHandler asyncHandler = new LocalAsyncHandler(testContext, false, realTimeData);
        Future<Long> f = asyncHttpClient.prepareGet(testContext.getUri()).execute(asyncHandler);
        // Makes this a sync http call - blocks until result is returned.
        Long result = f.get();
        return result;
    }

    public Future<Long> makeAsyncHttpPostCall(RealTimeData realTimeData) throws InterruptedException, ExecutionException, IOException, SQLException {
        LocalAsyncHandler asyncHandler = new LocalAsyncHandler(testContext, false, realTimeData);
        Future<Long> request = asyncHttpClient.preparePost(testContext.getUri()).
                setHeader("Content-Type","application/json").
                setBody(testContext.getRequestBody()).execute(asyncHandler);
        return request;
    }
    public Long makeSyncHttpPostCall(RealTimeData realTimeData) throws InterruptedException, ExecutionException, IOException, SQLException {
        LocalAsyncHandler asyncHandler = new LocalAsyncHandler(testContext, false, realTimeData);
        Future<Long> request = asyncHttpClient.preparePost(testContext.getUri()).
                setHeader("Content-Type","application/json").
                setBody(testContext.getRequestBody()).execute(asyncHandler);
        Long result =  request.get();
        return result;
    }

    public void makeHttpCall(RealTimeData realTimeData, boolean async) throws InterruptedException, ExecutionException, IOException, SQLException {
        if (async) {
            makeAsyncHttpCall(realTimeData);
        } else {
            makeSyncHttpCall(realTimeData);
        }
    }
    public void makeHttpPostCall(RealTimeData realTimeData, boolean async) throws InterruptedException, ExecutionException, IOException, SQLException {
        if (async) {
            makeAsyncHttpPostCall(realTimeData);
        } else {
            makeSyncHttpPostCall(realTimeData);
        }
    }

    @Override
    public void close() {
        asyncHttpClient.close();
    }
}
