package com.ibm.awb.misc;

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
 * Class Version is used to creat version objects that contain release
 * information, such as MAJOR, MINOR, BUILD, DATE, and KIND. Version objects can
 * also be created with an expiration date. Any attempt to create an instance of
 * the version object after the expiration date will fail and result in a call
 * to <tt>System.exit()</tt>. Creating the version object in the period 0 to 15
 * days before expiration will result in a warning message written by
 * <tt>System.out.println()</tt>. Values of a version object cannot be altered
 * once it has been created.
 * 
 * @version 1.02 96/06/28
 * @author Danny B. Lange
 */

public final class Version {

    /**
     * The description of the product.
     */
    private String product = "";

    /**
     * Release information.
     */
    private int majorVersion;
    private int minorVersion;
    private int microVersion;
    private boolean isStable = true;

    /**
     * Creates a version object which will never expire.
     * 
     * @param kind
     *            product information.
     * @param major
     *            major version number M.x.x
     * @param minor
     *            minor version number x.M.x
     * @param build
     *            build version number x.x.B
     * @param date
     *            date of this version.
     */
    public Version(String kind, int major, int minor, int micro) {
	this.product = (kind != null) ? kind : "";
	this.majorVersion = major;
	this.minorVersion = minor;
	this.microVersion = micro;
    }

    /**
     * Builds a version identifier.
     * 
     * @param kind
     *            the description of the product.
     * @param major
     *            the major versione number M.x.x
     * @param minor
     *            the minor version number x.M.x
     * @param micro
     *            the micro version number x.x.M
     * @param isStable
     *            true if this product is stable, false otherwise
     */
    public Version(String kind, int major, int minor, int micro,
                   boolean isStable) {
	this(kind, major, minor, micro);
	this.isStable = isStable;
    }

    /**
     * Gets the string that describes what is versioned.
     * 
     * @deprecated see getProduct()
     */
    @Deprecated
    public String getKind() {
	return this.getProduct();
    }

    /**
     * Provides a representation of the Version. The string returned is composed
     * with the product kind, the major.minor.micro version and a string that
     * provides information about the stable/unstable version.
     */
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer(50);
	buffer.append(this.product);
	buffer.append(" - ");
	buffer.append(this.majorVersion);
	buffer.append(".");
	buffer.append(this.minorVersion);
	buffer.append(".");
	buffer.append(this.microVersion);

	if (this.isStable)
	    buffer.append(" (stable)");
	else
	    buffer.append(" (unstable)");

	return buffer.toString();

    }

    /**
     * Gets back the isStable.
     * 
     * @return the isStable
     */
    public boolean isStable() {
	return this.isStable;
    }

    /**
     * Gets back the majorVersion.
     * 
     * @return the majorVersion
     */
    public int getMajorVersion() {
	return this.majorVersion;
    }

    /**
     * Gets back the microVersion.
     * 
     * @return the microVersion
     */
    public int getMicroVersion() {
	return this.microVersion;
    }

    /**
     * Gets back the minorVersion.
     * 
     * @return the minorVersion
     */
    public int getMinorVersion() {
	return this.minorVersion;
    }

    /**
     * Gets back the product.
     * 
     * @return the product
     */
    public String getProduct() {
	return this.product;
    }
}
