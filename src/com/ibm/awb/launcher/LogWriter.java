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

import java.awt.TextArea;
import java.io.IOException;
import java.io.OutputStream;

public class LogWriter extends OutputStream {
	private TextArea _log = null;

	public LogWriter(final TextArea f) {
		_log = f;
	}

	@Override
	public void close() throws IOException {

		// _log.dispose();
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(final byte[] b) throws IOException {
		synchronized (this) {
			_log.append(new String(b));
		}
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		synchronized (this) {
			_log.append(new String(b, off, len));
		}
	}

	@Override
	public void write(final int c) throws IOException {
		synchronized (this) {
			final char[] b = { (char) c };

			_log.append(new String(b));
		}
	}
}
