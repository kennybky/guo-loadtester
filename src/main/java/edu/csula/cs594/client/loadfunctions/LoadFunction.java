package edu.csula.cs594.client.loadfunctions;

public interface LoadFunction {

    
    
    /**
     * The total duration for the load generating function.
     * When the duration is not set, it is assumed to be infinite.
     * @param durationSec duration in seconds for the graph
     */
    public void setTotalDuration(int durationSec);
    
    /**
     * The amount of time to wait before changing the number of users/connections.
     * @param stepMs the amount of time to wait in milliseconds
     */
    public void setStepCount(int steps);
    
    /**
     * Sets the initial number of connections, the amount to change users/connections by for each step, and whether the
     * change should be a percentage change or not.
     * @param initialUsers the number of users/connections to start with after the warm-up period has completed
     * @param userDelta the number of users to increase/decrease by, or the percentage change we want
     * @param usePercentageInc when set to true, userDelta is treated as a percentage rather than an absolute number
     */
    public void setLoad(int initialUsers, double userDelta, boolean usePercentageInc);
    
    /**
     * Returns the number of users that should be used at the time, timeMillis, using the function represented by 
     * this implementation of the LoadFunction interface
     * @param timeMillis the input to the load generating function. In other words, the time to use to determine how 
     * many users to simulate
     * @return the number of users to simulate
     */
    public int getUserCountForTimeMs(long timeMillis);
    
    public void setStepDuration(int durationMs);

    
}
