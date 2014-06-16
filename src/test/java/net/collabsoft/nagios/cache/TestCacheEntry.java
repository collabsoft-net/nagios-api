package net.collabsoft.nagios.cache;


public class TestCacheEntry {

    public static final String CACHEKEY = "TestCacheEntry";
    public static final String ALT_CACHEKEY = "AltTestCacheEntry";
    public static final String ERROR_CACHEKEY = "ErrorTestCacheEntry";
    public static final String INVALID_CACHEKEY = "INVALID";
    
    private String someValue = "someValue";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    public TestCacheEntry() {
        
    }

    // ----------------------------------------------------------------------------------------------- Getters & Setters
    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

    // ----------------------------------------------------------------------------------------------- Public methods

    @CacheLoaderForKey(CACHEKEY)
    public static TestCacheEntry getTestCacheEntry() {
        return new TestCacheEntry();
    }
    
    @CacheLoaderForKey(ALT_CACHEKEY)
    public TestCacheEntry getAltTestCacheEntry() {
        return this;
    }
    
    @CacheLoaderForKey(ERROR_CACHEKEY)
    public TestCacheEntry getErrorTestCacheEntry() {
        throw new UnsupportedOperationException();
    }

    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
