package edu.csula.cs594.client.dao;

public class QueueItem {

    private int projectid;
    private String uri;
    private long duration;
    private long dateCreated;
    private long testStartDelta;
    private int statusCode;
    private long relativeTimeBucket;
    private int requests;
    private  int avgResponseTime;

    public int getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(int avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getProjectid() {
        return projectid;
    }

    public void setProjectid(int projectid) {
        this.projectid = projectid;
    }

    public long getTestStartDelta() {
        return testStartDelta;
    }

    public void setTestStartDelta(long testStartDelta) {
        this.testStartDelta = testStartDelta;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getRelativeTimeBucket() {
        return relativeTimeBucket;
    }

    public void setRelativeTimeBucket(long relativeTimeBucket) {
        this.relativeTimeBucket = relativeTimeBucket;
    }

    public int getRequests() {
        return requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }
}
