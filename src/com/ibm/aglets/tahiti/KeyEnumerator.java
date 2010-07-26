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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;

class KeyEnumerator implements Enumeration {
    int i = 0;
    String filelist[] = null;
    String spool_dir = null;

    KeyEnumerator(String dir) {
	this.spool_dir = dir;
	this.filelist = null;
	try {
	    final String fSpoolDir = this.spool_dir;

	    this.filelist = (String[]) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return new File(fSpoolDir).list();
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	// to make sure that str point to a directory.
	if (this.spool_dir.charAt(this.spool_dir.length() - 1) != File.separatorChar) {
	    this.spool_dir += File.separator;
	}
    }

    public boolean hasMoreElements() {
	if (this.filelist == null) {
	    return false;
	}
	try {
	    final KeyEnumerator fThis = this;
	    final String fSpoolDir = this.spool_dir;
	    final String[] fFilelist = this.filelist;

	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    while ((fFilelist.length > fThis.i)
			    && (new File(fSpoolDir
				    + fFilelist[KeyEnumerator.this.i]).isFile() == false)) {
			fThis.i++;
		    }
		    return null;
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return this.filelist.length > this.i;
    }

    public Object nextElement() {
	return ((this.filelist == null) ? null : this.filelist[this.i++]);
    }
}
