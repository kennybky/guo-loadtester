package edu.csula.cs594.client;

import edu.csula.cs594.client.dao.model.Project;
import edu.csula.cs594.client.graph.ScrollLineGraph;
import edu.csula.cs594.client.graph.dao.AngularChart;
import edu.csula.cs594.client.dao.ProjectResponse;
import edu.csula.cs594.client.dao.*;
import edu.csula.cs594.client.graph.BarGraph;
import edu.csula.cs594.client.graph.AngularLineGraph;
import edu.csula.cs594.client.graph.ScatterGraph;
import edu.csula.cs594.client.graph.dao.Category;
import edu.csula.cs594.client.graph.dao.CategoryLabel;
import edu.csula.cs594.client.graph.dao.Series;
import edu.csula.cs594.client.graph.dao.SeriesData;
import edu.csula.cs594.client.graph.data.GraphResultSetSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import edu.csula.cs594.client.results.GenericResult;
import edu.csula.cs594.client.results.project.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseClient {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseClient.class);    

    private Connection connect = null;

    String defaultDatabase = "stats";
    String dbUser = "root";
    String dbPwd = "root";
    String server = "localhost";

    public DatabaseClient() {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + server + "/" + defaultDatabase + "?user=" + dbUser + "&password=" + dbPwd);
            connect.setAutoCommit(true);
            connect.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            logger.info("Database connection initialized.");
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Unable connect the database: ", e);
        }
    }
    public DatabaseClient(String database, String user, String password) {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + server + "/" + database + "?user=" + user + "&password=" + password);
            connect.setAutoCommit(true);
            connect.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            logger.info("Database connection initialized.");
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Unable connect the database: ", e);
        }
    }
    
    public Connection getConnection() {
    	return connect;
    }

    public boolean validateProjectName(String projectName) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("select id from projects where projectname = ?")) {
            preparedStatement.setString(1, projectName);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return !rs.isBeforeFirst();
            }
        }
    }

    public void saveUrl(String uri) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into stats.services (testUri) values ( ? )")) {
            preparedStatement.setString(1, uri);
            preparedStatement.executeUpdate();
        }
    }

    public void saveUrlForBuilder(int id, String uri) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("update stats.services set testUri = ? where id = ? ")) {
            preparedStatement.setString(1, uri);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        }
    }

    public List<String> getSavedUrls() throws SQLException {
        List<String> urls = new ArrayList<>();
        try (PreparedStatement preparedStatement = connect.prepareStatement("select testUri from stats.services where testUri is not null ")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    urls.add(rs.getString(1));
                }
            }
        }
        return urls;
    }

    public void recordWsDuration(QueueItem item) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into stats.deltas (uri, roundTripTime, relativeTime,"
                + " projectid, relativeTimeBucket, requests, statusCode) values (?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, item.getUri());
            preparedStatement.setInt(2, (int) item.getDuration());
            preparedStatement.setInt(3, (int) item.getTestStartDelta());
            preparedStatement.setInt(4, (int) item.getProjectid());
            preparedStatement.setInt(5, (int) item.getRelativeTimeBucket());
            preparedStatement.setInt(6, item.getRequests());
            preparedStatement.setInt(7, item.getStatusCode());
            preparedStatement.executeUpdate();
        }
    }

    public void recordProjectSummary(TestContext context) throws SQLException{
        try (PreparedStatement preparedStatement = connect.prepareStatement("update stats.projects set requestCount = ? , avgResponseTime = ? ," +
                " failedRequests = ? where id = ?")){
            int requestCount = context.getConsumed().get() + context.getFailedCalls().get();
            int failedCalls = context.getFailedCalls().get();
            CummulativeData past = context.getCummulativeData();
            if (past !=null){
                requestCount += past.getCalls();
                failedCalls += past.getFailedCalls();
            }
            preparedStatement.setInt(1, requestCount);

            preparedStatement.setDouble(2, (context.getAvgResponse()));
            preparedStatement.setInt(3, failedCalls);
            preparedStatement.setInt(4,context.getProjectId());
            preparedStatement.executeUpdate();
        }
    }

    public int createScheduledRun(String projectName, String uri, String testType, long scheduleInterval) throws SQLException {
        int generatedKey = 0;
        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into stats.projects (id, "
                + "projectname, uri, testType, scheduleInterval) values (null, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, projectName);
            preparedStatement.setString(2, uri);
            preparedStatement.setString(3, testType);
            preparedStatement.setLong(4, scheduleInterval);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                generatedKey = rs.getInt(1);
            }
        }
        return generatedKey;
    }

    //http://www.barringer1.com/ar.htm. mission time = 1 month. so failure rate is based on avg failures per month.
    //results would be diff if we used an average failure rate per year and actually had a year's worth of data.
    public double getFailureRate(int projectId) throws SQLException {
        int failures = 0;
        int monthCount = 0;
        String query = "select year(dateCreated) as year, month(dateCreated) as month, count(*) as failures "
                + "from stats.deltas where projectid =? and statuscode != 200 "
                + "group by year(dateCreated), month(dateCreated)";
        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setInt(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    monthCount++;
                    failures += rs.getInt(3);
                }
            }
        }
        return failures / (1.0 * monthCount);
    }

    // TODO: use switch and add null-check for mode or invert comparision
    public double getAvailability(String mode, Date date, int projectId) throws SQLException {
        double availability;
        logger.info("Availabile for mode: " + mode);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        long uptime = 0;
        long downtime = 0;
        long interval = getScheduledInterval(projectId);
        List<Integer> statusCodes = new LinkedList<>();

        /* day */
        if (mode.equals("day")) {
            try (PreparedStatement preparedStatement = connect.prepareStatement(
                    "select d.statuscode "
                    + "from projects p inner join deltas d on (d.projectid=p.id) "
                    + "where d.projectid= ? and d.dateCreated between ? and ? ")) {
                String start = year + "-" + month + "-" + day;
                String end = start + " 23:59:59";
                preparedStatement.setInt(1, projectId);
                preparedStatement.setString(2, start);
                preparedStatement.setString(3, end);
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        statusCodes.add(rs.getInt(1));
                    }
                }
            }
        }

        if (mode.equals("month")) {
            try (PreparedStatement pstmt = connect.prepareStatement("select d.statusCode "
                    + "from projects p inner join deltas d on (d.projectid=p.id) "
                    + "where d.projectid = ? and month(p.dateCreated) = ? and year(p.dateCreated) = ? ")) {
                pstmt.setInt(1, projectId);
                pstmt.setInt(2, month);
                pstmt.setInt(3, year);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        statusCodes.add(rs.getInt(1));
                    }
                }
            }
        }

        if (mode.equals("year")) {
            logger.info("querying for year");
            String query = "select d.statuscode from projects p "
                    + "inner join deltas d on (d.projectid=p.id) "
                    + "where d.projectid = ? and year(p.dateCreated) = ? ";
            try (PreparedStatement pstmt = connect.prepareStatement(query)) {
                pstmt.setInt(1, projectId);
                logger.info("year: " + year);
                pstmt.setInt(2, year);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    statusCodes.add(rs.getInt(1));
                }
            }
        }

        for (Integer statusCode : statusCodes) {
            if (statusCode == 200) {
                uptime += interval;
            } else {
                downtime += interval;
            }
        }
        logger.info("Uptime: " + uptime);
        logger.info("Downtime: " + downtime);
        availability = (double) Math.round((uptime / (1.0 * (uptime + downtime))) * 100 * 100) / 100;
        logger.info("avail: " + availability);
        return availability;
    }

    public long getScheduledInterval(int projectId) throws SQLException {
        String query = "select scheduleInterval from projects where id = ?";
        long interval = -1;
        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setInt(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    interval = rs.getLong(1);
                }
            }
            if (-1 == interval) {
                logger.error("Couldn't get scheduled interval");
            }

        } catch (SQLException e) {
            logger.error("Couldn't get scheduled interval");
        }
        return interval;
    }

    // dbClient.createPerformanceProject(uri, projectName, requestCount);
    public int createPerformanceProject(String projectName, String uri, String method) throws SQLException {
        String query = "insert into stats.projects (uri, projectname, testType, method) values (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connect.prepareStatement(query)) {
            pstmt.setString(1, uri);
            pstmt.setString(2, projectName);
            pstmt.setString(3, "performance");
            pstmt.setString(4, method);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Couldn't create a performance project.", e);
        }
        return getProjectId(projectName);
    }

    public void deleteLoadTestData(int projectId) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("delete from stats.deltas where projectid = ?")) {
            preparedStatement.setInt(1, projectId);
            preparedStatement.executeUpdate();
        }
    }

    public void updateLoadTestProject(int projectId, String projectName, String uri, String distribution,
            int warmUpTime, int testTime, int stepDurationMs, int stepCount, int userCount, Date date, String type) throws SQLException {
        java.sql.Timestamp dateCreated = new java.sql.Timestamp(date.getTime());
        try (PreparedStatement preparedStatement = connect.prepareStatement("update stats.projects "
                + "set projectName = ?, uri = ?, distribution = ?, warmUpTime = ?,"
                + " testDuration = ?, stepDuration = ?, stepCount = ?, userCount = ?, dateCreated = ?, testType = ? "
                + "where id = ?")) {
            preparedStatement.setString(1, projectName);
            preparedStatement.setString(2, uri);
            preparedStatement.setString(3, distribution);
            preparedStatement.setInt(4, warmUpTime);
            preparedStatement.setInt(5, testTime);
            preparedStatement.setInt(6, stepDurationMs);
            preparedStatement.setInt(7, stepCount);
            preparedStatement.setInt(8, userCount);
            preparedStatement.setTimestamp(9, dateCreated);
            preparedStatement.setString(10, type);
            preparedStatement.setInt(11, projectId);
            preparedStatement.executeUpdate();
        }
    }

    public int getProjectId(String projectName) throws SQLException {

        int projectId;
        try (PreparedStatement preparedStatement = connect.prepareStatement("select id from stats.projects where projectname = ?")) {
            preparedStatement.setString(1, projectName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                projectId = -1;
                if (resultSet.next()) {
                    projectId = resultSet.getInt(1);
                }
            }
            if (-1 == projectId) {
                logger.error("Unable to get projectId for " + projectName);
            }
        }

        return projectId;
    }

    public int getServiceId(String uri) throws SQLException {
        int serviceId = -1;
        try (PreparedStatement preparedStatement = connect.prepareStatement("select id from stats.services where testUri = ?")) {
            preparedStatement.setString(1, uri);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    serviceId = resultSet.getInt(1);
                }
            }
            if (-1 == serviceId) {
                logger.error("Unable to get id for " + uri);
            }
        }
        return serviceId;
    }

    public int getOverAllAvg(int projectId) throws SQLException {
        int avgMs;
        try (PreparedStatement preparedStatement = connect.prepareStatement("select avg(roundTripTime)  from stats.deltas where projectid = ?")) {
            preparedStatement.setInt(1, projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                avgMs = -1;
                if (resultSet.next()) {
                    avgMs = resultSet.getInt(1);

                }
            }
            if (-1 == avgMs) {
                logger.error("Unable to get average round trip time.");
            } else {
                logger.info("Average round trip time for all calls is: " + avgMs + "ms");
            }
        }
        return avgMs;
    }

    public void saveService(String name, String descriptionUri, String baseUri, String method, List<String> params) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into services"
                + "(name, descriptionUri, baseUri, method, parameters) values (?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, descriptionUri);
            preparedStatement.setString(3, baseUri);
            preparedStatement.setString(4, method);
            StringBuilder parameters = new StringBuilder();
            for (String param : params) {
                parameters.append(param);
                if (params.indexOf(param) != (params.size() - 1)) {
                    parameters.append(",");
                }
            }
            preparedStatement.setString(5, parameters.toString());
            preparedStatement.executeUpdate();
        }
    }

    public void deleteService(int id) throws SQLException {
        try (PreparedStatement pstmt = connect.prepareStatement("delete from services where id=?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public List<Service> getServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        try (PreparedStatement preparedStatement = connect.prepareStatement("select * from services")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    String parameters = rs.getString(7);
                    List<String> params = new ArrayList<>();
                    if (parameters != null) {
                        params = Arrays.asList(parameters.split(","));
                    }
                    services.add(new Service(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(6), params, rs.getString(5)));
                }
            }
        }
        return services;
    }

    public ProjectResponse getProject(int projectId) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement(
                "select projectname, uri, " + "requestcount, datecreated, avgResponseTime, userCount from stats.projects where id = ?")) {
            preparedStatement.setInt(1, projectId);

            ProjectResponse p = new ProjectResponse();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String projectname = resultSet.getString(1);
                    String uri = resultSet.getString(2);
                    int count = resultSet.getInt(3);
                    Date date = resultSet.getTimestamp(4);
                    int avgResponseTime = resultSet.getInt(5);
                    int userCount = resultSet.getInt(6);
                    p.setProjectname(projectname);
                    p.setUri(uri);
                    p.setRequestCount(count);
                    p.setDateCreated(date);
                    p.setProjectid(projectId);
                    p.setAvgResponseTime(avgResponseTime);
                    p.setMaxConcurrentUsers(userCount);
                }
            }

            return p;
        }

    }

    public GetProjectResult getWebProjects() {
        String sql = "SELECT * FROM webprojects ORDER BY id;";
        List<Project> projects = new ArrayList<>();
        boolean success = false;

        try {
            PreparedStatement preparedStatement = connect.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                projects.add(toProject(resultSet));
            }

            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new GetProjectResult(projects, success);
    }

    public CreateProjectResult createWebProject(Project project) {
        System.out.println("data to push: " + project.getData());
        int projectId = -1;

        try {
            String sql = "INSERT INTO webprojects (data) VALUES(?)";
            PreparedStatement statement;
            statement = connect.prepareStatement(sql);
            statement.setString(1, project.getData());
            int rowCount = statement.executeUpdate();

            sql = "SELECT LAST_INSERT_ID();";
            statement = connect.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            if(rowCount > 0 && resultSet.next())
                projectId = resultSet.getInt("LAST_INSERT_ID()");
            //rowCount = statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("CREATED NEW PROJECT WITH ID: " + projectId);
        boolean successful = projectId != -1;
        if(successful)
            project.setId(projectId);
        return new CreateProjectResult(projectId, project, successful);
    }

    public GetProjectByIdResult getProjectById(int id) {
        try {
            String sql = "SELECT * FROM webprojects WHERE id = ?";
            PreparedStatement statement;
            statement = connect.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            Project project = null;
            if(resultSet.next()) {

               project =  new Project(resultSet.getInt("id"), resultSet.getString("data"));
            }

            return new GetProjectByIdResult(id, project);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public DeleteProjectByIdResult deleteWebProject(int id) {
        try {
            String sql = "DELETE  FROM webprojects WHERE id = ?";
            PreparedStatement statement;
            statement = connect.prepareStatement(sql);
            statement.setInt(1, id);
            int rowcount = statement.executeUpdate();
            boolean success = rowcount > 0? true : false;

            return new DeleteProjectByIdResult(id, success);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UpdateProjectResult updateProject(Project project, int id) {
        System.out.println("data to update: " + project.getData());
        String sql = "UPDATE webprojects SET data = ? WHERE id = ?";
        int rowsUpdated = 0;
        try {
            PreparedStatement statement = connect.prepareStatement(sql);
            statement.setString(1, project.getData());
            statement.setInt(2, id);
            rowsUpdated = statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new UpdateProjectResult(id, project, rowsUpdated > 0);
    }

    public static Project toProject(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String data = resultSet.getString("data");
        return new Project(id, data);
    }



    public List<ProjectResponse> getLoadProjects() throws SQLException {

        try (PreparedStatement preparedStatement = connect
                .prepareStatement("select projectname, uri, requestcount, datecreated, id, "
                        + "avgResponseTime, userCount, warmUpTime, testDuration, failedRequests, "
                        + "testType, distribution, stepDuration, stepCount,method from stats.projects"
                        + " where testType != ? ")) {
            preparedStatement.setString(1, "scheduled");
            List<ProjectResponse> results = new ArrayList<>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String projectname = resultSet.getString(1);
                    String uri = resultSet.getString(2);
                    int count = resultSet.getInt(3);
                    Date date = resultSet.getTimestamp(4);
                    int id = resultSet.getInt(5);
                    int avgResponseTime = resultSet.getInt(6);
                    int userCount = resultSet.getInt(7);
                    int warmUpTime = resultSet.getInt(8);
                    int duration = resultSet.getInt(9);
                    int failed = resultSet.getInt(10);
                    String testType = resultSet.getString(11);
                    String distribution = resultSet.getString(12);
                    int stepDuration = resultSet.getInt(13);
                    int stepCount = resultSet.getInt(14);
                    String method = resultSet.getString(15);
                    ProjectResponse p = new ProjectResponse();
                    p.setProjectname(projectname);
                    p.setUri(uri);
                    p.setRequestCount(count);
                    p.setDateCreated(date);
                    p.setProjectid(id);
                    p.setAvgResponseTime(avgResponseTime);
                    p.setMaxConcurrentUsers(userCount);
                    p.setWarmUpTime(warmUpTime);
                    p.setTestDuration(duration);
                    p.setFailedRequests(failed);
                    p.setTestType(testType);
                    p.setDistribution(distribution);
                    p.setStepDuration(stepDuration);
                    p.setStepCount(stepCount);
                    p.setMethod(method);
                    results.add(p);
                }
            }
            return results;
        }
    }

    public List<ProjectResponse> getScheduledProjects() throws SQLException {

        try (PreparedStatement preparedStatement = connect
                .prepareStatement("select id, projectname, uri, scheduleInterval, dateCreated, method from stats.projects where testType = ? ")) {
            preparedStatement.setString(1, "scheduled");

            List<ProjectResponse> scheduledProjects = new ArrayList<>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    String projectName = resultSet.getString(2);
                    String uri = resultSet.getString(3);
                    long scheduleInterval = resultSet.getLong(4);
                    Date date = resultSet.getTimestamp(5);
                    String method = resultSet.getString(6);
                    ProjectResponse p = new ProjectResponse();
                    p.setProjectid(id);
                    p.setProjectname(projectName);
                    p.setUri(uri);
                    p.setScheduleInterval(scheduleInterval);
                    p.setDateCreated(date);
                    p.setTestType("scheduled");
                    p.setMethod(method);
                    scheduledProjects.add(p);
                }
            }

            return scheduledProjects;
        }
    }

    public StatusResponse getLoadTestGraph2(int projectid) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("select count('x') as yaxis, roundTripTime as xaxis from stats.deltas where projectid = ? group by roundTripTime")) {
            preparedStatement.setInt(1, projectid);

            ScatterGraph graph = new ScatterGraph(false);
            graph.setCaption("Response Count vs Response Time (ms)");
            graph.setXaxis("Response Time (ms)");
            graph.setYaxis("Response Count");

            ProjectResponse projectInfo = getProject(projectid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                GraphResultSetSource iterator = new GraphResultSetSource(resultSet);
                graph.addSeries(iterator, projectInfo.getProjectname());
            }

            return graph.getResult();
        }
    }

    public AngularChart getPerformanceScalabilityGraph(int projectId) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("select relativeTimeBucket as xaxis, round(avg(roundTripTime), 2) as yaxis from stats.deltas where projectid = ? group by xaxis order by xaxis asc")) {
            preparedStatement.setInt(1, projectId);

            AngularChart chart = new AngularChart();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                GraphResultSetSource iterator = new GraphResultSetSource(resultSet, "integer", "double");
                AngularLineGraph graph = new AngularLineGraph(chart, iterator, "integer", "double");
                graph.populateChart();
                chart = graph.getChart();
            } catch (Exception e) {
                logger.error("Couldn't get performance scalability dataset.", e);
            }
            return chart;
        }
    }

    public AngularChart getSuccessScalabilityGraph(int projectId) throws SQLException {

        String bucketQuery = "select distinct(relativeTimeBucket) from stats.deltas where projectid = ?";
        List<Number> labels = new ArrayList<>();
        List<Number> data = new ArrayList<>();
        try (PreparedStatement pstmt = connect.prepareStatement(bucketQuery)) {
            pstmt.setInt(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    labels.add(rs.getInt(1));
                }
            }
        }

        for (int i = 0; i < labels.size(); i++) {
            data.add(0); //initialize each success rate to be 0, for each label, a.k.a. bucket
        }

        try (PreparedStatement preparedStatement = connect.prepareStatement("select relativeTimeBucket as xaxis, count(statusCode)"
                + " from stats.deltas where projectid = ? and statusCode = 200 group by xaxis order by xaxis asc;")) {
            preparedStatement.setInt(1, projectId);

            AngularChart chart = new AngularChart();
            chart.setLabels(labels);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Integer label = resultSet.getInt(1);
                    Integer numberOfSucesses = resultSet.getInt(2);
                    data.set(labels.indexOf(label), numberOfSucesses);
                }
                chart.setData(data);
            } catch (Exception e) {
                logger.error("Couldn't get success scalability dataset.", e);
            }
            return chart;
        }
    }

    public AngularChart getRequestGraph(int projectId) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("select relativeTimeBucket as xaxis, requests as yaxis from stats.deltas where projectid = ? group by xaxis, requests order by xaxis asc;")) {
            preparedStatement.setInt(1, projectId);

            AngularChart chart = new AngularChart();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                GraphResultSetSource iterator = new GraphResultSetSource(resultSet, "integer", "integer");
                AngularLineGraph graph = new AngularLineGraph(chart, iterator, "integer", "integer");
                graph.populateChart();
                chart = graph.getChart();
            } catch (Exception e) {
                logger.error("Couldn't get request input dataset.", e);
            }
            return chart;
        }
    }

    public String getStatsQuery(String type) {
        String result;
        switch (type.toLowerCase(Locale.ENGLISH)) {
            case "avg":
                result = "select \n"
                        + "	avg(yaxis) as avgRttPerSec, b.projectname\n"
                        + "from \n"
                        + "	(select round(relativeTime/1000.0, 0) as xaxis, avg(roundTripTime) as yaxis, projectid from stats.deltas where projectid = ? group by projectid, xaxis order by projectid, xaxis asc) as a \n"
                        + "inner join \n"
                        + "	projects as b\n"
                        + "	on a.projectid = b.id\n"
                        + "group by b.projectname";
                break;
            case "max":
                result = "select \n"
                        + "	max(yaxis) as avgRttPerSec, b.projectname\n"
                        + "from \n"
                        + "	(select round(relativeTime/1000.0, 0) as xaxis, avg(roundTripTime) as yaxis, projectid from stats.deltas where projectid = ? group by projectid, xaxis order by projectid, xaxis asc) as a \n"
                        + "inner join \n"
                        + "	projects as b\n"
                        + "	on a.projectid = b.id\n"
                        + "group by b.projectname";
                break;
            case "min":
                result = "select \n"
                        + "	min(yaxis) as avgRttPerSec, b.projectname\n"
                        + "from \n"
                        + "	(select round(relativeTime/1000.0, 0) as xaxis, avg(roundTripTime) as yaxis, projectid from stats.deltas where projectid = ? group by projectid, xaxis order by projectid, xaxis asc) as a \n"
                        + "inner join \n"
                        + "	projects as b\n"
                        + "	on a.projectid = b.id\n"
                        + "group by b.projectname";
                break;
            case "capacity":
                result = "select \n"
                        + "	max(yaxis) as capacityPerSec, b.projectname\n"
                        + "from \n"
                        + "	(select round(relativeTime/1000.0, 0) as xaxis, count('x') as yaxis, projectid from stats.deltas where projectid = ? group by projectid, xaxis order by projectid, xaxis asc) as a \n"
                        + "inner join \n"
                        + "	projects as b\n"
                        + "	on a.projectid = b.id\n"
                        + "group by b.projectname";
                break;
            case "reliability":
                result = "select exp(-(failedRequests / duration))*100 as reliability, projectname from stats.projects where id=?";
                break;
            default:
                throw new IllegalArgumentException("Unrecognized statistics query type: " + type);
        }
        return result;
    }

    public StatusResponse getStatsGraph(String[] projectNames, String type) throws SQLException {

        final String query = getStatsQuery(type);
        try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {

            BarGraph graph = new BarGraph(false);
            graph.setCaption("Statistics - " + type.toUpperCase(Locale.ENGLISH) + " for Selected Projects");
            graph.setXaxis("Selected Projects");

            switch (type) {
                case "avg":
                case "max":
                case "min":
                    graph.setYaxis(type.toUpperCase(Locale.ENGLISH) + " Response Time / Sec");
                    break;
                case "capacity":
                    graph.setYaxis(type.toUpperCase(Locale.ENGLISH) + " Users");
                    break;
                case "reliability":
                    graph.setYaxis(type.toUpperCase(Locale.ENGLISH) + " %");
                    break;
                default:
                    throw new RuntimeException("Cannot get statistics graph for unsupported type: " + type);
            }

            for (String projectName : projectNames) {
                logger.info("statsGraph: processing projectName=" + projectName);
                int projectId = getProjectId(projectName);
                logger.info("statsGraph: processing projectName=" + projectName + " and projectId=" + projectId);

                preparedStatement.setInt(1, projectId);

                ProjectResponse projectInfo = getProject(projectId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    GraphResultSetSource iterator = new GraphResultSetSource(resultSet, true);
                    graph.addSeries(iterator, projectInfo.getProjectname());
                }
            }

            return graph.getResult();
        }
    }

    @Deprecated
    public StatusResponse getLoadTestResults(int projectid) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("select roundTripTime, relativeTime from stats.deltas where projectid = ?")) {
            preparedStatement.setInt(1, projectid);

            StatusResponse result;
            Series series;
            Category category;
            List<Integer> rspTimes;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                result = new StatusResponse();
                series = new Series();
                rspTimes = new ArrayList<>();
                List<SeriesData> seriesData = new ArrayList<>();
                series.setData(seriesData);
                category = new Category();
                List<CategoryLabel> categoryLabels = new ArrayList<>();
                category.setCategory(categoryLabels);
                int minTime = 0;
                int maxTime = 0;
                while (resultSet.next()) {
                    final int roundTripTime = resultSet.getInt(1);
                    final int relativeTime = resultSet.getInt(2);
                    rspTimes.add(roundTripTime);
                    SeriesData data = new SeriesData();
//                data.setLabel(relativeTime + "");
//                data.setValue(roundTripTime + "");
                    data.setX(relativeTime + "");
                    data.setY(roundTripTime + "");

                    CategoryLabel label = new CategoryLabel();
                    // label.setLabel((relativeTime) + "");
                    label.setX((relativeTime) + "");
                    categoryLabels.add(label);

                    if (relativeTime > maxTime) {
                        maxTime = relativeTime;
                    }

                    seriesData.add(data);
                }
            }

            ProjectResponse projectInfo = getProject(projectid);
            series.setSeriesname(projectInfo.getProjectname());
            result.setSeries(series);
            result.setCategory(category);
            result.setRspTimes(rspTimes);

            return result;
        }
    }

    /**
     * public StatusResponse getLoadTestGraph2(int projectid) throws SQLException {
     *
     * try (PreparedStatement preparedStatement = connect.prepareStatement("select count('x') as yaxis, roundTripTime as
     * xaxis from stats.deltas where projectid = ? group by roundTripTime")) { preparedStatement.setInt(1, projectid);
     *
     * ScatterGraph graph = new ScatterGraph(false); graph.setCaption("Response Count vs Response Time (ms)");
     * graph.setXaxis("Response Time (ms)"); graph.setYaxis("Response Count");
     *
     * ProjectResponse projectInfo = getProject(projectid); try (ResultSet resultSet = preparedStatement.executeQuery())
     * { GraphResultSetSource iterator = new GraphResultSetSource(resultSet); graph.addSeries(iterator,
     * projectInfo.getProjectname()); }
     *
     * return graph.getResult(); } } *
     */
    public StatusResponse getPerformanceHistory(int projectId) throws SQLException {
        String query = "select dateCreated, round(avg(roundTripTime), 2) from deltas where projectid= ? group by UNIX_TIMESTAMP(dateCreated),\n"
                + "dateCreated";
        try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            ScrollLineGraph graph = new ScrollLineGraph();
            graph.setCaption("Average Response Time History");
            graph.setXaxis("Time");
            graph.setYaxis("Response Time (ms)");
            ProjectResponse projectInfo = getProject(projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                GraphResultSetSource iterator = new GraphResultSetSource(resultSet, "timestamp", "double");
                graph.addSeries(iterator, projectInfo.getProjectname());
            }
            return graph.getResult();
        }
    }
    
    public int[] getPerformanceDelta(int projectId) throws SQLException {
    	String query = "select requestCount, avgResponseTime, failedRequests from projects where id = ?";
    	int[] result = new int[3];
    	  try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
              preparedStatement.setInt(1, projectId);
              try (ResultSet resultSet = preparedStatement.executeQuery()) {
            	 if(resultSet.next()){
            	 result[0] =  resultSet.getInt(1);
            	 result[1] =  resultSet.getInt(2);
            	 result[2] = resultSet.getInt(3);
            	 return result;
            	 } else {
            		 return null;
            	 }
              }
    	  }
    }

