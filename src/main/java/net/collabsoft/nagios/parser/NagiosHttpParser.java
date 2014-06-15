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

public class NagiosHttpParser {

    private static final Logger log = Logger.getLogger(NagiosHttpParser.class);
    
    private String host;
    private String path;
    private String username;
    private String password;
    private boolean ignoressl;

    private final StatusObjects status;
    private static final String CGI_INFO="extinfo.cgi?&type=0&embedded";
    private static final String CGI_COMMENTS = "extinfo.cgi?&type=3&embedded";
    private static final String CGI_HOSTDETAILS="status.cgi?embedded&limit=0&hostgroup=all&style=hostdetail";
    private static final String CGI_SERVICEDETAILS="status.cgi?embedded&limit=0&hostgroup=all&style=detail";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public NagiosHttpParser() {
        this.status = new StatusObjects();
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public StatusObjects parse() {
        status.clear();
        
        status.add(getProgramInfo());
        for(StatusObject hostObj : getHosts()) {
            status.add(hostObj);
        }
        
        for(StatusObject serviceObj : getServices()) {
            status.add(serviceObj);
        }
        
        return status;
    }

    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private StatusObject getProgramInfo() {
        StatusObject result = new StatusObjectImpl(StatusObject.Type.PROGRAM);
        return result;
    }
    
    private List<StatusObject> getHosts() {
        List<StatusObject> result = Lists.newArrayList();
        return result;
    }
    
    private List<StatusObject> getServices() {
        List<StatusObject> result = Lists.newArrayList();
        return result;
    }
    
    private Document getDocument(String url) {
        String source = getSource(url);

        if (source == null) {
            return null;
        } else {
            Document document = Jsoup.parse(source);
            return document;
        }
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
            
            if(ignoressl) {
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
    
    
}
