package com.ibm.aglets;

/*
 * @(#)PersistentEntry.java
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PersistentEntry {
	InputStream getInputStream() throws IOException;

	OutputStream getOutputStream() throws IOException;
}
