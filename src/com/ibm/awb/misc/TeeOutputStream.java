package com.ibm.awb.misc;

import java.io.IOException;
import java.io.OutputStream;

/*
 * @(#)TeeOutputStream.java
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

/**
 * TeeOutputStream is java stream variant of "tee" command. It despatch its
 * content to two independent stream objects.
 * 
 * @version 1.00, 04 July 1996
 * @author M.Oshima
 */
public class TeeOutputStream extends OutputStream {

	private final OutputStream out1;
	private final OutputStream out2;

	private IOException exception = null;

	/*
	 * Constructs a TesOuputStream object with two output stream object.
	 */
	public TeeOutputStream(final OutputStream out1, final OutputStream out2) {
		this.out1 = out1;
		this.out2 = out2;
	}

	/**
	 * Closes the stream. This method must be called to release any resources
	 * associated with the stream.
	 * 
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	@Override
	synchronized public void close() throws IOException {
		exception = null;
		try {
			out1.close();
		} catch (final IOException ex) {
			exception = ex;
		}
		try {
			out2.close();
		} catch (final IOException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * Flushes the stream. This will write any buffered output bytes.
	 * 
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	@Override
	synchronized public void flush() throws IOException {
		exception = null;
		try {
			out1.flush();
		} catch (final IOException ex) {
			exception = ex;
		}
		try {
			out2.flush();
		} catch (final IOException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * @param b
	 *            the data to be written
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	@Override
	synchronized public void write(final byte b[]) throws IOException {
		exception = null;
		try {
			out1.write(b);
		} catch (final IOException ex) {
			exception = ex;
		}
		try {
			out2.write(b);
		} catch (final IOException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * @param b
	 *            the data to be written
	 * @param off
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	@Override
	synchronized public void write(final byte b[], final int off, final int len)
	throws IOException {
		exception = null;
		try {
			out1.write(b, off, len);
		} catch (final IOException ex) {
			exception = ex;
		}
		try {
			out2.write(b, off, len);
		} catch (final IOException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw exception;
		}
	}

	/**
	 * @param b
	 *            the byte
	 * @exception IOException
	 *                If an I/O error has occurred.
	 */
	@Override
	synchronized public void write(final int b) throws IOException {
		exception = null;
		try {
			out1.write(b);
		} catch (final IOException ex) {
			exception = ex;
		}
		try {
			out2.write(b);
		} catch (final IOException ex) {
			exception = ex;
		}
		if (exception != null) {
			throw exception;
		}
	}
}
