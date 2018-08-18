package edu.csula.cs594.client.resource;

import edu.csula.cs594.client.CliClient;
import edu.csula.cs594.client.DataConsumer;
import edu.csula.cs594.client.DatabaseClient;
import edu.csula.cs594.client.TestContext;
import edu.csula.cs594.client.TestContext.Type;
import edu.csula.cs594.client.dao.*;
import edu.csula.cs594.client.graph.dao.AngularChart;
import edu.csula.cs594.client.loadfunctions.LoadFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/test/")
public class TestResource {

    private static final Logger logger = LoggerFactory.getLogger(TestResource.class);
    private final DatabaseClient dbClient;
    private final ExecutorService testers;
    private final ExecutorService asyncHandlers;
    private final ExecutorService consumers;
    private final Map<Integer, CliClient> cliClientMap;
    private final Map<Integer, DataConsumer> consumerMap;
    private final Map<Integer, TestContext> testContextMap;

    public TestResource(@Context ServletContext context) {
        dbClient = (DatabaseClient) context.getAttribute("dbClient");
        testers = (ExecutorService) context.getAttribute("testThreadPool");
        asyncHandlers = (ExecutorService) context.getAttribute("asyncHandlerThreadPool");
        consumers = (ExecutorService) context.getAttribute("consumerThreadPool");
        cliClientMap = (Map<Integer, CliClient>) context.getAttribute("cliClientMap");
        consumerMap = (Map<Integer, DataConsumer>) context.getAttribute("consumerMap");
        testContextMap = (Map<Integer, TestContext>) context.getAttribute("testContextMap");
    }

    @GET
    @Path("startScheduledTest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startScheduledTest(@QueryParam("projectName") String projectName, @QueryParam("uri") String uri,
                                       @QueryParam("interval") long testInterval, @QueryParam("timeout") long timeout,
                                       @QueryParam("method") String method) throws SQLException,
            InterruptedException, IOException, ExecutionException {

        eraseOldProject(projectName);
        int projectId = dbClient.createScheduledRun(projectName, uri, "scheduled", testInterval);
        TestContext testContext = new TestContext(projectId, uri, Type.SCHEDULED, dbClient, consumers);
        testContext.setMethod(method);
        testContext.setTestInterval(testInterval);
        testContext.setConsumer(new DataConsumer(testContext));
        testContextMap.put(projectId, testContext);
        CliClient cli = new CliClient(testContext);
        testers.execute(cli);
        StatusResponse r = getFirstResponse(projectId, uri, testContext.getRunning().get());
        return Response.ok().entity(r).build();
    }

    @GET
    @Path("startCapacityTest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startCapacityTest(
            @QueryParam("projectname") String projectName,
            @QueryParam("uri") String uri,
            @QueryParam("userCount") int userCount, // concurrentUsers
            @QueryParam("warmUpTime") int warmUpTime, // seconds
            @QueryParam("testTime") int testTime, // seconds
            @DefaultValue("1000") @QueryParam("stepDuration") int stepDurationMs, // interval duration in milliseconds before increasing users/interval
            @DefaultValue("0") @QueryParam("stepCount") int stepCount, // increase concurrent users by this number for each interval
            @DefaultValue("0") @QueryParam("failuresPermitted") int failuresPermitted,
            @QueryParam("method") String method)  {
        
        eraseOldProject(projectName);
        StatusResponse r;
        try {
            int projectId = dbClient.createCapacityProject(projectName, uri, warmUpTime, testTime, stepDurationMs, stepCount, userCount);
            TestContext testContext = new TestContext(projectId, uri, Type.CAPACITY, dbClient, consumers);
            testContext.setInitialUserCount(userCount);
            testContext.setWarmUpDuration(warmUpTime);
            testContext.setMethod(method);
            testContext.setTestDuration(testTime);
            testContext.setStepDuration(stepDurationMs);
            testContext.setStepCount(stepCount);
            testContext.setFailuresPermitted(failuresPermitted);
            testContext.setConsumer(new DataConsumer(testContext));
            testContextMap.put(projectId, testContext);
            CliClient cli = new CliClient(testContext, LoadFactory.Type.CAPACITY);
            testers.execute(cli);
            r = getFirstResponse(projectId, uri, testContext.getRunning().get());
        } catch (SQLException e) {
            logger.error("Couldn't create new capacity project.", e);
            r = new StatusResponse();
            r.setRunning(false);
        }
        return Response.ok().entity(r).build();
    }

