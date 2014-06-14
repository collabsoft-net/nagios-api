package net.collabsoft.nagios;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.simple.SimpleContainerFactory;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class AppServer {

    private String hostname = "localhost";
    private int port = 5555;
    private Closeable server;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public AppServer() {
        server = null;
    }

    public AppServer(String hostname) {
        this();
        this.hostname = hostname;
    }
    
    public AppServer(int port) {
        this();
        this.port = port;
    }
    
    public AppServer(String hostname, int port) {
        this();
        this.hostname = hostname;
        this.port = port;
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public String getHostName() {
        return hostname;
    }
    
    public int getPort() {
        return port;
    }
    
    public URI getURI() {
        return UriBuilder.fromUri(String.format("http://%s", hostname)).port(port).build();
    }
    

    // ----------------------------------------------------------------------------------------------- Public methods

    public void start() throws IOException {
        server = null;
        server = SimpleContainerFactory.create(getURI(), getResourceConfig());
    }
    
    public void stop() throws IOException {
        if (server != null) {
            server.close();
        }
    }
    
    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private ResourceConfig getResourceConfig() {
        ResourceConfig config = new ResourceConfig();
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                  .addUrls(ClasspathHelper.forPackage("net.collabsoft.nagios"))
                                                  .addScanners(new TypeAnnotationsScanner()));

        for(Class clazz : reflections.getTypesAnnotatedWith(Path.class)) {
            config.register(clazz);
        }
        
        return config;
    }
    
}
