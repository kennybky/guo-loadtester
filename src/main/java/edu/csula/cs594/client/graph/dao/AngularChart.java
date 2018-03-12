package edu.csula.cs594.client.graph.dao;

import java.util.List;

/**
 * Created by jsunthon on 11/11/2016.
 */
public class AngularChart {
    List<Number> labels;
    List<Number> data;

    public List<Number> getLabels() {
        return labels;
    }

    public void setLabels(List<Number> labels) {
        this.labels = labels;
    }

    public List<Number> getData() {
        return data;
    }

    public void setData(List<Number> data) {
        this.data = data;
    }
}
