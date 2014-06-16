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
import net.collabsoft.nagios.rest.v1.Services;
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
public class TestServices extends MockObjectTestCase {

    private static final String PATH= System.getProperty("java.io.tmpdir") + "/nagios-api/status.dat";
    private static final String SERVICE_JSON_OUTPUT = "{\"id\":\"5847b2c\",\"type\":\"SERVICE\",\"properties\":{\"notification_period\":\"24x7\",\"last_hard_state_change\":\"1402828710\",\"has_been_checked\":\"1\",\"check_options\":\"0\",\"check_interval\":\"5.000000\",\"long_plugin_output\":\"\",\"next_check\":\"1402832010\",\"acknowledgement_type\":\"0\",\"is_flapping\":\"0\",\"last_time_critical\":\"1402830810\",\"problem_has_been_acknowledged\":\"0\",\"passive_checks_enabled\":\"1\",\"last_hard_state\":\"0\",\"current_state\":\"0\",\"current_notification_id\":\"0\",\"event_handler_enabled\":\"1\",\"no_more_notifications\":\"0\",\"next_notification\":\"0\",\"retry_interval\":\"1.000000\",\"last_time_ok\":\"1402831710\",\"state_type\":\"1\",\"process_performance_data\":\"1\",\"check_command\":\"check_local_disk!20%!10%!/\",\"last_state_change\":\"1402831110\",\"current_notification_number\":\"0\",\"current_attempt\":\"1\",\"current_event_id\":\"23\",\"last_time_unknown\":\"0\",\"max_attempts\":\"4\",\"last_problem_id\":\"5\",\"failure_prediction_enabled\":\"1\",\"active_checks_enabled\":\"1\",\"last_update\":\"1402831868\",\"notifications_enabled\":\"1\",\"should_be_scheduled\":\"1\",\"obsess_over_service\":\"1\",\"flap_detection_enabled\":\"1\",\"last_time_warning\":\"0\",\"last_notification\":\"0\",\"performance_data\":\"/\\u003d1269MB;11412;12838;0;14265\",\"scheduled_downtime_depth\":\"0\",\"check_type\":\"0\",\"percent_state_change\":\"10.92\",\"check_execution_time\":\"0.006\",\"host_name\":\"localhost\",\"check_period\":\"24x7\",\"modified_attributes\":\"0\",\"check_latency\":\"0.032\",\"event_handler\":\"\",\"plugin_output\":\"DISK OK - free space: / 12248 MB (90% inode\\u003d95%):\",\"current_problem_id\":\"0\",\"last_event_id\":\"8\",\"service_description\":\"Root Partition\",\"last_check\":\"1402831710\"}}";
    private static final String SERVICES_JSON_OUTPUT = "[{\"id\":\"5847b2c\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Root Partition\"}},{\"id\":\"d2c1c1e\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"SSH\"}},{\"id\":\"0c24351\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Total Processes\"}},{\"id\":\"708fedd\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"HTTP\"}},{\"id\":\"7f43319\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Current Load\"}},{\"id\":\"6f530f6\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Current Users\"}},{\"id\":\"b3215b5\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Swap Usage\"}},{\"id\":\"28d2917\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"PING\"}}]";
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
    public void testGetServices() {
        Services services = new Services();
        Response response = services.getServices();
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getServices());
        assertEquals(json, (String)response.getEntity());
        assertEquals(SERVICES_JSON_OUTPUT, (String) response.getEntity());
        
        response = services.getServices();
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testGetService() {
        Services services = new Services();
        Response response = services.getService("5847b2c");
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getService("5847b2c"));
        assertEquals(json, (String)response.getEntity());
        assertEquals(SERVICE_JSON_OUTPUT, (String) response.getEntity());
        
        response = services.getService("5847b2c");
        assertEquals(500, response.getStatus());
    }
    
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
