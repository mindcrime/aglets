package net.sourceforge.aglets.examples.logger;

import net.sourceforge.aglets.log.AgletsLogger;

import com.ibm.aglet.Aglet;

public class LoggingAgent extends Aglet {

    @Override
    public void run() {
	System.out.println("Hi, I'm a loggin aglet...");
	System.out.println("Let me get a logger ");
	AgletsLogger myLogger = this.getLogger(true);

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