    // http://localhost:8080/loadtester/v1/loadtest/startScalabilityTest?uri=http%3A%2F%2Fwww.webservicex.net%2Fglobalweather.asmx%2FGetCitiesByCountry%3FCountryName%3DUS&projectName=Project%201&distribution=Uniform
    //uri	http://www.webservicex.net/globalweather.asmx/GetCitiesByCountry?CountryName=US
    //projectname	Project 1
    //distribution	Uniform
    @GET
    @Path("startScalabilityTest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startScalabilityTest(
            @QueryParam("projectname") String projectName,
            @QueryParam("uri") String uri,
            @QueryParam("distribution") String distribution,
            @QueryParam("userCount") int userCount, // concurrentUsers
            @QueryParam("warmUpTime") int warmUpTime, // seconds
            @QueryParam("testTime") int testTime, // seconds
            @DefaultValue("1000") @QueryParam("stepDuration") int stepDurationMs, // interval duration in milliseconds before increasing users/interval
            @DefaultValue("0") @QueryParam("stepCount") int stepCount,
            @DefaultValue("0") @QueryParam("failuresPermitted") int failuresPermitted,
            @QueryParam("method") String method) {        // increase concurrent users by this number for each interval)

        LoadFactory.Type type;
        switch (distribution) {
            case "Uniform":
                type = LoadFactory.Type.UNIFORM;
                break;
            case "Normal":
                type = LoadFactory.Type.NORMAL;
                break;
            case "Poisson":
                type = LoadFactory.Type.POISSON;
                break;
            default:
                type = LoadFactory.Type.UNKNOWN;
        }

        eraseOldProject(projectName);
        StatusResponse r;
        try {
            int projectId = dbClient.createScalabilityProject(projectName, uri, distribution, warmUpTime,
                    testTime, stepDurationMs, stepCount, userCount);
            TestContext testContext = new TestContext(projectId, uri, Type.CAPACITY, dbClient, consumers);
            testContext.setInitialUserCount(userCount);
            testContext.setWarmUpDuration(warmUpTime);
            testContext.setTestDuration(testTime);
            testContext.setStepDuration(stepDurationMs);
            testContext.setStepCount(stepCount);
            testContext.setMethod(method);
            testContext.setFailuresPermitted(failuresPermitted);
            testContext.setConsumer(new DataConsumer(testContext));
            testContextMap.put(projectId, testContext);
            CliClient cli = new CliClient(testContext, type);
            testers.execute(cli);
            r = getFirstResponse(projectId, uri, testContext.getRunning().get());
        } catch (SQLException e) {
            logger.error("Couldn't create new scalability project.", e);
            r = new StatusResponse();
            r.setRunning(false);
        }
        return Response.ok().entity(r).build();
    }

    @GET
    @Path("startPerformanceTest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startPerformanceTest(@QueryParam("projectName") String projectName, @QueryParam("uri") String uri,
                                         @QueryParam("method") String method) throws SQLException,
            InterruptedException, IOException, ExecutionException {
        //eraseOldProject(projectName);
        StatusResponse r;
        try {
        	int projectId = getProjectId(projectName);// Get if project exists already
        	boolean isretest = false;
        	if (projectId == -1) {
        		projectId = dbClient.createPerformanceProject(projectName, uri, method);
        	} else {
        		isretest = true;
        	}
            TestContext testContext = new TestContext(projectId, uri, Type.PERFORMANCE, dbClient, consumers);
            testContext.setRetest(isretest);
            testContext.setConsumer(new DataConsumer(testContext));
            testContext.setMethod(method);
            logger.info("test context project id: " + testContext.getProjectId());
            testContextMap.put(projectId, testContext);
            CliClient cli = new CliClient(testContext);
            testers.execute(cli);
            r = getFirstResponse(projectId, uri, testContext.getRunning().get());
        } catch (SQLException e) {
            logger.error("Couldn't create new performance project.", e);
            r = new StatusResponse();
            r.setRunning(false);
        }
        return Response.ok().entity(r).build();
    }

