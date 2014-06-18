package net.collabsoft.nagios.parser;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import net.collabsoft.nagios.objects.StatusObject;
import net.collabsoft.nagios.objects.StatusObjectImpl;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.utils.X509TrustManagerImpl;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NagiosHttpParser {

    public enum StatusObjectType { COMMENTS, SERVICEDETAILS, HOSTDETAILS, INFO, PROGRAMDATA }
    private static final Logger log = Logger.getLogger(NagiosHttpParser.class);
    
    private String baseUrl;
    private String username="";
    private String password="";
    private boolean trustSSLCertificate=false;
    private StatusObjectType statusObjectType;

    private final StatusObjects status;
    private static final String CGI_INFO="extinfo.cgi?&type=0&embedded";
    private static final String CGI_COMMENTS = "extinfo.cgi?&type=3&embedded";
    private static final String CGI_DETAILS="status.cgi?embedded&limit=0&hostgroup=all&style=detail";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public NagiosHttpParser(StatusObjectType statusObjectType) {
        this.status = new StatusObjects();
        this.statusObjectType = statusObjectType;
    }
    
    public NagiosHttpParser(StatusObjectType statusObjectType, String baseUrl) {
        this(statusObjectType);
        this.baseUrl = baseUrl;
    }
    
    public NagiosHttpParser(StatusObjectType statusObjectType, String baseUrl, boolean trustSSLCertificate) {
        this(statusObjectType, baseUrl);
        this.trustSSLCertificate = trustSSLCertificate;
    }
        
    public NagiosHttpParser(StatusObjectType statusObjectType, String baseUrl, String username, String password) {
        this(statusObjectType, baseUrl);
        this.username = username;
        this.password = password;
    }

    public NagiosHttpParser(StatusObjectType statusObjectType, String baseUrl, String username, String password, boolean trustSSLCertificate) {
        this(statusObjectType, baseUrl, username, password);
        this.trustSSLCertificate = trustSSLCertificate;
    }
        
    // ----------------------------------------------------------------------------------------------- Getters & Setters
    public StatusObjectType getStatusObjectType() {
        return statusObjectType;
    }

    public void setStatusObjectType(StatusObjectType statusObjectType) {
        this.statusObjectType = statusObjectType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean shouldTrustSSLCertificate() {
        return trustSSLCertificate;
    }

    public void setTrustSSLCertificate(boolean trustSSLCertificate) {
        this.trustSSLCertificate = trustSSLCertificate;
    }
   
    public StatusObjects parse() throws UnsupportedOperationException {
        status.clear();
        
        switch(statusObjectType) {
            case COMMENTS: throw new UnsupportedOperationException();
            case INFO: status.add(getInfo()); break;
            case PROGRAMDATA: status.add(getProgramInfo()); break;
            case SERVICEDETAILS:
            case HOSTDETAILS:
                for(StatusObject hostObj : getHosts()) {
                    status.add(hostObj);
                }
                for(StatusObject serviceObj : getServices()) {
                    status.add(serviceObj);
                }
                break;
        }
        
        return status;
    }

    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private StatusObject getInfo() {
        StatusObject result = new StatusObjectImpl(StatusObject.Type.INFO);
        
        try {
            Document document = getDocument(getUrl(CGI_INFO));
            String value = document.select("table.data table > tbody > tr:eq(0) td:eq(1)").text();
            if(!value.isEmpty()) {
                result.setProperty("version", value);
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }

        return result;
    }
    
    private StatusObject getProgramInfo() {
        StatusObject result = new StatusObjectImpl(StatusObject.Type.PROGRAM);

        try {
            Document document = getDocument(getUrl(CGI_INFO));
            List<Element> elements = document.select("table.data table tr");
            for(Element element : elements) {
                Elements keyCell = element.select("td.dataVar");
                Elements valueCell = element.select("td.dataVal");
                result.setProperty(keyCell.text(), valueCell.text());
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }

        return result;
    }
    
    private List<StatusObject> getHosts() {
        List<StatusObject> result = Lists.newArrayList();

        try {
            Document document = getDocument(getUrl(CGI_DETAILS));
            List<Element> headers = document.select("table.status > tbody > tr:has(th) th");
            List<Element> rows = document.select("table.status > tbody > tr").not(":has(th)");

            String hostname;
            for(Element row : rows) {
                StatusObject host = new StatusObjectImpl(StatusObject.Type.HOST);

                if(row.select("> td:eq(0)").hasText()) {
                    hostname = row.select("> td:eq(0) table tbody tr:eq(0) td:eq(0) table tbody tr:eq(0) td:eq(0) a").text();
                    host.setProperty("host_name", hostname);
                    for(int i=0; i<headers.size(); i++) {
                        String key = headers.get(i).text();
                        String value = row.select(String.format("td:eq(%s)", i)).text();
                        host.setProperty(key, value);
                    }
                    result.add(host);
                }
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }
        
        return result;
    }
    
    private List<StatusObject> getServices() {
        List<StatusObject> result = Lists.newArrayList();
        
        try {
            Document document = getDocument(getUrl(CGI_DETAILS));
            List<Element> headers = document.select("table.status > tbody > tr:has(th) th");
            List<Element> rows = document.select("table.status > tbody > tr").not(":has(th)");

            String hostname=null;
            for(Element row : rows) {
                if(row.select("> td:eq(0)").hasText()) {
                    hostname = row.select("> td:eq(0) table tbody tr:eq(0) td:eq(0) table tbody tr:eq(0) td:eq(0) a").text();
                }

                StatusObject service = new StatusObjectImpl(StatusObject.Type.SERVICE);
                service.setProperty("host_name", hostname);
                service.setProperty("service_description", row.select("td:eq(1)").text());

                for(int i=0; i<headers.size(); i++) {
                    String key = headers.get(i).text();
                    String value = row.select(String.format("td:eq(%s)", i)).text();
                    service.setProperty(key, value);
                }
                result.add(service);
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }

        return result;
    }
    
    private Document getDocument(String url) throws IllegalArgumentException {
        String source = getSource(url);
        Document document = Jsoup.parse(source);
        return document;
    }
    
    private String getSource(String url) {
        String response = "";
        
        try {
            URI uri = new URI(url);
            HttpClient httpclient = getHttpClient(uri);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpGet httpget = new HttpGet(uri);
            response = httpclient.execute(httpget, responseHandler);
            httpget.releaseConnection();
        } catch (NullPointerException ex ) {
            log.debug("An error occurred while retrieving information from Nagios, could not connect to " + url + ". Please check your Nagios server configuration.", ex);
        } catch (URISyntaxException ex) {
            log.debug("An error occurred while retrieving information from Nagios, could not connect to " + url + ". Please check your Nagios server configuration.", ex);
        } catch (IOException ex) {
            log.debug("An error occurred while retrieving information from Nagios, could not connect to " + url + ". Please check your Nagios server configuration.", ex);
        }        
        return response;
    }
    
    private HttpClient getHttpClient(URI uri) {
        try {
            CredentialsProvider credentials = new BasicCredentialsProvider();
            credentials.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));
            
            if(trustSSLCertificate) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[] { new X509TrustManagerImpl() }, new SecureRandom());
                return HttpClientBuilder.create()
                                        .setDefaultCredentialsProvider(credentials)
                                        .setSslcontext(sslContext)
                                        .build();
            } else {
                return HttpClientBuilder.create()
                                        .setDefaultCredentialsProvider(credentials)
                                        .build();
            }
        } catch (KeyManagementException ex) {
            log.debug("An error occurred while retrieving information from Nagios", ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            log.debug("An error occurred while retrieving information from Nagios", ex);
            return null;
        }
    }
    
    private String getUrl(String path) {
        if(!baseUrl.endsWith("/")) {
            return String.format("%s/%s", baseUrl, path);
        } else {
            return String.format("%s%s", baseUrl, path);
        }
    }
    
}
