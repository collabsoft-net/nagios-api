
package net.collabsoft.nagios.parser;

import net.collabsoft.nagios.objects.StatusObjects;

public interface NagiosParser {
    
    public static final String CACHEKEY = "NagiosStatus";
    
    public boolean isValid();
    public StatusObjects parse();
    
}
