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

import java.security.AccessController;
import java.security.Permissions;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import com.ibm.awb.misc.URIPattern;
import com.ibm.awb.misc.MalformedURIPatternException;

import java.net.URL;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.security.PublicKey;

/**
 * The <tt>PolicyGrant</tt> class represents a grant
 * of Java policy database.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:40 $
 * @author      ONO Kouichi
 */
public class PolicyGrant {
	private static final String QUOTE = 
		String.valueOf(PolicyFileReader.CHAR_STRING_QUOTE);
	private static final String COMMA = 
		String.valueOf(PolicyFileReader.CHAR_COMMA);
	private static final String BEGIN_BLOCK = 
		String.valueOf(PolicyFileReader.CHAR_BEGIN_BLOCK);
	private static final String END_BLOCK = 
		String.valueOf(PolicyFileReader.CHAR_END_BLOCK);
	private static final String TERMINATOR = 
		String.valueOf(PolicyFileReader.CHAR_TERMINATOR);
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

	public PolicyGrant() {}
	public void addPermission(PolicyPermission permission) {
		_permissions.addElement(permission);
	}
	public boolean equals(Vector signers, URIPattern codeBase, 
						  Vector owners) {
		if (!PolicyPermission.equalsSigners(signers, _signers)) {
			return false;
		} 
		if (codeBase != null &&!codeBase.equals(_codeBase)) {
			return false;
		} 
		if (!PolicyPermission.equalsSigners(owners, _owners)) {
			return false;
		} 
		return true;
	}
	public URIPattern getCodeBase() {
		return _codeBase;
	}
	public String getOwnerNames() {
		return _ownerNames;
	}
	public Enumeration getOwners() {
		if (_owners != null) {
			return _owners.elements();
		} 
		return null;
	}
	public Enumeration getPermissions() {
		return _permissions.elements();
	}
	public Enumeration getPermissions(String className) {
		Vector permissions = new Vector();
		final int num = _permissions.size();
		int i = 0;

		for (i = 0; i < num; i++) {
			Object obj = _permissions.elementAt(i);

			if (obj instanceof PolicyPermission) {
				PolicyPermission permission = (PolicyPermission)obj;

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

			if (isCodeBase(codeBase) && isOwnedByAndSignedBy(certs)) {
				final int num = _permissions.size();

				if (num == 0) {
					return null;
				} 
				Permissions permissions = new Permissions();
				int i;

				for (i = 0; i < num; i++) {
					Object obj = _permissions.elementAt(i);

					if (obj instanceof PolicyPermission) {
						PolicyPermission permission = (PolicyPermission)obj;

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
		return _signerNames;
	}
	public Enumeration getSigners() {
		if (_signers != null) {
			return _signers.elements();
		} 
		return null;
	}
	private final String getUsername(Certificate cert) {
		String username = null;

		try {

			// final PublicKey pubkey = publicKey;
			// username = (String) AccessController.doPrivileged(new PrivilegedAction() {
			// public Object run() {
			// UserAuthenticator authenticator = UserAuthenticator.getUserAuthenticator();
			// return authenticator.getUsername(pubkey);
			// }
			// });
			final Certificate fCert = cert;

			username = 
				(String)AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return com.ibm.aglets.AgletRuntime
						.getCertificateAlias(fCert);
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return username;
	}
	protected boolean isCodeBase(URL codeBase) {
		final URIPattern myCB = _codeBase;
		final URL cb = codeBase;

		if (_codeBase == null) {

			// regard as anywhere
			return true;
		} 
		Boolean bb = 
			(Boolean)AccessController.doPrivileged(new PrivilegedAction() {
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
		if (_owners == null || _owners.size() == 0) {

			// regard as anybody
			return true;
		} 
		if (ownerCert == null) {

			// nobody owned
			return false;
		} 
		String ownerName = getUsername(ownerCert);

		if (ownerName == null || ownerName.equals("")) {
			return false;
		} 
		int num = _owners.size();

		for (int i = 0; i < num; i++) {
			Object obj = _owners.elementAt(i);

			if (obj instanceof String) {
				String name = (String)obj;

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

		// !     PublicKey[] signerKeys = null;
		if (certs != null) {
			ownerCert = certs[0];

			// !       int num = keys.length-1;
			// !       if(num>0) {
			// ! 	signerKeys = new PublicKey[num];
			// ! 	for(int i=0; i<num; i++) {
			// ! 	  signerKeys[i] = keys[i+1];
			// ! 	}
			// !       }
		} 

		// !     return isOwnedBy(ownerKey) && isSignedBy(signerKeys);
		// tentative
		return isOwnedBy(ownerCert);
	}
	private final boolean isRegisteredUser(String username) {
		Certificate cert = 
			com.ibm.aglets.AgletRuntime.getRegisteredCertificate(username);

		return (cert != null);
	}
	protected boolean isSignedBy(Certificate[] certs) {
		if (_signers == null || _signers.size() == 0) {

			// regard as anybody
			return true;
		} 
		if (certs == null || certs.length == 0) {

			// nobody signed
			return false;
		} 
		final int num = _signers.size();
		int i;

		for (i = 0; i < num; i++) {
			Object obj = _signers.elementAt(i);

			if (obj instanceof String) {
				String signer = (String)obj;

				if (!isSignedBy(signer, certs)) {
					return false;
				} 
			} 
		} 
		return true;
	}
	protected boolean isSignedBy(String signer, Certificate[] certs) {
		if (signer == null || signer.equals("")) {

			// regard as anybody
			return true;
		} 
		if (certs == null || certs.length == 0) {

			// nobody signed
			return false;
		} 
		for (int i = 0; i < certs.length; i++) {
			String username = getUsername(certs[i]);

			if (signer.equals(username)) {
				return true;
			} 
		} 
		return false;
	}
	public void setCodeBase(String codeBase) 
			throws MalformedURIPatternException {
		_codeBase = new URIPattern(codeBase);
	}
	public void setCodeBase(URL codeBase) 
			throws MalformedURIPatternException {
		_codeBase = new URIPattern(codeBase);
	}
	public void setOwnerNames(String ownerNames) {
		Vector owners = null;
		StringBuffer buff = null;

		if (ownerNames != null) {
			StringTokenizer st = new StringTokenizer(ownerNames, 
													 NAME_SEPARATOR);

			while (st.hasMoreTokens()) {
				String ownerName = st.nextToken().trim();

				if ("".equals(ownerName) || ANYBODY.equals(ownerName)) {
					_owners = null;
					_ownerNames = ANYBODY;
					return;
				} 
				if (!isRegisteredUser(ownerName)) {
					System.err
						.println("Unknown owner name '" + ownerName 
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
		_owners = owners;
		if (buff == null) {
			_ownerNames = null;
		} else {
			_ownerNames = buff.toString();
		} 
	}
	public void setSignerNames(String signerNames) {
		Vector signers = null;
		StringBuffer buff = null;

		if (signerNames != null) {
			StringTokenizer st = new StringTokenizer(signerNames, 
													 NAME_SEPARATOR);

			while (st.hasMoreTokens()) {
				String signerName = st.nextToken().trim();

				if ("".equals(signerName) || ANYBODY.equals(signerName)) {
					_signers = null;
					_signerNames = ANYBODY;
					return;
				} 
				if (!isRegisteredUser(signerName)) {
					System.err
						.println("Unknown signer name '" + signerName 
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
		_signers = signers;
		if (buff == null) {
			_signerNames = null;
		} else {
			_signerNames = buff.toString();
		} 
	}
	public String toString() {
		Vector lines = toVector();
		String str = "";
		final int num = lines.size();
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			final String line = (String)lines.elementAt(idx);

			str += line + crlf;
		} 
		return str;
	}
	public Vector toVector() {
		Vector lines = new Vector();

		// 1st line
		String line = PolicyFileReader.WORD_GRANT;

		// signed by
		if (_signerNames != null &&!_signerNames.equals("")) {
			line += " " + PolicyFileReader.WORD_SIGNEDBY + " " + QUOTE 
					+ _signerNames + QUOTE;
		} 

		// code base
		if (_codeBase != null) {
			if (_signerNames != null) {
				line += COMMA;
			} 
			line += " " + PolicyFileReader.WORD_CODEBASE + " " + QUOTE 
					+ _codeBase.toString() + QUOTE;
		} 

		// owned by
		if (_ownerNames != null &&!_ownerNames.equals("")) {
			if (_signerNames != null || _codeBase != null) {
				line += COMMA;
			} 
			line += " " + PolicyFileReader.WORD_OWNEDBY + " " + QUOTE 
					+ _ownerNames + QUOTE;
		} 
		line += " " + BEGIN_BLOCK;
		lines.addElement(line);

		// 2nd line
		final int numPerm = _permissions.size();
		int i;

		for (i = 0; i < numPerm; i++) {
			line = "  " + _permissions.elementAt(i).toString();
			lines.addElement(line);
		} 

		// last line
		line = END_BLOCK + TERMINATOR;
		lines.addElement(line);

		return lines;
	}
}
