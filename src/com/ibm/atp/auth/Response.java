package com.ibm.atp.auth;

/*
 * @(#)Response.java
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

import com.ibm.awb.misc.Hexadecimal;

/**
 * The <tt>Response</tt> class is byte sequence which is a response
 * authentication.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
final public class Response extends ByteSequence {
    /**
     * serial version UID
     */
    static final long serialVersionUID = 6004557419567685224L;

    /**
     * Constructor creates byte sequence as a copy of given byte sequence as a
     * response of authentication.
     * 
     * @param response
     *            a byte sequence to be copied as a response
     */
    public Response(byte[] response) {
	super(response);
    }

    /**
     * Constructor creates byte sequence as a copy of given byte sequence as a
     * response of authentication.
     * 
     * @param response
     *            a byte sequence to be copied as a response
     */
    public Response(Response response) {
	this(response.response());
    }

    /**
     * Constructor creates byte sequence as a copy of given hexadecimal string
     * of encoded bytes as a response of authentication.
     * 
     * @param str
     *            a string of encoded byte sequence to be copied as a response
     */
    public Response(String str) {
	super(0, str, null);
    }

    /**
     * Returns current byte sequence as a response of authentication.
     * 
     * @return current byte sequence as a response of authentication.
     */
    final public byte[] response() {
	return this.sequence();
    }

    /**
     * Returns a hexadecimal string representation of the byte sequence. The
     * series of hexadecimal strings are the contents of byte sequence.
     * 
     * @return a hexadecimal string representation of the byte sequence
     * @see com.ibm.atp.auth.ByteSequence#toString
     * @override com.ibm.atp.auth.ByteSequence#toString
     */
    @Override
    public String toString() {
	return Hexadecimal.valueOf(this.response());
    }
}
