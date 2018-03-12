package edu.csula.cs594.client.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by James on 11/20/16.
 */
public class RealTimeData {

    private static final Logger logger = LoggerFactory.getLogger(RealTimeData.class);
    private final int relativeTimeBucket;
    private final int requests;
    private final AtomicInteger successes = new AtomicInteger(0);
    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicInteger cumRspTimes = new AtomicInteger(0);

    public RealTimeData(long relativeTimeBucket, int requests) {
        this.relativeTimeBucket = (int) relativeTimeBucket;
        this.requests = requests;
    }

    public int getRelativeTimeBucket() {
        return relativeTimeBucket;
    }

    public int getRequests() {
        return requests;
    }
    
    public boolean infiniteRequests() {
        // By definition:
        return (requests == 0);
    }

    public AtomicInteger getSuccesses() {
        return successes;
    }

    public AtomicInteger getFailures() {
        return failures;
    }

    public AtomicInteger getCumRspTimes() {
        return cumRspTimes;
    }

    /**
     * Average performance over n responses.
     * @return
     */
    public double getPerformance() {        
        final int cumTimes = cumRspTimes.get();
        final int n = successes.get() + failures.get();
        final double performance;
        if (n > 0) {
            performance = Math.round((cumTimes * 1.0 / n) * 100.0) / 100.0;
        } else {
             performance = 0.00;
        }
        return performance;
    }
}
