package edu.csula.cs594.client.loadfunctions;

import org.apache.commons.math3.distribution.PoissonDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoissonFunction implements LoadFunction {
    
    public static final Logger logger = LoggerFactory.getLogger(PoissonFunction.class);   

    private int requests = 0;
    private int stepDurationMs = 0;
    private int totalDurationMs = 0;
    private PoissonDistribution poisson = null;

    public PoissonFunction() {
        // average of 10 occurence in time interval. want something like : https://en.wikipedia.org/wiki/Poisson_distribution picture on right
        poisson = new PoissonDistribution(10); //average of 10 occurence in the time interval. Wwan
    }

    @Override
    public void setStepDuration(int durationMs) {
        this.stepDurationMs = durationMs;
    }

    @Override
    public void setTotalDuration(int durationSec) {
        this.totalDurationMs = durationSec * 1000; //this needs divisble by stepDurationMs to equal 20.
    }

    @Override
    public void setLoad(int initialUsers, double userDelta, boolean usePercentageInc) {
        this.requests = initialUsers; // we'll multiplty this by the probably given with poisson to generate poisson distributed user
//        this.userDelta = (int) userDelta;
    }

    /* We want buckets to go from [1, 2, ..., 20] for a nice poisson looking graph */
    private int getBucket(long timeMs) {
        final long bucket = (timeMs / stepDurationMs); //500 / 500  = 1 ; 1000 / 500 = 2, 1500/500 = 3, .... , 10000 / 500 = 20
        return (int) bucket;
    }

    @Override
    public int getUserCountForTimeMs(long timeMillis) {
        final int bucket = getBucket(timeMillis); //bucket should be 1, 2, ..., 20. Middle bucket should be 10.
        logger.info("bucket(" + timeMillis + ") = " + bucket); // (500, 1), (1000, 2), ... ( 10000, 20)
        final double probability = poisson.probability(bucket);
        return (int) Math.round(requests * probability);
    }
   
    // Don't need this in this implementation
    // hard-coded to 20 buckets
    @Override
    public void setStepCount(int steps) {

    }

}
