package com.ibm.aglets.tahiti;

/*
 * @(#)SimplePEntry.java
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

import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * @version     1.10	$Date: 2009/07/28 07:04:53 $
 * @author      Mitsuru Oshima
 */
final class SimplePEntry implements com.ibm.aglets.PersistentEntry {
	File file;

	SimplePEntry(File f) {
		file = f;
	}
	public InputStream getInputStream() throws FileNotFoundException {
		try {
			final File f = file;

			return (InputStream)AccessController
				.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws FileNotFoundException {
					return new FileInputStream(f);
				} 
			});
		} catch (PrivilegedActionException ex) {
			throw (FileNotFoundException)ex.getException();
		} 
	}
	public OutputStream getOutputStream() throws IOException {
		try {
			final File f = file;

			return (OutputStream)AccessController
				.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws IOException {
					if (!f.exists()) {
						File dir = f.getCanonicalFile().getParentFile();

						dir.mkdirs();
						f.createNewFile();
					} 
					return new FileOutputStream(f);
				} 
			});
		} catch (PrivilegedActionException ex) {
			throw (IOException)ex.getException();
		} 
	}
}
