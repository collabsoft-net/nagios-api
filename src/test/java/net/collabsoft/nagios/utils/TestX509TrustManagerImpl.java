package net.collabsoft.nagios.utils;

import org.jmock.MockObjectTestCase;
import org.junit.Test;

public class TestX509TrustManagerImpl extends MockObjectTestCase {

    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() {
        
    }
    
    @Override
    public void tearDown() {
        
    }

    @Test
    public void testProperties() {
        X509TrustManagerImpl trustManager = new X509TrustManagerImpl();
        trustManager.checkClientTrusted(null, null);
        trustManager.checkServerTrusted(null, null);
        assertNull(trustManager.getAcceptedIssuers());
    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
