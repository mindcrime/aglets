package net.sourceforge.aglets.examples.logger;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;

public class LoggingAgent extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5751775040853226180L;

	@Override
	public void run() {
		System.out.println("Hi, I'm a loggin aglet...");
		System.out.println("Let me get a logger ");
		final AgletsLogger myLogger = getLogger(true);

		System.out.println("Ok, my logger is (reinitiliazed): " + myLogger);
		if (myLogger != null) {
			myLogger.info("Here's an info message");
			myLogger.warn("Here's a warn message");
			myLogger.error("Here's an error message");
			myLogger.debug("Here's a debug message");
			System.out.println("I've put some messages in the log, please check it!");
		}
	}
}
