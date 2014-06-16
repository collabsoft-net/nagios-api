package net.collabsoft.nagios.objects;

import java.util.List;
import org.jmock.MockObjectTestCase;
import org.junit.Test;

public class TestStatusObjects extends MockObjectTestCase {

    private static final String TEST_OBJECT_HOST_NAME = "host";
    private static final String TEST_OBJECT_SERVICE_DESCRIPTION = "service description";
    private static final String TEST_INVALID_HOST_NAME = "INVALID";
    
    private static final String TYPE_HOST = "hoststatus";
    private static final String TYPE_SERVICE = "servicestatus";
    private static final String TYPE_INFO = "info";
    private static final String TYPE_PROGRAM = "programstatus";
    private static final String TYPE_CONTACT = "contactstatus";
    private static final String TYPE_HOST_COMMENT = "hostcomment";
    private static final String TYPE_SERVICE_COMMENT = "servicecomment";
    private static final String TYPE_UNKNOWN = "unknown";
    
    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() {
        
    }
    
    @Override
    public void tearDown() {
        
    }

    @Test
    public void testConstructor() {
        StatusObjects objects = new StatusObjects();
        assertNotNull(objects);
        
        assertNull(objects.getInfo());
        assertNull(objects.getProgramStatus());
        assertNotNull(objects.getHosts());
        assertNotNull(objects.getServices());
        assertEquals(0, objects.getHosts().size());
        assertEquals(0, objects.getServices().size());
    }
    
    @Test
    public void testAddComment() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.COMMENT);
        try {
            objects.add(object);
            fail("This should throw an UnsupportedOperationException!");
        } catch (UnsupportedOperationException ex) {
            // THIS IS SUPPOSED TO HAPPEN!
        }
    }    
    
    @Test
    public void testAddContact() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.CONTACT);
        try {
            objects.add(object);
            fail("This should throw an UnsupportedOperationException!");
        } catch (UnsupportedOperationException ex) {
            // THIS IS SUPPOSED TO HAPPEN!
        }
    }    

    @Test
    public void testAddInfo() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.INFO);
        objects.add(object);
        assertNotNull(objects.getInfo());
        assertEquals(object, objects.getInfo());
    }    

    @Test
    public void testAddProgram() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.PROGRAM);
        objects.add(object);
        assertNotNull(objects.getProgramStatus());
        assertEquals(object, objects.getProgramStatus());
    }
    
    @Test
    public void testAddHost() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.HOST);
        object.setProperty("host_name", TEST_OBJECT_HOST_NAME);
        String objectId = objects.getHashIdentifier(object);
                
        objects.add(object);
        assertEquals(1, objects.getHosts().size());
        
        assertNotNull(objects.getHost(TEST_OBJECT_HOST_NAME));
        assertEquals(TEST_OBJECT_HOST_NAME, objects.getHost(TEST_OBJECT_HOST_NAME).getProperty("host_name"));
        assertEquals(objectId, objects.getHost(TEST_OBJECT_HOST_NAME).getId());
    }

    @Test
    public void testAddService() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.SERVICE);
        object.setProperty("host_name", TEST_OBJECT_HOST_NAME);
        object.setProperty("service_description", TEST_OBJECT_SERVICE_DESCRIPTION);
        String objectId = objects.getHashIdentifier(object);
                
        objects.add(object);
        assertEquals(1, objects.getServices().size());
        
        assertNotNull(objects.getService(objectId));
        assertEquals(TEST_OBJECT_HOST_NAME, objects.getService(objectId).getProperty("host_name"));
        assertEquals(TEST_OBJECT_SERVICE_DESCRIPTION, objects.getService(objectId).getProperty("service_description"));
        assertEquals(objectId, objects.getService(objectId).getId());
    }
    
    @Test
    public void testGetHost() {
        StatusObjects objects = new StatusObjects();
        StatusObject object = new StatusObjectImpl(StatusObject.Type.HOST);
        object.setProperty("host_name", TEST_OBJECT_HOST_NAME);
        String objectId = objects.getHashIdentifier(object);
                
        objects.add(object);
        assertEquals(1, objects.getHosts().size());
        
        object = objects.getHost(objectId);
        assertNotNull(object);

        object = objects.getHost(TEST_OBJECT_HOST_NAME);
        assertNotNull(object);

        object = objects.getHost(TEST_INVALID_HOST_NAME);
        assertNull(object);
    }

    @Test
    public void testGetServicesByHost() {
        StatusObjects objects = new StatusObjects();
        List<StatusObject> serviceObjects = objects.getServicesByHost(TEST_OBJECT_HOST_NAME);
        assertNull(serviceObjects);
        
        StatusObject object = new StatusObjectImpl(StatusObject.Type.HOST);
        object.setProperty("host_name", TEST_OBJECT_HOST_NAME);
        objects.add(object);
        
        serviceObjects = objects.getServicesByHost(TEST_OBJECT_HOST_NAME);
        assertNotNull(serviceObjects);
        assertEquals(0, serviceObjects.size());
        
        object = new StatusObjectImpl(StatusObject.Type.SERVICE);
        object.setProperty("host_name", TEST_INVALID_HOST_NAME);
        object.setProperty("service_description", TEST_OBJECT_SERVICE_DESCRIPTION);
        objects.add(object);
        
        serviceObjects = objects.getServicesByHost(TEST_OBJECT_HOST_NAME);
        assertNotNull(serviceObjects);
        assertEquals(0, serviceObjects.size());
        
        object = new StatusObjectImpl(StatusObject.Type.SERVICE);
        object.setProperty("host_name", TEST_OBJECT_HOST_NAME);
        object.setProperty("service_description", TEST_OBJECT_SERVICE_DESCRIPTION);
        String objectId = objects.getHashIdentifier(object);
        objects.add(object);
        
        serviceObjects = objects.getServicesByHost(TEST_OBJECT_HOST_NAME);
        assertNotNull(serviceObjects);
        assertEquals(1, serviceObjects.size());
        
        object = serviceObjects.get(0);
        assertEquals(objectId, object.getId());
    }
    
    @Test
    public void testGetTypeByName() {
        StatusObject.Type type;
        
        type = StatusObjects.getTypeByName(TYPE_HOST_COMMENT);
        assertEquals(StatusObject.Type.COMMENT, type);

        type = StatusObjects.getTypeByName(TYPE_SERVICE_COMMENT);
        assertEquals(StatusObject.Type.COMMENT, type);

        type = StatusObjects.getTypeByName(TYPE_CONTACT);
        assertEquals(StatusObject.Type.CONTACT, type);

        type = StatusObjects.getTypeByName(TYPE_HOST);
        assertEquals(StatusObject.Type.HOST, type);

        type = StatusObjects.getTypeByName(TYPE_INFO);
        assertEquals(StatusObject.Type.INFO, type);
        
        type = StatusObjects.getTypeByName(TYPE_PROGRAM);
        assertEquals(StatusObject.Type.PROGRAM, type);
        
        type = StatusObjects.getTypeByName(TYPE_SERVICE);
        assertEquals(StatusObject.Type.SERVICE, type);
        
        type = StatusObjects.getTypeByName(TYPE_UNKNOWN);
        assertNull(type);
        
        type = StatusObjects.getTypeByName(null);
        assertNull(type);
    }

    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
