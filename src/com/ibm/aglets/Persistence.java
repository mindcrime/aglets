package com.ibm.aglets;

/*
 * @(#)Persistence.java
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

import java.util.Enumeration;

public interface Persistence {
	PersistentEntry createEntryWith(String key);

	Enumeration entryKeys();

	/*
	 * OutputStream getOutputStream(String key) throws IOException; InputStream
	 * getInputStream(String key) throws IOException;
	 */
	PersistentEntry getEntry(String key);

	// Enumeration entries();
	void removeEntry(String key);
}
