package com.ibm.aglets.tahiti;

/*
 * @(#)Util.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.awb.misc.Resource;

class Util {
    static Font titleFont = null;
    static Font proportionalFont = null;
    static Font fixedFont = null;
    static Color background = null;

    private static Vector titles = new Vector();
    private static Vector proportionals = new Vector();
    private static Vector fixeds = new Vector();
    private static Vector backgrounds = new Vector();

    synchronized public static Color getBackground() {
	if (background == null) {
	    Resource res = Resource.getResourceFor("tahiti");

	    background = res.getColor("tahiti.background", null);
	}
	return background;
    }

    synchronized public static Font getFixedFont() {
	if (fixedFont == null) {
	    Resource res = Resource.getResourceFor("tahiti");

	    fixedFont = res.getFont("tahiti.fixedFont", null);
	}
	return fixedFont;
    }

    synchronized public static Font getProportionalFont() {
	if (proportionalFont == null) {
	    Resource res = Resource.getResourceFor("tahiti");

	    proportionalFont = res.getFont("tahiti.font", null);
	}
	return proportionalFont;
    }

    synchronized public static Font getTitleFont() {
	if (titleFont == null) {
	    Resource res = Resource.getResourceFor("tahiti");

	    titleFont = res.getFont("tahiti.titleFont", null);
	}
	return titleFont;
    }

    synchronized public static void reset() {
	titleFont = null;
	proportionalFont = null;
	fixedFont = null;
	background = null;
    }

    public static void setBackground(Component cmp) {
	backgrounds.addElement(cmp);
	cmp.setBackground(getBackground());
    }

    public static void setFixedFont(Component cmp) {
	if (fixeds.contains(cmp) == false) {
	    fixeds.addElement(cmp);
	}
	cmp.setFont(getFixedFont());
    }

    public static void setFont(Component cmp) {
	if (proportionals.contains(cmp) == false) {
	    proportionals.addElement(cmp);
	}
	cmp.setFont(getProportionalFont());
    }

    public static void setTitleFont(Component cmp) {
	if (titles.contains(cmp) == false) {
	    titles.addElement(cmp);
	}
	cmp.setFont(getTitleFont());
    }

    synchronized static public void update() {
	Enumeration e = titles.elements();
	Component c;

	while (e.hasMoreElements()) {
	    c = (Component) e.nextElement();
	    c.setFont(getTitleFont());
	}

	e = proportionals.elements();
	while (e.hasMoreElements()) {
	    c = (Component) e.nextElement();
	    c.setFont(getProportionalFont());
	}

	e = fixeds.elements();
	while (e.hasMoreElements()) {
	    c = (Component) e.nextElement();
	    c.setFont(getFixedFont());
	}

	/*
	 * e = backgrounds.elements(); while(e.hasMoreElements()) { c =
	 * (Component)e.nextElement(); c.setBackground(getBackground()); }
	 */
    }
}
