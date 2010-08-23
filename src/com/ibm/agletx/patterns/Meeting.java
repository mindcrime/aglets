package com.ibm.agletx.patterns;

/*
 * @(#)Meeting.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.ReplySet;

/**
 * This <tt>Meeting</tt> class abstracts light synchronization between multiple
 * aglets by using a meeting concept. A meeting is established by creating a
 * meeting object and distribute it among the aglets which suppose to
 * participate in the meeting. A meeting object includes: <br>
 * 1. the meeting place (URL) <br>
 * 2. a meeting identifier <br>
 * 3. an optional list of agletIDs, named <tt>colleagues</tt>, of aglets which
 * are expected to be presented at the meeting place <br>
 * 4. an optional object (named <tt>attachedInfo</tt>) describing additional
 * information regarding the meeting (e.g. list or number of participants,
 * minimum number of required participants). <br>
 * <br>
 * Upon arrival to the meeting, an aglet notifies its arrival via the
 * <tt>ready</tt> method. As a result all aglets already presented are notified
 * of its arrival by receiving a special message (its kind is that of the
 * meeting ID) with the agletID of this newly arrived aglet.
 * 
 * @version 1.0 96/12/28
 * @author Yariv Aridor
 */

public final class Meeting implements java.io.Serializable {

    private String _id = null;
    private String _place = null;
    private Vector _colleagues = null;
    private Object _info = null;

    /**
     * A Constructor
     * 
     * @param place
     *            the meeting place.
     */
    public Meeting(String place) {
	this._place = place;
	this._id = "meet." + String.valueOf(Math.random());
    }

    /**
     * A Constructor
     * 
     * @param place
     *            the meeting place.
     * @param colleagues
     *            the colleagues
     * @exception AgletException
     *                if colleagues is invalid (should include only AgletID
     *                objects)
     */

    public Meeting(String place, Vector colleagues) throws AgletException {
	this._place = place;
	this._id = "meet." + String.valueOf(Math.random());
	this.setColleagues(colleagues);
    }

    private Vector append(Vector v1, Vector v2) {
	Vector v = new Vector();

	if (v1 == null) {
	    return v2;
	}
	if (v2 == null) {
	    return v1;

	}
	for (int i = 0; i < v1.size(); i++) {
	    v.addElement(v1.elementAt(i));
	}
	for (int j = 0; j < v2.size(); j++) {
	    v.addElement(v2.elementAt(j));
	}
	return v;
    }

    private void checkColleagues(Vector c) throws AgletException {
	for (int i = 0; i < c.size(); i++) {
	    if ((c.elementAt(i) instanceof AgletID) == false) {
		throw new AgletException("non AgletIdentifer object is included");
	    }
	}
    }

    private void checkColleaguesArePresent(AgletContext ctx, Vector c)
    throws AgletException {
	boolean found = false;

	if (c == null) {
	    return;
	}
	for (int i = 0; i < c.size(); i++) {
	    found = false;
	    for (Enumeration e = ctx.getAgletProxies(); e.hasMoreElements();) {
		AgletProxy p = (AgletProxy) e.nextElement();

		if (p.getAgletID().equals(c.elementAt(i))) {
		    found = true;
		    break;
		}
	    }
	    if (found == false) {
		throw new AgletException("not all colleagues are presented");
	    }
	}
    }

    public Object getAttachedInfo() {
	return this._info;
    }

    // temporaty function to overcome adding a trial "/" while
    // converting from a string to a URL.
    private URL getHostingURL(AgletContext ctx) {
	try {
	    return new URL(ctx.getHostingURL().toString());
	} catch (IOException e) {
	    return null;
	}
    }

    public String getID() {
	return this._id;
    }

    public String getPlace() {
	return this._place;
    }

    /**
     * Accepts an aglet to a meeting.
     * 
     * @param aglet
     *            the arrived aglet.
     * @return enumeration of the agletID of all presented aglets.
     * @exception AgletException
     *                if wrong meeting place or if any of the colleagues is not
     *                presented.
     */
    public Enumeration ready(Aglet ag) throws AgletException {

	// get aglet identifier
	AgletID aid = ag.getAgletID();
	AgletContext ctx = ag.getAgletContext();

	URL url = null;

	try {
	    url = new URL(this._place);
	} catch (Exception ex) {
	}

	if (!NetUtils.sameURL(url, this.getHostingURL(ctx))) {
	    throw new AgletException("a wrong meeting place : " + this._place);
	} else {
	    this.checkColleaguesArePresent(ctx, this._colleagues);
	    return this.append(ready0(aid, ctx, this._id, ag), this._colleagues).elements();
	}
    }

    private synchronized static Vector ready0(
                                              AgletID aid,
                                              AgletContext ctx,
                                              String id,
                                              Aglet ag) throws AgletException {

	ReplySet rs = ctx.multicastMessage(new Message(id, aid));

	ag.subscribeMessage(id);
	return toVector(rs);
    }

    public void setAttachedInfo(Object obj) {
	this._info = obj;
    }

    public void setColleagues(Vector colleagues) throws AgletException {
	this.checkColleagues(colleagues);
	this._colleagues = colleagues;
    }

    public void setID(String id) {
	this._id = "meet." + id;
    }

    private static Vector toVector(ReplySet rs) throws AgletException {
	Vector v = new Vector();

	try {
	    while (rs.hasMoreFutureReplies()) {
		Object obj = rs.getNextFutureReply().getReply();

		if (obj instanceof AgletID) {
		    v.addElement(obj);
		} else {
		    throw new AgletException("a non AgletIdentifer object");
		}
	    }
	} catch (Exception e) {
	}
	return v;
    }
}
