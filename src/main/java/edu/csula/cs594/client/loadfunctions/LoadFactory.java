package edu.csula.cs594.client.loadfunctions;

public class LoadFactory {

    public enum Type {
        CAPACITY, NORMAL, POISSON, UNIFORM, UNKNOWN;
    }
    
    public static LoadFunction getLoadFunction(Type type) {
        
        LoadFunction loadFunction;
        
        switch (type) {
            case CAPACITY:
                loadFunction = new CapacityFunction();
                break;
            case NORMAL:
                loadFunction = new NormalFunction();
                break;
            case POISSON:
                loadFunction = new PoissonFunction();
                break;
            case UNIFORM:
                loadFunction = new UniformFunction();
                break;
            default:
                throw new IllegalArgumentException("Unknown load generating function type");
        }
        
        return loadFunction;
    }
}
