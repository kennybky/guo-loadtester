package edu.csula.cs594.client.resource;
import com.google.gson.Gson;

import edu.csula.cs594.client.CliClient;
import edu.csula.cs594.client.DatabaseClient;
import edu.csula.cs594.client.dao.ProjectResponse;
import edu.csula.cs594.client.dao.StatusResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.csula.cs594.client.dao.model.WebProjectTest;
import org.slf4j.LoggerFactory;

@Path("/ws")
public class WebServiceResource {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProjectResource.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final DatabaseClient dbClient;
	private final Map<Integer, CliClient> cliClientMap;
    
    public WebServiceResource(@Context ServletContext context) {
		dbClient = (DatabaseClient) context.getAttribute("dbClient");
		cliClientMap = (Map<Integer, CliClient>) context.getAttribute("cliClientMap");
    }
    
    //I'll change this later for only websites
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjects() {
        System.out.println("GET - /ws");
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

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectById(@PathParam("id") int id) {
        System.out.println("GET - /ws/" + id);

       
        return Response
                .status(Response.Status.OK)
                .entity(new Gson().toJson("Not yet implemented"))
                .build();
    }

    @POST
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryWS(String project) {
        System.out.println("POST - /ws/query");
        StatusResponse statusResponse = new StatusResponse();

        WebProjectTest testProject = new Gson().fromJson(project, WebProjectTest.class);
        String uri = testProject.getUri();
        String method = testProject.getMethod();
        Map<String, String> params = testProject.getParams();
        try {
        	boolean need_params = true;
        	boolean ssl = false;
        	
        	
        	
        	
        	// automagically detect a secure connection requirement
        	if(uri.toLowerCase().contains("https"))
        		ssl = true;
        	
        	// reconstruct a GET request url string thing

        	if(method.equals("GET")) {
        		need_params = false;
        		if (params.keySet().size()>0) {
                    uri += "?";
                    uri += getParams(params);
                }

        	}
        	
        	System.out.println("sending a  request to " + uri + " ssl is " + ssl);

        	
        	long start = System.currentTimeMillis();
        	HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
        	
        	if (ssl) {
        		SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        		((HttpsURLConnection) connection).setSSLSocketFactory(socketFactory);
        	}
        	
        	connection.setDoInput(true);
        	connection.setRequestMethod(method);
        	connection.setInstanceFollowRedirects(true);
        	connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        	connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
        

        	if(need_params) {
        		connection.setDoOutput(true);
        		
        		OutputStream os = connection.getOutputStream();
        		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        		writer.write(postParams(params));
        		writer.flush();
        		writer.close();
        		os.close();
        		
        		connection.connect();
        	}
        	
        	//URLConnection connection = new URL(ws.getUrl()).openConnection();
        	InputStream response = connection.getInputStream();
        	
        	Scanner scanner = new Scanner(response);
        	String responseBody = scanner.useDelimiter("\\A").next();
        	scanner.close();
        	
        	
        	
        	long end = System.currentTimeMillis();
     
        	int time = (int)(end - start);
        	
        	 statusResponse.setAvgResponseTime(time);
        	 statusResponse.setUri(uri);
            statusResponse.setMessage(responseBody);
        	 
        }
        catch(Exception e) {
        	// do something?
        	System.out.println(e.getMessage() + " here " );
            e.printStackTrace();
        	return Response.status(400).entity(e.getMessage()).build();
        }

        //Project pendingProject = new Gson().fromJson(ws, Project.class);

        return Response.ok().entity(statusResponse).build();
    }
    
    private String postParams(Map<String, String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if(first){
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }



        return result.toString();
    }

    private String getParams(Map<String, String> params){
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if(first){
                first = false;
            } else {
                result.append("&");
            }
            result.append(entry.getKey() +"="+ entry.getValue());
        }
       return result.toString();
    }

}