    @GET
    @Path("stop")
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopTest(@QueryParam("projectId") int projectId) throws InterruptedException {
        StatusResponse r = new StatusResponse();
        CliClient client = cliClientMap.get(projectId);
        try {
            dbClient.recordProjectSummary(client.getTestContext());
        } catch(SQLException e){
            e.printStackTrace();
        }
        if (client != null) {
            testContextMap.remove(projectId);
            client.shutdown();
            client.getThread().interrupt();
        }
        DataConsumer consumer = consumerMap.get(projectId);
        if (consumer != null) {
            consumer.shutdown();
            consumer.getThread().interrupt();
        }
        // TODO: this should timeout after a certain number of tries or a timeout
        while (cliClientMap.containsKey(projectId)) {
            Thread.sleep(500);//check every 500ms until CliClient is removed from cliClientMap in ServletContextListenerImpl
        }
        r.setMessage("Test for project id: " + projectId + " successfully stopped.");
        return Response.ok().entity(r).build();
    }

    @GET
    @Path("performanceScalabilityGraph")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPerformanceScalability(@QueryParam("uri") String uri, @QueryParam("projectId") int projectId) throws SQLException {
        logger.info("Returning performance scalability graph data for " + uri + "for project ID: " + projectId);
        AngularChart chart = dbClient.getPerformanceScalabilityGraph(projectId);
        ChartResponse chartResponse = new ChartResponse();
        chartResponse.setChart(chart);
        chartResponse.setProjectId(projectId);
        chartResponse.setUri(uri);
        return Response.ok().entity(chartResponse).build();
    }

    @GET
    @Path("successScalabilityGraph")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSuccessScalability(@QueryParam("uri") String uri, @QueryParam("projectId") int projectId) throws SQLException {
        logger.info("Returning success scalability graph data for " + uri + "for project ID: " + projectId);
        AngularChart chart = dbClient.getSuccessScalabilityGraph(projectId);
        ChartResponse chartResponse = new ChartResponse();
        chartResponse.setChart(chart);
        chartResponse.setProjectId(projectId);
        chartResponse.setUri(uri);
        return Response.ok().entity(chartResponse).build();
    }

    @GET
    @Path("requestsGraph")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestsGraph(@QueryParam("uri") String uri, @QueryParam("projectId") int projectId) throws SQLException {
        logger.info("Returning requests graph data for " + uri + "for project ID: " + projectId);
        AngularChart chart = dbClient.getRequestGraph(projectId);
        ChartResponse chartResponse = new ChartResponse();
        chartResponse.setChart(chart);
        chartResponse.setProjectId(projectId);
        chartResponse.setUri(uri);
        return Response.ok().entity(chartResponse).build();
    }

    @GET
    @Path("realTimePerformance")
    public Response getRealTimePerformance(@QueryParam("projectId") int projectId) {
        StatusResponse statusResponse = new StatusResponse();
        TestContext testContext = testContextMap.get(projectId);
        if (testContext != null) {
            statusResponse.setMessage(testContext.getFinishMessage());
            statusResponse.setRunning(testContext.getRunning().get());
            if (testContext.getRunning().get()) {
            	double performance = testContext.getRealTimeDatas().get(0).getPerformance();
            	if (testContext.getCummulativeData()!=null){
            	    RealTimeData now = testContext.getRealTimeDatas().get(0);
                    CummulativeData then = testContext.getCummulativeData();
            	    double totalCalls = now.getSuccesses().get() + now.getFailures().get() + then.getCalls();
            	    double present = now.getCumRspTimes().get();
            		double past  = then.getCumRspTimes();
            		double avg = (present + past)/totalCalls;
            		statusResponse.setCumAvgResponseTime(avg);
            		testContext.setAvgResponse(avg);
            	} else {
            		statusResponse.setCumAvgResponseTime(-1);
            		testContext.setAvgResponse(performance);
            	}
            	
            	
                statusResponse.setAvgResponseTime(performance);
            }
        }
        return Response.ok().entity(statusResponse).build();
    }

