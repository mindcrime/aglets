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

    public void getLocalInfo(Message msg) {
	this.buffer.append("Username : " + this.getProperty("user.name"));
	this.buffer.append("\n");
	this.buffer.append("Home directory : " + this.getProperty("user.home"));
	this.buffer.append("\n");
	this.buffer.append("Currect working directory : "
		+ this.getProperty("user.dir"));
	this.buffer.append("\n");
	this.buffer.append("Machine architecture : "
		+ this.getProperty("os.arch"));
	this.buffer.append("\n");
	this.buffer.append("OS name : " + this.getProperty("os.name"));
	this.buffer.append("\n");
	this.buffer.append("OS version : " + this.getProperty("os.version"));
	this.buffer.append("\n");
	this.buffer.append("Java version : " + this.getProperty("java.version"));
	this.buffer.append("\n");
    }

    private String getProperty(String key) {
	return System.getProperty(key, "Unknown");
    }

    public void getProxies(Message msg) {
	Enumeration e = this.getAgletContext().getAgletProxies(ACTIVE);

	while (e.hasMoreElements()) {
	    this.proxies.addElement(e.nextElement());
	}
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("getLocalInfo")) {
	    this.getLocalInfo(msg);
	    return true;
	} else if (msg.sameKind("getProxies")) {
	    this.getProxies(msg);
	    return true;
	} else if (msg.sameKind("dialog")) {
	    CirculateFrame f = new CirculateFrame(this);

	    f.pack();
	    f.setVisible(true);
	    this.init();
	    return true;
	} else if (msg.sameKind("printResult")) {
	    System.out.println(this.buffer);
	    Enumeration e = this.proxies.elements();

	    while (e.hasMoreElements()) {
		AgletProxy p = (AgletProxy) e.nextElement();

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
	this.buffer = new StringBuffer();
	this.proxies = new Vector();
    }

    public void oncemore() {
	try {
	    this.itinerary.startTrip();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    public void onCreation(Object ini) {
	this.itinerary = new SeqPlanItinerary(this);
	this.itinerary.addPlan("atp://sirius.trl.ibm.com:2000/", "getLocalInfo");
	this.itinerary.addPlan("atp://vmoshima.trl.ibm.com/", "getProxies");
	this.itinerary.addPlan(this.getAgletContext().getHostingURL().toString(), "printResult");
    }

    public void start() {
	this.init();
	this.oncemore();
    }
}
