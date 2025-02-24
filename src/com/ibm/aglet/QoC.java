package com.ibm.aglet;

/*
 * @(#)QoC.java
 * 
 * (c) Copyright IBM Corp. 1998
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

import java.io.Serializable;
import java.util.Date;

/**
 * <tt>QoC</tt>
 * @version     0.20    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */

/**
 * QoC defines the quality of aglet communication.
 */
public class QoC implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2429080288901779379L;
	//
	// Constants
	//
	// Integrity (Strength against tampering data)
	/**
	 * <pre>
	 * INTEGRITY = Strength against tampering data
	 * </pre>
	 * 
	 * Integrity : No Integrity (data are sent with no additional information).
	 */
	public static final String NOINTEGRITY = "NOINTEGRITY";
	/**
	 * Integrity : byte sequence with its digest (data are sent with its digest
	 * generated by one-way hash function and shared secret byte sequence).
	 * 
	 * <pre>
	 * For example,
	 * data+hash(data+SharedSecret)
	 * </pre>
	 */
	private static final String DIGEST = "DIGEST";
	/**
	 * Integrity : Normal Integrity (equals to data with its digest).
	 */
	public static final String NORMALINTEGRITY = DIGEST;
	/**
	 * Integrity : data with its signature (data are sent with its signature
	 * signed by message sender with his private key).
	 * 
	 * <pre>
	 * For example,
	 * data+sign(hash(data), KeyPrivA)
	 * </pre>
	 */
	private static final String SIGNATURE = "SIGNATURE";
	/**
	 * Integrity : Strong Integrity (equals to data with its signature).
	 */
	public static final String STRONGINTEGRITY = SIGNATURE;
	/**
	 * Integrity : Default Integrity (equals to normal integrity).
	 */
	public static final String DEFAULTINTEGRITY = NORMALINTEGRITY;

	//
	// Confidentiality (Strength against tapping data)
	/**
	 * <pre>
	 * CONFIDENTIALITY = Strength against tapping data
	 * </pre>
	 * 
	 * Confidentiality : No Confidentiality (data are sent through raw data
	 * stream).
	 */
	public static final String NOCONFIDENTIALITY = "NOCONFIDENTIALITY";
	/**
	 * Confidentiality : data encrypted by short secret key (data are encrypted
	 * by short secret key).
	 * 
	 * <pre>
	 * For example,
	 * encrypt(msg, KeySecretShort)
	 * </pre>
	 */
	private static final String SHORTSECRETKEY = "SHORTSECRETKEY";
	/**
	 * Confidentiality : Normal Confidentiality (equals to data encrypted by
	 * short secret key).
	 */
	public static final String NORMALCONFIDENTIALITY = SHORTSECRETKEY;
	/**
	 * Confidentiality : data encrypted by long secret key (data are encrypted
	 * by long secret key).
	 * 
	 * <pre>
	 * For example,
	 * encrypt(msg, KeySecretLong)
	 * </pre>
	 */
	private static final String LONGSECRETKEY = "LONGSECRETKEY";
	/**
	 * Confidentiality : Strong Confidentiality (equals to message encrypted by
	 * private key)
	 */
	public static final String STRONGCONFIDENTIALITY = LONGSECRETKEY;
	/**
	 * Confidentiality : Default Confidentiality (equals to normal
	 * confidentiality)
	 */
	public static final String DEFAULTCONFIDENTIALITY = NORMALCONFIDENTIALITY;

	//
	//
	//
	private String _integrity = DEFAULTINTEGRITY;
	private String _confidentiality = DEFAULTCONFIDENTIALITY;
	private long _timeout = 0;

	/**
	 * Default Constructor. Use default integrity and default confidentiality.
	 */
	public QoC() {
		this(DEFAULTINTEGRITY, DEFAULTCONFIDENTIALITY);
	}

	/**
	 * Constructor with communication scheme/protocol, integrity and
	 * confidentiality.
	 * 
	 * @param integrity
	 *            the way for integrity of communication
	 * @param confidentiality
	 *            confidentiality of communication
	 */
	public QoC(final String integrity, final String confidentiality) {
		setIntegrity(integrity);
		setConfidentiality(confidentiality);
	}

	/**
	 * Returns the confidentiality of aglet transfer and messages.
	 * 
	 * @return the confidentiality of aglet transfer and messages
	 */
	public String getConfidentiality() {
		return _confidentiality;
	}

	/**
	 * Gets the due-date to connect via this channel.
	 * 
	 * @return the date when to connect via this channel
	 */
	public Date getDueDate() {
		return new Date(_timeout);
	}

	/**
	 * Returns the way to warrant integrity of messages.
	 * 
	 * @return the way to warrant integrity of messages
	 */
	public String getIntegrity() {
		return _integrity;
	}

	/**
	 * Gets the waiting time for time-out to connect via this channel.
	 * 
	 * @return waiting time for time-out[milli seconds]
	 */
	public long getTimeout() {
		return _timeout;
	}

	/**
	 * Sets the confidentiality for aglet transfer and message.
	 * 
	 * @param confidentiality
	 *            Confidentiality for aglet transfer and message.
	 */
	public void setConfidentiality(final String confidentiality) {
		if (confidentiality.equalsIgnoreCase(NOCONFIDENTIALITY)
				|| (confidentiality == null) || confidentiality.equals("")) {
			_confidentiality = NOCONFIDENTIALITY;
		} else if (confidentiality.equalsIgnoreCase(NORMALCONFIDENTIALITY)) {
			_confidentiality = NORMALCONFIDENTIALITY;
		} else if (confidentiality.equalsIgnoreCase(STRONGCONFIDENTIALITY)) {
			_confidentiality = STRONGCONFIDENTIALITY;
		} else {

			// no changes
		}
	}

	/**
	 * Sets the due-date to connect via this channel. When null is given, there
	 * is no due-date and infinitely try to connect.
	 * 
	 * @param date
	 *            due-date for time-out
	 */
	public void setDueDate(final Date date) {
		_timeout = date.getTime();
	}

	/**
	 * Sets the switch to warrant the integrity of communication. This is to
	 * protect messages from tampering. The aglet adds the message digest or
	 * digital signature to his message.
	 * 
	 * @param integrity
	 *            way to warrant the integrity of communication
	 */
	public void setIntegrity(final String integrity) {
		if (integrity.equalsIgnoreCase(NOINTEGRITY) || (integrity == null)
				|| integrity.equals("")) {
			_integrity = NOINTEGRITY;
		} else if (integrity.equalsIgnoreCase(NORMALINTEGRITY)) {
			_integrity = NORMALINTEGRITY;
		} else if (integrity.equalsIgnoreCase(STRONGINTEGRITY)) {
			_integrity = STRONGINTEGRITY;
		} else {

			// no changes
		}
	}

	/**
	 * Sets the waiting time for time-out to connect via this channel. When 0 is
	 * given, there is no waiting time for time-out and infinitely try to
	 * connect.
	 * 
	 * @param milliseconds
	 *            waiting time for time-out [milli seconds]
	 */
	public void setTimeout(final long milliseconds) {
		_timeout = milliseconds;
	}

	/**
	 * Returns a string representation of the quality of communication.
	 * 
	 * @return a string representation of the quality of communication
	 * @see java.lang.Object#toString
	 */
	@Override
	public String toString() {
		final String integrity = "Integrity=" + _integrity;
		final String confidentiality = "Confidentiality="
			+ _confidentiality;
		final String timeout = "Timeout=" + _timeout;
		final String str = integrity + ", " + confidentiality + ", " + timeout;

		return str;
	}
}
