package net.collabsoft.nagios;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import jersey.repackaged.com.google.common.collect.Lists;
import net.collabsoft.nagios.AppConfig.ParserType;
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
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 8081;
    private static final boolean TEST_STATELESS = true;
    private static final String TEST_URL = "http://localhost/nagios/cgi-bin/";
    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASSWORD = "password";
    private static final boolean TEST_INSECURE = false;

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
    public void testStatusFromArgs() {
        List<String> arguments = Lists.newArrayList("status", "-f", PATH, "-h", TEST_HOST, "-p", String.valueOf(TEST_PORT));
        if(TEST_STATELESS) { arguments.add("-s"); }
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
            assertEquals(PATH, config.getFile());
            assertEquals(TEST_HOST, config.getHostname());
            assertEquals(TEST_PORT, config.getPort());
            assertEquals(TEST_STATELESS, config.isStateless());
        }
    }

    @Test
    public void testHttpFromArgs() {
        List<String> arguments = Lists.newArrayList("http", "-u", TEST_URL, "-username", TEST_USERNAME, "-password", TEST_PASSWORD, "-h", TEST_HOST, "-p", String.valueOf(TEST_PORT));
        if(TEST_INSECURE) { arguments.add("-insecure"); }
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
            assertEquals(TEST_HOST, config.getHostname());
            assertEquals(TEST_PORT, config.getPort());
            assertEquals(TEST_URL, config.getUrl());
            assertEquals(TEST_USERNAME, config.getUsername());
            assertEquals(TEST_PASSWORD, config.getPassword());
            assertEquals(TEST_INSECURE, config.isInsecure());
        }
    }
    
    @Test
    public void testInvalidArgs1() {
        List<String> arguments = Lists.newArrayList("-f", "invalid_path");
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
        List<String> arguments = Lists.newArrayList("-f", "");
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
        List<String> arguments = Lists.newArrayList("status", "-f", PATH);
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
            assertEquals(TEST_HOST, config.getHostname());
            assertEquals(AppServer.DEFAULT_PORT, config.getPort());
            assertEquals(false, config.isStateless() );
        }
    }
    
    @Test
    public void testProperties() {
        AppConfig config = AppConfig.getInstance();

        config.setParserType(ParserType.STATUS);
        assertEquals(ParserType.STATUS, config.getParserType());
        
        config.setFile(PATH);
        assertEquals(PATH, config.getFile());
        
        config.setHostname(TEST_HOST);
        assertEquals(TEST_HOST, config.getHostname());
        
        config.setPort(TEST_PORT);
        assertEquals(TEST_PORT, config.getPort());
        
        config.setStateless(TEST_STATELESS);
        assertEquals(TEST_STATELESS, config.isStateless());
        
        config.setUrl(TEST_URL);
        assertEquals(TEST_URL, config.getUrl());
        
        config.setUsername(TEST_USERNAME);
        assertEquals(TEST_USERNAME, config.getUsername());
        
        config.setPassword(TEST_PASSWORD);
        assertEquals(TEST_PASSWORD, config.getPassword());
        
        config.setInsecure(TEST_INSECURE);
        assertEquals(TEST_INSECURE, config.isInsecure());
    }
    
    @Test
    public void testShowOptions() throws IOException {
        String options = IOUtils.toString(this.getClass().getResourceAsStream("/showOptions.txt"),"UTF-8");
        
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));

        AppConfig config = AppConfig.getInstance();
        config.setParserType(ParserType.UNKNOWN);
        AppConfig.showOptions();
        assertEquals(options, stdOut.toString().trim());
        System.setOut(null);
    }
    
    @Test
    public void testShowStatusOptions() throws IOException {
        String options = IOUtils.toString(this.getClass().getResourceAsStream("/showOptions-status.txt"),"UTF-8");
        
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));

        AppConfig config = AppConfig.getInstance();
        config.setParserType(ParserType.STATUS);
        AppConfig.showOptions();

        assertEquals(options, stdOut.toString().trim());
        System.setOut(null);
    }

    @Test
    public void testShowHttpOptions() throws IOException {
        String options = IOUtils.toString(this.getClass().getResourceAsStream("/showOptions-http.txt"),"UTF-8");
        
        ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdOut));
        
        AppConfig config = AppConfig.getInstance();
        config.setParserType(ParserType.HTTP);
        AppConfig.showOptions();

        assertEquals(options, stdOut.toString().trim());
        System.setOut(null);
    }
    

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
