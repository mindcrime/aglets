package com.ibm.awb.misc;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/*
 * @(#)URIPattern.java
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
 * The <tt>URIPattern</tt> class represents a URI pattern.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class URIPattern {
    private static final char CHAR_COLON = ':';
    private static final char PROTOCOL_TERMINATOR = CHAR_COLON;
    private static final char CHAR_SLASH = '/';
    private static final String STRING_SLASH = String.valueOf(CHAR_SLASH);
    private static final String HOSTNAME_LEADER = STRING_SLASH + STRING_SLASH;
    private static final char PORT_LEADER = CHAR_COLON;
    private static final String FILE_LEADER = STRING_SLASH;

    private static final char CHAR_ASTERISK = '*';
    private static final char CHAR_HYPHEN = '-';
    private static final char CHAR_DOT = '.';
    private static final String STRING_ASTERISK = String.valueOf(CHAR_ASTERISK);
    private static final String STRING_HYPHEN = String.valueOf(CHAR_HYPHEN);
    private static final String STRING_DOT = String.valueOf(CHAR_DOT);
    private static final String WILDCARD_HOST = STRING_ASTERISK;
    private static final String WILDCARD_PROTOCOL = STRING_ASTERISK;
    private static final String WILDCARD_ANYDIR = STRING_HYPHEN;
    private static final String WILDCARD_SUBDIR = STRING_ASTERISK;
    private static final String DOMAIN_DELIMITER = STRING_DOT;

    private static final String PROTOCOL_FILE = "file";

    private String _pattern = null;
    private String _protocol = null;
    private String _host = null;
    private String _port = null;
    private PortPattern _ppat = null;
    private String _file = null;

    public URIPattern(String pattern) throws MalformedURIPatternException {
	this._pattern = this.expandPropertyRef(pattern);
	final int idxProtocol = pattern.indexOf(PROTOCOL_TERMINATOR);

	if (idxProtocol == -1) {
	    throw new MalformedURIPatternException("Protocol does not specified : \""
		    + pattern + "\".");
	}
	this._protocol = pattern.substring(0, idxProtocol);
	final String body = pattern.substring(idxProtocol + 1);

	if ("file".equalsIgnoreCase(this._protocol)) {
	    this._host = "";
	    this._port = null;
	    this._file = body;
	} else if (body.startsWith(HOSTNAME_LEADER)) {
	    final String rest = body.substring(2);
	    String hostpart = null;
	    final int idxFile = rest.indexOf(FILE_LEADER);

	    if (idxFile == -1) {
		hostpart = rest;
		this._file = "";
	    } else {
		hostpart = rest.substring(0, idxFile);
		this._file = rest.substring(idxFile);
	    }
	    final int idxPort = hostpart.indexOf(PORT_LEADER);

	    if (idxPort == -1) {
		this._host = hostpart;
		this._port = null;
	    } else {
		this._host = hostpart.substring(0, idxPort);
		this._port = hostpart.substring(idxPort + 1);
	    }
	} else if (body.startsWith(FILE_LEADER)) {

	    // no hostpart specifed
	    this._host = "";
	    this._port = null;
	    this._file = body;
	} else {
	    throw new MalformedURIPatternException("Hostname does not specified : \""
		    + pattern + "\".");
	}
	if ((this._file == null) || this._file.equals("")) {
	    this._file = FILE_LEADER;
	}
	try {
	    this._ppat = new PortPattern(this._port);
	} catch (IllegalArgumentException excpt) {
	    throw new MalformedURIPatternException(excpt.toString());
	}
    }

    public URIPattern(URL url) throws MalformedURIPatternException {
	this._pattern = url.toString();
	this._protocol = url.getProtocol();
	this._host = url.getHost();
	final int port = url.getPort();

	this._port = String.valueOf(port);
	try {
	    this._ppat = new PortPattern(port);
	} catch (IllegalArgumentException excpt) {
	    throw new MalformedURIPatternException(excpt.toString());
	}
	this._file = url.getFile();
    }

    public final static String canonicalFilename(String filename) {
	if (filename == null) {
	    return null;
	}
	final int len = filename.length();

	if (len < 1) {
	    return null;
	}
	String fn = filename;
	boolean usewild = false;
	final String dir = filename.substring(len - 1);

	if (len > 1) {
	    final String sep = filename.substring(len - 2, len - 1);

	    if ((sep.equals(STRING_SLASH) || sep.equals(File.separator))
		    && (dir.equals(WILDCARD_ANYDIR) || dir.equals(WILDCARD_SUBDIR))) {
		usewild = true;
		fn = filename.substring(0, len - 1);
	    }
	}
	final File file = new File(fn);

	if (file == null) {
	    return null;
	}
	String canonFile = file.getPath();

	try {
	    canonFile = file.getCanonicalPath();
	} catch (IOException excpt) {
	    return null;
	}
	if (usewild == true) {
	    if (!canonFile.endsWith(File.separator)) {
		canonFile += File.separator;
	    }
	    canonFile += dir;
	}
	return canonFile;
    }

    public boolean equals(URIPattern uripat) {
	return this.equals(uripat._protocol, uripat._host, uripat._ppat, uripat._file);
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof URIPattern) {
	    URIPattern uripat = (URIPattern) obj;

	    return this.equals(uripat);
	}
	return false;
    }

    public boolean equals(String protocol, String host, PortPattern ppat,
	    String file) {
	if ((protocol == null) || (this._protocol == null)) {
	    return false;
	}
	if (!protocol.equals(this._protocol)) {
	    return false;
	}
	if (protocol.equals(PROTOCOL_FILE)) {
	    if ((file == null) || (this._file == null)) {
		return false;
	    }
	    if (!file.equals(this._file)) {
		return false;
	    }
	    return true;
	} else {
	    if ((host == null) || (this._host == null)) {
		return false;
	    }
	    if (!host.equals(this._host)) {
		return false;
	    }
	    if ((ppat != null) && !ppat.equals(this._ppat)) {
		return false;
	    }
	    if ((file != null) && !file.equals(this._file)) {
		return false;
	    }
	    return true;
	}
    }

    private String expandPropertyRef(String pat) {
	int idx1 = pat.indexOf("${");

	if (idx1 < 0) {
	    return pat;
	}
	int idx2 = pat.indexOf("}");
	final String propName = pat.substring(idx1 + 2, idx2);
	String propValue = System.getProperty(propName);

	return this.expandPropertyRef(pat.substring(0, idx1) + propValue
		+ pat.substring(idx2 + 1));
    }

    public String getFile() {
	return this._file;
    }

    public String getHost() {
	return this._host;
    }

    public String getPattern() {
	return this._pattern;
    }

    public String getPort() {
	return this._port;
    }

    public PortPattern getPortPattern() {
	return this._ppat;
    }

    public String getProtocol() {
	return this._protocol;
    }

    public boolean isMatch(URL url) {
	if (url == null) {

	    // nowhere
	    return false;
	}

	// check protocol
	final String protocol = url.getProtocol();

	if (!isMatchProtocol(this._protocol, protocol)) {
	    return false;
	}
	final String filename = url.getFile();

	if (protocol.equalsIgnoreCase(PROTOCOL_FILE)) {

	    // check file name
	    final String canonFileA = canonicalFilename(this._file);
	    final String canonFileB = canonicalFilename(filename);

	    if ((canonFileA == null) || (canonFileB == null)) {
		return false;
	    }

	    // return fileA.compareTo(fileB); // JDK 1.2
	    return isMatchFile(canonFileA, canonFileB);
	} else { // ATP or HTTP

	    // check host name
	    final String host = url.getHost();

	    if ((this._host == null) || (host == null)) {
		return false;
	    }
	    if (!isMatchHost(this._host, host)) {
		return false;
	    }

	    // check port number
	    final int port = url.getPort();

	    if (!isMatchPort(this._ppat, port)) {
		return false;
	    }

	    // check file name
	    return isMatchFile(this._file, filename, STRING_SLASH);
	}
    }

    final static boolean isMatchFile(String pattern, String path) {
	return isMatchFile(pattern, path, File.separator);
    }

    final static boolean isMatchFile(String pattern, String path,
	    String separator) {
	if ((pattern == null) || (path == null) || (separator == null)) {
	    return false;
	}
	if (pattern.equals(separator)) {
	    return true;
	}
	final String anyDir = separator + WILDCARD_ANYDIR;

	if (pattern.equals(anyDir)) {
	    return true;
	}
	final String subDir = separator + WILDCARD_SUBDIR;

	if (pattern.endsWith(anyDir)) {
	    final String pat = pattern.substring(0, pattern.length() - 1);
	    String p = path;
	    final int idx = path.lastIndexOf(separator);

	    if (idx >= 0) {
		p = path.substring(0, idx + 1);
	    }
	    return p.startsWith(pat);
	} else if (pattern.endsWith(subDir)) {
	    final String pat = pattern.substring(0, pattern.length() - 1);
	    String p = path;
	    final int idx = path.lastIndexOf(separator);

	    if (idx >= 0) {
		p = path.substring(0, idx + 1);
	    }
	    return p.equals(pat);
	} else {
	    return path.equals(pattern);
	}
    }

    final static private boolean isMatchHost(String pattern, String host) {
	if (pattern.equals(WILDCARD_HOST)) {
	    return true;
	}
	final String anyDir = WILDCARD_HOST + DOMAIN_DELIMITER;

	if (pattern.startsWith(anyDir)) {
	    final String domain = pattern.substring(2);

	    return host.endsWith(domain);
	} else {
	    return host.equalsIgnoreCase(pattern);
	}
    }

    final static boolean isMatchPort(PortPattern pattern, int port) {
	if (pattern == null) {
	    return false;
	}
	return pattern.isMatch(port);
    }

    final static private boolean isMatchProtocol(String pattern, String protocol) {
	if (pattern.equals(WILDCARD_PROTOCOL)) {
	    return true;
	}
	return pattern.equalsIgnoreCase(protocol);
    }

    public static void main(String[] arg) {
	URIPattern uripat = null;
	int i;

	for (i = 0; i < arg.length; i++) {
	    try {
		uripat = new URIPattern(arg[i]);
	    } catch (MalformedURIPatternException excpt) {
		excpt.printStackTrace();
	    }
	    System.out.println(uripat.toString());
	}
    }

    @Override
    public String toString() {
	String hostpart = "";

	// initialize hostpart only if the host is not empty
	// (for the "file" URI scheme the host is parsed as empty)
	if (this._host != "") {
	    hostpart = HOSTNAME_LEADER + this._host;

	    // since there is a host part, add a port, too, if there is one
	    if (this._port != null) {
		hostpart = hostpart + PORT_LEADER + this._port;
	    }
	}

	return this._protocol + PROTOCOL_TERMINATOR + hostpart + this._file;
    }
}
