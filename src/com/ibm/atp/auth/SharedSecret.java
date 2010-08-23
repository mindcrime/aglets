package com.ibm.atp.auth;

/*
 * @(#)SharedSecret.java
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Vector;

import com.ibm.awb.misc.Hexadecimal;

/**
 * The <tt>SharedSecret</tt> class is byte sequence for authentication. which is
 * shared by individuals (agent, context, domain).
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
final public class SharedSecret extends ByteSequence {
    /**
     * serial version UID
     */
    static final long serialVersionUID = -7990001265976183031L;

    /**
     * message digest algorithm.
     */
    final private static String MESSAGE_DIGEST_ALGORITHM = "SHA";
    private static MessageDigest _mdigest = null;

    /**
     * signature algorithm.
     */
    final private static String SIGNATURE_ALGORITHM = "DSA";

    /**
     * The length of byte sequence.
     */
    final public static int LENGTH = 32;

    /**
     * field names
     */
    final private static String FIELD_SECRET = "Secret";
    final private static String FIELD_DOMAIN_NAME = "Domain";
    final private static String FIELD_CREATOR = "Creator";
    final private static String FIELD_SIGNATURE = "Signature";

    // final private static String FIELD_KEYSTORE_FILE = "KeyStoreFile";
    // final private static String FIELD_KEYSTORE_PASSWORD = "KeyStorePassword";
    // final private static String FIELD_KEY_ALIAS = "KeyAlias";
    // final private static String FIELD_KEY_PASSWORD = "KeyPassword";
    // final private static String FIELD_DATE = "Date";
    // final private static String FIELD_OWNER_NAME = "Owner";
    final private static char CHAR_COLON = ':';
    final private static String FIELD_NAME_TERM = String.valueOf(CHAR_COLON)
    + " ";

    // final private static String FORMAT_DATE = "yyyy.MM.dd HH:mm:ss.SSS z";

    /**
     * signature.
     */
    private Signature _sign = null;

    /**
     * Domain name/Owner name
     */

    // private Date _date = null;
    private transient String _domainName = null;
    private transient String _signature = null;

    // private transient String _keyStoreFile = null;
    // private transient String _keyStorePassword = null;
    // private transient String _ownerKeyAlias = null;
    // private transient String _ownerKeyPassword = null;

    private transient Certificate _creatorCert = null;

    // private transient PrivateKey _ownerKey = null;
    private transient byte[] _domainNameSeq = null;
    private transient byte[] _signatureSeq = null;

    /**
     * Gets new line string.
     */
    private static final String PROPERTY_CRLF = "line.separator";
    private static final String DEFAULT_CRLF = "\r\n";
    private static String _strNewLine = null;

    static {
	try {
	    _mdigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
	} catch (NoSuchAlgorithmException ex) {
	    ex.printStackTrace();
	}
	try {
	    _strNewLine = (String) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    return System.getProperty(PROPERTY_CRLF, DEFAULT_CRLF);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    private transient byte[] _creatorCertSeq = null;

    /**
     * Constructor creates a secure random generator, and generate byte sequence
     * as a shared secret (password) for authentication.
     */
    private SharedSecret(String domainName, Certificate creatorCert) {

	// Sets a random number as the secret of this shared secret.
	super(LENGTH);
	this.init();
	this.setDomainName(domainName);
	this.setCreator(creatorCert);

	// setSignature((byte[])null);
    }

    /**
     * Constructor creates byte sequence as a copy of given hexadecimal string
     * of encoded bytes as a shared secret (password) for authentication.
     * 
     * @param str
     *            a string of encoded byte sequence to be copied as a shared
     *            secret
     */
    private SharedSecret(String domainName, Certificate creatorCert,
                         String secret, String signature) throws KeyStoreException {
	super(0, secret, null);
	this.init();
	this.setDomainName(domainName);
	this.setCreator(creatorCert);
	this.setSignature(signature);
    }

    /**
     * Converts lines into a shared secret.
     */
    final static SharedSecret convertLinesToSharedSecret(Enumeration lines) {
	if (lines == null) {
	    return null;
	}
	String domain = null;
	String secret = null;
	String signature = null;
	Certificate creator = null;

	for (String line = null; lines.hasMoreElements();) {
	    line = (String) lines.nextElement();
	    if (line == null) {

		// end of line
		break;
	    }
	    final int idx = line.indexOf(FIELD_NAME_TERM);

	    if (idx >= 0) {
		final String fieldName = line.substring(0, idx);
		final String fieldValue = line.substring(idx
			+ FIELD_NAME_TERM.length() - 1).trim();

		if (FIELD_DOMAIN_NAME.equals(fieldName)) {
		    domain = fieldValue;
		} else if (FIELD_SECRET.equals(fieldName)) {
		    secret = fieldValue;
		} else if (FIELD_SIGNATURE.equals(fieldName)) {
		    signature = fieldValue;
		} else if (FIELD_CREATOR.equals(fieldName)) {
		    String encodedStr = fieldValue;
		    byte[] encoded = Hexadecimal.parseSeq(encodedStr);

		    creator = com.ibm.aglets.AgletRuntime.getCertificate(encoded);
		} else {

		    // unknown field name
		}
	    }
	}

	// Checks the parameters.
	if ((domain == null) || domain.equals("")) {
	    System.err.println("Domain name of shared secret is null.");
	    return null;
	}
	if ((secret == null) || secret.equals("")) {
	    System.err.println("Byte sequence of shared secret is null.");
	    return null;
	}
	if ((signature == null) || signature.equals("")) {
	    System.err.println("Byte sequence of shared secret is null.");
	    return null;
	}
	if (creator == null) {
	    System.err.println("Creator of shared secret is null.");
	    return null;
	}

	// Creates a new shared secret and verify it.
	try {
	    SharedSecret sec = new SharedSecret(domain, creator, secret, signature);

	    if (sec.verify()) {
		return sec;
	    }
	} catch (KeyStoreException ex) {
	    ex.printStackTrace();
	    return null;
	}
	System.err.println("Signature of shared secret is incorrect.");
	return null;
    }

    /**
     * Creates a new shared secret.
     */
    public synchronized final static SharedSecret createNewSharedSecret(
                                                                        String domainName,
                                                                        String creatorKeyAlias,
                                                                        String creatorKeyPassword) {
	Certificate cert = com.ibm.aglets.AgletRuntime.getCertificate(creatorKeyAlias);

	if (cert == null) {
	    System.err.println("SharedSecret.createNewSharedSecret: Creator's certificate was not found");
	    return null;
	}
	char[] pwd = null;

	if (creatorKeyPassword != null) {
	    pwd = creatorKeyPassword.toCharArray();
	}
	PrivateKey key = com.ibm.aglets.AgletRuntime.getPrivateKey(cert, pwd);

	if (key == null) {
	    System.err.println("SharedSecret.createNewSharedSecert: Failed to get creator's private key");
	    return null;
	}
	SharedSecret aSharedSecret = new SharedSecret(domainName, cert);

	aSharedSecret.sign(key);
	return aSharedSecret;
    }

    /**
     * Gets creator's certificate.
     * 
     * @return creator's certificate
     */
    public Certificate getCreatorCert() {
	return this._creatorCert;
    }

    /**
     * Gets the string representation of the encoded creator's certificate
     * 
     * @return a string
     */
    public String getCreatorEncodedString() {
	return Hexadecimal.valueOf(this._creatorCertSeq);
    }

    /**
     * Gets domain name.
     * 
     * @return domain name
     */
    public String getDomainName() {
	return this._domainName;
    }

    /**
     * Gets secret.
     * 
     * @return shared secret
     */
    private String getSecret() {
	return Hexadecimal.valueOf(this.sequence());
    }

    /**
     * Gets signature.
     * 
     * @return signature
     */
    public byte[] getSignature() {
	return this._signatureSeq;
    }

    /**
     * Gets signature string.
     * 
     * @return signature strnig
     */
    public String getSignatureString() {
	return this._signature;
    }

    /**
     * Initializes data.
     */
    private final void init() {
	try {
	    this._sign = Signature.getInstance(SIGNATURE_ALGORITHM);
	} catch (NoSuchAlgorithmException excpt) {
	    System.err.println(excpt.toString());
	}
    }

    /**
     * Loads shared secret.
     * 
     * @param filename
     *            filename of the shared secret file to be loaded
     */
    public synchronized static SharedSecret load(String filename)
    throws FileNotFoundException,
    IOException {
	FileReader freader = new FileReader(filename);
	BufferedReader breader = new BufferedReader(freader);
	Vector lines = new Vector();
	String line = null;

	while (true) {
	    line = breader.readLine();
	    if (line == null) {

		// end of line
		break;
	    }
	    lines.addElement(line);
	}
	breader.close();
	return convertLinesToSharedSecret(lines.elements());
    }

    /**
     * Saves to file.
     * 
     * @param filename
     *            filename of the shared secret file to be saved
     */
    public void save(String filename) throws IOException {
	Enumeration lines = this.toLines();

	if (lines == null) {
	    System.err.println("No secret.");
	    return;
	}
	FileWriter fwriter = new FileWriter(filename);
	BufferedWriter bwriter = new BufferedWriter(fwriter);

	while (lines.hasMoreElements()) {
	    String line = (String) lines.nextElement();

	    bwriter.write(line);
	    bwriter.newLine();
	}
	bwriter.flush();
	bwriter.close();
    }

    /**
     * Saves shared secret.
     * 
     * @param filename
     *            filename of the shared secret file to be saved
     * @param secrets
     *            the shared secret to be saved
     */
    public synchronized static void save(String filename, SharedSecret secret)
    throws IOException {
	if (secret == null) {
	    throw new IOException("Secret is null.");
	}
	secret.save(filename);
    }

    /**
     * Returns current byte sequence as a shared secret (password) for
     * authentication.
     * 
     * @return current byte sequence as a shared secret (password) for
     *         authentication.
     */
    final public byte[] secret() {
	try {
	    ByteSequence seq = new ByteSequence(this.sequence());

	    // seq.append(_dateSeq);
	    seq.append(this._domainNameSeq);
	    seq.append(this._creatorCert.getEncoded());
	    return seq.sequence();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null; // ??????Is this OK?(HT)
	}
    }

    /**
     * Sets signature.
     * 
     * @param signature
     *            signature
     */
    private void setCreator(Certificate creator) {
	try {
	    this._creatorCert = creator;
	    this._creatorCertSeq = creator.getEncoded();
	} catch (java.security.cert.CertificateEncodingException ex) {
	    System.out.println("Cannot get encoded byte sequence of the creator's certificate: "
		    + creator.toString());
	    this._creatorCert = null;
	    this._creatorCertSeq = null;
	}
    }

    /**
     * Sets domain name.
     * 
     * @param name
     *            domain name
     */
    private void setDomainName(String name) {
	this._domainName = name;
	ByteSequence seq = new ByteSequence(name);

	this._domainNameSeq = seq.sequence();
    }

    /**
     * Sets signature.
     * 
     * @param signature
     *            signature
     */
    private void setSignature(byte[] signature) {
	this._signature = Hexadecimal.valueOf(signature);
	this._signatureSeq = signature;
    }

    /**
     * Sets signature.
     * 
     * @param signature
     *            signature string
     */
    private void setSignature(String signature) {
	byte[] seq = null;

	try {
	    seq = Hexadecimal.parseSeq(signature);
	} catch (NumberFormatException excpt) {
	    return;
	}
	this._signature = signature;
	this._signatureSeq = seq;
    }

    /**
     * Signs the signature.
     */
    final private void sign(PrivateKey key) {
	if (key == null) {

	    // unknown user
	    System.err.println("Sharedsecret.sign(): null private key");
	    return;
	}
	try {
	    _mdigest.reset();
	    _mdigest.update(this.secret());
	    this._sign.initSign(key);
	    this._sign.update(_mdigest.digest());
	    this.setSignature(this._sign.sign());
	} catch (InvalidKeyException excpt) {
	    System.err.println(excpt.toString());
	    return;
	} catch (SignatureException excpt) {
	    System.err.println(excpt.toString());
	    return;
	}
    }

    /**
     * Returns lines representation of the shared secret.
     * 
     * @return lines representation of the shared secret
     */
    public Enumeration toLines() {
	Vector lines = null;
	final String secret = this.getSecret();
	final String domain = this.getDomainName();
	final String creator = this.getCreatorEncodedString();
	final String signature = this.getSignatureString();

	if ((secret != null) && !secret.equals("")) {
	    if (lines == null) {
		lines = new Vector();
	    }
	    lines.addElement(FIELD_SECRET + FIELD_NAME_TERM + secret);
	}
	if ((domain != null) && !domain.equals("")) {
	    if (lines == null) {
		lines = new Vector();
	    }
	    lines.addElement(FIELD_DOMAIN_NAME + FIELD_NAME_TERM + domain);
	}
	if ((creator != null) && !creator.equals("")) {
	    if (lines == null) {
		lines = new Vector();
	    }
	    lines.addElement(FIELD_CREATOR + FIELD_NAME_TERM + creator);
	}
	if ((signature != null) && !signature.equals("")) {
	    if (lines == null) {
		lines = new Vector();
	    }
	    lines.addElement(FIELD_SIGNATURE + FIELD_NAME_TERM + signature);
	}
	if (lines == null) {
	    return null;
	}
	return lines.elements();
    }

    /**
     * Returns a string representation of the shared secret.
     * 
     * @return a string representation of the shared secret
     * @see ByteSequence#toString
     * @override ByteSequence#toString
     */
    @Override
    public String toString() {
	Enumeration lines = this.toLines();

	if (lines == null) {
	    return null;
	}
	String str = null;

	while (lines.hasMoreElements()) {
	    String line = (String) lines.nextElement();

	    if (str == null) {
		str = line;
	    } else {
		str += _strNewLine + line;
	    }
	}
	return str;
    }

    /**
     * Verifies the signature.
     * 
     * @return true if the signature is correct, otherwise false.
     */
    final private boolean verify() {
	if (this._signatureSeq == null) {
	    return false;
	}
	try {
	    _mdigest.reset();
	    _mdigest.update(this.secret());
	    this._sign.initVerify(this._creatorCert.getPublicKey());

	    // - System.out.println("secret : "+Hexadecimal.valueOf(secret()));
	    this._sign.update(_mdigest.digest());

	    // -
	    // System.out.println("signature : "+Hexadecimal.valueOf(_signatureSeq));
	    return this._sign.verify(this.getSignature());
	} catch (InvalidKeyException excpt) {
	    System.err.println(excpt.toString());
	    return false;
	} catch (SignatureException excpt) {
	    System.err.println(excpt.toString());
	    return false;
	}
    }
}
