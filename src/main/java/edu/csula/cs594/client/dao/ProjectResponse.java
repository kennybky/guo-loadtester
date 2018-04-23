package edu.csula.cs594.client.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProjectResponse {
    private int projectid;
    private String projectname;
    private String method;
    private String uri;
    private String testType;



    private long scheduleInterval; // used for scheduled tests
    private String distribution;
    private int warmUpTime;
    private int testDuration;
    private int stepDuration;
    private int stepCount;
    private int requestCount;
    private int avgResponseTime;
    private int maxConcurrentUsers;
    private int failedRequests;
    private Date dateCreated;

    private String status;
    private boolean inProgress = false;
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
    
    public int getMaxConcurrentUsers() {
        return maxConcurrentUsers;
    }

    public void setMaxConcurrentUsers(int maxConcurrentUsers) {
        this.maxConcurrentUsers = maxConcurrentUsers;
    }

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

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getProjectid() {
        return  projectid;
    }

    public void setProjectid(int projectid) {
        this. projectid = projectid;
    }

    public String getDateCreated() {
        String dateStr = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aaa").format(dateCreated);
        return dateStr;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public void setWarmUpTime(int warmUpTime) {
        this.warmUpTime = warmUpTime;
    }

    public int getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(int testDuration) {
        this.testDuration = testDuration;
    }

    public int getFailedRequests() {
        return failedRequests;
    }

    public void setFailedRequests(int failedRequests) {
        this.failedRequests = failedRequests;
    }

    public long getScheduleInterval() {
        return scheduleInterval;
    }

    public void setScheduleInterval(long scheduleInterval) {
        this.scheduleInterval = scheduleInterval;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getStepDuration() {
        return stepDuration;
    }

    public void setStepDuration(int stepDuration) {
        this.stepDuration = stepDuration;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}

