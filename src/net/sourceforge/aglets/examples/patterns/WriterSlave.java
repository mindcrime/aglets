package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)WriterSlave.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.net.URL;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Arguments;
import com.ibm.agletx.patterns.Slave;

/**
 * WriterSlave is a aglet of Slave pattern that moves to a remote site and
 * displays a message
 * 
 * @see Slave
 * @version 1.00 96/12/28
 * @author Yariv Aridor
 * 
 */

public class WriterSlave extends Slave {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8630541103850357287L;
	// time (in secs) of showing the message window.
	private final int SHOW_TIME = 10;

	@Override
	protected void doJob() throws AgletException {
		WriterSlaveWindow win = null;

		try {
			final Arguments args = (Arguments) ARGUMENT;
			final String from = new String((String) (args.getArg("user")) + "@"
					+ new URL(getOrigin()).getHost());

			win = new WriterSlaveWindow(this, (String) (args.getArg("msg")), from);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new AgletException("unable to create a remote message window");
		}
		this.suspend();
		win.dispose();
		setResult("returned"
				+ ((RESULT != null) ? ":" + (String) RESULT : "."));
	}

	@Override
	protected void initializeJob() {
		RESULT = null;
	}

	void setResult(final String text) {
		RESULT = text;
	}

	public synchronized void suspend() {
		try {
			this.wait(SHOW_TIME * 1000);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void wakeup() {
		notify();
	}
}
