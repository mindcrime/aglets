package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)FingerSlave.java
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

import java.util.Date;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.agletx.patterns.Slave;

/**
 * FingerSlave is a aglet of Slave pattern that moves to a remote site and backs
 * to the original site with information.
 * 
 * @see Slave
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yariv Aridor
 * 
 */

public class FingerSlave extends Slave {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4290392056404272921L;

	@Override
	protected void doJob() throws AgletException {
		RESULT = getLocalInfo();
	}

	private Object getLocalInfo() throws AgletException {
		final AgletContext ac = getAgletContext();
		String hostname;

		if (ac.getHostingURL() == null) {
			hostname = "Unknown";
		} else {
			hostname = ac.getHostingURL().getHost().toString();
		}

		final FingerInfo info = new FingerInfo(hostname,

				// PropertyPermission for the following properties
				// should be specified in aglets.policy file.
				getProperty("user.name"), getProperty("user.home"), getProperty("user.dir"), getProperty("os.arch"), getProperty("os.name"), getProperty("os.version"), getProperty("java.version"), (new Date()));

		return info;
	}

	private String getProperty(final String key) {
		return System.getProperty(key, "Unknown");
	}

	@Override
	protected void initializeJob() {
		RESULT = null;
	}
}
