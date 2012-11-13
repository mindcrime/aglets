package com.ibm.atp;

/*
 * @(#)ContentOutputStream.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.ibm.awb.misc.Resource;

/**
 * The content output stream writes a message content to the given output
 * stream. The content output stream creats MIME header fields, combines it with
 * a message body, and writes it into the specifid output stream.
 * 
 * @version 1.20 3 Mar 1997
 * @author Danny B. Lange
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */
final public class ContentOutputStream extends ByteArrayOutputStream implements
ContentBuffer {

	/**
	 * A separator in the message's header.
	 */
	public static final String CRLF = "\r\n";

	/**
	 * An output stream into which ATP messages is written. An atp output stream
	 * writes ATP messages into it.
	 */
	protected OutputStream _out = null;
	private boolean content_started = false;
	private boolean content_sent = false;

	private static int BUFFSIZE = 2048;

	int wrote = 0;
	byte buffer[];

	static {
		final Resource res = Resource.getResourceFor("atp");

		BUFFSIZE = res.getInteger("atp.buffersize", 2048);
	}

	/**
	 * Create a new instance of ContentOutputStream.
	 * 
	 * @param os
	 *            an instance of OutputStream into which the instantiated atp
	 *            output stream writes.
	 */
	public ContentOutputStream(final OutputStream os) {
		this(os, false);
	}

	public ContentOutputStream(final OutputStream os, final boolean started) {
		super(4096);
		_out = os;
		buffer = new byte[BUFFSIZE];
		wrote = 0;
		content_started = started;
	}

	/**
	 * Close the stream. This automatically flushe if the request has not been
	 * sent.
	 */
	@Override
	synchronized public void close() throws IOException {
		content_sent = true;
		flush();
		_out.close();
	}

	@Override
	public void flush() throws IOException {
		if (content_sent) {
			_out.write(buf, 0, count);
			_out.flush();
			reset();
		}
	}

	@Override
	synchronized public void sendContent() throws IOException {
		if (content_sent) {
			throw new IOException("Content has been already sent");
		}
		content_sent = true;

		// _out.flush();
		// _out = _tmp;

		String cl = CRLF;

		if (count != 0) {
			cl = "Content-Length:" + String.valueOf(count) + CRLF + CRLF;
		} else {

			// length unknown
		}
		final byte ab[] = cl.getBytes();

		if (wrote + ab.length < BUFFSIZE) {

			// store
			System.arraycopy(ab, 0, buffer, wrote, ab.length);
			wrote += ab.length;
		} else {

			// flush()
			_out.write(buffer, 0, wrote);
			wrote = 0;

			// write
			_out.write(ab, 0, ab.length);
		}

		if (wrote + count < BUFFSIZE) {
			System.arraycopy(buf, 0, buffer, wrote, count);
			wrote += count;
			_out.write(buffer, 0, wrote);
		} else {
			_out.write(buffer, 0, wrote);
			_out.write(buf, 0, count);
		}
		wrote = 0;

		_out.flush();
		reset();

		if (_out instanceof ContentBuffer) {
			((ContentBuffer) _out).sendContent();
		}
	}

	synchronized public void startContent() throws IOException {
		if (content_started) {
			throw new IOException("Content has been already started");
		}
		content_started = true;

		if (count < BUFFSIZE) {

			// store
			System.arraycopy(buf, 0, buffer, 0, count);
			wrote = count;
		} else {
			_out.write(buf, 0, count);
			wrote = 0;
		}

		reset();
	}
}
