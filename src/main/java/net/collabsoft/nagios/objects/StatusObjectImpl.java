package net.collabsoft.nagios.objects;

import com.google.gson.annotations.Expose;
import java.util.LinkedHashMap;

public class StatusObjectImpl implements StatusObject {

    @Expose private String id;
    @Expose private Type type;
    @Expose private LinkedHashMap<String, String> properties;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public StatusObjectImpl() {
        this.properties = new LinkedHashMap<String, String>();
    }
    
    public StatusObjectImpl(Type type) {
        this();
        this.type = type;
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }
    
    public String getProperty(String name) {
        return this.properties.get(name);
    }
    
    public void setProperty(String key, String value) {
        this.properties.put(key, value);
    }
    
    public LinkedHashMap<String, String> getProperties() {
        return this.properties;
    }
    
    public void setProperties(LinkedHashMap<String, String> properties) {
        this.properties = properties;
    }

    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters


}