//    public StatusResponse getPerformanceHistory(int projectId) throws SQLException {
//        String query = "select dateCreated, avg(roundTripTime) from deltas where projectid= ? group by UNIX_TIMESTAMP(dateCreated),\n" +
//                "dateCreated";
//        try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
//            preparedStatement.setInt(1, projectId);
//            StatusResponse result;
//            List<Series> seriesList = new ArrayList<>();
//            Series performanceTimeSeries = new Series();
//            Category category;
//            try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                result = new StatusResponse();
//                List<SeriesData> performanceTimeSeriesList= new ArrayList<>();
//                performanceTimeSeries.setData(performanceTimeSeriesList);
//
//                category = new Category();
//                List<CategoryLabel> categoryLabels = new ArrayList<>();
//                category.setCategory(categoryLabels);
//
//                while (resultSet.next()) {
//                    final Timestamp timeStamp = resultSet.getTimestamp(1);
//                    final double avgResponseTime = resultSet.getDouble(2);
//                    logger.info("Date: " + timeStamp);
//                    logger.info("Avg resp time: " + avgResponseTime);
//
//                    CategoryLabel label = new CategoryLabel();
//                    label.setLabel(timeStamp + "");
//                    categoryLabels.add(label);
//
//                    SeriesData performanceTimeSeriesData = new SeriesData();
//                    performanceTimeSeriesData.setValue(avgResponseTime + "");
//                    performanceTimeSeriesList.add(performanceTimeSeriesData);
//                }
//            }
//
//            ProjectResponse projectInfo = getProject(projectId);
//            performanceTimeSeries.setSeriesname(projectInfo.getProjectname() + " Averages");
//            seriesList.add(performanceTimeSeries);
//            result.setDataset(seriesList);
//            result.setCategory(category);
//            return result;
//        }
//    }
    public StatusResponse getLoadTestAverages(int projectid) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("select round(relativeTime / 1000, 1) as relativeTimeSec, count('x') as calls, avg(roundTripTime) avgMsPerSec from deltas where projectid = ? group by relativeTimeSec;")) {
            preparedStatement.setInt(1, projectid);

            StatusResponse result;
            List<Series> seriesList = new ArrayList<>();
            Series avgTimeSeries = new Series();
            Series totalCallSeries = new Series();
            Category category;
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                result = new StatusResponse();

                List<SeriesData> avgTimeSeriesDataList = new ArrayList<>();
                avgTimeSeries.setData(avgTimeSeriesDataList);

                List<SeriesData> totalCallSeriesDataList = new ArrayList<>();
                totalCallSeries.setData(totalCallSeriesDataList);

                category = new Category();
                List<CategoryLabel> categoryLabels = new ArrayList<>();
                category.setCategory(categoryLabels);

                while (resultSet.next()) {
                    final double relativeTime = resultSet.getDouble(1);
                    final int resultCount = resultSet.getInt(2);
                    final int avgTime = resultSet.getInt(3);

                    SeriesData avgTimeSeriesData = new SeriesData();
                    avgTimeSeriesData.setLabel(relativeTime + "");
                    avgTimeSeriesData.setValue(avgTime + "");
                    avgTimeSeriesDataList.add(avgTimeSeriesData);

                    SeriesData totalCallSeriesData = new SeriesData();
                    totalCallSeriesData.setLabel(relativeTime + "");
                    totalCallSeriesData.setValue(resultCount + "");
                    totalCallSeriesDataList.add(totalCallSeriesData);

                    CategoryLabel label = new CategoryLabel();
                    label.setLabel((relativeTime) + "");
                    categoryLabels.add(label);
                }
            }

            ProjectResponse projectInfo = getProject(projectid);
            avgTimeSeries.setSeriesname(projectInfo.getProjectname() + " Averages");
            totalCallSeries.setSeriesname(projectInfo.getProjectname() + " Webservice Calls");

            seriesList.add(avgTimeSeries);
            seriesList.add(totalCallSeries);

            result.setDataset(seriesList);

            result.setCategory(category);

            return result;
        }
    }

    public int createScalabilityProject(String projectName, String uri, String distribution, int warmUpTime,
            int testDuration, int stepDurationMs, int stepCount, int userCount) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into stats.projects (projectname, uri, testType, "
                + "distribution, warmUpTime, testDuration, stepDuration, stepCount, userCount) values (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, projectName);
            preparedStatement.setString(2, uri);
            preparedStatement.setString(3, "scalability");
            preparedStatement.setString(4, distribution);
            preparedStatement.setInt(5, warmUpTime);
            preparedStatement.setInt(6, testDuration);
            preparedStatement.setInt(7, stepDurationMs);
            preparedStatement.setInt(8, stepCount);
            preparedStatement.setInt(9, userCount);

            preparedStatement.executeUpdate();
        }

        int primaryKey = getProjectId(projectName);

        return primaryKey;
    }

    public int createCapacityProject(String projectName, String uri, int warmUpTime,
            int testDuration, int stepDurationMs, int stepCount, int userCount) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("insert into stats.projects (projectname, uri, testType, "
                + "warmUpTime, testDuration, stepDuration, stepCount, userCount) values (?, ?, ?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setString(1, projectName);
            preparedStatement.setString(2, uri);
            preparedStatement.setString(3, "capacity");
            preparedStatement.setInt(4, warmUpTime);
            preparedStatement.setInt(5, testDuration);
            preparedStatement.setInt(6, stepDurationMs);
            preparedStatement.setInt(7, stepCount);
            preparedStatement.setInt(8, userCount);

            preparedStatement.executeUpdate();
        }
        int primaryKey = getProjectId(projectName);

        return primaryKey;
    }

    public int updateProjectRequestCount(int projectId, int count) throws SQLException {

        try (PreparedStatement preparedStatement = connect.prepareStatement("update stats.projects set requestCount=? where id = ?")) {
            preparedStatement.setInt(1, count);
            preparedStatement.setInt(2, projectId);

            preparedStatement.executeUpdate();
        }

        return projectId;
    }

    public void deleteProject(int projectId) throws SQLException {
        try (PreparedStatement preparedStatement = connect.prepareStatement("delete from stats.projects where id = ?")) {
            preparedStatement.setInt(1, projectId);
            preparedStatement.executeUpdate();
        }
    }

    public void setProjAvgResponseTimeMaxUsers(int avgResponseTime, int userCount, int projectId)
            throws SQLException {
        try (PreparedStatement preparedStatement = connect
                .prepareStatement("update stats.projects set avgResponseTime=?, userCount=? where id=?")) {
            preparedStatement.setInt(1, avgResponseTime);
            preparedStatement.setInt(2, userCount);
            preparedStatement.setInt(3, projectId);
            preparedStatement.executeUpdate();
        }
    }

    public void close() {
        try {
            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            // Ignore
        }
    }


    public GenericResult saveStat(int id, String url, long avg) throws SQLException {
        try (PreparedStatement preparedStatement = connect
                .prepareStatement("insert into stats.webstats (projectid, uri, responsetime, testdate ) values (?,?,?,now())")) {
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, url);
            preparedStatement.setLong(3, avg);
            int rs = preparedStatement.executeUpdate();
            return new GenericResult(rs >1, id);
        }
    }

    public StatusResponse getGraphData(int projectId) throws SQLException{
        String query = "select testdate, round(avg(responsetime), 2) from stats.webstats where projectid= ? group by UNIX_TIMESTAMP(testdate), testdate;";
        try (PreparedStatement preparedStatement = connect.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            ScrollLineGraph graph = new ScrollLineGraph();
            graph.setCaption("Average Response Time History");
            graph.setXaxis("Time");
            graph.setYaxis("Response Time (ms)");
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                GraphResultSetSource iterator = new GraphResultSetSource(resultSet, "timestamp", "double");
                graph.addSeries(iterator, "Upload Test");
            }
            return graph.getResult();
        }
    }
}