    @GET
    @Path("scalabilityStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScalabilityStatus(@QueryParam("uri") String uri, @QueryParam("projectId") int projectId) throws SQLException {
        ChartResponse chartResponse = new ChartResponse();
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setProjectid(projectId);
        statusResponse.setUri(uri);
        chartResponse.setStatusResponse(statusResponse);
        List<AngularChart> charts = new ArrayList<>();
        chartResponse.setCharts(charts);
        TestContext testContext = testContextMap.get(projectId);
        if (testContext != null) {
            statusResponse.setMessage(testContext.getFinishMessage());
            statusResponse.setRunning(testContext.getRunning().get());
            if (testContext.getRunning().get()) {
                logger.info("Returning real time scalablity status for project id : " + projectId);
                List<RealTimeData> realTimeDatas = testContext.getRealTimeDatas();

                AngularChart requestChart = new AngularChart();
                AngularChart performanceChart = new AngularChart();
                AngularChart successChart = new AngularChart();

                List<Number> relativeTimeBuckets = new ArrayList<>();
                List<Number> requests = new ArrayList<>();
                List<Number> successes = new ArrayList<>();
                List<Number> performances = new ArrayList<>();
                for (RealTimeData realTimeData : realTimeDatas) {
                    //x axis
                    relativeTimeBuckets.add(realTimeData.getRelativeTimeBucket());
                    requests.add(realTimeData.getRequests());
                    successes.add(realTimeData.getSuccesses().get());
                    performances.add(realTimeData.getPerformance());
//                    logger.info("Relative Time Bucket: " + realTimeData.getRelativeTimeBucket());
//                    logger.info("Requests: " + realTimeData.getRequests());
//                    logger.info("Successes: " + realTimeData.getSuccesses());
//                    logger.info("Failures: " + realTimeData.getFailures());
//                    logger.info("Performance: " + realTimeData.getPerformance());
                }
                requestChart.setLabels(relativeTimeBuckets);
                requestChart.setData(requests);

                successChart.setLabels(relativeTimeBuckets);
                successChart.setData(successes);

                performanceChart.setLabels(relativeTimeBuckets);
                performanceChart.setData(performances);

                charts.add(requestChart);
                charts.add(performanceChart);
                charts.add(successChart);
            }
        }
        return Response.ok().entity(chartResponse).build();
    }

    @GET
    @Path("capacityStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCapacityStatus(@QueryParam("uri") String uri, @QueryParam("projectId") int projectId) throws SQLException {
        ChartResponse chartResponse = new ChartResponse();
        chartResponse.setProjectId(projectId);
        chartResponse.setUri(uri);
        StatusResponse statusResponse = new StatusResponse();
        chartResponse.setStatusResponse(statusResponse);
        statusResponse.setProjectid(projectId);
        statusResponse.setUri(uri);
        List<AngularChart> charts = new ArrayList<>();
        chartResponse.setCharts(charts);
        TestContext testContext = testContextMap.get(projectId);
        if (testContext != null) {
            statusResponse.setMessage(testContext.getFinishMessage());
            statusResponse.setRunning(testContext.getRunning().get());
            if (testContext.getRunning().get()) {
                logger.info("Returning real time capacity status for project id: " + projectId);
                List<RealTimeData> realTimeDatas = testContext.getRealTimeDatas();
                AngularChart requestChart = new AngularChart();
                AngularChart successChart = new AngularChart();

                List<Number> relativeTimeBuckets = new ArrayList<>();
                List<Number> requests = new ArrayList<>();
                List<Number> successes = new ArrayList<>();

                for (RealTimeData realTimeData : realTimeDatas) {
                    //x axis
                    relativeTimeBuckets.add(realTimeData.getRelativeTimeBucket());
                    requests.add(realTimeData.getRequests());
                    successes.add(realTimeData.getSuccesses().get());
//                    logger.info("Relative Time Bucket: " + realTimeData.getRelativeTimeBucket());
//                    logger.info("Requests: " + realTimeData.getRequests());
//                    logger.info("Successes: " + realTimeData.getSuccesses());
//                    logger.info("Failures: " + realTimeData.getFailures());
                }
                requestChart.setLabels(relativeTimeBuckets);
                requestChart.setData(requests);

                successChart.setLabels(relativeTimeBuckets);
                successChart.setData(successes);

                charts.add(requestChart);
                charts.add(successChart);
            }
        }
        return Response.ok().entity(chartResponse).build();
    }

