package com.ibm.aglet;

/*
 * @(#)AgletInfo.java
 * 
 * (c) Copyright IBM Corp. 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import java.net.URL;
import java.io.*;
import java.security.cert.Certificate;
import org.aglets.log.*;

/**
 * AgletInfo class is a object which contains the information of the aglet.
 * 
 * @version     $Revision: 1.4 $	$Date: 2007/07/19 12:23:08 $  $Author: cat4hire $
 * @author	Mitsuru Oshima
 */
final public class AgletInfo implements java.io.Serializable, Cloneable {

	static final long serialVersionUID = 5077220171230015552L;
	static AgletsLogger logger = new AgletsLogger(AgletInfo.class.getName());

	// static attributes
	private AgletID aid;
	private String classname;
	private String origin;
	private long birthtime;
	private short api_major_version;
	private short api_minor_version;

	private String codebase = null;
	private transient Certificate authorityCert = null;

	private byte[] authorityCertEncoded = null;

	/**
	 * Constructs a AgletInfo with the specified parameters.
	 * This is used internally and the aglet programmers should not use
	 * this API.
	 * 
	 * @param aid     the aglet identifier.
	 * @param address the address where the aglet currently resides.
	 * @param classname the classname of the aglet.
	 * @param codebase the codebase of the aglet classes.
	 * @param origin   the address where the aglet was instantiated.
	 * @param birthtime the time when the aglet was instantiated.
	 * @param api_major_version major version.
	 * @param api_minor_version minor version.
	 * @param authorityCert the authority's certificate of the aglet.
	 */
	public AgletInfo(AgletID aid, String classname, URL codebase, 
					 String origin, long birthtime, short api_major_version, 
					 short api_minor_version, Certificate authorityCert) {
		this.aid = aid;
		this.classname = classname;
		this.codebase = codebase.toExternalForm();
		this.origin = origin;
		this.birthtime = birthtime;
		this.api_major_version = api_major_version;
		this.api_minor_version = api_minor_version;
		this.authorityCert = authorityCert;
		try {
			this.authorityCertEncoded = authorityCert.getEncoded();
		} catch (java.security.cert.CertificateEncodingException ex) {
			ex.printStackTrace();
			this.authorityCertEncoded = null;
		} 

		// this.address   = address;
	}
	/*
	 * Gets the address where the aglet currently resides.
	 * 
	 * @deprecated
	 */
	public String getAddress() {
		return "atp://unknown/";
	}
	/**
	 * Gets the class name of the aglet.
	 */
	public String getAgletClassName() {
		return classname;
	}
	/**
	 * Gets the aglet id
	 */
	public AgletID getAgletID() {
		return aid;
	}
	/**
	 * Gets the API major version to which the aglet is instantiated.
	 */
	public short getAPIMajorVersion() {
		return api_major_version;
	}
	/**
	 * Gets the API minor version to which the aglet is instantiated.
	 */
	public short getAPIMinorVersion() {
		return api_minor_version;
	}
	/*
	 * Gets the authority's certificate. This will be replaced with new Security API.
	 * Please do not use this method for the moment.
	 * 
	 * @deprecated
	 */
	public Certificate getAuthorityCertificate() {
		return authorityCert;
	}
	/*
	 * Gets the authority's name(CN; CommonName).
	 * This will be replaced with new Security API.
	 * Please do not use this method for the moment.
	 * 
	 * @deprecated
	 */
	public String getAuthorityName() {
		if (authorityCert == null) {
			return "(Unknown)";
		} else {
			String alias = com.ibm.aglets.AgletRuntime.getCertificateAlias(authorityCert);
			if (alias == null) {
				return "(Unknown)";
			} else {
				return alias;
			}
			//return ((java.security.cert.X509Certificate)authorityCert)
			//	.getSubjectDN().getName();
		} 
	}
	/**
	 * Gets the codebase address where the aglet code resides.
	 */
	public URL getCodeBase() {
		try {
			return new URL(codebase);
		} catch (java.net.MalformedURLException ex) {
			ex.printStackTrace();
			return null;
		} 
	}
	/**
	 * Gets the time when the aglet is created.
	 */
	public long getCreationTime() {
		return birthtime;
	}
	/**
	 * Gets the origin address where the aglet was instantiated.
	 */
	public String getOrigin() {
		return origin;
	}
	/**
	 * 
	 * public AgletInfo clone(String a, URL c) {
	 * try {
	 * AgletInfo clone = (AgletInfo)clone();
	 * //	    clone.address = a;
	 * //	    clone.codebase = c.toExternalForm();
	 * return clone;
	 * } catch (CloneNotSupportedException ex) {
	 * return null;
	 * }
	 * }
	 */

	private void readObject(ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		authorityCert = 
			com.ibm.aglets.AgletRuntime.getCertificate(authorityCertEncoded);
		if (api_major_version != Aglet.MAJOR_VERSION) {
			throw new StreamCorruptedException("API version mismatch : " 
											   + api_major_version 
											   + " shold be " 
											   + Aglet.MAJOR_VERSION);
		} 
		if (api_minor_version != Aglet.MINOR_VERSION) {
			logger.error("API minor version mismatch.");
		} 
	}
	/**
	 * Gets a string representation of the object.
	 */
	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("ID       : " + aid + '\n');

		// b.append("Address  : " + address + '\n');
		b.append("ClassName: " + classname + '\n');
		b.append("Origin   : " + origin + '\n');
		b.append("CodeBase : " + codebase + '\n');
		return b.toString();
	}
}
