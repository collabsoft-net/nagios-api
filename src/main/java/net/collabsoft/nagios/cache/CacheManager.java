
package net.collabsoft.nagios.cache;

import com.google.common.cache.LoadingCache;

public interface CacheManager {
    
    public LoadingCache<String, Object> getCache();
    public Object getEntry(String key);
    public void setEntry(String key, Object value);
    public void refresh();
    public void clear();
    
}
