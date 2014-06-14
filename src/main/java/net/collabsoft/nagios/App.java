package net.collabsoft.nagios;

import java.io.IOException;
import static java.lang.System.exit;
import org.apache.commons.cli.ParseException;

public class App 
{
    
    public static void main( String[] args ) throws IOException
    {
        AppConfig config = AppConfig.getInstance();
        try {
           config = AppConfig.fromArgs(args);
        } catch (ParseException ex) {
            System.out.println("One or more arguments are invalid: " + ex.getMessage());
            AppConfig.showOptions();
            exit(-1);
        }

        // Creates a server and listens on the address below.
        // Scans classpath for JAX-RS resources
        AppServer appServer = new AppServer(config.getHostname(), config.getPort());
        appServer.start();

        System.out.println("Press any key to stop the service...");
        System.in.read();

        appServer.stop();
    }
    
}
