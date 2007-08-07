package org.aglets.log;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class represents a wrapper around the Log4J library for logging. Each class in the Aglets library should now
 * use this wrapper to log messages. This class statically loads the configuration of the Log4J library
 * to set up the logging facility.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 */
public class AgletsLogger {
 
	    /**
	     * The logger to use.
	     */
	    private Logger logger = null;
	    
	    // configure the loggin system
	    static{
		String agletsHome = System.getProperty("aglets.home");
		String pathSeparator = "/";
		
		// check if the aglets home has already a path separator
		if( agletsHome != null && (! (agletsHome.endsWith(pathSeparator)) ) )
		    agletsHome += pathSeparator;
		else 
		if( agletsHome == null )
		    agletsHome = "."  + pathSeparator;
		
		// append the config directory and the name of the configuration file
		agletsHome += "cnf" + pathSeparator + "log4j.xml";
		
		// configure the loggin system
		DOMConfigurator.configure(agletsHome);
	    }
	    
	    
	    /**
	     * Provides a logger for the specified resource.
	     * @param name the name of the logger
	     * @return the logger to use
	     */
	    public static Logger getLogger(String name){
		return Logger.getLogger(name);
	    }
	    
	    
	    
	    /**
	     * Creates a new logger with the specified name.
	     * @param name the name for the logger.
	     */
	    public AgletsLogger(String name) {
	        logger = Logger.getLogger(name);
	    }
	    
	    /**
	     * Logs a message at Fatal priority.
	     */
	    public void fatal(Object msg) {
	        logger.fatal(msg);
	    }
	    
	     /**
	     *  Logs a message at error priority.
	     *
	     * @param  msg  Message to be logged.
	     * @since       1.0
	     */
	    public void error(Object msg) {
	        logger.error(msg);
	    }

	    
	    

	    /**
	     *  Logs a message at error priority and passes an exception for logging.
	     *
	     * @param  msg  Message to be logged.
	     * @param  exc  Description of Parameter
	     * @since       1.0
	     */
	    public void error(Object msg, Exception exc) {
	        logger.error(msg, exc);
	    }


	    /**
	     *  Logs a message at warn priority.
	     *
	     * @param  msg  Message to be logged.
	     * @since       1.0
	     */
	    public void warn(Object msg) {
		logger.warn(msg);
	    }




	    /**
	     *  Logs a message at debug priority.
	     *
	     * @param  msg  Message to be logged.
	     * @since       1.0
	     */
	    public void debug(Object msg) {
		logger.debug(msg);
	    }
	    
	    /**
	     * Log a message at the info priority.
	     * @param msg the message to log
	     */
	    public void info(Object msg){
		logger.info(msg);
	    }
	    
	    /**
	    *  Check whether this category is enabled for the <code>DEBUG</code>
	    *  priority.
	    *  
	    *  <p> This function is intended to lessen the computational cost of
	    *  disabled log debug statements.
	    * 
	    *  <p> For some <code>cat</code> Category object, when you write,
	    *  <pre>
	    *      cat.debug("This is entry number: " + i );
	    *  </pre>
	    *  
	    *  <p>You incur the cost constructing the message, concatenation in
	    *  this case, regardless of whether the message is logged or not.
	    * 
	    *  <p>If you are worried about speed, then you should write
	    *  <pre>
	    * 	 if(cat.isDebugEnabled()) { 
	    * 	   cat.debug("This is entry number: " + i );
	    * 	 }
	    *  </pre>
	    * 
	    *  <p>This way you will not incur the cost of parameter
	    *  construction if debugging is disabled for <code>cat</code>. On
	    *  the other hand, if the <code>cat</code> is debug enabled, you
	    *  will incur the cost of evaluating whether the category is debug
	    *  enabled twice. Once in <code>isDebugEnabled</code> and once in
	    *  the <code>debug</code>.  This is an insignificant overhead
	    *  since evaluating a category takes about 1%% of the time it
	    *  takes to actually log.
	    * 
	    *  @return boolean - <code>true</code> if this category is debug
	    *  enabled, <code>false</code> otherwise.
	    *   
	    */
	    public boolean isDebugEnabled() {
	        return logger.isDebugEnabled();
	    }
}


