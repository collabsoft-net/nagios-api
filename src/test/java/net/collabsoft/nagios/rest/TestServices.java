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
    private static final String SERVICE_JSON_OUTPUT = "{\"id\":\"1176561\",\"type\":\"SERVICE\",\"properties\":{\"host_name\":\"localhost\",\"service_description\":\"Root Partition\",\"modified_attributes\":\"0\",\"check_command\":\"check_local_disk!20%!10%!/\",\"check_period\":\"24x7\",\"notification_period\":\"24x7\",\"check_interval\":\"5.000000\",\"retry_interval\":\"1.000000\",\"event_handler\":\"\",\"has_been_checked\":\"1\",\"should_be_scheduled\":\"1\",\"check_execution_time\":\"0.006\",\"check_latency\":\"0.032\",\"check_type\":\"0\",\"current_state\":\"0\",\"last_hard_state\":\"0\",\"last_event_id\":\"8\",\"current_event_id\":\"23\",\"current_problem_id\":\"0\",\"last_problem_id\":\"5\",\"current_attempt\":\"1\",\"max_attempts\":\"4\",\"state_type\":\"1\",\"last_state_change\":\"1402831110\",\"last_hard_state_change\":\"1402828710\",\"last_time_ok\":\"1402831710\",\"last_time_warning\":\"0\",\"last_time_unknown\":\"0\",\"last_time_critical\":\"1402830810\",\"plugin_output\":\"DISK OK - free space: / 12248 MB (90% inode\\u003d95%):\",\"long_plugin_output\":\"\",\"performance_data\":\"/\\u003d1269MB;11412;12838;0;14265\",\"last_check\":\"1402831710\",\"next_check\":\"1402832010\",\"check_options\":\"0\",\"current_notification_number\":\"0\",\"current_notification_id\":\"0\",\"last_notification\":\"0\",\"next_notification\":\"0\",\"no_more_notifications\":\"0\",\"notifications_enabled\":\"1\",\"active_checks_enabled\":\"1\",\"passive_checks_enabled\":\"1\",\"event_handler_enabled\":\"1\",\"problem_has_been_acknowledged\":\"0\",\"acknowledgement_type\":\"0\",\"flap_detection_enabled\":\"1\",\"failure_prediction_enabled\":\"1\",\"process_performance_data\":\"1\",\"obsess_over_service\":\"1\",\"last_update\":\"1402831868\",\"is_flapping\":\"0\",\"percent_state_change\":\"10.92\",\"scheduled_downtime_depth\":\"0\"}}";
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
        Response response = services.getService("1176561");
        assertEquals(200, response.getStatus());

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObjects.getService("1176561"));
        assertEquals(json, (String)response.getEntity());
        assertEquals(SERVICE_JSON_OUTPUT, (String) response.getEntity());
        
        response = services.getService("1176561");
        assertEquals(500, response.getStatus());
    }
    
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
