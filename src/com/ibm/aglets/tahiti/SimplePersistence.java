package com.ibm.aglets.tahiti;

/*
 * @(#)SimplePersistence.java
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

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import com.ibm.aglets.PersistentEntry;
import com.ibm.awb.misc.FileUtils;

/**
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
final class SimplePersistence implements com.ibm.aglets.Persistence {
    String spool_dir;

    SimplePersistence(String str) throws IOException {
	this.spool_dir = str;

	// to make sure that str point to a directory.
	if (this.spool_dir.charAt(this.spool_dir.length() - 1) != File.separatorChar) {
	    this.spool_dir += File.separator;
	}
	final File dir = new File(this.spool_dir);
	boolean exists = false;
	boolean isDir = false;

	try {
	    boolean[] bb = (boolean[]) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    boolean[] result = new boolean[2];

		    result[0] = dir.exists();
		    result[1] = dir.isDirectory();
		    return result;
		}
	    });

	    exists = bb[0];
	    isDir = bb[1];
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	if (exists && (isDir == false)) {
	    throw new IOException(str + "is not a directory.");
	}
	if (exists == false) {
	    if (FileUtils.ensureDirectory(this.spool_dir) == false) {
		System.err.println("Failed to create new spool directory : "
			+ str);
	    }
	}
    }

    @Override
    public PersistentEntry createEntryWith(String key) {
	final File f = new File(this.spool_dir + key);

	try {
	    return (PersistentEntry) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    if (!f.exists() || f.canWrite()) {
			return new SimplePEntry(f);
		    } else {
			return null;
		    }
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    @Override
    public Enumeration entryKeys() {
	return new KeyEnumerator(this.spool_dir);
    }

    @Override
    public PersistentEntry getEntry(String key) {
	final File f = new File(this.spool_dir + key);

	try {
	    return (PersistentEntry) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    if (f.exists() && f.canWrite()) {
			return new SimplePEntry(f);
		    } else {
			return null;
		    }
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    @Override
    public void removeEntry(String key) {
	try {
	    final String fSpoolDir = this.spool_dir;
	    final String fKey = key;

	    AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    String filename = fSpoolDir + fKey;

		    (new File(filename)).delete();
		    return null;
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
