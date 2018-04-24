package edu.csula.cs594.client.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by James on 11/20/16.
 */
public class CummulativeData {

    private static final Logger logger = LoggerFactory.getLogger(CummulativeData.class);
    private final Integer calls;
    private final Integer cumRspTimes;
    private final Integer failedCalls;

    public CummulativeData(int[] data) {
        this.calls = data[0];
        this.cumRspTimes = data[1] * data[0];
        this.failedCalls = data[2];
    }

    public Integer getCumRspTimes() {
        return cumRspTimes;
    }
    
    public Integer getCalls() {
        return calls;
    }

    public Integer getFailedCalls (){return failedCalls;}

    /**
     * Average performance over n responses.
     * @return
     */
    public double getPerformance() {        
        final int cumTimes = cumRspTimes;
        final int n = calls;
        final double performance;
        if (n > 0) {
            performance = Math.round((cumTimes * 1.0 / n) * 100.0) / 100.0;
            //performance = cumRspTimes
        } else {
             performance = 0.00;
        }
        return performance;
    }
}
