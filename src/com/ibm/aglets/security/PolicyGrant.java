package com.ibm.aglets.security;

/*
 * @(#)PolicyGrant.java
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

import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.awb.misc.MalformedURIPatternException;
import com.ibm.awb.misc.URIPattern;

/**
 * The <tt>PolicyGrant</tt> class represents a grant of Java policy database.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class PolicyGrant {
    private static final String QUOTE = String.valueOf(PolicyFileReader.CHAR_STRING_QUOTE);
    private static final String COMMA = String.valueOf(PolicyFileReader.CHAR_COMMA);
    private static final String BEGIN_BLOCK = String.valueOf(PolicyFileReader.CHAR_BEGIN_BLOCK);
    private static final String END_BLOCK = String.valueOf(PolicyFileReader.CHAR_END_BLOCK);
    private static final String TERMINATOR = String.valueOf(PolicyFileReader.CHAR_TERMINATOR);
    private static final String NAME_SEPARATOR = COMMA;

    private String _signerNames = null;
    private Vector _signers = null;
    private URIPattern _codeBase = null;
    private String _ownerNames = null;
    private Vector _owners = null;
    private Vector _permissions = new Vector();

    private static final String ANYBODY = "*";

    private static final String PROPERTY_CRLF = "line.separator";
    private static final String DEFAULT_CRLF = "\r\n";
    private static String crlf = null;
    static {
	crlf = PolicyImpl.getSystemProperty(PROPERTY_CRLF, DEFAULT_CRLF);
    }

    public PolicyGrant() {
    }

    public void addPermission(PolicyPermission permission) {
	this._permissions.addElement(permission);
    }

    public boolean equals(Vector signers, URIPattern codeBase, Vector owners) {
	if (!PolicyPermission.equalsSigners(signers, this._signers)) {
	    return false;
	}
	if ((codeBase != null) && !codeBase.equals(this._codeBase)) {
	    return false;
	}
	if (!PolicyPermission.equalsSigners(owners, this._owners)) {
	    return false;
	}
	return true;
    }

    public URIPattern getCodeBase() {
	return this._codeBase;
    }

    public String getOwnerNames() {
	return this._ownerNames;
    }

    public Enumeration getOwners() {
	if (this._owners != null) {
	    return this._owners.elements();
	}
	return null;
    }

    public Enumeration getPermissions() {
	return this._permissions.elements();
    }

    public Enumeration getPermissions(String className) {
	Vector permissions = new Vector();
	final int num = this._permissions.size();
	int i = 0;

	for (i = 0; i < num; i++) {
	    Object obj = this._permissions.elementAt(i);

	    if (obj instanceof PolicyPermission) {
		PolicyPermission permission = (PolicyPermission) obj;

		if (permission.equalsClassName(className)) {
		    permissions.addElement(permission);
		}
	    }
	}
	return permissions.elements();
    }

    public Permissions getPermissions(CodeSource cs) {
	if (cs != null) {
	    URL codeBase = cs.getLocation();
	    Certificate[] certs = cs.getCertificates();

	    if (this.isCodeBase(codeBase) && this.isOwnedByAndSignedBy(certs)) {
		final int num = this._permissions.size();

		if (num == 0) {
		    return null;
		}
		Permissions permissions = new Permissions();
		int i;

		for (i = 0; i < num; i++) {
		    Object obj = this._permissions.elementAt(i);

		    if (obj instanceof PolicyPermission) {
			PolicyPermission permission = (PolicyPermission) obj;

			permissions.add(permission.getPermission());
		    }
		}
		return permissions;
	    }
	    return null;
	}
	return null;
    }

    public String getSignerNames() {
	return this._signerNames;
    }

    public Enumeration getSigners() {
	if (this._signers != null) {
	    return this._signers.elements();
	}
	return null;
    }

    private final String getUsername(Certificate cert) {
	String username = null;

	try {

	    // final PublicKey pubkey = publicKey;
	    // username = (String) AccessController.doPrivileged(new
	    // PrivilegedAction() {
	    // public Object run() {
	    // UserAuthenticator authenticator =
	    // UserAuthenticator.getUserAuthenticator();
	    // return authenticator.getUsername(pubkey);
	    // }
	    // });
	    final Certificate fCert = cert;

	    username = (String) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return com.ibm.aglets.AgletRuntime.getCertificateAlias(fCert);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return username;
    }

    protected boolean isCodeBase(URL codeBase) {
	final URIPattern myCB = this._codeBase;
	final URL cb = codeBase;

	if (this._codeBase == null) {

	    // regard as anywhere
	    return true;
	}
	Boolean bb = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
	    public Object run() {
		if (myCB == null) {
		    return new Boolean(true);
		} else {
		    return new Boolean(myCB.isMatch(cb));
		}
	    }
	});

	return bb.booleanValue();
    }

    protected boolean isOwnedBy(Certificate ownerCert) {
	if ((this._owners == null) || (this._owners.size() == 0)) {

	    // regard as anybody
	    return true;
	}
	if (ownerCert == null) {

	    // nobody owned
	    return false;
	}
	String ownerName = this.getUsername(ownerCert);

	if ((ownerName == null) || ownerName.equals("")) {
	    return false;
	}
	int num = this._owners.size();

	for (int i = 0; i < num; i++) {
	    Object obj = this._owners.elementAt(i);

	    if (obj instanceof String) {
		String name = (String) obj;

		if (ownerName.equals(name)) {
		    return true;
		}
	    }
	}
	return false;
    }

    protected boolean isOwnedByAndSignedBy(Certificate[] certs) {

	// the first key is for owner, the rest keys are for signers
	Certificate ownerCert = null;

	// ! PublicKey[] signerKeys = null;
	if (certs != null) {
	    ownerCert = certs[0];

	    // ! int num = keys.length-1;
	    // ! if(num>0) {
	    // ! signerKeys = new PublicKey[num];
	    // ! for(int i=0; i<num; i++) {
	    // ! signerKeys[i] = keys[i+1];
	    // ! }
	    // ! }
	}

	// ! return isOwnedBy(ownerKey) && isSignedBy(signerKeys);
	// tentative
	return this.isOwnedBy(ownerCert);
    }

    private final boolean isRegisteredUser(String username) {
	Certificate cert = com.ibm.aglets.AgletRuntime.getRegisteredCertificate(username);

	return (cert != null);
    }

    protected boolean isSignedBy(Certificate[] certs) {
	if ((this._signers == null) || (this._signers.size() == 0)) {

	    // regard as anybody
	    return true;
	}
	if ((certs == null) || (certs.length == 0)) {

	    // nobody signed
	    return false;
	}
	final int num = this._signers.size();
	int i;

	for (i = 0; i < num; i++) {
	    Object obj = this._signers.elementAt(i);

	    if (obj instanceof String) {
		String signer = (String) obj;

		if (!this.isSignedBy(signer, certs)) {
		    return false;
		}
	    }
	}
	return true;
    }

    protected boolean isSignedBy(String signer, Certificate[] certs) {
	if ((signer == null) || signer.equals("")) {

	    // regard as anybody
	    return true;
	}
	if ((certs == null) || (certs.length == 0)) {

	    // nobody signed
	    return false;
	}
	for (Certificate cert : certs) {
	    String username = this.getUsername(cert);

	    if (signer.equals(username)) {
		return true;
	    }
	}
	return false;
    }

    public void setCodeBase(String codeBase)
					    throws MalformedURIPatternException {
	this._codeBase = new URIPattern(codeBase);
    }

    public void setCodeBase(URL codeBase) throws MalformedURIPatternException {
	this._codeBase = new URIPattern(codeBase);
    }

    public void setOwnerNames(String ownerNames) {
	Vector owners = null;
	StringBuffer buff = null;

	if (ownerNames != null) {
	    StringTokenizer st = new StringTokenizer(ownerNames, NAME_SEPARATOR);

	    while (st.hasMoreTokens()) {
		String ownerName = st.nextToken().trim();

		if ("".equals(ownerName) || ANYBODY.equals(ownerName)) {
		    this._owners = null;
		    this._ownerNames = ANYBODY;
		    return;
		}
		if (!this.isRegisteredUser(ownerName)) {
		    System.err.println("Unknown owner name '"
			    + ownerName
			    + "' is specified in aglets policy file. Ignore the owner name.");
		} else {
		    if (owners == null) {
			owners = new Vector();
		    }
		    if (buff == null) {
			buff = new StringBuffer();
		    } else {
			buff.append(NAME_SEPARATOR);
		    }
		    owners.addElement(ownerName);
		    buff.append(ownerName);
		}
	    }
	}
	this._owners = owners;
	if (buff == null) {
	    this._ownerNames = null;
	} else {
	    this._ownerNames = buff.toString();
	}
    }

    public void setSignerNames(String signerNames) {
	Vector signers = null;
	StringBuffer buff = null;

	if (signerNames != null) {
	    StringTokenizer st = new StringTokenizer(signerNames, NAME_SEPARATOR);

	    while (st.hasMoreTokens()) {
		String signerName = st.nextToken().trim();

		if ("".equals(signerName) || ANYBODY.equals(signerName)) {
		    this._signers = null;
		    this._signerNames = ANYBODY;
		    return;
		}
		if (!this.isRegisteredUser(signerName)) {
		    System.err.println("Unknown signer name '"
			    + signerName
			    + "' is specified in aglets policy file. Ignore the signer name.");
		} else {
		    if (signers == null) {
			signers = new Vector();
		    }
		    if (buff == null) {
			buff = new StringBuffer();
		    } else {
			buff.append(NAME_SEPARATOR);
		    }
		    signers.addElement(signerName);
		    buff.append(signerName);
		}
	    }
	}
	this._signers = signers;
	if (buff == null) {
	    this._signerNames = null;
	} else {
	    this._signerNames = buff.toString();
	}
    }

    @Override
    public String toString() {
	Vector lines = this.toVector();
	String str = "";
	final int num = lines.size();
	int idx = 0;

	for (idx = 0; idx < num; idx++) {
	    final String line = (String) lines.elementAt(idx);

	    str += line + crlf;
	}
	return str;
    }

    public Vector toVector() {
	Vector lines = new Vector();

	// 1st line
	String line = PolicyFileReader.WORD_GRANT;

	// signed by
	if ((this._signerNames != null) && !this._signerNames.equals("")) {
	    line += " " + PolicyFileReader.WORD_SIGNEDBY + " " + QUOTE
		    + this._signerNames + QUOTE;
	}

	// code base
	if (this._codeBase != null) {
	    if (this._signerNames != null) {
		line += COMMA;
	    }
	    line += " " + PolicyFileReader.WORD_CODEBASE + " " + QUOTE
		    + this._codeBase.toString() + QUOTE;
	}

	// owned by
	if ((this._ownerNames != null) && !this._ownerNames.equals("")) {
	    if ((this._signerNames != null) || (this._codeBase != null)) {
		line += COMMA;
	    }
	    line += " " + PolicyFileReader.WORD_OWNEDBY + " " + QUOTE
		    + this._ownerNames + QUOTE;
	}
	line += " " + BEGIN_BLOCK;
	lines.addElement(line);

	// 2nd line
	final int numPerm = this._permissions.size();
	int i;

	for (i = 0; i < numPerm; i++) {
	    line = "  " + this._permissions.elementAt(i).toString();
	    lines.addElement(line);
	}

	// last line
	line = END_BLOCK + TERMINATOR;
	lines.addElement(line);

	return lines;
    }

    /**
     * Returns the number of the permission stored whitin this grant.
     * 
     * @return the number of permission within this grant, zero if there are no
     *         permissions.
     */
    public final int getPermissionCount() {
	if (this._permissions == null)
	    return 0;
	else
	    return this._permissions.size();
    }

    /**
     * Returns the number of signers for the current policy grant.
     * 
     * @return the number of signers (>= 0)
     */
    public final int getSignersCount() {
	if (this._signers == null)
	    return 0;
	else
	    return this._signers.size();
    }

    /**
     * Returns the number of onwers for this policy grant.
     * 
     * @return the numbero fo owners or zero if no one has been specified.
     */
    public final int getOwnersCount() {
	if (this._owners == null)
	    return 0;
	else
	    return this._owners.size();
    }
}
