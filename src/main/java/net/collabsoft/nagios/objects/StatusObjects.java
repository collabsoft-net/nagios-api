package net.collabsoft.nagios.objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import net.collabsoft.nagios.objects.StatusObject.Type;
import org.apache.commons.codec.digest.DigestUtils;

public class StatusObjects {

    @Expose private StatusObject info;
    @Expose private StatusObject programstatus;
    @Expose private LinkedHashMap<String, StatusObject> hosts;
    @Expose private LinkedHashMap<String, StatusObject> services;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public StatusObjects() {
        this.hosts = Maps.newLinkedHashMap();
        this.services = Maps.newLinkedHashMap();
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
        
        Collections.sort(result, new Comparator<StatusObject>() {
            @Override
            public int compare(StatusObject t, StatusObject t1) {
                return t.getProperty("host_name").compareToIgnoreCase(t1.getProperty("host_name"));
            }
        });
        
        return result;
    }
    
    public StatusObject getHost(String id) {
        StatusObject result = hosts.get(id);
        if(result == null) {
            for(StatusObject statusObj : getHosts()) {
                if(id.equals(statusObj.getProperty("host_name"))) {
                    return statusObj;
                }
            }
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
        
        Collections.sort(result, new Comparator<StatusObject>() {
            @Override
            public int compare(StatusObject t, StatusObject t1) {
                return t.getProperty("service_description").compareToIgnoreCase(t1.getProperty("service_description"));
            }
        });
        
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
            
            Collections.sort(result, new Comparator<StatusObject>() {
                @Override
                public int compare(StatusObject t, StatusObject t1) {
                    return t.getProperty("service_description").compareToIgnoreCase(t1.getProperty("service_description"));
                }
            });
            
            return result;
        }
        return null;
    }
    
    public String getHashIdentifier(StatusObject statusObj) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String json = gson.toJson(statusObj);
        return getHashIdentifier(json);
    }
    
    public String getHashIdentifier(String fingerprint) {
        String hash = DigestUtils.sha1Hex(fingerprint);
        return hash.substring(0,7);
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods

    public void clear() {
        this.info = null;
        this.programstatus = null;
        this.hosts = Maps.newLinkedHashMap();
        this.services = Maps.newLinkedHashMap();
    }
    
    public void add(StatusObject statusObj) throws UnsupportedOperationException {
        
        statusObj.setId(getHashIdentifier(statusObj));
        switch(statusObj.getType()) {
            case HOST:
                this.hosts.put(statusObj.getId(), statusObj);
                break;
            case SERVICE:
                this.services.put(statusObj.getId(), statusObj);
                break;
            case INFO:
                this.info = statusObj;
                break;
            case PROGRAM:
                this.programstatus = statusObj;
                break;
            case CONTACT:
            case COMMENT:
            default:
                throw new UnsupportedOperationException(String.format("The provided status object type (%s) is currently not supported", statusObj.getType()));
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
