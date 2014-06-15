package net.collabsoft.nagios;

import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.jmock.MockObjectTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert.*;

public class TestAppServer extends MockObjectTestCase {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8081;
    private static final String DIFF_HOST = "example.org";
    private static final int DIFF_PORT = 8081;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    
    // ----------------------------------------------------------------------------------------------- Getters & Setters

    @Override
    public void setUp() {
        
    }
    
    @After
    public void testWrapUp() {
        
    }
    
    @Test
    public void testConstructor() {
        AppServer appServer = new AppServer();
        assertNotNull(appServer);
        assertEquals(DEFAULT_HOST, appServer.getHostName());
        assertEquals(DEFAULT_PORT, appServer.getPort());
        
        appServer = new AppServer(DIFF_HOST);
        assertNotNull(appServer);
        assertEquals(DIFF_HOST, appServer.getHostName());
        assertEquals(DEFAULT_PORT, appServer.getPort());
        
        appServer = new AppServer(DIFF_PORT);
        assertNotNull(appServer);
        assertEquals(DEFAULT_HOST, appServer.getHostName());
        assertEquals(DIFF_PORT, appServer.getPort());

        appServer = new AppServer(DIFF_HOST, DIFF_PORT);
        assertNotNull(appServer);
        assertEquals(DIFF_HOST, appServer.getHostName());
        assertEquals(DIFF_PORT, appServer.getPort());
    }

    @Test
    public void testProperties() {
        URI uri = UriBuilder.fromUri(String.format("http://%s", DIFF_HOST)).port(DIFF_PORT).build();
        
        AppServer appServer = new AppServer(DIFF_HOST, DIFF_PORT);
        assertEquals(DIFF_HOST, appServer.getHostName());
        assertEquals(DIFF_PORT, appServer.getPort());
        assertEquals(uri, appServer.getURI());
    }
    
    @Test
    public void testServer() {
        try {
            AppServer appServer = new AppServer();
            appServer.start();
            appServer.stop();
        } catch (IOException ex) {
            fail("This should not throw an error");
        }
        
        try {
            AppServer appServer = new AppServer();
            appServer.stop();
        } catch (IOException ex) {
            fail("This should not throw an error");
        }
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
