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

import java.net.URL;
import java.security.cert.Certificate;

import com.ibm.awb.misc.Archive;
import com.ibm.awb.misc.Archive.Entry;
import com.ibm.awb.misc.JarArchive;
import com.ibm.maf.ClassName;

class JarAgletClassLoader extends AgletClassLoader {
	private static URL checkAndTrim(final URL codeBase) throws java.io.IOException {
		String f = codeBase.getFile();

		if ((f != null) && f.toLowerCase().endsWith(".jar")) {
			System.out.println(f);
			f = f.substring(0, f.lastIndexOf('/') + 1);
			System.out.println(f);
			return new URL(codeBase, f);
		}
		return codeBase;
	}

	static boolean isJarFile(final URL codebase) {
		final String f = codebase.getFile();

		return (f != null) && f.toLowerCase().endsWith(".jar");
	}

	JarArchive _jar = null;

	/*
	 * synchronized public boolean match(DigestTable table) { return
	 * _jar.getDigestTable().match(table, false); }
	 * 
	 * synchronized public boolean matchAndImport(DigestTable table) { return
	 * _digest_table.match(table, false); }
	 */
	JarAgletClassLoader(final String name, final Certificate cert)
	throws java.io.IOException {
		this(new URL(name), cert);
	}

	JarAgletClassLoader(final URL codebase, final Certificate cert)
	throws java.io.IOException {
		super(checkAndTrim(codebase), cert);
		_jar = new com.ibm.awb.misc.JarArchive(codebase.openStream());

		// _digest_table = _jar.getDigestTable();
		final Archive.Entry ae[] = _jar.entries();

		_digest_table = new DigestTable(ae.length);
		for (final Entry element : ae) {
			_digest_table.setDigest(element.name(), element.digest());
		}
	}

	@Override
	public Archive getArchive(final ClassName[] t) {
		if (match(t)) {
			return _jar;
		} else {
			return null;
		}
	}

	@Override
	synchronized protected byte[] getResourceAsByteArray(final String filename) {
		return _jar.getResourceAsByteArray(filename);
	}

	/* overritten */
	@Override
	public void importArchive(final Archive a) {
		final Archive.Entry ae[] = a.entries();

		for (final Entry element : ae) {
			final long digest = _digest_table.getDigest(element.name());

			if (digest == 0) {
				throw new RuntimeException("Cannot Add JarArchive!");
			}
		}
	}
}
