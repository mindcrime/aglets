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
	Resource res = Resource.getResourceFor("atp");

	BUFFSIZE = res.getInteger("atp.buffersize", 2048);
    }

    /**
     * Create a new instance of ContentOutputStream.
     * 
     * @param os
     *            an instance of OutputStream into which the instantiated atp
     *            output stream writes.
     */
    public ContentOutputStream(OutputStream os) {
	this(os, false);
    }

    public ContentOutputStream(OutputStream os, boolean started) {
	super(4096);
	this._out = os;
	this.buffer = new byte[BUFFSIZE];
	this.wrote = 0;
	this.content_started = started;
    }

    /**
     * Close the stream. This automatically flushe if the request has not been
     * sent.
     */
    @Override
    synchronized public void close() throws IOException {
	this.content_sent = true;
	this.flush();
	this._out.close();
    }

    @Override
    public void flush() throws IOException {
	if (this.content_sent) {
	    this._out.write(this.buf, 0, this.count);
	    this._out.flush();
	    this.reset();
	}
    }

    synchronized public void sendContent() throws IOException {
	if (this.content_sent) {
	    throw new IOException("Content has been already sent");
	}
	this.content_sent = true;

	// _out.flush();
	// _out = _tmp;

	String cl = CRLF;

	if (this.count != 0) {
	    cl = "Content-Length:" + String.valueOf(this.count) + CRLF + CRLF;
	} else {

	    // length unknown
	}
	byte ab[] = cl.getBytes();

	if (this.wrote + ab.length < BUFFSIZE) {

	    // store
	    System.arraycopy(ab, 0, this.buffer, this.wrote, ab.length);
	    this.wrote += ab.length;
	} else {

	    // flush()
	    this._out.write(this.buffer, 0, this.wrote);
	    this.wrote = 0;

	    // write
	    this._out.write(ab, 0, ab.length);
	}

	if (this.wrote + this.count < BUFFSIZE) {
	    System.arraycopy(this.buf, 0, this.buffer, this.wrote, this.count);
	    this.wrote += this.count;
	    this._out.write(this.buffer, 0, this.wrote);
	} else {
	    this._out.write(this.buffer, 0, this.wrote);
	    this._out.write(this.buf, 0, this.count);
	}
	this.wrote = 0;

	this._out.flush();
	this.reset();

	if (this._out instanceof ContentBuffer) {
	    ((ContentBuffer) this._out).sendContent();
	}
    }

    synchronized public void startContent() throws IOException {
	if (this.content_started) {
	    throw new IOException("Content has been already started");
	}
	this.content_started = true;

	if (this.count < BUFFSIZE) {

	    // store
	    System.arraycopy(this.buf, 0, this.buffer, 0, this.count);
	    this.wrote = this.count;
	} else {
	    this._out.write(this.buf, 0, this.count);
	    this.wrote = 0;
	}

	this.reset();
    }
}
