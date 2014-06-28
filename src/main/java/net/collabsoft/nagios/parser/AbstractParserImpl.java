package net.collabsoft.nagios.parser;

import net.collabsoft.nagios.objects.StatusObjects;
import org.apache.log4j.Logger;

public abstract class AbstractParserImpl implements NagiosParser {

    private static final Logger log = Logger.getLogger(NagiosParser.class);
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public AbstractParserImpl() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public boolean isValid() {
        try {
            StatusObjects objects = parse();

            String version = objects.getInfo().getProperty("version");
            return (Integer.parseInt(version.substring(0,1)) >= 3);
        } catch(Exception e) {
            log.debug(e);
            return false;
        }
    }    

    public abstract StatusObjects parse();
    
    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
