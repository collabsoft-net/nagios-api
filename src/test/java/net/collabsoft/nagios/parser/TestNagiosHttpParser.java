package net.collabsoft.nagios.parser;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import static junit.framework.TestCase.assertNotNull;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.parser.NagiosHttpParser.StatusObjectType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {HttpClientBuilder.class , SSLContext.class })
public class TestNagiosHttpParser extends MockObjectTestCase {

    private enum NagiosVersion { INVALID, NOSTATUSFILE, V20, V30, V31, V32, V33, V34, V35, V40 }
    
    private static final String PATH = System.getProperty("java.io.tmpdir") + "/nagios-api/input.html";
    private static final String TEST_BASEURL = "http://localhost/nagios/cgi-bin/";
    private static final String TEST_USERNAME= "nagiosadmin";
    private static final String TEST_PASSWORD= "nagiosadmin";
    private static final boolean TEST_TRUSTSSLCERTIFICATE = true;
    private static final String TEST_INVALID_URL = "bla^INVALID";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() throws IOException {
    }
    
    @Override
    public void tearDown() {
        FileUtils.deleteQuietly(new File(PATH));
    }

    @Test
    public void testConstructor() {
        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO);
        assertNotNull(parser);
        
        parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        assertNotNull(parser);
        assertEquals(StatusObjectType.INFO, parser.getStatusObjectType());
        assertEquals(TEST_BASEURL, parser.getBaseUrl());

        parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL, TEST_TRUSTSSLCERTIFICATE);
        assertNotNull(parser);
        assertEquals(StatusObjectType.INFO, parser.getStatusObjectType());
        assertEquals(TEST_BASEURL, parser.getBaseUrl());
        assertEquals(TEST_TRUSTSSLCERTIFICATE, parser.shouldTrustSSLCertificate());
        
        parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL, TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(parser);
        assertEquals(StatusObjectType.INFO, parser.getStatusObjectType());
        assertEquals(TEST_BASEURL, parser.getBaseUrl());
        assertEquals(TEST_USERNAME, parser.getUsername());
        assertEquals(TEST_PASSWORD, parser.getPassword());
        
        parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL, TEST_USERNAME, TEST_PASSWORD, TEST_TRUSTSSLCERTIFICATE);
        assertNotNull(parser);
        assertEquals(StatusObjectType.INFO, parser.getStatusObjectType());
        assertEquals(TEST_BASEURL, parser.getBaseUrl());
        assertEquals(TEST_USERNAME, parser.getUsername());
        assertEquals(TEST_PASSWORD, parser.getPassword());
        assertEquals(TEST_TRUSTSSLCERTIFICATE, parser.shouldTrustSSLCertificate());
    }
    
    @Test
    public void testProperties() {
        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO);
        parser.setStatusObjectType(StatusObjectType.COMMENTS);
        parser.setBaseUrl(TEST_BASEURL);
        parser.setUsername(TEST_USERNAME);
        parser.setPassword(TEST_PASSWORD);
        parser.setTrustSSLCertificate(TEST_TRUSTSSLCERTIFICATE);
        
        assertNotNull(parser);
        assertEquals(StatusObjectType.COMMENTS, parser.getStatusObjectType());
        assertEquals(TEST_BASEURL, parser.getBaseUrl());
        assertEquals(TEST_USERNAME, parser.getUsername());
        assertEquals(TEST_PASSWORD, parser.getPassword());
        assertEquals(TEST_TRUSTSSLCERTIFICATE, parser.shouldTrustSSLCertificate());
    }
    
    @Test
    public void testParse() throws IOException {
        testBackwardsCompatibilityForVersion(NagiosVersion.V30, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V31, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V32, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V33, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V34, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V35, StatusObjectType.INFO);
        testBackwardsCompatibilityForVersion(NagiosVersion.V40, StatusObjectType.INFO);

        testBackwardsCompatibilityForVersion(NagiosVersion.V30, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V31, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V32, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V33, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V34, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V35, StatusObjectType.PROGRAMDATA);
        testBackwardsCompatibilityForVersion(NagiosVersion.V40, StatusObjectType.PROGRAMDATA);
        
        testBackwardsCompatibilityForVersion(NagiosVersion.V30, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V31, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V32, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V33, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V34, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V35, StatusObjectType.HOSTDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V40, StatusObjectType.HOSTDETAILS);
        
        testBackwardsCompatibilityForVersion(NagiosVersion.V30, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V31, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V32, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V33, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V34, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V35, StatusObjectType.SERVICEDETAILS);
        testBackwardsCompatibilityForVersion(NagiosVersion.V40, StatusObjectType.SERVICEDETAILS);
        
        try {
            NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.COMMENTS, TEST_BASEURL);
            StatusObjects status = parser.parse();
            fail("This should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // THIS EXCEPTION SHOULD HAVE BEEN THROWN
        }
    }
    
    @Test
    public void testSSLConnection() throws IOException, NoSuchAlgorithmException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(null);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        SSLContext mockSSLContext = PowerMockito.mock(SSLContext.class);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);
        PowerMockito.mockStatic(SSLContext.class);
        PowerMockito.when(SSLContext.getInstance((String)any())).thenReturn(mockSSLContext);

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        parser.setTrustSSLCertificate(TEST_TRUSTSSLCERTIFICATE);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
    }
    
    
    @Test
    public void testInvalidResponse() throws IOException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(null);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
        
        parser = new NagiosHttpParser(StatusObjectType.PROGRAMDATA, TEST_BASEURL);
        status = parser.parse();
        assertNull(status.getProgramStatus().getProperty("Program Version:"));

        parser = new NagiosHttpParser(StatusObjectType.HOSTDETAILS, TEST_BASEURL);
        status = parser.parse();
        assertEquals(0, status.getHosts().size());

        parser = new NagiosHttpParser(StatusObjectType.SERVICEDETAILS, TEST_BASEURL);
        status = parser.parse();
        assertEquals(0, status.getServices().size());
    }

    @Test
    public void testInvalidUri() throws IOException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(null);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_INVALID_URL);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
    }    
    
    @Test
    public void testResponseIOException() throws IOException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenThrow(new IOException());
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
    }    
    
    @Test
    public void testSSLNoSuchAlgorithmException() throws IOException, NoSuchAlgorithmException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(null);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);
        PowerMockito.mockStatic(SSLContext.class);
        PowerMockito.when(SSLContext.getInstance((String)any())).thenThrow(new NoSuchAlgorithmException());

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        parser.setTrustSSLCertificate(TEST_TRUSTSSLCERTIFICATE);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
    }

    @Test
    public void testSSLKeyManagementException() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(null);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        SSLContext mockSSLContext = PowerMockito.mock(SSLContext.class);
        doThrow(new KeyManagementException()).when(mockSSLContext).init((KeyManager[]) any(), (TrustManager[]) any(), (SecureRandom) any());
                
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);
        PowerMockito.mockStatic(SSLContext.class);
        PowerMockito.when(SSLContext.getInstance((String)any())).thenReturn(mockSSLContext);

        NagiosHttpParser parser = new NagiosHttpParser(StatusObjectType.INFO, TEST_BASEURL);
        parser.setTrustSSLCertificate(TEST_TRUSTSSLCERTIFICATE);
        StatusObjects status = parser.parse();
        assertNull(status.getInfo().getProperty("version"));
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    
    
    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods

    private void testBackwardsCompatibilityForVersion(NagiosVersion version, StatusObjectType type) throws IOException {
        prepareStatusFile(version, type);
        String contents = FileUtils.readFileToString(new File(PATH));
        
        CloseableHttpClient mockHttpClient = PowerMockito.mock(CloseableHttpClient.class);
        PowerMockito.when(mockHttpClient.execute((HttpGet) any(), (ResponseHandler) any())).thenReturn(contents);
        
        HttpClientBuilder mockBuilder = new MockHttpClientBuilder(mockHttpClient);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn((HttpClientBuilder)mockBuilder);
        
        NagiosHttpParser parser = new NagiosHttpParser(type, TEST_BASEURL);
        StatusObjects status = parser.parse();
        
        switch(type) {
            case INFO:
                assertEquals(getVersionNumber(version), status.getInfo().getProperty("version"));
                break;
            case HOSTDETAILS:
                assertNotNull(status.getHost("localhost"));
                break;
            case SERVICEDETAILS:
                assertEquals(8, status.getServicesByHost("localhost").size());
                break;
        }
    }

    private void prepareStatusFile(NagiosVersion version, StatusObjectType type) throws IOException {
        FileUtils.deleteQuietly(new File(PATH));
        String status = IOUtils.toString(this.getClass().getResourceAsStream(getPathForVersionAndType(version, type)),"UTF-8");
        FileUtils.write(new File(PATH), status);
    }
    
    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private String getPathForVersionAndType(NagiosVersion version, StatusObjectType type) {
        
        String typeName = "";
        switch(type) {
            case COMMENTS: typeName = "comments"; break;
            case SERVICEDETAILS : typeName = "details"; break;
            case HOSTDETAILS : typeName = "hostdetails"; break;
            case PROGRAMDATA:
            case INFO : typeName = "info"; break;
        }
        
        switch(version) {
            case V20: return String.format("/data/nagios-2.0-%s.html", typeName);
            case V30: return String.format("/data/nagios-3.0-%s.html", typeName);
            case V31: return String.format("/data/nagios-3.1-%s.html", typeName); 
            case V32: return String.format("/data/nagios-3.2-%s.html", typeName);
            case V33: return String.format("/data/nagios-3.3-%s.html", typeName);
            case V34: return String.format("/data/nagios-3.4-%s.html", typeName);
            case V35: return String.format("/data/nagios-3.5-%s.html", typeName);
            case V40: return String.format("/data/nagios-4.0-%s.html", typeName);
            case INVALID: return "/data/nagios-invalid-input.html";
            case NOSTATUSFILE: return "/data/nagios-3.0-status.dat";
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
    
    private class MockHttpClientBuilder extends HttpClientBuilder {
        
        private final HttpClient client;
        
        public MockHttpClientBuilder(HttpClient client) {
            this.client = client;
        }
        
        @Override
        public CloseableHttpClient build() {
            return (CloseableHttpClient)client;
        }
    }

}
