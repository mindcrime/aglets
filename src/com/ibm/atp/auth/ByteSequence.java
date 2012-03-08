package com.ibm.atp.auth;

/*
 * @(#)ByteSequence.java
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

import java.io.Serializable;

import com.ibm.aglets.security.Randoms;
import com.ibm.awb.misc.Hexadecimal;

/**
 * The <tt>ByteSequence</tt> class is byte sequence using for authentication.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 * 
 * @see com.ibm.atp.auth.Challenge
 * @see com.ibm.atp.auth.SharedSecret
 */
public class ByteSequence extends Object implements Cloneable, Serializable {
    /**
     * serial version UID
     */
    static final long serialVersionUID = -742497536523647344L;

    /**
     * 
     */
    final private static char DELIMITER = ' ';
    final private static String DELIMITERS = String.valueOf(DELIMITER);

    /**
     * The byte sequence using for authentication.
     */
    private byte[] _seq = null;

    /**
     * Constructor creates byte sequence as a copy of given byte sequence.
     * 
     * @param seq
     *            a byte sequence to be copied
     */
    public ByteSequence(byte[] seq) {
	if (seq != null) {
	    this._seq = new byte[seq.length];
	    this.copy(seq);
	}
    }

    /**
     * Constructor creates a secure random generator to generate byte sequence.
     * 
     * @param length
     *            length of byte sequence to be generated
     */
    public ByteSequence(int length) {
	this._seq = new byte[length];
	Randoms.getRandom(length, this._seq);
    }

    /**
     * Constructor creates byte sequence as a copy of length and given
     * hexadecimal string of encoded bytes. If length is 0, calculates the
     * number of encoded bytes and generates byte sequence.
     * 
     * @param length
     *            length of byte sequence
     * @param str
     *            a hexadecimal string to be copied
     * @see #toString
     */
    public ByteSequence(int length, String str) {
	this(length, str, DELIMITERS);
    }

    /**
     * Constructor creates byte sequence as a copy of length and given
     * hexadecimal string of encoded bytes. If length is 0, calculates the
     * number of encoded bytes and generates byte sequence.
     * 
     * @param length
     *            length of byte sequence
     * @param str
     *            a hexadecimal string to be copied
     * @param delimiters
     *            delimiters between hexadecimal numbers
     * @see #toString
     */
    public ByteSequence(int length, String str, String delimiters) {
	byte[] seq = null;

	try {
	    seq = Hexadecimal.parseSeq(str, delimiters);
	} catch (NumberFormatException excpt) {
	    System.err.println(excpt.toString());
	}
	int len;

	if (length == 0) {
	    len = seq.length;
	} else {
	    len = length;
	}
	this._seq = new byte[len];
	for (int i = 0; i < len; i++) {
	    this._seq[i] = seq[i];
	}
    }

    /**
     * Constructor creates byte sequence as a copy of given byte sequence.
     * 
     * @param seq
     *            a byte sequence to be copied
     */
    public ByteSequence(ByteSequence seq) {
	if (seq != null) {
	    byte[] sq = seq.sequence();

	    if (seq != null) {
		this._seq = new byte[sq.length];
		this.copy(seq);
	    }
	}
    }

    /**
     * Constructor creates byte sequence as a copy of given string.
     * 
     * @param str
     *            a string to be copied
     */
    public ByteSequence(String str) {
	if (str != null) {
	    byte[] seq = str.getBytes();

	    if (seq != null) {
		this._seq = new byte[seq.length];
		this.copy(seq);
	    }
	}
    }

    /**
     * Append a given byte sequence into the byte sequence.
     * 
     * @param seq
     *            a byte sequence to be appended
     */
    public synchronized void append(byte[] seq) {
	if (seq == null) {
	    return;
	}
	final byte[] seqCurrent = this.sequence();
	int len = 0;

	if (seqCurrent != null) {
	    len = seqCurrent.length;
	}
	byte[] newseq = new byte[len + seq.length];
	int i;

	for (i = 0; i < len; i++) {
	    newseq[i] = seqCurrent[i];
	}
	for (i = 0; i < seq.length; i++) {
	    newseq[i + len] = seq[i];
	}
	this._seq = newseq;
    }

