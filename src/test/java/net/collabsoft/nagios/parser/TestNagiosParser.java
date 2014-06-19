package net.collabsoft.nagios.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.objects.StatusObject;
import net.collabsoft.nagios.objects.StatusObjects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.MockObjectTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( FileUtils.class )
public class TestNagiosParser extends MockObjectTestCase {

    private enum NagiosVersion { INVALID, NOSTATUSFILE, V20, V30, V31, V32, V33, V34, V35, V40 }
    
    private static final String PATH = System.getProperty("java.io.tmpdir") + "/nagios-api/status.dat";
    private static final String TEST_PATH = "path";

    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() throws IOException {
        prepareStatusFile(NagiosVersion.V30);
    }
    
    @Override
    public void tearDown() {
        FileUtils.deleteQuietly(new File(PATH));
    }

    @Test
    public void testConstructor() {
        NagiosParser parser = new NagiosParser(PATH);
        assertNotNull(parser);
    }
    
    @Test
    public void testProperties() {
        NagiosParser parser = new NagiosParser(PATH);
        assertEquals(PATH, parser.getPath());
        
        parser.setPath(TEST_PATH);
        assertEquals(TEST_PATH, parser.getPath());
    }
    
    @Test
    public void testValidStatusFile() throws IOException {
        assertTrue(NagiosParser.isValidStatusFile(PATH));
        assertFalse(NagiosParser.isValidStatusFile(null));

        prepareStatusFile(NagiosVersion.V20);
        assertFalse(NagiosParser.isValidStatusFile(PATH));
    }
    
    @Test
    public void testBackwardsCompatibility() {
        try {
            testBackwardsCompatibilityForVersion(NagiosVersion.V30);
            testBackwardsCompatibilityForVersion(NagiosVersion.V31);
            testBackwardsCompatibilityForVersion(NagiosVersion.V32);
            testBackwardsCompatibilityForVersion(NagiosVersion.V33);
            testBackwardsCompatibilityForVersion(NagiosVersion.V34);
            testBackwardsCompatibilityForVersion(NagiosVersion.V35);
            testBackwardsCompatibilityForVersion(NagiosVersion.V40);
        } catch(IOException ex) {
            fail("This should not throw an IOException");
        }
    }
    
    @Test
    public void testInvalidInputFile() throws IOException {
        prepareStatusFile(NagiosVersion.V20);
        assertFalse(NagiosParser.isValidStatusFile(PATH));

        prepareStatusFile(NagiosVersion.INVALID);
        assertFalse(NagiosParser.isValidStatusFile(PATH));
        NagiosParser parser = new NagiosParser(PATH);
        StatusObjects status = parser.parse();
        assertEquals(1, status.getServices().size());
        
        prepareStatusFile(NagiosVersion.NOSTATUSFILE);
        assertFalse(NagiosParser.isValidStatusFile(PATH));
        
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.when(FileUtils.readFileToString(new File(PATH))).thenThrow(IOException.class);
        prepareStatusFile(NagiosVersion.INVALID);
        assertFalse(NagiosParser.isValidStatusFile(PATH));
    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods

    private void testBackwardsCompatibilityForVersion(NagiosVersion version) throws IOException {
        prepareStatusFile(version);
        AppConfig.getInstance().setFile(PATH);
        StatusObjects status = NagiosParser.getNagiosStatus();
        assertNotNull(status);
        
        assertNotNull(status.getInfo());
        assertEquals(getVersionNumber(version), status.getInfo().getProperty("version"));
        
        assertNotNull(status.getProgramStatus());
        assertEquals("1", status.getProgramStatus().getProperty("enable_notifications"));

        assertEquals(1, status.getHosts().size());
        assertNotNull(status.getHost("localhost"));
        
        List<StatusObject> services = status.getServicesByHost("localhost");
        assertEquals(8, services.size());
        
        int ok=0, warning=0;
        for(StatusObject service : services) {
            StatusObject serviceObj = status.getService(service.getId());
            if(serviceObj.getProperty("current_state").equals("0")) {
                ok++;
            } else if(serviceObj.getProperty("current_state").equals("1")) {
                warning++;
            }
        }
        
        assertEquals(7, ok);
        assertEquals(1, warning);
    }
    
    private void prepareStatusFile(NagiosVersion version) throws IOException {
        String status = IOUtils.toString(this.getClass().getResourceAsStream(getPathForVersion(version)),"UTF-8");
        FileUtils.write(new File(PATH), status);
    }
    
    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private String getPathForVersion(NagiosVersion version) {
        switch(version) {
            case V20: return "/data/nagios-2.0-status.dat";
            case V30: return "/data/nagios-3.0-status.dat";
            case V31: return "/data/nagios-3.1-status.dat"; 
            case V32: return "/data/nagios-3.2-status.dat"; 
            case V33: return "/data/nagios-3.3-status.dat"; 
            case V34: return "/data/nagios-3.4-status.dat"; 
            case V35: return "/data/nagios-3.5-status.dat"; 
            case V40: return "/data/nagios-4.0-status.dat"; 
            case INVALID: return "/data/nagios-invalid-status.dat";
            case NOSTATUSFILE: return "/data/nagios-3.0-info.html";
            default: return "";
        }
    }
    
    private String getVersionNumber(NagiosVersion version) {
        switch(version) {
            case V20: return "2.0.0";
            case V30: return "3.0";
            case V31: return "3.1.0";
            case V32: return "3.2.0";
            case V33: return "3.3.1";
            case V34: return "3.4.0";
            case V35: return "3.5.0"; 
            case V40: return "4.0.0";
            case INVALID: return "INVALID";
            default: return "";
        }
    }
    
}