    /**
     * @param projectlist comma-delimited list of project names
     * @param type        one of the following: avg, min, max, capacity, reliability
     * @return a Servlet Response object
     * @throws SQLException if there's an issue retrieving the data
     */
    @GET
    @Path("stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatsData(@QueryParam("projectlist") String projectlist, @QueryParam("type") String type) throws SQLException {

        String[] projectNames = projectlist.split(";");

        logger.info("Returning stats graph data for projects: " + Arrays.toString(projectNames));

        StatusResponse r = dbClient.getStatsGraph(projectNames, type);
        r.setUri("http://localhost:8080/loadtester/v1/stats");
        r.setRequestType("stats");

        return Response.ok().entity(r).build();
    }

    @GET
    @Path("availability")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailability(@QueryParam("mode") String mode, @QueryParam("date") String date, @QueryParam("projectId") int projectId) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date dateObj = null;
        String availability = "";
        StatusResponse status = new StatusResponse();
        try {
            dateObj = dateFormat.parse(date);
        } catch (java.text.ParseException e) {
            status.setMessage(e.toString());
        }

        if (null != dateObj) {
            try {
                availability += dbClient.getAvailability(mode, dateObj, projectId) + "%";
                status.setRunning(true);
                status.setMessage(availability);
            } catch (SQLException e) {
                status.setMessage(e.toString());
            }
        }
        return Response.ok().entity(status).build();
    }

    @GET
    @Path("reliability")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReliability(@QueryParam("projectId") int projectId) throws SQLException {
        logger.info("reliability called");
        StatusResponse status = new StatusResponse();
        status.setProjectid(projectId);
        try {
            double failureRate = (double) Math.round(dbClient.getFailureRate(projectId) * 100) / 100;
            double reliability = Math.pow(Math.E, (-1 * failureRate)) * 100;
            DecimalFormat df = new DecimalFormat("0.00");
            status.setMessage("Given an average failure rate of " + failureRate + " failures per month, "
                    + "reliability over one month is " + df.format(reliability) + "%");
            status.setRunning(true);
        } catch (SQLException e) {
            status.setMessage(e.toString());
        }
        return Response.ok().entity(status).build();
    }


    @GET
    @Path("performanceHistory")
    @Produces(MediaType.APPLICATION_JSON)
    public Response performanceHistory(@QueryParam("projectId") int projectId, @QueryParam("uri") String uri) throws SQLException {
        StatusResponse response = dbClient.getPerformanceHistory(projectId);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("validateProjectName")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateProjectName(@QueryParam("name") String projectName) {
        ValidationResponse response = new ValidationResponse();
        boolean validName;
        try {
            validName = dbClient.validateProjectName(projectName);
            response.setValid(validName);
            if (!validName) {
                response.setMessage("Project name already in use.");
            }
        } catch (SQLException e) {
            response.setValid(false);
            response.setMessage("Failed to validate project name. Error: " + e.toString());
        }
        return Response.ok().entity(response).build();
    }

    private StatusResponse getFirstResponse(int projectId, String uri, boolean running) {
        StatusResponse status = new StatusResponse();
        status.setRunning(running);
        status.setProjectid(projectId);
        status.setUri(uri);
        if (!running) {
            status.setMessage("Error! Could not start service testing.");
            try {
                stopTest(projectId);
            } catch (InterruptedException e) {
                logger.error("Got an interrupted exception trying to stop the test");
            }
        } else {
            status.setMessage("Successfully started service testing.");
        }
        return status;
    }

    /* Helpers */
    private int getProjectId(String projectName) {
        int projectId = -1;
        // Create 'current' project if projectname is null
        try {
            if (null == projectName) {
                projectName = "current";
            }
            projectId = dbClient.getProjectId(projectName);

        } catch (SQLException e) {
            logger.error("Unable to get projectId for project " + projectName + " from the database", e);
        }
        return projectId;
    }

    private void eraseOldProject(String projectName) {
        int projectId = getProjectId(projectName);
        if (projectId != -1) {
            try {
                dbClient.deleteLoadTestData(projectId);
                testContextMap.remove(projectId);
            } catch (SQLException ex) {
                logger.error("Failed to delete previous test data", ex);
            }
        }
        try {
            dbClient.deleteProject(projectId);
        } catch (SQLException ex) {
            logger.error("Failed to delete previous found project.", ex);
        }
    }
}



