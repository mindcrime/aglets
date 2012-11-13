package net.sourceforge.aglets.examples.itinerary;

/*
 * @(#)CirculateAglet.java
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

import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.util.SeqPlanItinerary;

/**
 * <tt> CirculateAglet </tt> illustrates how to use SeqPlanItinerary.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class CirculateAglet extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5121407791713217163L;
	StringBuffer buffer;
	SeqPlanItinerary itinerary;
	Vector proxies;

	public void getLocalInfo(final Message msg) {
		buffer.append("Username : " + getProperty("user.name"));
		buffer.append("\n");
		buffer.append("Home directory : " + getProperty("user.home"));
		buffer.append("\n");
		buffer.append("Currect working directory : "
				+ getProperty("user.dir"));
		buffer.append("\n");
		buffer.append("Machine architecture : "
				+ getProperty("os.arch"));
		buffer.append("\n");
		buffer.append("OS name : " + getProperty("os.name"));
		buffer.append("\n");
		buffer.append("OS version : " + getProperty("os.version"));
		buffer.append("\n");
		buffer.append("Java version : " + getProperty("java.version"));
		buffer.append("\n");
	}

	private String getProperty(final String key) {
		return System.getProperty(key, "Unknown");
	}

	public void getProxies(final Message msg) {
		final Enumeration e = getAgletContext().getAgletProxies(ACTIVE);

		while (e.hasMoreElements()) {
			proxies.addElement(e.nextElement());
		}
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("getLocalInfo")) {
			getLocalInfo(msg);
			return true;
		} else if (msg.sameKind("getProxies")) {
			getProxies(msg);
			return true;
		} else if (msg.sameKind("dialog")) {
			final CirculateFrame f = new CirculateFrame(this);

			f.pack();
			f.setVisible(true);
			init();
			return true;
		} else if (msg.sameKind("printResult")) {
			System.out.println(buffer);
			final Enumeration e = proxies.elements();

			while (e.hasMoreElements()) {
				final AgletProxy p = (AgletProxy) e.nextElement();

				try {
					System.out.println(p.getAgletInfo());
				} catch (final InvalidAgletException ex) {
					System.out.println("InvalidAglet");
				}
			}
			return true;
		}
		return false;
	}

	private void init() {
		buffer = new StringBuffer();
		proxies = new Vector();
	}

	public void oncemore() {
		try {
			itinerary.startTrip();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onCreation(final Object ini) {
		itinerary = new SeqPlanItinerary(this);
		itinerary.addPlan("atp://sirius.trl.ibm.com:2000/", "getLocalInfo");
		itinerary.addPlan("atp://vmoshima.trl.ibm.com/", "getProxies");
		itinerary.addPlan(getAgletContext().getHostingURL().toString(), "printResult");
	}

	public void start() {
		init();
		oncemore();
	}
}
