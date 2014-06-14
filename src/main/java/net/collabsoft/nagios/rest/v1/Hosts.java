package net.collabsoft.nagios.rest.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.collabsoft.nagios.cache.CacheManager;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosParser;

@Path("/rest/v1/hosts")
public class Hosts {

    // ----------------------------------------------------------------------------------------------- Constructor

    public Hosts() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    @GET 
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/list") 
    public Response getHosts() {
        CacheManager cm = CacheManager.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getHosts());
            return Response.ok(json).build();
        }
    }

    @GET
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/detail/{id}")
    public Response getHost(@PathParam("id") String nameOrId) {
        CacheManager cm = CacheManager.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getHost(nameOrId));
            return Response.ok(json).build();
        }
    }

    @GET
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/detail/{id}/services")
    public Response getHostServices(@PathParam("id") String nameOrId) {
        CacheManager cm = CacheManager.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getServicesByHost(nameOrId));
            return Response.ok(json).build();
        }
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
