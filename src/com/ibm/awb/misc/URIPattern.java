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

	public final static String canonicalFilename(final String filename) {
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
		} catch (final IOException excpt) {
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
	final static boolean isMatchFile(final String pattern, final String path) {
		return isMatchFile(pattern, path, File.separator);
	}
	final static boolean isMatchFile(
	                                 final String pattern,
	                                 final String path,
	                                 final String separator) {
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
	final static private boolean isMatchHost(final String pattern, final String host) {
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
	final static boolean isMatchPort(final PortPattern pattern, final int port) {
		if (pattern == null) {
			return false;
		}
		return pattern.isMatch(port);
	}
	final static private boolean isMatchProtocol(final String pattern, final String protocol) {
		if (pattern.equals(WILDCARD_PROTOCOL)) {
			return true;
		}
		return pattern.equalsIgnoreCase(protocol);
	}

	public static void main(final String[] arg) {
		URIPattern uripat = null;
		int i;

		for (i = 0; i < arg.length; i++) {
			try {
				uripat = new URIPattern(arg[i]);
			} catch (final MalformedURIPatternException excpt) {
				excpt.printStackTrace();
			}
			System.out.println(uripat.toString());
		}
	}

	private String _pattern = null;

	private String _protocol = null;

	private String _host = null;

	private String _port = null;

	private PortPattern _ppat = null;

	private String _file = null;

	public URIPattern(final String pattern) throws MalformedURIPatternException {
		_pattern = expandPropertyRef(pattern);
		final int idxProtocol = pattern.indexOf(PROTOCOL_TERMINATOR);

		if (idxProtocol == -1) {
			throw new MalformedURIPatternException("Protocol does not specified : \""
					+ pattern + "\".");
		}
		_protocol = pattern.substring(0, idxProtocol);
		final String body = pattern.substring(idxProtocol + 1);

		if ("file".equalsIgnoreCase(_protocol)) {
			_host = "";
			_port = null;
			_file = body;
		} else if (body.startsWith(HOSTNAME_LEADER)) {
			final String rest = body.substring(2);
			String hostpart = null;
			final int idxFile = rest.indexOf(FILE_LEADER);

			if (idxFile == -1) {
				hostpart = rest;
				_file = "";
			} else {
				hostpart = rest.substring(0, idxFile);
				_file = rest.substring(idxFile);
			}
			final int idxPort = hostpart.indexOf(PORT_LEADER);

			if (idxPort == -1) {
				_host = hostpart;
				_port = null;
			} else {
				_host = hostpart.substring(0, idxPort);
				_port = hostpart.substring(idxPort + 1);
			}
		} else if (body.startsWith(FILE_LEADER)) {

			// no hostpart specifed
			_host = "";
			_port = null;
			_file = body;
		} else {
			throw new MalformedURIPatternException("Hostname does not specified : \""
					+ pattern + "\".");
		}
		if ((_file == null) || _file.equals("")) {
			_file = FILE_LEADER;
		}
		try {
			_ppat = new PortPattern(_port);
		} catch (final IllegalArgumentException excpt) {
			throw new MalformedURIPatternException(excpt.toString());
		}
	}

	public URIPattern(final URL url) throws MalformedURIPatternException {
		_pattern = url.toString();
		_protocol = url.getProtocol();
		_host = url.getHost();
		final int port = url.getPort();

		_port = String.valueOf(port);
		try {
			_ppat = new PortPattern(port);
		} catch (final IllegalArgumentException excpt) {
			throw new MalformedURIPatternException(excpt.toString());
		}
		_file = url.getFile();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof URIPattern) {
			final URIPattern uripat = (URIPattern) obj;

			return this.equals(uripat);
		}
		return false;
	}

	public boolean equals(
	                      final String protocol,
	                      final String host,
	                      final PortPattern ppat,
	                      final String file) {
		if ((protocol == null) || (_protocol == null)) {
			return false;
		}
		if (!protocol.equals(_protocol)) {
			return false;
		}
		if (protocol.equals(PROTOCOL_FILE)) {
			if ((file == null) || (_file == null)) {
				return false;
			}
			if (!file.equals(_file)) {
				return false;
			}
			return true;
		} else {
			if ((host == null) || (_host == null)) {
				return false;
			}
			if (!host.equals(_host)) {
				return false;
			}
			if ((ppat != null) && !ppat.equals(_ppat)) {
				return false;
			}
			if ((file != null) && !file.equals(_file)) {
				return false;
			}
			return true;
		}
	}

	public boolean equals(final URIPattern uripat) {
		return this.equals(uripat._protocol, uripat._host, uripat._ppat, uripat._file);
	}

	private String expandPropertyRef(final String pat) {
		final int idx1 = pat.indexOf("${");

		if (idx1 < 0) {
			return pat;
		}
		final int idx2 = pat.indexOf("}");
		final String propName = pat.substring(idx1 + 2, idx2);
		final String propValue = System.getProperty(propName);

		return expandPropertyRef(pat.substring(0, idx1) + propValue
				+ pat.substring(idx2 + 1));
	}

	public String getFile() {
		return _file;
	}

	public String getHost() {
		return _host;
	}

	public String getPattern() {
		return _pattern;
	}

	public String getPort() {
		return _port;
	}

	public PortPattern getPortPattern() {
		return _ppat;
	}

	public String getProtocol() {
		return _protocol;
	}

	public boolean isMatch(final URL url) {
		if (url == null) {

			// nowhere
			return false;
		}

		// check protocol
		final String protocol = url.getProtocol();

		if (!isMatchProtocol(_protocol, protocol)) {
			return false;
		}
		final String filename = url.getFile();

		if (protocol.equalsIgnoreCase(PROTOCOL_FILE)) {

			// check file name
			final String canonFileA = canonicalFilename(_file);
			final String canonFileB = canonicalFilename(filename);

			if ((canonFileA == null) || (canonFileB == null)) {
				return false;
			}

			// return fileA.compareTo(fileB); // JDK 1.2
			return isMatchFile(canonFileA, canonFileB);
		} else { // ATP or HTTP

			// check host name
			final String host = url.getHost();

			if ((_host == null) || (host == null)) {
				return false;
			}
			if (!isMatchHost(_host, host)) {
				return false;
			}

			// check port number
			final int port = url.getPort();

			if (!isMatchPort(_ppat, port)) {
				return false;
			}

			// check file name
			return isMatchFile(_file, filename, STRING_SLASH);
		}
	}

	@Override
	public String toString() {
		String hostpart = "";

		// initialize hostpart only if the host is not empty
		// (for the "file" URI scheme the host is parsed as empty)
		if (_host != "") {
			hostpart = HOSTNAME_LEADER + _host;

			// since there is a host part, add a port, too, if there is one
			if (_port != null) {
				hostpart = hostpart + PORT_LEADER + _port;
			}
		}

		return _protocol + PROTOCOL_TERMINATOR + hostpart + _file;
	}
}
