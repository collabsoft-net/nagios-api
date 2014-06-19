package net.collabsoft.nagios.parser;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.AppConfig.ParserType;
import net.collabsoft.nagios.cache.CacheLoaderForKey;
import net.collabsoft.nagios.cache.CacheLoaderForParserType;
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

public class NagiosHttpParserImpl extends AbstractParserImpl {

    private static final Logger log = Logger.getLogger(NagiosParser.class);
    
    private String baseUrl;
    private String username="";
    private String password="";
    private boolean trustSSLCertificate=false;

    private final StatusObjects status;
    private static final String CGI_INFO="extinfo.cgi?&type=0&embedded";
    private static final String CGI_COMMENTS = "extinfo.cgi?&type=3&embedded";
    private static final String CGI_DETAILS="status.cgi?embedded&limit=0&hostgroup=all&style=detail";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public NagiosHttpParserImpl() {
        this.status = new StatusObjects();
    }
    
    public NagiosHttpParserImpl(String baseUrl) {
        this();
        this.baseUrl = baseUrl;
    }
    
    public NagiosHttpParserImpl(String baseUrl, boolean trustSSLCertificate) {
        this(baseUrl);
        this.trustSSLCertificate = trustSSLCertificate;
    }
        
    public NagiosHttpParserImpl(String baseUrl, String username, String password) {
        this(baseUrl);
        this.username = (username != null) ? username : "";
        this.password = (password != null) ? password : "";
    }

    public NagiosHttpParserImpl(String baseUrl, String username, String password, boolean trustSSLCertificate) {
        this(baseUrl, username, password);
        this.trustSSLCertificate = trustSSLCertificate;
    }
        
    // ----------------------------------------------------------------------------------------------- Getters & Setters

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
   
    public StatusObjects parse() {
        status.clear();
        
        status.add(getInfo());
        status.add(getProgramInfo());
        for(StatusObject statusObj : getDetails()) {
            status.add(statusObj);
        }
        
        return status;
    }

    @CacheLoaderForKey(CACHEKEY)
    @CacheLoaderForParserType(ParserType.HTTP)
    public static StatusObjects getNagiosStatus() {
        NagiosParser parser = new NagiosHttpParserImpl(AppConfig.getInstance().getUrl(), 
                                                       AppConfig.getInstance().getUsername(), 
                                                       AppConfig.getInstance().getPassword(),
                                                       AppConfig.getInstance().isInsecure());
        return parser.parse();
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
                result.setProperty("version", Normalizer.normalize(value, Normalizer.Form.NFKC));
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
                result.setProperty(getSanitizedKey(keyCell.text()), Normalizer.normalize(valueCell.text(), Normalizer.Form.NFKC));
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }

        return result;
    }
    
    private List<StatusObject> getDetails() {
        List<StatusObject> result = Lists.newArrayList();

        try {
            Document document = getDocument(getUrl(CGI_DETAILS));
            List<Element> headers = document.select("table.status > tbody > tr:has(th) th");
            List<Element> rows = document.select("table.status > tbody > tr").not(":has(th)");

            String hostname = null;
            for(Element row : rows) {
                StatusObject detailObj = getDetailObject(row, headers, hostname);
                hostname = detailObj.getProperty("host_name");
                
                if(detailObj.getType().equals(StatusObject.Type.HOST)) {
                    StatusObject serviceObj = new StatusObjectImpl(StatusObject.Type.SERVICE);
                    serviceObj.setProperties(detailObj.getProperties());
                    serviceObj.setProperty("service_description", detailObj.getProperty("service"));
                    result.add(serviceObj);
                    
                    detailObj.getProperties().remove("service");
                    detailObj.getProperties().remove("service_description");
                }
                
                result.add(detailObj);
            }
        } catch(IllegalArgumentException ex) {
            log.warn(ex);
        }
        
        return result;
    }
    
    private StatusObject getDetailObject(Element element, List<Element> headers, String hostname) {
        StatusObject statusObj;

        if(element.select("> td:eq(0)").hasText()) {
            hostname = element.select("> td:eq(0) table tbody tr:eq(0) td:eq(0) table tbody tr:eq(0) td:eq(0) a").text();
            statusObj = new StatusObjectImpl(StatusObject.Type.HOST);
        } else {
            statusObj = new StatusObjectImpl(StatusObject.Type.SERVICE);
            statusObj.setProperty("service_description", Normalizer.normalize(element.select("td:eq(1)").text(), Normalizer.Form.NFKC));
        }

        statusObj.setProperty("host_name", hostname);
        for(int i=1; i<headers.size(); i++) {
            String key = headers.get(i).text();
            String value = element.select(String.format("td:eq(%s)", i)).text();
            statusObj.setProperty(getSanitizedKey(key), Normalizer.normalize(value, Normalizer.Form.NFKC));
        }
        return statusObj;
    }
    
    private String getSanitizedKey(String key) {
        key = key.replace(" ", "_");
        key = key.replace(System.lineSeparator(), "");
        key = Normalizer.normalize(key, Normalizer.Form.NFKC);
        key = key.toLowerCase().trim();
        return key;
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
