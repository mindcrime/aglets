package org.aglets.log.quiet;

import org.aglets.log.LogCategory;

/**
 * Logging category that does absolutely nothing. This will probably be the
 * default setup.
 * 
 * @version $Revision: 1.3 $ $Date: 2009/07/27 10:31:42 $ $Author: cat4hire $
 * @since
 * @author Robert Bergstrom
 * @created July 16, 2001
 */
public class QuietCategory implements LogCategory {

    /**
     * Check whether this category is enabled for the <code>DEBUG</code>
     * priority.
     * <p>
     * This version always returns true.
     */
    public boolean isDebugEnabled() {
	return true;
    }

    /**
     * Logs a message at fatal priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void fatal(Object msg) {

    }

    /**
     * Logs a message at error priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void error(Object msg) {

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

    }

    /**
     * Logs a message at warn priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void warn(Object msg) {

    }

    /**
     * Logs a mesasge at info priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void info(Object msg) {

    }

    /**
     * Logs a message at debug priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void debug(Object msg) {

    }

    /**
     * Constructor
     * 
     * @param name
     *            Name of category used as a prefix to the log messages.
     */
    public QuietCategory(String name) {

    }
}
