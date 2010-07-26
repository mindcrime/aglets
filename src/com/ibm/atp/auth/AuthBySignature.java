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
    private String _signatureAlgorithm = this.DEFAULTDIGITALSIGNATUREALGORITHM;

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
     * Constructor creates a specified message digest function.
     * 
     * @param digestName
     *            the name of message digest function algorithm
     */
    protected AuthBySignature(String digestName) {
	super(digestName);
	this.setSignatureAlgorithm(this.DEFAULTDIGITALSIGNATUREALGORITHM);
    }

    /**
     * Constructor creates a specified message digest function.
     * 
     * @param digestName
     *            the name of message digest function algorithm
     * @param signatureName
     *            the name of message digest function algorithm
     */
    protected AuthBySignature(String digestName, String signatureName) {
	super(digestName);
	this.setSignatureAlgorithm(signatureName);
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
    protected AuthBySignature(String digestName, String signatureName,
	    PrivateKey privateKey, PublicKey publicKey) {
	this(digestName, signatureName);
	this.setPrivateKey(privateKey);
	this.setPublicKey(publicKey);
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
    protected AuthBySignature(String digestName, PrivateKey privateKey,
	    PublicKey publicKey) {
	this(digestName);
	this.setPrivateKey(privateKey);
	this.setPublicKey(publicKey);
    }

    /**
     * Constructor creates a default message digest function.
     * 
     * @param privateKey
     *            private key of self
     * @param publicKey
     *            public key of opponent
     */
    protected AuthBySignature(PrivateKey privateKey, PublicKey publicKey) {
	this();
	this.setPrivateKey(privateKey);
	this.setPublicKey(publicKey);
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
    public final byte[] calculateResponse(int turn, Challenge challenge)
	    throws AuthenticationException {
	return this.sign(turn, challenge);
    }

    /**
     * Gets the private key of self.
     * 
     * @return private key of self
     */
    public PrivateKey getPrivateKey() {
	return this._privateKey;
    }

    /**
     * Gets the public key of opponent.
     * 
     * @return public key of opponent
     */
    public PublicKey getPublicKey() {
	return this._publicKey;
    }

    /**
     * Returns the name of digital signature algorithm.
     * 
     * @return the name of digital signature algorithm.
     */
    public String getSignatureAlgorithm() {
	return this._signatureAlgorithm;
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
    protected final byte[] hash(int turn, Challenge challenge)
	    throws AuthenticationException {
	this.resetDigest();

	if (challenge == null) {
	    throw new AuthenticationException("Challenge is null.");
	}
	this.addBytes(challenge.challenge());

	final String turnPad = this.getTurnPad(turn);

	if ((turnPad == null) || turnPad.equals("")) {
	    throw new AuthenticationException("TurnPad is null.");
	}
	this.addBytes(turnPad.getBytes());

	return this.getDigestValue();
    }

    /**
     * Sets the private key of self.
     * 
     * @param privateKey
     *            private key of self
     */
    protected void setPrivateKey(PrivateKey privateKey) {
	this._privateKey = privateKey;
    }

    /**
     * Sets the public key of opponent.
     * 
     * @param publicKey
     *            public key of opponent
     */
    protected void setPublicKey(PublicKey publicKey) {
	this._publicKey = publicKey;
    }

    /**
     * Sets the name of digital signature algorithm.
     * 
     * @param name
     *            the name of digital signature algorithm
     */
    protected void setSignatureAlgorithm(String name) {
	this._signatureAlgorithm = name;
	try {
	    this._signature = Signature.getInstance(name);
	} catch (NoSuchAlgorithmException excpt) {
	    System.err.println("Exception: Authenticate: " + excpt);
	    this._signatureAlgorithm = null;
	    this._signature = null;
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
    protected final byte[] sign(int turn, Challenge challenge)
	    throws AuthenticationException {
	byte[] signature = null;

	try {

	    /* Initialize digital signature with a private key */
	    this._signature.initSign(this._privateKey);

	    /* Update and sign the hashed data */
	    this._signature.update(this.hash(turn, challenge));

	    /* sign it */
	    signature = this._signature.sign();
	} catch (InvalidKeyException excpt) {
	    System.err.println("Exception: Authenticate: " + excpt);
	    throw new AuthenticationException("private key is invalid.");
	} catch (SignatureException excpt) {
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
     * @param response
     *            response value for authentication
     * @exception AuthenticationException
     *                byte sequence for response is invalid
     */
    @Override
    public final boolean verify(int turn, Challenge challenge, byte[] signature)
	    throws AuthenticationException {
	boolean verifies = true;

	try {

	    /* Initialize digital signature with a public key */
	    this._signature.initVerify(this._publicKey);

	    /* Update and sign the hashed data */
	    this._signature.update(this.hash(turn, challenge));

	    /* verify it */
	    verifies = this._signature.verify(signature);
	} catch (InvalidKeyException excpt) {
	    System.err.println("Exception: Authenticate: " + excpt);
	    throw new AuthenticationException("public key is invalid.");
	} catch (SignatureException excpt) {
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
     * @param response
     *            response value for authentication
     * @exception AuthenticationException
     *                byte sequence for response is invalid
     */
    @Override
    public final boolean verify(int turn, Challenge challenge,
	    ByteSequence signature) throws AuthenticationException {
	if (signature == null) {
	    return false;
	}
	return this.verify(turn, challenge, signature.sequence());
    }
}
