package edu.csula.cs594.client.resource;

import edu.csula.cs594.client.DatabaseClient;
import eu.impact_project.iif.ws.generic.SoapInput;
import eu.impact_project.iif.ws.generic.SoapOperation;
import eu.impact_project.iif.ws.generic.SoapService;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.apache.commons.validator.routines.UrlValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/service/")
public class ServiceResource {

    private static final Logger logger = LoggerFactory.getLogger(ServiceResource.class);
    private final DatabaseClient dbClient;

    public ServiceResource(@Context ServletContext context) {
        dbClient = (DatabaseClient) context.getAttribute("dbClient");
    }

    @GET
    @Path("save")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveService(@QueryParam("name") String name, @QueryParam("descriptionUri") String descriptionUri) {
        if ("".equals(name) || "".equals(descriptionUri)) {
            return Response.serverError().entity("Both the name and wsdl uri need to be non-empty.").build();
        }
        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(descriptionUri)) {
            return Response.serverError().entity("Invalid uri format.").build();
        }
        SoapService service;
        try {
            service = new SoapService(descriptionUri);
        } catch (IOException m) {
            return Response.serverError().entity("Invalid WSDL uri.").build();
        }
        List<SoapOperation> allOps = service.getOperations();

        for (SoapOperation op : allOps) {

            logger.info("Operation - " + op.getName());

            final List<SoapInput> allInputs = op.getInputs();
            final List<String> params = new ArrayList<>();

            for (SoapInput in : allInputs) {
                logger.info("Input Parameters" + in.getName());
                params.add(in.getName());
            }
            String[] uriArr = descriptionUri.split("\\?");
            if (validateService(name, uriArr[0] + "/", op.getName())) {
                try {
                    dbClient.saveService(name, descriptionUri, uriArr[0] + "/", op.getName(), params);
                } catch (SQLException e) {
                    return Response.serverError().entity("Encountered server error whilst adding wsdl url to db.").build();
                }
            }
        }

        return Response.ok().entity("Successfully added wsdl url to database.").build();
    }

    @GET
    @Path("delete")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteService(@QueryParam("id") int id) {
        try {
            dbClient.deleteService(id);
            return Response.ok().entity("Successfully deleted web service id: " + id).build();
        } catch (SQLException e) {
            return Response.serverError().entity("Encountered  SQL error deleting service id: " + id).build();
        }
    }

    @GET
    @Path("saveUrlForBuilder")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveUrlForBuilder(@QueryParam("id") int id, @QueryParam("uri") String uri) {
        if (!nullAndNotEmpty(uri)) {
            return Response.serverError().entity("Cannot save a blank uri.").build();
        } else {
            UrlValidator urlValidator = new UrlValidator();
            if (!urlValidator.isValid(uri)) {
                return Response.serverError().entity("Invalid uri format.").build();
            }
            boolean exists = serviceExists(uri);
            if (exists) {
                return Response.serverError().entity("Uri: " + uri + " already exists in the database.").build();
            } else {
                try {
                    dbClient.saveUrlForBuilder(id, uri);
                    return Response.ok().entity("Successfully saved uri: " + uri).build();
                } catch (SQLException e) {
                    return Response.serverError().entity("Error saving uri: " + uri + " to the database.").build();
                }
            }
        }
    }

    @GET
    @Path("saveUrl")
    @Produces(MediaType.TEXT_PLAIN)
    public Response saveUrl(@QueryParam("uri") String uri) {
        if (!nullAndNotEmpty(uri)) {
            return Response.serverError().entity("Cannot save a blank uri.").build();
        } else {
            UrlValidator urlValidator = new UrlValidator();
            if (!urlValidator.isValid(uri)) {
                return Response.serverError().entity("Invalid uri format.").build();
            }
            boolean exists = serviceExists(uri);
            if (exists) {
                return Response.serverError().entity("Uri: " + uri + " already exists in the database.").build();
            } else {
                try {
                    dbClient.saveUrl(uri);
                    return Response.ok().entity("Successfully saved uri: " + uri).build();
                } catch (SQLException e) {
                    return Response.serverError().entity("Error saving uri: " + uri + " to the database.").build();
                }
            }
        }
    }

    @GET
    @Path("getSavedUrls")
    public Response getSavedUrls() throws SQLException, IOException {
        List<String> urls = dbClient.getSavedUrls();
        return Response.ok().entity(urls).build();
    }

    @GET
    @Path("get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServices() throws SQLException, IOException {
        return Response.ok().entity(dbClient.getServices()).build();
    }

    public boolean validateService(String name, String baseUri, String method) {
        return nullAndNotEmpty(name) && nullAndNotEmpty(baseUri) && nullAndNotEmpty(method);
    }

    public boolean nullAndNotEmpty(String str) {
        return (str != null && !str.isEmpty());
    }

    public boolean serviceExists(String uri) {
        boolean exists = false;
        try {
            int uriId = dbClient.getServiceId(uri);
            if (uriId != -1) exists = true;
        } catch (SQLException e) {
            logger.error("Couldn't get id for service", e);
        }
        return exists;
    }
}
