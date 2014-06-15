package net.collabsoft.nagios;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jmock.MockObjectTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert.*;

public class TestAppConfig extends MockObjectTestCase {

    private static final Logger log = Logger.getLogger(TestAppConfig.class);
    
    private static final String PATH= System.getProperty("java.io.tmpdir") + "/nagios-api/status.dat";
    private static final String HOST = "localhost";
    private static final int PORT = 8081;
    private static final boolean STATELESS = true;

    // ----------------------------------------------------------------------------------------------- Constructor


    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods

    @Override
    public void setUp() throws Exception {
        String status = IOUtils.toString(this.getClass().getResourceAsStream("/data/nagios-3.0-status.dat"),"UTF-8");
        FileUtils.write(new File(PATH), status);
    }
    
    @After
    public void testWrapUp() {
        FileUtils.deleteQuietly(new File(PATH));
    }
    
    @Test
    public void testConstructor() {
        assertNotNull(AppConfig.getInstance());
    }
    
    @Test
    public void testFromArgs() {
        List<String> arguments = Lists.newArrayList("-i", PATH, "-h", HOST, "-p", String.valueOf(PORT));
        if(STATELESS) { arguments.add("-s"); }
        String[] args = arguments.toArray(new String[arguments.size()]);
        
        AppConfig config = null;
        try {
            config = AppConfig.fromArgs(args);
        } catch (ParseException ex) {
            fail("This should not result in an exception");
            log.error(ex);
        }

        assertNotNull(config);
        if(config != null) {
            assertEquals(PATH, config.getInputFile());
            assertEquals(HOST, config.getHostname());
            assertEquals(PORT, config.getPort());
            assertEquals(STATELESS, config.isStateless());
        }
    }
    
    @Test
    public void testInvalidArgs1() {
        List<String> arguments = Lists.newArrayList("-i", "invalid_path");
        String[] args = arguments.toArray(new String[arguments.size()]);
        
        AppConfig config = null;
        try {
            config = AppConfig.fromArgs(args);
            fail("The above statement should have thrown an exception");
        } catch (ParseException ex) {
            // This should throw an error!
        }

        assertNull(config);
    }    
    
    @Test
    public void testInvalidArgs2() {
        List<String> arguments = Lists.newArrayList();
        String[] args = arguments.toArray(new String[arguments.size()]);
        
        AppConfig config = null;
        try {
            config = AppConfig.fromArgs(args);
            fail("The above statement should have thrown an exception");
        } catch (ParseException ex) {
            // This should throw an error!
        }

        assertNull(config);
    }
    
    @Test
    public void testInvalidArgs3() {
        List<String> arguments = Lists.newArrayList("-i", "");
        String[] args = arguments.toArray(new String[arguments.size()]);
        
        AppConfig config = null;
        try {
            config = AppConfig.fromArgs(args);
            fail("The above statement should have thrown an exception");
        } catch (ParseException ex) {
            // This should throw an error!
        }

        assertNull(config);
    }    
    
    @Test
    public void testDefaultArgs() {
        List<String> arguments = Lists.newArrayList("-i", PATH);
        String[] args = arguments.toArray(new String[arguments.size()]);
        
        AppConfig config = null;
        try {
            config = AppConfig.fromArgs(args);
        } catch (ParseException ex) {
            fail("This should not result in an exception");
            log.error(ex);
        }

        assertNotNull(config);
        if(config != null) {
            assertEquals(HOST, config.getHostname());
            assertEquals(AppServer.DEFAULT_PORT, config.getPort());
            assertEquals(false, config.isStateless() );
        }
    }
    
    @Test
    public void testProperties() {
        AppConfig config = AppConfig.getInstance();

        config.setInputFile(PATH);
        assertEquals(PATH, config.getInputFile());
        
        config.setHostname(HOST);
        assertEquals(HOST, config.getHostname());
        
        config.setPort(PORT);
        assertEquals(PORT, config.getPort());
        
        config.setStateless(STATELESS);
        assertEquals(STATELESS, config.isStateless());
    }
    
    @Test
    public void testShowOptions() throws IOException {
        String options = IOUtils.toString(this.getClass().getResourceAsStream("/showOptions.txt"),"UTF-8");
        
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));
        
        AppConfig.showOptions();
        assertEquals(options, stdOut.toString());
        System.setOut(null);
    }
    
    

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
