package examples.itinerary;

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

import com.ibm.aglet.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.SeqPlanItinerary;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;
/**
 * <tt> CirculateAglet </tt> illustrates how to use SeqPlanItinerary.
 * 
 * @version     1.00	$Date: 2001/07/28 06:34:17 $
 * @author	Mitsuru Oshima
 */
public class CirculateAglet extends Aglet {
	StringBuffer buffer;
	SeqPlanItinerary itinerary;
	Vector proxies;

	public void getLocalInfo(Message msg) {
		buffer.append("Username : " + getProperty("user.name"));
		buffer.append("\n");
		buffer.append("Home directory : " + getProperty("user.home"));
		buffer.append("\n");
		buffer.append("Currect working directory : " 
					  + getProperty("user.dir"));
		buffer.append("\n");
		buffer.append("Machine architecture : " + getProperty("os.arch"));
		buffer.append("\n");
		buffer.append("OS name : " + getProperty("os.name"));
		buffer.append("\n");
		buffer.append("OS version : " + getProperty("os.version"));
		buffer.append("\n");
		buffer.append("Java version : " + getProperty("java.version"));
		buffer.append("\n");
	}
	private String getProperty(String key) {
		return System.getProperty(key, "Unknown");
	}
	public void getProxies(Message msg) {
		Enumeration e = getAgletContext().getAgletProxies(ACTIVE);

		while (e.hasMoreElements()) {
			proxies.addElement(e.nextElement());
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("getLocalInfo")) {
			getLocalInfo(msg);
			return true;
		} else if (msg.sameKind("getProxies")) {
			getProxies(msg);
			return true;
		} else if (msg.sameKind("dialog")) {
			CirculateFrame f = new CirculateFrame(this);

			f.pack();
			f.setVisible(true);
			init();
			return true;
		} else if (msg.sameKind("printResult")) {
			System.out.println(buffer);
			Enumeration e = proxies.elements();

			while (e.hasMoreElements()) {
				AgletProxy p = (AgletProxy)e.nextElement();

				try {
					System.out.println(p.getAgletInfo());
				} catch (InvalidAgletException ex) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public void onCreation(Object ini) {
		itinerary = new SeqPlanItinerary(this);
		itinerary.addPlan("atp://sirius.trl.ibm.com:2000/", "getLocalInfo");
		itinerary.addPlan("atp://vmoshima.trl.ibm.com/", "getProxies");
		itinerary.addPlan(getAgletContext().getHostingURL().toString(), 
						  "printResult");
	}
	public void start() {
		init();
		oncemore();
	}
}
