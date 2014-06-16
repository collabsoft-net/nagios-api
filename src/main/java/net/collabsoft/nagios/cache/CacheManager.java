package net.collabsoft.nagios.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.collabsoft.nagios.AppConfig;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public enum CacheManager {

    INSTANCE;
    private static final Logger log = Logger.getLogger(CacheManager.class);
    
    private LoadingCache<String, Object> cache;
    private int refreshInterval = 5;
    private TimeUnit refreshIntervalUnit = TimeUnit.MINUTES;
    
    // ----------------------------------------------------------------------------------------------- Constructor

    private CacheManager() {

    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public TimeUnit getRefreshIntervalUnit() {
        return refreshIntervalUnit;
    }

    public void setRefreshIntervalUnit(TimeUnit refreshIntervalUnit) {
        this.refreshIntervalUnit = refreshIntervalUnit;
    }
    
    public LoadingCache<String, Object> getCache() {
        if(this.cache == null) {
            this.cache = getLoadingCache();
        }
        return this.cache;
    }

    public Object getEntry(String key) {
        try {
            return getCache().get(key);
        } catch (Exception ex) {
            log.debug("An error occurred while retrieving cache entry for key " + key, ex);
            return null;
        }
    }
    
    public void setEntry(String key, Object value) {
        if(AppConfig.getInstance().isStateless()) {
            throw new UnsupportedOperationException("The cache is initialized as 'stateless', meaning it will recreate the entry upon each GET request. Storing entries in cache is therefor not supported.");
        }
        getCache().put(key, value);
    }
    
    // ----------------------------------------------------------------------------------------------- Public methods

    public void refresh() {
        for(String key : getCache().asMap().keySet()) {
            getCache().refresh(key);
        }
    }
    
    public void clear() {
        this.cache = null;
    }

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

    private LoadingCache<String, Object> getLoadingCache() {
            if(AppConfig.getInstance().isStateless()) {
                return CacheBuilder.newBuilder()
                        .maximumSize(0)
                        .build(getCacheLoader());
            } else {
                return CacheBuilder.newBuilder()
                        .refreshAfterWrite(refreshInterval, refreshIntervalUnit)
                        .build(getCacheLoader());
            }        
    }
    
    private CacheLoader getCacheLoader() {
        return new CacheLoader<String, Object>() {
            @Override
            public Object load( String key ) throws Exception {
                return getCacheObjectForKey(key);
            }
            
            @Override
            public ListenableFuture<Object> reload(final String key, Object currentObj) {
                // asynchronous!
                ListenableFutureTask<Object> task = ListenableFutureTask.create(new Callable<Object>() {
                    public Object call() {
                        return getCacheObjectForKey(key);
                    }
                });
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.execute(task);
                return task;
            }
        };
    }
    
    private Object getCacheObjectForKey(String key) {
        log.debug("Refreshing entry for key " + key);
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                                                  .addUrls(ClasspathHelper.forPackage("net.collabsoft.nagios"))
                                                  .addScanners(new MethodAnnotationsScanner()));
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(CacheLoaderForKey.class);
        for(Method method : annotated) {
            CacheLoaderForKey annotation = method.getAnnotation(CacheLoaderForKey.class);
            if(annotation.value().equals(key)) {
                try {
                    return method.invoke(null);
                } catch(Exception e) {
                    try {
                        Class classObj = method.getDeclaringClass();
                        return method.invoke(classObj.newInstance());
                    } catch (Exception ex) {
                        log.warn("An error occurred while loading cache entry for key" + key, ex);
                        log.warn("Inner exception", e);
                    }
                }
            }
        }
        return null;
    }
    
}
