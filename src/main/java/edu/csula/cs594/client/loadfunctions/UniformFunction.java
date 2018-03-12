package edu.csula.cs594.client.loadfunctions;

public class UniformFunction implements LoadFunction {

    private int initialUsers = 0;
    private int userDelta = 0;
    private int currentUsers = 0;
    
    public UniformFunction() {
    
    }
    
    @Override
    public void setTotalDuration(int durationSec) {
        // no-op
    }

    @Override
    public void setLoad(int initialUsers, double userDelta, boolean usePercentageInc) {
        this.initialUsers = initialUsers;
        this.userDelta = (int) userDelta;
        currentUsers = initialUsers;
    }

    @Override
    public int getUserCountForTimeMs(long timeMillis) {
        return this.currentUsers;
    }

    @Override
    public void setStepCount(int steps) {
        // no-op
    }

    @Override
    public void setStepDuration(int durationMs) {
        // no-op
    }
    
}
