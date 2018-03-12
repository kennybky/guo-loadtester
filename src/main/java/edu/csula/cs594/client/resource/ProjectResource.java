
package edu.csula.cs594.client.resource;

import edu.csula.cs594.client.CliClient;
import edu.csula.cs594.client.DatabaseClient;
import edu.csula.cs594.client.dao.ProjectResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/project/")
public class ProjectResource {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProjectResource.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final DatabaseClient dbClient;
	private final Map<Integer, CliClient> cliClientMap;
    
    public ProjectResource(@Context ServletContext context) {
		dbClient = (DatabaseClient) context.getAttribute("dbClient");
		cliClientMap = (Map<Integer, CliClient>) context.getAttribute("cliClientMap");
    }
     
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listProjects() {

		List<ProjectResponse> loadProjects;
		List<ProjectResponse> scheduledProjects;

		try {
			loadProjects = dbClient.getLoadProjects(); //load test projects
			scheduledProjects = dbClient.getScheduledProjects(); //for regularly running projects used for reliability and availalbility

		} catch (SQLException ex) {
			Logger.getLogger(ProjectResource.class.getName()).log(Level.SEVERE, null, ex);
			return Response.serverError().build();
		}

		List<List<ProjectResponse>> projects = new ArrayList<>();
		projects.add(loadProjects);
		projects.add(scheduledProjects);

		for (List<ProjectResponse> projectList : projects) {
			for (ProjectResponse project : projectList) {
				if (cliClientMap.containsKey(project.getProjectid())) {
					project.setInProgress(true);
				}
			}
		}
		return Response.ok().entity(projects).build();
	}

	@POST
	@Path("deleteProjects")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteProjects(InputStream jsonData) throws SQLException {

		StringBuilder jsonStr = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(jsonData, UTF8));
			String line;
			while ((line = in.readLine()) != null) {
				jsonStr.append(line);
			}
		} catch(IOException e) {
			logger.info("Error parsing JSON data");
			return Response.serverError().entity("Error parsing JSON data").build();
		}
		logger.info("JSON received: " + jsonStr.toString());
		JSONObject jsonObj;
		JSONParser parser = new JSONParser();
		try {
			jsonObj = (JSONObject) parser.parse(jsonStr.toString());
		} catch(ParseException e) {
			logger.error("Couldn't parse JSON Obj", e);
			return Response.serverError().entity("Couldn't parse JSON Obj").build();
		}
		JSONArray projectIds;
		try {
			projectIds = (JSONArray) jsonObj.get("projectIds");
		} catch (Exception e) {
			logger.error("Couldn't parse JSON array for projectIds", e);
			return Response.serverError().entity("Couldn't parse JSON array for projectIds").build();
		}
		for (int i = 0; i < projectIds.size(); i++) {
			try {
				int projectId = ((int) (long)projectIds.get(i));
				dbClient.deleteLoadTestData(projectId);
				logger.info("Deleting project load test data for id: " + projectId);
				dbClient.deleteProject(projectId);
				logger.info("Deleting project data for id: " + projectId);
			} catch (SQLException ex) {
				logger.error("Error deleting project data", ex);
				return Response.serverError().entity("Error deleting project data").build();
			}
		}
		//return Response.serverError().entity("Error deleting project data").build();
		return Response.ok().entity("Project deletion request successful for finished load tests.").build();
	}
}
