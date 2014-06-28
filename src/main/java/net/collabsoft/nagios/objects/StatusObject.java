package net.collabsoft.nagios.objects;

import java.util.LinkedHashMap;

public interface StatusObject {

    public enum Type { HOST, SERVICE, INFO, PROGRAM, CONTACT, COMMENT}
    
    // ----------------------------------------------------------------------------------------------- Constructor
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public String getId();
    public void setId(String id);
    public Type getType();
    public void setType(Type type);
    public String getProperty(String name);
    public LinkedHashMap<String, String> getProperties();
    public void setProperty(String key, String value);
    public void setProperties(LinkedHashMap<String, String> properties);

    // ----------------------------------------------------------------------------------------------- Public methods

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
