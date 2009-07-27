package org.aglets.log.console;

import org.aglets.log.LogInitializer;
import org.aglets.log.LogCategory;


/**
 * Initializes the logging system to simply print to the console.
 */
public class ConsoleInitializer extends LogInitializer {
    static {
        m_instance = new ConsoleInitializer();
    }
    /** 
     * Return the Log4j Implmentation  of  the logging 
     * interface.
     * @param  name The name of the category.
     */
    protected LogCategory getCategoryImpl(String name) {
        return new ConsoleCategory(name);
    }
}
