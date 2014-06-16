package net.collabsoft.nagios.objects;

import org.jmock.MockObjectTestCase;
import org.junit.Test;

public class TestStatusObject extends MockObjectTestCase {

    private static final String TEST_ID = "objectId";
    private static final StatusObject.Type TEST_TYPE = StatusObject.Type.INFO;
    private static final String TEST_PROPERTY_KEY = "property";
    private static final String TEST_PROPERTY_VALUE = "value";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() {
        
    }
    
    @Override
    public void tearDown() {
        
    }

    @Test
    public void testConstructor() {
        StatusObject statusObject = new StatusObjectImpl();
        assertNotNull(statusObject);
        
        statusObject = new StatusObjectImpl(TEST_TYPE);
        assertNotNull(statusObject);
        assertEquals(TEST_TYPE, statusObject.getType());
    }
    
    @Test
    public void testProperties() {
        StatusObject statusObject = new StatusObjectImpl();
        
        assertNull(statusObject.getType());
        assertNull(statusObject.getId());
        assertNotNull(statusObject.getProperties());
        assertEquals(0, statusObject.getProperties().size());
        
        statusObject.setId(TEST_ID);
        assertEquals(TEST_ID, statusObject.getId());
                
        statusObject.setType(TEST_TYPE);
        assertEquals(TEST_TYPE, statusObject.getType());

        statusObject.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
        assertEquals(TEST_PROPERTY_VALUE, statusObject.getProperty(TEST_PROPERTY_KEY));
        assertEquals(1, statusObject.getProperties().size());
        
    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
