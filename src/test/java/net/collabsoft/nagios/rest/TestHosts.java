package net.collabsoft.nagios.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.core.Response;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.cache.CacheManager;
import net.collabsoft.nagios.cache.CacheManagerImpl;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosFileParserImpl;
import net.collabsoft.nagios.rest.v1.Hosts;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( CacheManagerImpl.class )
public class TestHosts extends MockObjectTestCase {

    private static final String PATH= System.getProperty("java.io.tmpdir") + "/nagios-api/status.dat";
    private static final String HOST_JSON_OUTPUT = "{\"id\":\"3c0c617\",\"type\":\"HOST\",\"properties\":{\"host_name\":\"localhost\"}}";
    private static final String HOSTS_JSON_OUTPUT = "[{\"id\":\"3c0c617\",\"type\":\"HOST\",\"properties\":{\"host_name\":\"localhost\"}}]";
    private static final String SERVICES_JSON_OUTPUT = "[{\"id\":\"8996851\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Current Load\"}},{\"id\":\"95bfafa\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Current Users\"}},{\"id\":\"0fe0ed6\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"HTTP\"}},{\"id\":\"8b9c2ef\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"PING\"}},{\"id\":\"1176561\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Root Partition\"}},{\"id\":\"e0a544f\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"SSH\"}},{\"id\":\"23d18e2\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Swap Usage\"}},{\"id\":\"2c9703c\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Total Processes\"}}]";
    private StatusObjects statusObjects;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() throws IOException {
        String status = IOUtils.toString(this.getClass().getResourceAsStream("/data/nagios-3.0-status.dat"),"UTF-8");
        FileUtils.write(new File(PATH), status);
        AppConfig.getInstance().setFile(PATH);
        statusObjects = NagiosFileParserImpl.getNagiosStatus();

        Mock mockCacheManager = new Mock(CacheManager.class);
        mockCacheManager.expects(once()).method("getEntry").will(returnValue(null));
        mockCacheManager.expects(once()).method("getEntry").will(returnValue(statusObjects));
        
        PowerMockito.mockStatic(CacheManagerImpl.class);
        PowerMockito.when(CacheManagerImpl.getInstance()).thenReturn((CacheManager)mockCacheManager.proxy());
    }
    
    @Override
    public void tearDown() {
        FileUtils.deleteQuietly(new File(PATH));
    }
    
    @Test
    public void testGetHosts() {
        Hosts hosts = new Hosts();
        Response response = hosts.getHosts();
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getHosts());
        assertEquals(json, (String)response.getEntity());
        assertEquals(HOSTS_JSON_OUTPUT, (String) response.getEntity());
        
        response = hosts.getHosts();
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testGetHost() {
        Hosts hosts = new Hosts();
        Response response = hosts.getHost("localhost");
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getHost("localhost"));
        assertEquals(json, (String)response.getEntity());
        assertEquals(HOST_JSON_OUTPUT, (String) response.getEntity());
        
        response = hosts.getHost("localhost");
        assertEquals(500, response.getStatus());
    }
    
    @Test
    public void testGetHostServices() {
        Hosts hosts = new Hosts();
        Response response = hosts.getHostServices("localhost");
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getServicesByHost("localhost"));
        assertEquals(json, (String)response.getEntity());
        assertEquals(SERVICES_JSON_OUTPUT, (String) response.getEntity());
        
        response = hosts.getHostServices("localhost");
        assertEquals(500, response.getStatus());
    }
    
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
