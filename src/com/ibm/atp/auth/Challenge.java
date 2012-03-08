package com.ibm.atp.auth;

/*
 * @(#)Challenge.java
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
 * The <tt>Challenge</tt> class is random byte sequence which is a challenge for
 * authentication.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
final public class Challenge extends ByteSequence {
    /**
     * serial version UID
     */
    static final long serialVersionUID = 286760688223181885L;

    /**
     * The length of byte sequence.
     */
    final public static int LENGTH = 32;

    /**
     * Constructor creates a secure random generator, and generate byte sequence
     * as a challenge for authentication.
     */
    public Challenge() {
	super(LENGTH);
    }

    /**
     * Constructor creates byte sequence as a copy of given byte sequence as a
     * challenge for authentication.
     * 
     * @param challenge
     *            a byte sequence to be copied as a challenge
     */
    public Challenge(Challenge challenge) {
	super(challenge.challenge());
    }

    /**
     * Constructor creates byte sequence as a copy of given hexadecimal string
     * of encoded bytes as a challenge for authentication.
     * 
     * @param str
     *            a string of encoded byte sequence to be copied as a challenge
     */
    public Challenge(String str) {
	super(0, str, null);
    }

    /**
     * Returns current byte sequence as a challenge for authentication.
     * 
     * @return current byte sequence as a challenge for authentication.
     */
    final public byte[] challenge() {
	return this.sequence();
    }

    /**
     * Returns a hexadecimal string representation of the byte sequence. The
     * series of hexadecimal strings are the contents of byte sequence.
     * 
     * @return a hexadecimal string representation of the byte sequence
     * @see com.ibm.atp.auth.ByteSequence#toString
     */
    @Override
    public String toString() {
	return Hexadecimal.valueOf(this.challenge());
    }
}
