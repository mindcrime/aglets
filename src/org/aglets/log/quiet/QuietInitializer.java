package org.aglets.log.quiet;

import org.aglets.log.LogCategory;
import org.aglets.log.LogInitializer;

/**
 * Initializes the logging system to simply print to the console.
 */
public class QuietInitializer extends LogInitializer {
    static {
	m_instance = new QuietInitializer();
    }

    /**
     * Return the Log4j Implmentation of the logging interface.
     * 
     * @param name
     *            The name of the category.
     */
    @Override
    protected LogCategory getCategoryImpl(String name) {
	return new QuietCategory(name);
    }
}
