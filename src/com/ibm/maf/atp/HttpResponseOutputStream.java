package com.ibm.maf.atp;

/*
 * @(#)HttpResponseOutputStream.java
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
import java.io.PrintStream;

import com.ibm.atp.ContentBuffer;

/**
 * @version 1.10 96/10/5
 * @author Danny B. Lange
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */

final class HttpResponseOutputStream extends ByteArrayOutputStream implements
	ContentBuffer {

    /**
     * A separator in the message's header.
     */
    public static final String CRLF = "\r\n";

    /**
     * An output stream into which ATP messages is written. An atp output stream
     * writes ATP messages into it.
     */
    private OutputStream _out = null;
    private boolean content_sent = false;

    /**
     * Create a new instance
     * 
     * @param os
     *            an instance of OutputStream into which the instantiated atp
     *            output stream writes.
     */
    public HttpResponseOutputStream(OutputStream out) {
	this._out = out;
    }

    @Override
    public void close() throws IOException {
	this.content_sent = true;
	this.flush();
    }

    /*
     * Flush the stream.
     */
    @Override
    public void flush() throws IOException {
	if (this.content_sent) {
	    this._out.write(this.buf, 0, this.count);
	    this._out.flush();
	    this.reset();
	}
    }

    /*
     * Sends the content
     */
    public void sendContent() throws IOException {
	synchronized (this) {
	    if (this.content_sent) {
		throw new IllegalAccessError("content already sent");
	    }
	    this.content_sent = true;
	}

	PrintStream p = new PrintStream(this._out);

	p.print("HTTP/1.0 200 OKAY" + CRLF);
	p.print("Content-type: application/x-atp" + CRLF);
	p.print("Content-Length:" + this.count + CRLF);
	p.print(CRLF);
	this._out.write(this.buf, 0, this.count);
	this._out.flush();
	this.reset();

	if (this._out instanceof ContentBuffer) {
	    ((ContentBuffer) this._out).sendContent();
	}
    }
}
