package net.collabsoft.nagios.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.List;
import net.collabsoft.nagios.objects.StatusObject.Type;
import org.apache.commons.codec.digest.DigestUtils;

public class StatusObjects {

    @Expose private StatusObject info;
    @Expose private StatusObject programstatus;
    @Expose private HashMap<String, StatusObject> hosts;
    @Expose private HashMap<String, StatusObject> services;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public StatusObjects() {
        this.hosts = Maps.newHashMap();
        this.services = Maps.newHashMap();
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public StatusObject getInfo() {
        return this.info;
    }
    
    public StatusObject getProgramStatus() {
        return programstatus;
    }
    
    public List<StatusObject> getHosts() {
        List<StatusObject> result = Lists.newArrayList();
        for(StatusObject host : hosts.values()) {
            StatusObject item = new StatusObjectImpl(host.getType());
            item.setId(host.getId());
            item.setProperty("host_name", host.getProperty("host_name"));
            result.add(item);
        }
        return result;
    }
    
    public StatusObject getHost(String id) {
        StatusObject result = hosts.get(id);
        if(result == null) {
            result = hosts.get(getHashIdentifier(id));
        }
        return result;
    }
    
    public List<StatusObject> getServices() {
        List<StatusObject> result = Lists.newArrayList();
        for(StatusObject service : services.values()) {
            StatusObject item = new StatusObjectImpl(service.getType());
            item.setId(service.getId());
            item.setProperty("host_name", service.getProperty("host_name"));
            item.setProperty("service_description", service.getProperty("service_description"));
            result.add(item);
        }
        return result;
    }

    public StatusObject getService(String id) {
        return services.get(id);
    }

    public List<StatusObject> getServicesByHost(String hostIdentifier) {
        StatusObject host = getHost(hostIdentifier);
        if(host != null) {
            List<StatusObject> result = Lists.newArrayList();
            for(StatusObject statusObj : getServices()) {
                if(statusObj.getProperty("host_name").equals(host.getProperty("host_name"))) {
                    result.add(statusObj);
                }
            }
            return result;
        }
        return null;
    }
    
    public String getHashIdentifier(String fingerprint) {
        String hash = DigestUtils.sha1Hex(fingerprint);
        return hash.substring(0,7);
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods

    public void clear() {
        this.info = null;
        this.programstatus = null;
        this.hosts = Maps.newHashMap();
        this.services = Maps.newHashMap();
    }
    
    public void add(StatusObject statusObj) {
        
        switch(statusObj.getType()) {
            case HOST:
                String name = statusObj.getProperty("host_name");
                statusObj.setId(getHashIdentifier(name));
                this.hosts.put(statusObj.getId(), statusObj);
                break;
            case SERVICE:
                String host = statusObj.getProperty("host_name");
                String desc = statusObj.getProperty("service_description");
                statusObj.setId(getHashIdentifier(host + ";" + desc));
                this.services.put(statusObj.getId(), statusObj);
                break;
            case INFO:
                this.info = statusObj;
                break;
            case PROGRAM:
                this.programstatus = statusObj;
                break;
            case CONTACT:
                break;
            case COMMENT:
                break;
        }
        
    }
    
    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    public static Type getTypeByName(String name) {
        try {
            name = name.toLowerCase().trim();
            if(name.equals("hoststatus")) {
                return StatusObject.Type.HOST;
            } else if(name.equals("servicestatus")) {
                return StatusObject.Type.SERVICE;
            } else if(name.equals("info")) {
                return StatusObject.Type.INFO;
            } else if(name.equals("programstatus")) {
                return StatusObject.Type.PROGRAM;
            } else if(name.equals("contactstatus")) {
                return StatusObject.Type.CONTACT;
            } else if(name.equals("hostcomment")) {
                return StatusObject.Type.COMMENT;
            } else if(name.equals("servicecomment")) {
                return StatusObject.Type.COMMENT;
            } else {
                return null;
            }
        } catch(NullPointerException npe) {
            return null;
        }
    }
}
