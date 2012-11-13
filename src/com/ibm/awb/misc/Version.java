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
 * Class Version is used to create version objects that contain release
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
	private final int majorVersion;
	private final int minorVersion;
	private final int microVersion;
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
	 * @param micro
	 *            build version number x.x.B
	 */
	public Version(final String kind, final int major, final int minor, final int micro) {
		product = (kind != null) ? kind : "";
		majorVersion = major;
		minorVersion = minor;
		microVersion = micro;
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
	public Version(final String kind, final int major, final int minor, final int micro,
	               final boolean isStable) {
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
		return getProduct();
	}

	/**
	 * Gets back the majorVersion.
	 * 
	 * @return the majorVersion
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * Gets back the microVersion.
	 * 
	 * @return the microVersion
	 */
	public int getMicroVersion() {
		return microVersion;
	}

	/**
	 * Gets back the minorVersion.
	 * 
	 * @return the minorVersion
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * Gets back the product.
	 * 
	 * @return the product
	 */
	public String getProduct() {
		return product;
	}

	/**
	 * Gets back the isStable.
	 * 
	 * @return the isStable
	 */
	public boolean isStable() {
		return isStable;
	}

	/**
	 * Provides a representation of the Version. The string returned is composed
	 * with the product kind, the major.minor.micro version and a string that
	 * provides information about the stable/unstable version.
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer(50);
		buffer.append(product);
		buffer.append(" - ");
		buffer.append(majorVersion);
		buffer.append(".");
		buffer.append(minorVersion);
		buffer.append(".");
		buffer.append(microVersion);

		if (isStable)
			buffer.append(" (stable)");
		else
			buffer.append(" (unstable)");

		return buffer.toString();

	}
}
