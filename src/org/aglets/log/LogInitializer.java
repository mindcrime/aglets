package org.aglets.log;

/**
 * Abstract class for logging system initializers. Used to obtain the proper
 * LogCategory implementation. Implementers of this class will also intialize
 * the logging system backend statically. An Aglets server should load the
 * desired logging implementation dynamically in the first few lines of it's
 * main routine.
 * 
 * @author Robert Bergstrom
 * @created July 16, 2001
 * @version 1.0
 */
public abstract class LogInitializer {

    /**
     * Singleton. Subclasses initialize to proper class.
     * 
     * @since
     */
    protected static LogInitializer m_instance = null;

    /**
     * Gets the CategoryImpl attribute of the LogInitializer object
     * 
     * @param name
     *            Description of Parameter
     * @return The CategoryImpl value
     * @since
     */
    protected LogCategory getCategoryImpl(String name) {
	throw new RuntimeException("Not Implemented!");
    }

    /**
     * Create a category in the logging hierachry.
     * 
     * @param categoryName
     *            Description of Parameter
     * @return The LogCategory implementation.
     * @since 1.0
     * @returns LogCategory implementation.
     */
    static public LogCategory getCategory(String categoryName) {
	return m_instance.getCategoryImpl(categoryName);
    }
}
