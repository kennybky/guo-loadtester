package edu.csula.cs594.client.loadfunctions;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalFunction implements LoadFunction {
    
    public static final Logger logger = LoggerFactory.getLogger(NormalFunction.class);   

    private static final double TERM2 = Math.sqrt(2 * Math.PI);
    private int initialUsers = 0;
    private int totalDurationMs = 0;
    private int stepDurationMs = 0;
    private double mu = 0;
    private double sigma = 0;
    private double sigmaInverse = 0;
    NormalDistribution n;
    
    public NormalFunction() {
        n = null;
    }
    
    @Override
    public void setTotalDuration(int durationSec) {
        this.totalDurationMs = durationSec * 1000;
        this.mu = (int) (totalDurationMs / 2);
        this.sigma = totalDurationMs / 6;
        this.sigmaInverse = 1/this.sigma;
        
        n = new NormalDistribution(mu, sigma);

        logger.info("totalDurationMs=" + this.totalDurationMs);
        logger.info("mu=" + this.mu);
        logger.info(" * mean=" + n.getMean());
        logger.info("sigma=" + this.sigma);
        logger.info(" * stdev=" + n.getStandardDeviation());
        logger.info("sigmaInverse=" + this.sigmaInverse);
        logger.info(" * lower=" + n.getSupportLowerBound());
        logger.info(" * upper=" + n.getSupportUpperBound());
        logger.info(" * sample = " + n.sample());
    }

    @Override
    public void setLoad(int initialUsers, double userDelta, boolean usePercentageInc) {
        this.initialUsers = initialUsers;
    }

    @Override
    public int getUserCountForTimeMs(long timeMillis) {        
        final double prob = n.probability(timeMillis, timeMillis + stepDurationMs);
        logger.info("n.prob(" + timeMillis + ") = " + prob);
        final int result = (int) (prob * this.initialUsers);
        logger.info("users @ " + timeMillis + ") = " + result);
        return result;
    }
    
    @Override
    public void setStepCount(int steps) {
        // for now
    }

    @Override
    public void setStepDuration(int durationMs) {
        this.stepDurationMs = durationMs;
    }

}
