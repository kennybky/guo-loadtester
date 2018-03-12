package edu.csula.cs594.client.loadfunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapacityFunction implements LoadFunction {

    private static final Logger logger = LoggerFactory.getLogger(CapacityFunction.class);
    private int initialUsers = 0;
    private int userDelta = 0;
    private int currentUsers = 0;
    private int steps = 0;
    private int totalDurationMs = 0;
    private int stepDurationMs = 0;
    
    public CapacityFunction() {
    
    }
    
    @Override
    public void setTotalDuration(int durationSec) {
        this.totalDurationMs = durationSec * 1000;
    }

    @Override
    public void setLoad(int initialUsers, double userDelta, boolean usePercentageInc) {
        this.initialUsers = initialUsers;
        this.userDelta = (int) userDelta;
        currentUsers = initialUsers;
    }

    private int getBucket(long timeMs) {
        final int bucket = (int) (timeMs / stepDurationMs);
        return bucket;
    }

    @Override
    public int getUserCountForTimeMs(long timeMillis) {
        currentUsers *= userDelta;
        logger.info("Current users: " + currentUsers);
        return currentUsers;
    }

    @Override
    public void setStepCount(int steps) {
        this.steps = steps;
    }

    @Override
    public void setStepDuration(int durationMs) {
        this.stepDurationMs = durationMs;
    }
    }