    /**
     * Append a given byte into the byte sequence.
     * 
     * @param b
     *            a byte to be appended
     */
    public synchronized void append(byte b) {
	final byte[] seqCurrent = this.sequence();
	int len = 0;

	if (seqCurrent != null) {
	    len = seqCurrent.length;
	}
	this._seq = new byte[len + 1];
	int i;

	for (i = 0; i < len; i++) {
	    this._seq[i] = seqCurrent[i];
	}
	this._seq[len] = b;
    }

    /**
     * Append a given byte sequence object into the byte sequence.
     * 
     * @param seq
     *            a byte sequence object to be appended
     */
    public synchronized void append(ByteSequence seq) {
	if (seq != null) {
	    this.append(seq.sequence());
	}
    }

    /**
     * Append a given string into the byte sequence.
     * 
     * @param str
     *            a string to be appended
     */
    public synchronized void append(String str) {
	if (str != null) {
	    this.append(str.getBytes());
	}
    }

    /**
     * Creates a clone of the byte sequence.
     * 
     * @see java.lang.Object#clone
     * @see java.lang.Cloneable
     */
    @Override
    protected Object clone() {
	ByteSequence seq = new ByteSequence(this);

	return seq;
    }

    /**
     * Copy a byte sequence using for authentication. If given byte sequence is
     * shorter than the required byte sequence, fill the byte sequence by 0s. If
     * given byte sequence is longer than the required byte sequence, truncate
     * the rest of given byte sequence.
     * 
     * @param seq
     *            a byte sequence
     */
    public synchronized void copy(byte[] seq) {
	if (this._seq == null) {
	    return;
	}

	// java.lang.System#arraycopy() should be used ?
	int i;

	for (i = 0; i < this._seq.length; i++) {
	    if (i < seq.length) {
		this._seq[i] = seq[i];
	    } else {

		// If given byte sequence is shorter than the required byte
		// sequence,
		// Fill the byte sequence with 0s.
		this._seq[i] = 0;
	    }
	}

	// If given byte sequence is longer than the required byte sequence,
	// truncate the rest of given byte sequence.
    }

    /**
     * Copy a byte sequence object as a byte sequence using for authentication.
     * 
     * @param seq
     *            a byte sequence object to be copied as a byte sequence using
     *            for authentication
     */
    public synchronized void copy(ByteSequence seq) {
	if (seq != null) {
	    this.copy(seq.sequence());
	}
    }

    /**
     * Copy a string as byte sequence using for authentication.
     * 
     * @param str
     *            a string to be copied as a byte sequence using for
     *            authentication
     */
    public synchronized void copy(String str) {
	if (str != null) {
	    this.copy(str.getBytes());
	}
    }

    /**
     * Verifies the byte sequence equals to given byte sequence. If given byte
     * sequence does not equal to the required byte sequence, return false.
     * 
     * @param seq
     *            a byte sequence
     * @return true if the byte sequence is same; otherwise false.
     */
    public boolean equals(byte[] seq) {
	if ((seq == null) && (this._seq == null)) {
	    return true;
	}
	if ((seq == null) || (this._seq == null)) {
	    return false;
	}
	if (seq.length != this._seq.length) {
	    return false;
	}

	int i;

	for (i = 0; i < this._seq.length; i++) {
	    if (this._seq[i] != seq[i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Verifies the byte sequence equals to given byte sequence.
     * 
     * @param seq
     *            a byte sequence
     * @return true if the byte sequence is same; otherwise false.
     */
    public boolean equals(ByteSequence seq) {
	if (seq == null) {
	    return false;
	}
	return this.equals(seq.sequence());
    }

    /**
     * Verifies the byte sequence equals to given object.
     * 
     * @param obj
     *            a object
     * @return true if the object has same byte sequence; otherwise false.
     * @see java.lang.Object#equals
     */
    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ByteSequence) {
	    return this.equals((ByteSequence) obj);
	}
	return false;
    }

    /**
     * Returns length of byte sequence.
     * 
     * @return length of byte sequence
     */
    public int length() {
	if (this._seq == null) {
	    return 0;
	}
	return this._seq.length;
    }

    /**
     * Returns current byte sequence.
     * 
     * @return current byte sequence using for authentication.
     */
    public byte[] sequence() {
	return this._seq;
    }

    /**
     * Returns a hexadecimal string representation of the byte sequence. The
     * series of hexadecimal strings are the contents of byte sequence.
     * 
     * @return a hexadecimal string representation of the byte sequence
     * @see java.lang.Object#toString
     */
    @Override
    public String toString() {
	return Hexadecimal.valueOf(this._seq, DELIMITER);
    }
}
