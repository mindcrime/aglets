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

import com.ibm.aglets.PersistentEntry;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.awb.misc.FileUtils;


class KeyEnumerator implements Enumeration {
	int i = 0;
	String filelist[] = null;
	String spool_dir = null;

	KeyEnumerator(String dir) {
		spool_dir = dir;
		filelist = null;
		try {
			final String fSpoolDir = spool_dir;

			filelist = 
				(String[])AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return new File(fSpoolDir).list();
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 

		// to make sure that str point to a directory.
		if (spool_dir.charAt(spool_dir.length() - 1) != File.separatorChar) {
			spool_dir += File.separator;
		} 
	}
	public boolean hasMoreElements() {
		if (filelist == null) {
			return false;
		} 
		try {
			final KeyEnumerator fThis = this;
			final String fSpoolDir = spool_dir;
			final String[] fFilelist = filelist;

			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					while (fFilelist.length > fThis.i 
						   && new File(fSpoolDir + fFilelist[i]).isFile() 
							  == false) {
						fThis.i++;
					} 
					return null;
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
		return filelist.length > i;
	}
	public Object nextElement() {
		return ((filelist == null) ? null : filelist[i++]);
	}
}
