package edu.csula.cs594.client;

import edu.csula.cs594.client.dao.QueueItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reads web service performance result, QueueItem, and uses DatabaseClient to write it to the database
 */
public class DataConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DataConsumer.class);
    private final BlockingQueue<QueueItem> queue;
    private final DatabaseClient client;
    private final TestContext testContext;
    private Thread thread;
    final AtomicInteger consumed = new AtomicInteger(0);
    private volatile boolean shutdown = false;

    public DataConsumer(TestContext testContext) {
        this.testContext = testContext;
        this.queue = this.testContext.getQueue();
        this.client = this.testContext.getDbClient();
        logger.info("Data consumer initialized.");
    }

    public void setThread(Thread t) {
        this.thread = t;
    }

    public Thread getThread() {
        return thread;
    }

    public int getConsumed() {
        return consumed.get();
    }

    public TestContext getTestContext() {
        return testContext;
    }

    public void shutdown() {
        shutdown = true;
    }

    public void reset() {
        consumed.set(0);
    }

    @Override
    public void run() {
        try {
            QueueItem queueItem;
            //consuming messages until exit message is received
            while ((queueItem = queue.take()) != null && !shutdown) {
                try {
                    if (queueItem != null) {
                        client.recordWsDuration(queueItem);
                    }
                } catch (SQLException ex) {
                    logger.error("Couldn't record ws duration.", ex);
                } finally {
                    consumed.incrementAndGet();
                }
            }
        } catch (InterruptedException e) {
            // Exit thread
            logger.error("The DataConsumer thread for TestContext=" + testContext + " was interrupted. Assuming we received shutdown signal, ending task.");
        }
    }
}
