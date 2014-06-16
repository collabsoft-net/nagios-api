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
import net.collabsoft.nagios.parser.NagiosParser;
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
    private static final String PROGRAM_JSON_OUTPUT = "{\"id\":\"8941f1b\",\"type\":\"PROGRAM\",\"properties\":{\"modified_host_attributes\":\"0\",\"active_scheduled_service_check_stats\":\"1,8,27\",\"total_external_command_buffer_slots\":\"4096\",\"next_event_id\":\"28\",\"nagios_pid\":\"6957\",\"active_host_checks_enabled\":\"1\",\"passive_service_checks_enabled\":\"1\",\"enable_notifications\":\"1\",\"passive_host_checks_enabled\":\"1\",\"high_external_command_buffer_slots\":\"0\",\"next_problem_id\":\"9\",\"check_host_freshness\":\"0\",\"external_command_stats\":\"0,0,0\",\"parallel_host_check_stats\":\"0,1,3\",\"obsess_over_services\":\"0\",\"active_scheduled_host_check_stats\":\"0,1,3\",\"enable_flap_detection\":\"1\",\"enable_failure_prediction\":\"1\",\"modified_service_attributes\":\"0\",\"enable_event_handlers\":\"1\",\"global_host_event_handler\":\"\",\"daemon_mode\":\"1\",\"check_service_freshness\":\"1\",\"global_service_event_handler\":\"\",\"last_command_check\":\"1402831868\",\"serial_host_check_stats\":\"0,0,0\",\"used_external_command_buffer_slots\":\"0\",\"cached_host_check_stats\":\"1,1,5\",\"active_ondemand_host_check_stats\":\"1,1,5\",\"next_notification_id\":\"1\",\"next_downtime_id\":\"1\",\"last_log_rotation\":\"0\",\"active_service_checks_enabled\":\"1\",\"obsess_over_hosts\":\"0\",\"active_ondemand_service_check_stats\":\"0,0,0\",\"passive_service_check_stats\":\"0,0,0\",\"process_performance_data\":\"0\",\"cached_service_check_stats\":\"0,0,0\",\"next_comment_id\":\"1\",\"program_start\":\"1402831028\",\"passive_host_check_stats\":\"0,0,0\"}}";
    private StatusObjects statusObjects;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() throws IOException {
        String status = IOUtils.toString(this.getClass().getResourceAsStream("/data/nagios-3.0-status.dat"),"UTF-8");
        FileUtils.write(new File(PATH), status);
        AppConfig.getInstance().setInputFile(PATH);
        statusObjects = NagiosParser.getNagiosStatus();

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
