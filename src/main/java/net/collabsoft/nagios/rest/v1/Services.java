package net.collabsoft.nagios.rest.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.collabsoft.nagios.cache.CacheManager;
import net.collabsoft.nagios.objects.StatusObject;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosParser;

@Path("/rest/v1/services")
public class Services {

    // ----------------------------------------------------------------------------------------------- Constructor

    public Services() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    @GET 
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/list") 
    public Response getServices() {
        CacheManager cm = CacheManager.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getServices());
            return Response.ok(json).build();
        }
    }

    @GET
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/detail/{id}")
    public Response getService(@PathParam("id") String id) {
        CacheManager cm = CacheManager.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            StatusObject host = status.getService(id);
            String json = gson.toJson(host);
            return Response.ok(json).build();
        }
    }

    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
