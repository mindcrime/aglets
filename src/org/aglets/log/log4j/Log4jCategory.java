package org.aglets.log.log4j;

import org.aglets.log.LogCategory;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * Wrapper class for log4j implementation of logging category.
 */
public class Log4jCategory implements LogCategory {

    // Delegate reference.
    private Category m_category = null;

    private final String fullClassName = "org.aglets.log.log4j.Log4jCategory";

    public Log4jCategory(String name) {
	this.m_category = Category.getInstance(name);
    }

    /**
     * Logs a message at Fatal priority.
     */
    public void fatal(Object msg) {
	this.m_category.log(this.fullClassName, Priority.FATAL, msg, null);
    }

    /**
     * Logs a message at error priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void error(Object msg) {
	this.m_category.log(this.fullClassName, Priority.ERROR, msg, null);
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
	this.m_category.log(this.fullClassName, Priority.ERROR, msg, exc);
    }

    /**
     * Logs a message at warn priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void warn(Object msg) {
	this.m_category.log(this.fullClassName, Priority.WARN, msg, null);
    }

    /**
     * Logs a mesasge at info priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void info(Object msg) {
	this.m_category.log(this.fullClassName, Priority.INFO, msg, null);
    }

    /**
     * Logs a message at debug priority.
     * 
     * @param msg
     *            Message to be logged.
     * @since 1.0
     */
    public void debug(Object msg) {
	this.m_category.log(this.fullClassName, Priority.DEBUG, msg, null);
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
	return this.m_category.isDebugEnabled();
    }
}
