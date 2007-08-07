package com.ibm.awb.misc;

import java.util.Calendar;
import java.util.Locale;

/*
 * @(#)Version.java
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

/**
 * Class Version is used to create version objects that contain release
 * information, such as MAJOR, MINOR, BUILD, DATE, and KIND.
 * Version objects can also be created with an expiration date. Any attempt
 * to create an instance of the version object after the expiration date
 * will fail and result in a call to <tt>System.exit()</tt>. Creating the version
 * object in the period 0 to 15 days before expiration will result in a
 * warning message written by <tt>System.out.println()</tt>. Values of
 * a version object cannot be altered once it has been created.
 * 
 * @version     1.02    96/06/28
 * @author      Danny B. Lange
 */

public final class Version {

	// -------------------------------------------------------------------
	// -- Instance variables.

	// -- Product information.
	private String _kind = "";

	// -- Release information.
    //-- Release information.
    private int _majorVersion;
    private int _minorVersion;
    private int _microVersion;

	/**
	 * Creates a version object which will never expire.
	 * @param kind product information.
	 * @param major major version number M.x.x
	 * @param minor minor version number x.M.x
	 * @param micro build version number x.x.B
	 */
	public Version(String kind, int major, int minor, int micro) {
		_kind = (kind != null) ? kind : "";
		_majorVersion = major;
		_minorVersion = minor;
		_microVersion = micro;
	}

	/**
	 * Gets the string that describes what is versioned.
	 */
	public String getKind() {
		return _kind;
	}

    /**
     * Returns a long text representation of the version numbers:
     * e.g., Alpha2b, Beta1, V2.0.
     */
    public String toString() { 
		return _kind + " " +
			_majorVersion + "." + _minorVersion + "." + _microVersion;
    }
}
