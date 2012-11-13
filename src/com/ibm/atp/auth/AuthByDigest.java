package com.ibm.atp.auth;

/*
 * @(#)AuthByDigest.java
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
 * The <tt>AuthByDigest</tt> class is the class for challenge-response
 * authentication by message digest.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthByDigest extends Auth {
	/**
	 * The secret shared by each other
	 */
	private SharedSecret _secret = null;

	/**
	 * Default constructor creates a default message digest function.
	 */
	protected AuthByDigest() {
		super();
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param secret
	 *            secret shared by each other
	 */
	public AuthByDigest(final SharedSecret secret) {
		this();
		setSecret(secret);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param name
	 *            the name of message digest function algorithm
	 */
	protected AuthByDigest(final String name) {
		super(name);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param name
	 *            the name of message digest function algorithm
	 * @param secret
	 *            secret shared by each other
	 */
	public AuthByDigest(final String name, final SharedSecret secret) {
		this(name);
		setSecret(secret);
	}

	/**
	 * Calculate response value for authentication.
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @return response value for authentication
	 * @exception AuthenticationException
	 *                byte sequence to be hased is invalid
	 */
	@Override
	public final byte[] calculateResponse(final int turn, final Challenge challenge)
	throws AuthenticationException {
		return hash(turn, challenge);
	}

	/**
	 * Gets the secret shared by each other.
	 * 
	 * @return secret shared by each other
	 */
	public SharedSecret getSecret() {
		return _secret;
	}

	/**
	 * Calculate hashed value for authentication.
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @return hashed value for authentication
	 * @exception AuthenticationException
	 *                byte sequence to be hased is invalid
	 */
	@Override
	protected final byte[] hash(final int turn, final Challenge challenge)
	throws AuthenticationException {
		resetDigest();

		if (challenge == null) {
			throw new AuthenticationException("Challenge is null.");
		}
		addBytes(challenge.challenge());

		final String turnPad = getTurnPad(turn);

		if ((turnPad == null) || turnPad.equals("")) {
			throw new AuthenticationException("TurnPad is null.");
		}
		addBytes(turnPad.getBytes());

		final SharedSecret secret = getSecret();

		if (secret == null) {
			throw new AuthenticationException("Shared secret is null.");
		}
		addBytes(secret.secret());

		return getDigestValue();
	}

	/**
	 * Sets the secret shared by each other.
	 * 
	 * @param secret
	 *            secret shared by each other
	 */
	protected void setSecret(final SharedSecret secret) {
		_secret = secret;
	}

	/**
	 * Verify response value for authentication.
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @param response
	 *            response value for authentication
	 * @exception AuthenticationException
	 *                byte sequence for response is invalid
	 */
	@Override
	public boolean verify(final int turn, final Challenge challenge, final byte[] response)
	throws AuthenticationException {
		final ByteSequence seq = new ByteSequence(calculateResponse(turn, challenge));

		return seq.equals(response);
	}

	/**
	 * Verify response value for authentication.
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @param response
	 *            response value for authentication
	 * @exception AuthenticationException
	 *                byte sequence for response is invalid
	 */
	@Override
	public boolean verify(final int turn, final Challenge challenge, final ByteSequence response)
	throws AuthenticationException {
		final ByteSequence seq = new ByteSequence(calculateResponse(turn, challenge));

		return seq.equals(response);
	}
}
