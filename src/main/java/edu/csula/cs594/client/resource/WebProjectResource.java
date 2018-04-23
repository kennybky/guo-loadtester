package edu.csula.cs594.client.resource;

import com.google.gson.Gson;
import edu.csula.cs594.client.CliClient;
import edu.csula.cs594.client.DatabaseClient;
import edu.csula.cs594.client.dao.StatusResponse;
import edu.csula.cs594.client.results.GenericResult;
import edu.csula.cs594.client.results.project.GetProjectResult;
import edu.csula.cs594.client.dao.model.Project;
import edu.csula.cs594.client.results.project.*;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Map;

@Path("/webprojects")
public class WebProjectResource {

    private final DatabaseClient dbClient;
    private final Map<Integer, CliClient> cliClientMap;

    public WebProjectResource(@Context ServletContext context) {
        dbClient = (DatabaseClient) context.getAttribute("dbClient");
        cliClientMap = (Map<Integer, CliClient>) context.getAttribute("cliClientMap");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjects() {
        System.out.println("GET - /projects");

        GetProjectResult result = dbClient.getWebProjects();
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson(result))
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectById(@PathParam("id") int id) {
        System.out.println("GET - /project/" + id);

        GetProjectByIdResult result = dbClient.getProjectById(id);
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson(result))
                .build();
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProject(String project) {
        System.out.println("POST - /projects");

        Project pendingProject = new Gson().fromJson(project, Project.class);
        CreateProjectResult result = dbClient.createWebProject(pendingProject);
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson(result))
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProjectById(@PathParam("id") int id) {
        System.out.println("DELETE - /projects/" + id);

        DeleteProjectByIdResult result = dbClient.deleteWebProject(id);
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson(result))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProject(@PathParam("id") int id, String project) {
        System.out.println("PUT - /projects/" + id);

        Project updatedProject = new Gson().fromJson(project, Project.class);
        UpdateProjectResult result = dbClient.updateProject(updatedProject, id);
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson(result))
                .build();
    }

    @POST
    @Path("/{id}/save")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveStat(@PathParam("id") int id, @QueryParam("url") String url, @QueryParam("avg") long avg){

        try {
            GenericResult i = dbClient.saveStat(id, url, avg);
            return Response.ok().entity(new Gson().toJson(i)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(400).entity(new Gson().toJson(e)).build();
        }
    }

    @GET
    @Path("/{id}/graph")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGraphData(@PathParam("id") int id){

        try {
            StatusResponse response = dbClient.getGraphData(id);
            return Response.ok().entity(new Gson().toJson(response)).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(400).entity(new Gson().toJson(e)).build();
        }


    }

}
