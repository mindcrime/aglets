package org.aglets.log.console;

import org.aglets.log.LogCategory;

/**
 * Logging object that writes all messages to stdout.
 *
 * @version    $Revision: 1.1 $ $Date: 2001/07/28 06:34:40 $ $Author: kbd4hire $
 * @since
 * @author     Robert Bergstrom
 * @created    July 16, 2001
 */
public class ConsoleCategory implements LogCategory {
    
    private String m_name = null;

    /**
     *  Check whether this category is enabled for the <code>DEBUG</code>
     *  priority. <p>
     *
     *  This function is intended to lessen the computational cost of disabled
     *  log debug statements. <p>
     *
     *  For some <code>cat</code> Category object, when you write, <pre>
     *
     *  cat.debug("This is entry number: " + i );</pre> <p>
     *
     *  You incur the cost constructing the message, concatenatiion in this
     *  case, regardless of whether the message is logged or not. <p>
     *
     *  If you are worried about speed, then you should write <pre>
     *
     *  if(cat.isDebugEnabled()) { cat.debug("This is entry number: " + i ); }
     *  </pre><p>
     *
     *  This way you will not incur the cost of parameter construction if
     *  debugging is disabled for <code>cat</code> . On the other hand, if the
     *  <code>cat</code> is debug enabled, you will incur the cost of evaluating
     *  whether the category is debug enabled twice. Once in <code>
     *  isDebugEnabled</code> and once in the <code>debug</code> . This is an
     *  insignificant overhead since evaluating a category takes about 1%% of
     *  the time it takes to actually log.
     *
     * @return    boolean - <code>true</code> if this category is debug enabled,
     *      <code>false</code> otherwise.
     * @since
     */
    public boolean isDebugEnabled() {
        return true;
    }


    /**
     *  Logs a message at fatal priority.
     *
     * @param  msg  Message to be logged.
     * @since       1.0
     */
    public void fatal(Object msg) {
        System.out.println(msg.toString());
    }


    /**
     *  Logs a message at error priority.
     *
     * @param  msg  Message to be logged.
     * @since       1.0
     */
    public void error(Object msg) {
        System.out.println(msg.toString());
    }


    /**
     *  Logs a message at error priority and passes an exception for logging.
     *
     * @param  msg  Message to be logged.
     * @param  exc  Description of Parameter
     * @since       1.0
     */
    public void error(Object msg, Exception exc) {
        System.out.println(msg.toString() + "\n");
        exc.printStackTrace();
    }


    /**
     *  Logs a message at warn priority.
     *
     * @param  msg  Message to be logged.
     * @since       1.0
     */
    public void warn(Object msg) {
        System.out.println(msg.toString());
    }


    /**
     *  Logs a mesasge at info priority.
     *
     * @param  msg  Message to be logged.
     * @since       1.0
     */
    public void info(Object msg) {
        System.out.println(msg.toString());
    }


    /**
     *  Logs a message at debug priority.
     *
     * @param  msg  Message to be logged.
     * @since       1.0
     */
    public void debug(Object msg) {
        System.out.println(msg.toString());
    }

    /**
     * Constructor
     * @param name Name of category used as a prefix to the log messages.
     */
    public ConsoleCategory( String name ) {
        m_name = name;
    }
}

