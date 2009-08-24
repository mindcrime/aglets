/*
 * Created on Oct 11, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.log;

import java.io.*;

import org.apache.log4j.*;
import org.apache.log4j.xml.*;

/**
 * A factory to get the logger without concerning with details such as the configuration file, etc.
 * This class is not a real factory, rather a static class.
 */
public class LoggerFactory {

    /**
     * The logger configuration file, by default the log4j.conf.xml file in the cnf directory.
     */
    protected static  String loggerConfigFile = System.getProperty("aglets.home")+"/cnf/log4j.conf.xml";
    
    /**
     * Indicates whever or not the logging has been already configured.
     */
    protected static boolean configured = false;
    
    
    /**
     * Get a logger for the log file.
     * @param clazz the class to associate with the logger
     * @return the logger or null if the class is null or is Object (do you really want to log from Object to
     * everything?)
     */
    public synchronized static Logger getLogger(Class clazz){
        // check parameters
        if(clazz==null || clazz.equals(Object.class)){
            return null;
        }
        
        // create a new logger
        Logger logger = Logger.getLogger(clazz);
        
        // configure the logger (if needed)
        if( ! configured )
        	configure();
        
        return logger;
        
    }
    
    
    /**
     * A method to perform the configuration of the logging facility. This method tries to configure the logging
     * with the specified XML file, otherwise the configuration is extracted from the environment properties.
     *
     */
    protected static void configure(){
	try{
	    // check if the file exists
	    File configFile = new File(loggerConfigFile);
	    if( configFile.exists() ){
		// perform the configuration depending on this file
		// configure the logger
		DOMConfigurator.configure(loggerConfigFile);
		configured = true;
		return;
	    }
	}
	finally{
	    // if here an exception has been raised, or the file does not exists, try with a default
	    // configuration
	    System.err.print("\tExploiting the property configuration for the loggin facility...");
	    // try to configure the logging system from the system properties
	    PropertyConfigurator.configure(System.getProperties());    		
	    System.err.println("\tdone!");
	    configured = true;
	}

    }
    
    public synchronized static Logger getLogger(String categoryName){
        if(categoryName == null || categoryName.equals("") || categoryName.equals("java.lang.Object")){
            return null;
        }
        
        // create a new logger
        Logger logger = Logger.getLogger(categoryName);
        
        // configure the logger (if needed)
        if( ! configured )
        	configure();

        return logger;
        
    }
    
    /**
     * Get the logger configuration file
     * @return the file used as configuration file
     */
    public static String getConfigurationFile(){
        return loggerConfigFile;
    }
 
}
