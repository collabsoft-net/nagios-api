package net.collabsoft.nagios;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.commons.io.Charsets;

@Path("/")
public class AppServerIndex {

    // ----------------------------------------------------------------------------------------------- Constructor

    public AppServerIndex() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    @GET
    @Produces("text/html")    
    public Response index() {
        try {
            URL url = Resources.getResource("index.html");
            String html = Resources.toString(url, Charsets.UTF_8);
            return Response.ok(html).build();
        } catch (IOException ex) {
            return Response.serverError().build();
        }
    }

    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
