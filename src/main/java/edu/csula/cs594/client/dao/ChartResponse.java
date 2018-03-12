package edu.csula.cs594.client.dao;

import edu.csula.cs594.client.graph.dao.AngularChart;

import java.util.List;

/**
 * Created by jsunthon on 11/12/2016.
 */
public class ChartResponse {
    private int projectId;
    private String uri;
    private AngularChart chart;
    private List<AngularChart> charts;
    private StatusResponse statusResponse;

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public AngularChart getChart() {
        return chart;
    }

    public void setChart(AngularChart chart) {
        this.chart = chart;
    }

    public List<AngularChart> getCharts() {
        return charts;
    }

    public void setCharts(List<AngularChart> charts) {
        this.charts = charts;
    }

    public StatusResponse getStatusResponse() {
        return statusResponse;
    }

    public void setStatusResponse(StatusResponse statusResponse) {
        this.statusResponse = statusResponse;
    }
}
