package net.collabsoft.nagios.rest.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.cache.CacheManager;
import net.collabsoft.nagios.cache.CacheManagerImpl;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosParser;

@Path("/rest/v1/nagios")
public class Nagios {

    // ----------------------------------------------------------------------------------------------- Constructor

    public Nagios() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    @GET 
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/api")
    public Response getConfig() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(AppConfig.getInstance());
        return Response.ok(json).build();
    }
    
    @GET 
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/info") 
    public Response getInfo() {
        CacheManager cm = CacheManagerImpl.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getInfo());
            return Response.ok(json).build();
        }
    }
    

    // ----------------------------------------------------------------------------------------------- Public methods

    @GET 
    @Produces (MediaType.APPLICATION_JSON) 
    @Path ("/program") 
    public Response getProgramStatus() {
        CacheManager cm = CacheManagerImpl.getInstance();
        StatusObjects status = (StatusObjects)cm.getEntry(NagiosParser.CACHEKEY);
        if(status == null) {
            return Response.serverError().build();
        } else {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String json = gson.toJson(status.getProgramStatus());
            return Response.ok(json).build();
        }
    }
    

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
