package com.ibm.atp;

/*
 * @(#)AtpConstants.java
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
 * AtciConstants interface defines constant value for ATCI
 * 
 * @version 1.10 23 Mar 1997
 * @author Mitsuru Oshima
 */
public interface AtpConstants {

	static public final int REQUEST_TYPE_BASE = 1000;
	static public final int DISPATCH = REQUEST_TYPE_BASE + 1;
	static public final int RETRACT = REQUEST_TYPE_BASE + 2;
	static public final int FETCH = REQUEST_TYPE_BASE + 3;
	static public final int PING = REQUEST_TYPE_BASE + 4;
	static public final int MESSAGE = REQUEST_TYPE_BASE + 5;
	static public final int REPLY = REQUEST_TYPE_BASE + 6;
	static public final int ILLEGAL_REQUEST = REQUEST_TYPE_BASE + 7;
	static public final int MAX_REQUEST_TYPE = REQUEST_TYPE_BASE + 8;

	static public final int NO_AUTHENTICATION_MANNER = 0;
	static public final int AUTHENTICATION_MANNER_DIGEST = 1;
	static public final int AUTHENTICATION_MANNER_SIGNATURE = 2;

	/**
	 * The value representing that an ATCI message is a response.
	 */
	public static final int ATCI_REQUEST = 1;

	/**
	 * The value representing that an ATCI message is a response.
	 */
	public static final int ATCI_RESPONSE = 2;

	/**
	 * Status code representing that the request has succeeded.
	 */

	public static final int OKAY = 100;

	/**
	 * Status code representing that the requested resource is no longer at the
	 * recipient.
	 */
	public static final int MOVED = 200;

	/**
	 * Status code representing that the recipient was unable to understand the
	 * request message due to malformed syntax.
	 */
	public static final int BAD_REQUEST = 300;

	/**
	 * Status code representing that although the recipient understood the
	 * request message, it is refused to fulfill it.
	 */
	public static final int FORBIDDEN = 301;

	/**
	 * Status code representing that the recipient could not find the requested
	 * resource.
	 */
	public static final int NOT_FOUND = 302;

	/**
	 * Status code representing that the recipient encountered an unexpected
	 * condition which prevented it from fullfiling the request.
	 */
	public static final int INTERNAL_ERROR = 400;

	/**
	 * Status code representing that the recipient does not support the
	 * functionality required to fulfill the request.
	 */
	public static final int NOT_IMPLEMENTED = 401;

	/**
	 * Status code representing that the recipient, while acting as a gateway or
	 * proxy, recieved an invalid response from upstream server.
	 */
	public static final int BAD_GATEWAY = 402;

	/**
	 * Status code representing that the recipient is currently unable to handle
	 * the request due to a temporary overloading of the recipient.
	 */
	public static final int SERVICE_UNAVAILABLE = 403;

	/**
	 * Status code representing that the recipient does not authenticate the
	 * sender.
	 */
	public static final int NOT_AUTHENTICATED = 500;

	/*
	 * For Messaging
	 */
	public static final int SYNC = 0;
	public static final int FUTURE = 1;
	public static final int ONEWAY = 2;

	/*
	 * 
	 */
	public static final int HANDLED = 0;
	public static final int NOT_HANDLED = 1;
	public static final int EXCEPTION = 2;
}
