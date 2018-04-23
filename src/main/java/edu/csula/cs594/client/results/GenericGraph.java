package edu.csula.cs594.client.results;

public class GenericGraph {

    public GenericGraph(String time, long data) {
        this.time = time;
        this.data = data;
    }

    private String time;
    private long data;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }
}
