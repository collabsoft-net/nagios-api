package net.collabsoft.nagios;

import com.google.gson.annotations.Expose;
import net.collabsoft.nagios.parser.NagiosParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public enum AppConfig {

    INSTANCE;
    
    @Expose private String hostname;
    @Expose private int port;
    @Expose private boolean stateless;
    @Expose private String inputFile;
    
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

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }    
    
    // ----------------------------------------------------------------------------------------------- Public methods

    public static void showOptions() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "nagios-api -i \"path to status.dat\" [options]" + System.lineSeparator(), getInstance().getOptions() );
    }
    
    // ----------------------------------------------------------------------------------------------- Private methods

    private void loadArgs(String[] args) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        cmd = parser.parse(getOptions(), args);

        // Throw exception for required options
        if(!cmd.hasOption("i") || cmd.getOptionValue("i").isEmpty()) throw new ParseException("'-i','--inputFile' is required.");
        
        this.hostname = cmd.hasOption("h") ? cmd.getOptionValue("h") : "localhost";
        this.port = cmd.hasOption("p") ? Integer.parseInt(cmd.getOptionValue("p")) : AppServer.DEFAULT_PORT;
        this.stateless = cmd.hasOption("s");
        this.inputFile = cmd.getOptionValue("i");
        
        if(!NagiosParser.isValidStatusFile(inputFile)) { throw new ParseException("Could not parse Nagios status information file"); }
    }

    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private Options getOptions() {
        Options options = new Options();
        options.addOption("h", "host", true, "Binds the Nagios API server to the given hostname or IP address");
        options.addOption("p", "port", true, "Binds the Nagios API server to the given TCP port");
        options.addOption("s", "stateless", false, "Disables caching of Nagios status information. Caution: can cause performance issues!");
        options.addOption("i", "input", true, "Path to Nagios 'status.dat' file (Nagios version 3 or later)");
        return options;
    }

}
