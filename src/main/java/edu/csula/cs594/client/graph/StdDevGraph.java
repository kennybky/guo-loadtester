
package edu.csula.cs594.client.graph;

import java.util.List;


public class StdDevGraph {
    private final GraphInterface graph;
    
    public StdDevGraph(GraphInterface graph) {
        this.graph = graph;
    }

    public void setLabelValues(double mean, double stdDev, double minValue, double maxValue) {        
          graph.addLabel((int) minValue, false, (int)minValue + "");
          graph.addLabel((int) (mean - stdDev - stdDev), true, (int)(mean - stdDev - stdDev) + " (2σ)");
          graph.addLabel((int) (mean - stdDev), true, (int)(mean - stdDev) + " (σ)");
          graph.addLabel((int) mean, true, (int) mean + " (μ)");
          graph.addLabel((int) (mean + stdDev), true, (int)(mean + stdDev) + " (σ)");
          graph.addLabel((int) (mean + stdDev + stdDev), true, (int)(mean + stdDev + stdDev) + " (2σ)");
          graph.addLabel((int) maxValue, false, (int)maxValue + "");
    }

    public static double stdDev(double mean, List<Integer> items) {
        double diffSquared = 0.0;
        
        for (int value : items) {
            final double result = Math.pow(value - mean,2);
            diffSquared = diffSquared + result;
        }
        
        diffSquared = diffSquared / items.size();
        diffSquared = Math.sqrt(diffSquared);
        return diffSquared;
    }        
}
