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
    JarArchive _jar = null;

    /*
     * synchronized public boolean match(DigestTable table) { return
     * _jar.getDigestTable().match(table, false); }
     * 
     * synchronized public boolean matchAndImport(DigestTable table) { return
     * _digest_table.match(table, false); }
     */
    JarAgletClassLoader(String name, Certificate cert)
    throws java.io.IOException {
	this(new URL(name), cert);
    }

    JarAgletClassLoader(URL codebase, Certificate cert)
    throws java.io.IOException {
	super(checkAndTrim(codebase), cert);
	this._jar = new com.ibm.awb.misc.JarArchive(codebase.openStream());

	// _digest_table = _jar.getDigestTable();
	Archive.Entry ae[] = this._jar.entries();

	this._digest_table = new DigestTable(ae.length);
	for (Entry element : ae) {
	    this._digest_table.setDigest(element.name(), element.digest());
	}
    }

    private static URL checkAndTrim(URL codeBase) throws java.io.IOException {
	String f = codeBase.getFile();

	if ((f != null) && f.toLowerCase().endsWith(".jar")) {
	    System.out.println(f);
	    f = f.substring(0, f.lastIndexOf('/') + 1);
	    System.out.println(f);
	    return new URL(codeBase, f);
	}
	return codeBase;
    }

    @Override
    public Archive getArchive(ClassName[] t) {
	if (this.match(t)) {
	    return this._jar;
	} else {
	    return null;
	}
    }

    @Override
    synchronized protected byte[] getResourceAsByteArray(String filename) {
	return this._jar.getResourceAsByteArray(filename);
    }

    /* overritten */
    @Override
    public void importArchive(Archive a) {
	Archive.Entry ae[] = a.entries();

	for (Entry element : ae) {
	    long digest = this._digest_table.getDigest(element.name());

	    if (digest == 0) {
		throw new RuntimeException("Cannot Add JarArchive!");
	    }
	}
    }

    static boolean isJarFile(URL codebase) {
	String f = codebase.getFile();

	return (f != null) && f.toLowerCase().endsWith(".jar");
    }
}
