package com.ibm.awb.misc;

import java.io.OutputStream;
import java.io.IOException;

/*
 * @(#)NullOutputStream.java
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
 * NullOutputStream is a stream which outputs nothing.
 * 
 * @version 	1.00, 04 July 1996
 * @author	M.Oshima
 */
public class NullOutputStream extends OutputStream {

	public NullOutputStream() {}
	/**
	 * Closes the stream. This method must be called
	 * to release any resources associated with the
	 * stream.
	 * @exception IOException If an I/O error has occurred.
	 */
	public void close() throws IOException {}
	/**
	 * Flushes the stream. This will write any buffered
	 * output bytes.
	 * @exception IOException If an I/O error has occurred.
	 */
	public void flush() throws IOException {}
	/**
	 * @param b	the data to be written
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(byte b[]) throws IOException {}
	/**
	 * @param b	the data to be written
	 * @param off	the start offset in the data
	 * @param len	the number of bytes that are written
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(byte b[], int off, int len) throws IOException {}
	/**
	 * @param b	the byte
	 * @exception IOException If an I/O error has occurred.
	 */
	public void write(int b) throws IOException {}
}
