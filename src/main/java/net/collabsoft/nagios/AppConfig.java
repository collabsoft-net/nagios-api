package net.collabsoft.nagios;

import com.google.gson.annotations.Expose;
import net.collabsoft.nagios.parser.NagiosFileParserImpl;
import net.collabsoft.nagios.parser.NagiosHttpParserImpl;
import net.collabsoft.nagios.parser.NagiosParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public enum AppConfig {

    INSTANCE;
    
    public enum ParserType { UNKNOWN, FILE, HTTP };
    
    @Expose private ParserType parserType = ParserType.UNKNOWN;
    @Expose private String hostname;
    @Expose private int port;
    @Expose private boolean stateless;
    @Expose private String file;
    @Expose private String url;
    @Expose private String username;
    @Expose private String password;
    @Expose private boolean insecure;
    
    // ----------------------------------------------------------------------------------------------- Constructor
    
    private AppConfig() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public static AppConfig getInstance() {
        return INSTANCE;
    }
    
    public static AppConfig fromArgs(String[] args) throws ParseException {
        AppConfig instance = getInstance();
        instance.loadArgs(args);
        return instance;
    }

    public ParserType getParserType() {
        return parserType;
    }

    public void setParserType(ParserType parserType) {
        this.parserType = parserType;
    }
    
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStateless() {
        return stateless;
    }

    public void setStateless(boolean stateless) {
        this.stateless = stateless;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }    

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public boolean isInsecure() {
        return insecure;
    }

    public void setInsecure(boolean insecure) {
        this.insecure = insecure;
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods

    public static void showOptions() {
        AppConfig config = AppConfig.getInstance();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
        
        switch(config.getParserType()) {
            case UNKNOWN:
                formatter.printHelp( "nagios-api [file|http] [options]" + System.lineSeparator(), config.getOptions() );
                break;
            case FILE:
                formatter.printHelp( "nagios-api file -i \"path to status.dat\" [options]" + System.lineSeparator(), config.getOptions() );
                break;
            case HTTP:
                formatter.printHelp( "nagios-api http -u \"Url to Nagios cgi-bin directory ('http://example.org/nagios/cgi-bin/')\" [options]" + System.lineSeparator(), config.getOptions() );
                break;
        }
    }
    
    // ----------------------------------------------------------------------------------------------- Private methods

    private void loadArgs(String[] args) throws ParseException {

        if(args.length >= 1) {
            if(args[0].equals("file")) {
                this.parserType = ParserType.FILE;
            } else if(args[0].equals("http")) {
                this.parserType = ParserType.HTTP;
            } else {
                this.parserType = ParserType.UNKNOWN;
                throw new ParseException("Please choose a Nagios parser ('file' or 'http')");
            }
        } else {
            this.parserType = ParserType.UNKNOWN;
            throw new ParseException("Please choose which parser type to use ('file' or 'http') to retrieve Nagios information");
        }
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        cmd = parser.parse(getOptions(), args);
                
        this.hostname = cmd.hasOption("h") ? cmd.getOptionValue("h") : "localhost";
        this.port = cmd.hasOption("p") ? Integer.parseInt(cmd.getOptionValue("p")) : AppServer.DEFAULT_PORT;
        this.stateless = cmd.hasOption("s");
        
        this.file = cmd.getOptionValue("f");
        this.url = cmd.getOptionValue("u");
        this.username = cmd.getOptionValue("username");
        this.password = cmd.getOptionValue("password");
        this.insecure = cmd.hasOption("insecure");
        
        // Throw exception for required options
        switch(parserType) {
            case FILE:
                if(!cmd.hasOption("f") || cmd.getOptionValue("f").isEmpty()) throw new ParseException("'-f','--file' is required.");
                NagiosParser nagiosFileParser = new NagiosFileParserImpl(file);
                if(!nagiosFileParser.isValid()) { throw new ParseException("Could not parse Nagios status information file"); }
                break;
                
            case HTTP:
                if(!cmd.hasOption("u") || cmd.getOptionValue("u").isEmpty()) throw new ParseException("'-u','--url' is required.");
                NagiosParser nagiosHttpParser = new NagiosHttpParserImpl(url, username, password, insecure);
                if(!nagiosHttpParser.isValid()) { throw new ParseException("Could not parse Nagios status information"); }
                break;
        }
    }

    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private Options getOptions() {
        Options options = new Options();

        if(!parserType.equals(ParserType.UNKNOWN)) {
            options.addOption("h", "host", true, "Binds the Nagios API server to the given hostname or IP address");
            options.addOption("p", "port", true, "Binds the Nagios API server to the given TCP port");
            options.addOption("s", "stateless", false, "Disables caching of Nagios status information. Caution: can cause performance issues!");
        }
        
        switch(parserType) {
            case FILE:
                options.addOption("f", "file", true, "Path to Nagios 'status.dat' file (Nagios version 3 or later)");
                break;
            case HTTP:
                options.addOption("u", "url", true, "Url to Nagios cgi-bin directory ('http://example.org/nagios/cgi-bin/')");
                options.addOption("username", true, "Username for connecting to the Nagios instance");
                options.addOption("password", true, "Password for connecting to the Nagios instance");
                options.addOption("insecure", false, "Ignore invalid SSL certificates when connecting to https");
        }
        return options;
    }

}
