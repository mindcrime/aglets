package org.aglets.log;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class represents a wrapper around the Log4J library for logging. Each
 * class in the Aglets library should now use this wrapper to log messages. This
 * class statically loads the configuration of the Log4J library to set up the
 * logging facility.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 */
public class AgletsLogger implements Cloneable {

    /**
     * The logger to use.
     */
    private Logger logger = null;

    /**
     * A map to map each class with a specific logger, thus in the future
     * different instances of the same class can get the same logger.
     */
    private static Map<String, AgletsLogger> loggerMap = new HashMap<String, AgletsLogger>();

    // configure the loggin system
    static {
	String agletsHome = System.getProperty("aglets.home");
	String pathSeparator = "/";

	// check if the aglets home has already a path separator
	if ((agletsHome != null) && (!(agletsHome.endsWith(pathSeparator))))
	    agletsHome += pathSeparator;
	else if (agletsHome == null)
	    agletsHome = "." + pathSeparator;

	// append the config directory and the name of the configuration file
	agletsHome += "cnf" + pathSeparator + "log4j.xml";

	// configure the loggin system
	DOMConfigurator.configure(agletsHome);

    }

    /**
     * Provides a logger for the specified resource. Please note that this
     * method uses a map to store the logger providden associating it with the
     * name specified. This means that in further calls, the logger returned
     * will be the same logger instance.
     * 
     * @param name
     *            the name of the logger
     * @return the logger to use
     */
    public static AgletsLogger getLogger(String name) {
	AgletsLogger foundLogger = null;

	foundLogger = loggerMap.get(name);

	if (foundLogger == null) {
	    foundLogger = new AgletsLogger(name);
	    loggerMap.put(name, foundLogger);
	}

	return foundLogger;
    }

    /**
     * Creates a new logger with the specified name.
     * 
     * @param name
     *            the name for the logger.
     */
    private AgletsLogger(String name) {
	this.logger = Logger.getLogger(name);
    }

    /**
     * Logs a message at Fatal priority.
     */
    public void fatal(Object msg) {
	this.logger.fatal(msg);
    }

    /**
     * Logs a message at error priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void error(Object msg) {
	this.logger.error(msg);
    }

    /**
     * Logs a message at error priority and passes an exception for logging.
     * 
     * @param msg
     *            Message to be logged.
     * @param exc
     *            Description of Parameter
     * @since 1.0
     */
    public void error(Object msg, Exception exc) {
	this.logger.error(msg, exc);
    }

    /**
     * Logs a message at warn priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void warn(Object msg) {
	this.logger.warn(msg);
    }

    /**
     * Logs a message at debug priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void debug(Object msg) {
	this.logger.debug(msg);
    }

    /**
     * Log a message at the info priority.
     * 
     * @param msg
     *            the message to log
     */
    public void info(Object msg) {
	this.logger.info(msg);
    }

    /**
     * Check whether this category is enabled for the <code>DEBUG</code>
     * priority.
     * 
     * <p>
     * This function is intended to lessen the computational cost of disabled
     * log debug statements.
     * 
     * <p>
     * For some <code>cat</code> Category object, when you write,
     * 
     * <pre>
     * cat.debug(&quot;This is entry number: &quot; + i);
     * </pre>
     * 
     * <p>
     * You incur the cost constructing the message, concatenatiion in this case,
     * regardless of whether the message is logged or not.
     * 
     * <p>
     * If you are worried about speed, then you should write
     * 
     * <pre>
     * if (cat.isDebugEnabled()) {
     *     cat.debug(&quot;This is entry number: &quot; + i);
     * }
     * </pre>
     * 
     * <p>
     * This way you will not incur the cost of parameter construction if
     * debugging is disabled for <code>cat</code>. On the other hand, if the
     * <code>cat</code> is debug enabled, you will incur the cost of evaluating
     * whether the category is debug enabled twice. Once in
     * <code>isDebugEnabled</code> and once in the <code>debug</code>. This is
     * an insignificant overhead since evaluating a category takes about 1%% of
     * the time it takes to actually log.
     * 
     * @return boolean - <code>true</code> if this category is debug enabled,
     *         <code>false</code> otherwise.
     * 
     */
    public boolean isDebugEnabled() {
	return this.logger.isDebugEnabled();
    }

    /**
     * Logs a translating action, this works only with the debug level
     * activated.
     * 
     * @param src
     *            the original string
     * @param dest
     *            the translated string
     */
    public void translation(String src, String dest) {
	if ((src != null) || (dest != null))
	    this.logger.debug("Translated/changed the string <<" + src
		    + ">> into <<" + dest + ">>");
    }

    /**
     * Sets the debug level for this logger.
     * 
     */
    public void setDebugLevel() {
	this.logger.setLevel(Level.DEBUG);
    }

    /**
     * Sets the info level for this logger.
     * 
     */
    public void setInfoLevel() {
	this.logger.setLevel(Level.INFO);
    }

    /**
     * Sets the level of this logger to error.
     * 
     */
    public void setErrorLevel() {
	this.logger.setLevel(Level.ERROR);
    }

    /**
     * Sets the level of the logger to warning.
     * 
     */
    public void setWarningLevel() {
	this.logger.setLevel(Level.WARN);
    }

    /**
     * Sets the level of the logger to trace.
     * 
     */
    public void setTraceLevel() {
	this.logger.setLevel(Level.TRACE);
    }

    /**
     * Is this logger working for debug level?
     * 
     * @return true if the debug level on this logger is active
     */
    public boolean isDebugLevel() {
	return this.logger.isDebugEnabled();
    }

    /**
     * Is this worker working as trace?
     * 
     * @return true if the logger is working for trace
     */
    public boolean isTraceLevel() {
	return this.logger.isTraceEnabled();
    }

    /**
     * Is this logger working as info?
     * 
     * @return true if the logger is working at the info level.
     */
    public boolean isInfoLevel() {
	return this.logger.isInfoEnabled();
    }
}
