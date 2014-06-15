package net.collabsoft.nagios;

import com.google.common.io.Resources;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.jmock.MockObjectTestCase;
import org.junit.Test;
import org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( Resources.class )
public class TestAppServerIndex extends MockObjectTestCase {

    // ----------------------------------------------------------------------------------------------- Constructor

    @Override
    public void setUp() throws Exception {
        
    }
    
    @Override
    public void tearDown() {
        
    }

    @Test
    public void testConstructor() {
        AppServerIndex appServerIndex = new AppServerIndex();
        assertNotNull(appServerIndex);
    }
    
    @Test
    public void testIndex() {
        AppServerIndex appServerIndex = new AppServerIndex();
        Response response = appServerIndex.index();
        assertEquals(200, response.getStatus());

        PowerMockito.mockStatic(Resources.class);
        PowerMockito.when(Resources.getResource("index.html")).thenThrow(IOException.class);
        response = appServerIndex.index();
        assertEquals(500, response.getStatus());
    }
    
    // ----------------------------------------------------------------------------------------------- Getters & Setters


    // ----------------------------------------------------------------------------------------------- Public methods


    // ----------------------------------------------------------------------------------------------- Private methods


    // ----------------------------------------------------------------------------------------------- Private Getters & Setters

}
