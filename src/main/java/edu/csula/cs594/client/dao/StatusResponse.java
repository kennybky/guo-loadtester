package edu.csula.cs594.client.dao;

import edu.csula.cs594.client.graph.dao.Chart;
import edu.csula.cs594.client.graph.dao.Category;
import edu.csula.cs594.client.graph.dao.FusionChart;
import edu.csula.cs594.client.graph.dao.Series;
import java.util.List;

public class StatusResponse {
    private String uri;
    private int numberOfCalls;
    private int percentDone;
    private boolean running = false;
    private String message;
    private String requestType;
    private Category category;
    private double durationProgress;
    private List<Integer> rspTimes;
    private Chart chart;
    private FusionChart fusionChart;
    // Deprecate
    private Series series;
    
    private double cumAvgResponseTime;
    
    // Use this intead
    private List<Series> dataset;
    private double avgResponseTime;
    private int maxConcurrentUsers;
    private int projectid;
    private StatusResponse averages;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    public void setNumberOfCalls(int numberOfCalls) {
        this.numberOfCalls = numberOfCalls;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public int getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public int getMaxConcurrentUsers() {
        return maxConcurrentUsers;
    }

    public void setMaxConcurrentUsers(int maxConcurrentUsers) {
        this.maxConcurrentUsers = maxConcurrentUsers;
    }

    public int getProjectid() {
        return projectid;
    }

    public void setProjectid(int projectid) {
        this.projectid = projectid;
    }

    public StatusResponse getAverages() {
        return averages;
    }

    public void setAverages(StatusResponse averages) {
        this.averages = averages;
    }

    public double getCumAvgResponseTime() {
		return cumAvgResponseTime;
	}

	public void setCumAvgResponseTime(double cumAvgResponseTime) {
		this.cumAvgResponseTime = cumAvgResponseTime;
	}

	public List<Series> getDataset() {
        return dataset;
    }

    public void setDataset(List<Series> dataset) {
        this.dataset = dataset;
    }

    public List<Integer> getRspTimes() {
        return rspTimes;
    }

    public void setRspTimes(List<Integer> rspTimes) {
        this.rspTimes = rspTimes;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public double getDurationProgress() {
        return durationProgress;
    }

    public void setDurationProgress(double durationProgress) {
        this.durationProgress = durationProgress;
    }

    public FusionChart getFusionChart() {
        return fusionChart;
    }

    public void setFusionChart(FusionChart fusionChart) {
        this.fusionChart = fusionChart;
    }


}
