package net.collabsoft.nagios.parser;

import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import net.collabsoft.nagios.AppConfig;
import net.collabsoft.nagios.AppConfig.ParserType;
import net.collabsoft.nagios.cache.CacheLoaderForKey;
import net.collabsoft.nagios.cache.CacheLoaderForParserType;
import net.collabsoft.nagios.objects.StatusObjects;
import net.collabsoft.nagios.objects.StatusObject;
import net.collabsoft.nagios.objects.StatusObject.Type;
import net.collabsoft.nagios.objects.StatusObjectImpl;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class NagiosFileParserImpl extends AbstractParserImpl {

    private static final Logger log = Logger.getLogger(NagiosParser.class);

    private String path;
    private final StatusObjects status;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public NagiosFileParserImpl(String path) {
        this.path = path;
        this.status = new StatusObjects();
    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    
    public StatusObjects parse() {
        status.clear();
        String content = getFileContents();
        String[] lines = content.split(System.lineSeparator());

        String name = "";
        String properties = "";
        boolean inClosure = false;
        
        for(String line : lines) {
            line = line.trim();

            // Ignore empty lines and comments
            if(line.isEmpty() || line.startsWith("#"))
                continue;
            
            if(inClosure) {
                if(line.endsWith("}")) {
                    addType(name, properties);
                    name = "";
                    properties = "";
                    inClosure = false;
                } else {
                    properties += line + System.lineSeparator();
                }
            } else {
                name = line.substring(0, line.length() - 1);
                inClosure = true;
            }
        }
        
        return status;
    }

    @CacheLoaderForKey(CACHEKEY)
    @CacheLoaderForParserType(ParserType.FILE)
    public static StatusObjects getNagiosStatus() {
        NagiosParser parser = new NagiosFileParserImpl(AppConfig.getInstance().getFile());
        return parser.parse();
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods

    
    // ----------------------------------------------------------------------------------------------- Private methods

    private void addType(String name, String properties) {
        Type type = StatusObjects.getTypeByName(name);
        if(type != null) {
            StatusObject statusObj = new StatusObjectImpl(type);
            statusObj.setProperties(getProperties(properties));
            try {
                status.add(statusObj);
            } catch(UnsupportedOperationException ex) {
                log.warn(ex);
            }
        }
    }
    
    

    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private LinkedHashMap<String, String> getProperties(String content) {
        LinkedHashMap<String, String> properties = Maps.newLinkedHashMap();
        String[] lines = content.split(System.lineSeparator());
        for(String line : lines) {
            line = line.trim();

            if(line.contains("=")) {
                String key = line.substring(0, line.indexOf("="));
                String value= line.substring(line.indexOf("=") + 1);
                properties.put(key.toLowerCase(), value);
            }
        }
        
        return properties;
    }
    
    private String getFileContents() {
        try {
            File inputFile = new File(path);
            if(inputFile.exists()) {
                try {
                    return FileUtils.readFileToString(new File(path));
                } catch(IOException ex) {
                    log.debug(ex);
                    return null;
                }
            } else {
                log.error("Nagios 'status.dat' file not found. Path: " + path);
                return null;
            }
        } catch (NullPointerException npe) {
            log.error("Nagios 'status.dat' file not found: invalid path provided", npe);
            return null;
        }
    }
    
}
