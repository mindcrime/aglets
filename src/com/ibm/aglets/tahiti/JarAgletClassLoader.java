package com.ibm.aglets.tahiti;

/*
 * @(#)JarAgletClassLoader.java
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

import java.io.*;
import java.net.*;
import java.security.Identity;
import java.security.cert.Certificate;
import com.ibm.maf.ClassName;
import com.ibm.awb.misc.*;

class JarAgletClassLoader extends AgletClassLoader {
	JarArchive _jar = null;

	/*
	 * synchronized public boolean match(DigestTable table) {
	 * return _jar.getDigestTable().match(table, false);
	 * }
	 * 
	 * synchronized public boolean matchAndImport(DigestTable table) {
	 * return _digest_table.match(table, false);
	 * }
	 */
	JarAgletClassLoader(String name, 
						Certificate cert) throws java.io.IOException {
		this(new URL(name), cert);
	}
	JarAgletClassLoader(URL codebase, 
						Certificate cert) throws java.io.IOException {
		super(checkAndTrim(codebase), cert);
		_jar = new com.ibm.awb.misc.JarArchive(codebase.openStream());

		// _digest_table = _jar.getDigestTable();
		Archive.Entry ae[] = _jar.entries();

		_digest_table = new DigestTable(ae.length);
		for (int i = 0; i < ae.length; i++) {
			_digest_table.setDigest(ae[i].name(), ae[i].digest());
		} 
	}
	private static URL checkAndTrim(URL codeBase) throws java.io.IOException {
		String f = codeBase.getFile();

		if (f != null && f.toLowerCase().endsWith(".jar")) {
			System.out.println(f);
			f = f.substring(0, f.lastIndexOf('/') + 1);
			System.out.println(f);
			return new URL(codeBase, f);
		} 
		return codeBase;
	}
	public Archive getArchive(ClassName[] t) {
		if (match(t)) {
			return _jar;
		} else {
			return null;
		} 
	}
	synchronized protected byte[] getResourceAsByteArray(String filename) {
		return _jar.getResourceAsByteArray(filename);
	}
	/* overritten */
	public void importArchive(Archive a) {
		Archive.Entry ae[] = a.entries();

		for (int i = 0; i < ae.length; i++) {
			long digest = _digest_table.getDigest(ae[i].name());

			if (digest == 0) {
				throw new RuntimeException("Cannot Add JarArchive!");
			} 
		} 
	}
	static boolean isJarFile(URL codebase) {
		String f = codebase.getFile();

		return f != null && f.toLowerCase().endsWith(".jar");
	}
}
