package org.aglets.log.log4j;

import org.aglets.log.LogInitializer;
import org.aglets.log.LogCategory;
import java.util.Properties;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Initializes the Log4J logging system.  Logging
 * system will be initialized staticly using the DOMConfigurater.
 * The configuration file agletslog.xml must be in "aglets.home".
 */
public class Log4jInitializer extends LogInitializer {
    
     static {
        // Set the inherited static member to an instance 
        // of this subclass.  Thus, this getCategoryImpl
        // will be invoked.
        m_instance = new Log4jInitializer();
        
        // Get system properties
		Properties system_props = System.getProperties();

		String FS = system_props.getProperty("file.separator");
		String PS = system_props.getProperty("path.separator");
		        
        // Setup XML configuration of log4J
        String logConfig = system_props.getProperty("aglets.home");
        if( !logConfig.endsWith(FS) ) {
            logConfig = logConfig + FS;
        }
        
        System.out.println("Log URL: "+logConfig+"cnf"+FS+"agletslog.xml");
        DOMConfigurator.configure(logConfig+"cnf"+FS+"agletslog.xml");
    }
    
    /** 
     * Return the Log4j Implmentation  of  the logging 
     * interface.
     * @param  name The name of the category.
     */
    protected LogCategory getCategoryImpl(String name) {
        return new Log4jCategory(name);
    }
}
