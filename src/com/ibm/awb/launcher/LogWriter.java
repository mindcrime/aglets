package com.ibm.awb.launcher;

/*
 * @(#)LogWriter.java
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
import java.awt.TextArea;

public class LogWriter extends OutputStream {
	private TextArea _log = null;

	public LogWriter(TextArea f) {
		_log = f;
	}
	public void close() throws IOException {

		// _log.dispose();
	}
	public void flush() throws IOException {}
	public void write(byte[] b) throws IOException {
		synchronized (this) {
			_log.append(new String(b));
		} 
	}
	public void write(byte[] b, int off, int len) throws IOException {
		synchronized (this) {
			_log.append(new String(b, off, len));
		} 
	}
	public void write(int c) throws IOException {
		synchronized (this) {
			char[] b = {
				(char)c
			};

			_log.append(new String(b));
		} 
	}
}
