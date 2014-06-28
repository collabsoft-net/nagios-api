package net.collabsoft.nagios.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import javax.ws.rs.core.Response;
import static junit.framework.TestCase.assertEquals;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.cache.CacheManager;
import net.collabsoft.nagios.cache.CacheManagerImpl;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosFileParserImpl;
import net.collabsoft.nagios.rest.v1.Nagios;
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
public class TestNagios extends MockObjectTestCase {

    private static final String PATH= System.getProperty("java.io.tmpdir") + "/nagios-api/status.dat";
    private static final String INFO_JSON_OUTPUT = "{\"id\":\"ab3347e\",\"type\":\"INFO\",\"properties\":{\"created\":\"1402831868\",\"version\":\"3.0\"}}";
    private static final String PROGRAM_JSON_OUTPUT = "{\"id\":\"abb1c73\",\"type\":\"PROGRAM\",\"properties\":{\"modified_host_attributes\":\"0\",\"modified_service_attributes\":\"0\",\"nagios_pid\":\"6957\",\"daemon_mode\":\"1\",\"program_start\":\"1402831028\",\"last_command_check\":\"1402831868\",\"last_log_rotation\":\"0\",\"enable_notifications\":\"1\",\"active_service_checks_enabled\":\"1\",\"passive_service_checks_enabled\":\"1\",\"active_host_checks_enabled\":\"1\",\"passive_host_checks_enabled\":\"1\",\"enable_event_handlers\":\"1\",\"obsess_over_services\":\"0\",\"obsess_over_hosts\":\"0\",\"check_service_freshness\":\"1\",\"check_host_freshness\":\"0\",\"enable_flap_detection\":\"1\",\"enable_failure_prediction\":\"1\",\"process_performance_data\":\"0\",\"global_host_event_handler\":\"\",\"global_service_event_handler\":\"\",\"next_comment_id\":\"1\",\"next_downtime_id\":\"1\",\"next_event_id\":\"28\",\"next_problem_id\":\"9\",\"next_notification_id\":\"1\",\"total_external_command_buffer_slots\":\"4096\",\"used_external_command_buffer_slots\":\"0\",\"high_external_command_buffer_slots\":\"0\",\"active_scheduled_host_check_stats\":\"0,1,3\",\"active_ondemand_host_check_stats\":\"1,1,5\",\"passive_host_check_stats\":\"0,0,0\",\"active_scheduled_service_check_stats\":\"1,8,27\",\"active_ondemand_service_check_stats\":\"0,0,0\",\"passive_service_check_stats\":\"0,0,0\",\"cached_host_check_stats\":\"1,1,5\",\"cached_service_check_stats\":\"0,0,0\",\"external_command_stats\":\"0,0,0\",\"parallel_host_check_stats\":\"0,1,3\",\"serial_host_check_stats\":\"0,0,0\"}}";
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
    public void testGetConfig() {
        Nagios nagios = new Nagios();
        Response response = nagios.getConfig();
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(AppConfig.getInstance());
        assertEquals(json, (String)response.getEntity());
    }

    @Test
    public void testGetInfo() {
        Nagios nagios = new Nagios();
        Response response = nagios.getInfo();
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getInfo());
        assertEquals(json, (String)response.getEntity());
        assertEquals(INFO_JSON_OUTPUT, (String) response.getEntity());
        
        response = nagios.getInfo();
        assertEquals(500, response.getStatus());
    }
    
    @Test
    public void testGetProgram() {
        Nagios nagios = new Nagios();
        Response response = nagios.getProgramStatus();
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getProgramStatus());
        assertEquals(json, (String)response.getEntity());
        assertEquals(PROGRAM_JSON_OUTPUT, (String) response.getEntity());
        
        response = nagios.getProgramStatus();
        assertEquals(500, response.getStatus());
    }
    
    
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
