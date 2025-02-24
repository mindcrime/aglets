package com.ibm.atp.auth;

/*
 * @(#)AuthBySignature.java
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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * The <tt>AuthBySignature</tt> class is the class for challenge-response
 * authentication by message digest.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthBySignature extends Auth {
	/**
	 * The Digital Signature algorithm "DSA".
	 * docs/guide/security/CryptoSpec.html#AppA
	 */
	private final String DEFAULTDIGITALSIGNATUREALGORITHM = "DSA";
	private String _signatureAlgorithm = DEFAULTDIGITALSIGNATUREALGORITHM;

	/**
	 * A Digital Signature
	 */
	private Signature _signature = null;

	/**
	 * The private key of self
	 */
	private PrivateKey _privateKey = null;

	/**
	 * The public key of opponent
	 */
	private PublicKey _publicKey = null;

	/**
	 * Default constructor creates a default message digest function.
	 */
	protected AuthBySignature() {
		super();
	}

	/**
	 * Constructor creates a default message digest function.
	 * 
	 * @param privateKey
	 *            private key of self
	 * @param publicKey
	 *            public key of opponent
	 */
	protected AuthBySignature(final PrivateKey privateKey, final PublicKey publicKey) {
		this();
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param digestName
	 *            the name of message digest function algorithm
	 */
	protected AuthBySignature(final String digestName) {
		super(digestName);
		setSignatureAlgorithm(DEFAULTDIGITALSIGNATUREALGORITHM);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param digestName
	 *            the name of message digest function algorithm
	 * @param privateKey
	 *            private key of self
	 * @param publicKey
	 *            public key of opponent
	 */
	protected AuthBySignature(final String digestName, final PrivateKey privateKey,
	                          final PublicKey publicKey) {
		this(digestName);
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param digestName
	 *            the name of message digest function algorithm
	 * @param signatureName
	 *            the name of message digest function algorithm
	 */
	protected AuthBySignature(final String digestName, final String signatureName) {
		super(digestName);
		setSignatureAlgorithm(signatureName);
	}

	/**
	 * Constructor creates a specified message digest function.
	 * 
	 * @param digestName
	 *            the name of message digest function algorithm
	 * @param signatureName
	 *            the name of message digest function algorithm
	 * @param privateKey
	 *            private key of self
	 * @param publicKey
	 *            public key of opponent
	 */
	protected AuthBySignature(final String digestName, final String signatureName,
	                          final PrivateKey privateKey, final PublicKey publicKey) {
		this(digestName, signatureName);
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
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
	 *                byte sequence for response is invalid
	 */
	@Override
	public final byte[] calculateResponse(final int turn, final Challenge challenge)
	throws AuthenticationException {
		return sign(turn, challenge);
	}

	/**
	 * Gets the private key of self.
	 * 
	 * @return private key of self
	 */
	public PrivateKey getPrivateKey() {
		return _privateKey;
	}

	/**
	 * Gets the public key of opponent.
	 * 
	 * @return public key of opponent
	 */
	public PublicKey getPublicKey() {
		return _publicKey;
	}

	/**
	 * Returns the name of digital signature algorithm.
	 * 
	 * @return the name of digital signature algorithm.
	 */
	public String getSignatureAlgorithm() {
		return _signatureAlgorithm;
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
	 *                byte sequence for response is invalid
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

		return getDigestValue();
	}

	/**
	 * Sets the private key of self.
	 * 
	 * @param privateKey
	 *            private key of self
	 */
	protected void setPrivateKey(final PrivateKey privateKey) {
		_privateKey = privateKey;
	}

	/**
	 * Sets the public key of opponent.
	 * 
	 * @param publicKey
	 *            public key of opponent
	 */
	protected void setPublicKey(final PublicKey publicKey) {
		_publicKey = publicKey;
	}

	/**
	 * Sets the name of digital signature algorithm.
	 * 
	 * @param name
	 *            the name of digital signature algorithm
	 */
	protected void setSignatureAlgorithm(final String name) {
		_signatureAlgorithm = name;
		try {
			_signature = Signature.getInstance(name);
		} catch (final NoSuchAlgorithmException excpt) {
			System.err.println("Exception: Authenticate: " + excpt);
			_signatureAlgorithm = null;
			_signature = null;
		}
	}

	/**
	 * Calculate signature
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @exception AuthenticationException
	 *                byte sequence for response is invalid
	 */
	protected final byte[] sign(final int turn, final Challenge challenge)
	throws AuthenticationException {
		byte[] signature = null;

		try {

			/* Initialize digital signature with a private key */
			_signature.initSign(_privateKey);

			/* Update and sign the hashed data */
			_signature.update(hash(turn, challenge));

			/* sign it */
			signature = _signature.sign();
		} catch (final InvalidKeyException excpt) {
			System.err.println("Exception: Authenticate: " + excpt);
			throw new AuthenticationException("private key is invalid.");
		} catch (final SignatureException excpt) {
			System.err.println("Exception: Authenticate: " + excpt);
			throw new AuthenticationException("signature cannot be calculated.");
		}

		return signature;
	}

	/**
	 * Verify signature
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @param signature
	 *            response value for authentication
	 * @exception AuthenticationException
	 *                byte sequence for response is invalid
	 */
	@Override
	public final boolean verify(final int turn, final Challenge challenge, final byte[] signature)
	throws AuthenticationException {
		boolean verifies = true;

		try {

			/* Initialize digital signature with a public key */
			_signature.initVerify(_publicKey);

			/* Update and sign the hashed data */
			_signature.update(hash(turn, challenge));

			/* verify it */
			verifies = _signature.verify(signature);
		} catch (final InvalidKeyException excpt) {
			System.err.println("Exception: Authenticate: " + excpt);
			throw new AuthenticationException("public key is invalid.");
		} catch (final SignatureException excpt) {
			System.err.println("Exception: Authenticate: " + excpt);
			throw new AuthenticationException("signature verification is failed.");
		}

		return verifies;
	}

	/**
	 * Verify signature
	 * 
	 * @param turn
	 *            of individual
	 * @param challenge
	 *            a challenge
	 * @param signature
	 *            response value for authentication
	 * @exception AuthenticationException
	 *                byte sequence for response is invalid
	 */
	@Override
	public final boolean verify(
	                            final int turn,
	                            final Challenge challenge,
	                            final ByteSequence signature)
	throws AuthenticationException {
		if (signature == null) {
			return false;
		}
		return this.verify(turn, challenge, signature.sequence());
	}
}
