package com.ibm.maf.atp;

/*
 * @(#)HttpCGIResponseImpl.java
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
import java.io.OutputStream;

import com.ibm.atp.AtpConstants;
import com.ibm.atp.ContentOutputStream;

/**
 * @version 1.10 $Date :$
 * @author Mitsuru Oshima
 */
final class HttpCGIResponseImpl implements AtpResponse, AtpConstants {

    private static final String CRLF = "\r\n";

    private OutputStream out = null;
    private ContentOutputStream bout = null;
    private int statusCode = -1;
    private String statusMsg;

    private String content_type = "text/html";

    public HttpCGIResponseImpl(OutputStream out) throws IOException {
	this.out = out;
    }

    public OutputStream getOutputStream() throws IOException {
	if (this.bout == null) {
	    this.bout = new ContentOutputStream(this.out, true);
	}
	return this.bout;
    }

    public int getStatusCode() {
	return this.statusCode;
    }

    public void sendError(int i) throws IOException {
	this.setStatusCode(i);
	this.writeStatusLine();
	String h = "<HTML><BODY>ERROR statusCode = " + i + " : "
		+ this.statusMsg + "</BODY></HTML>";

	int len = h.length();

	String ct = "Content-Length: " + len + CRLF + CRLF;

	for (int j = 0; j < ct.length(); j++) {
	    this.out.write(ct.charAt(j));
	}

	for (int j = 0; j < len; j++) {
	    this.out.write(h.charAt(j));
	}
	this.out.flush();
    }

    public void sendResponse() throws IOException {
	this.writeStatusLine();
	this.writeHeaders();
	if (this.bout != null) {
	    if (this.bout.size() == 0) {
		java.io.PrintWriter p = new java.io.PrintWriter(this.bout);

		p.println("No Response is written by the aglet");
		p.flush();
	    }
	    this.bout.sendContent();
	}
	this.out.flush();
    }

    public void setContentType(String type) {
	return;
    }

    public void setStatusCode(int i) {
	this.statusCode = i;
	switch (this.statusCode) {
	case OKAY:
	    this.statusMsg = "OKAY";
	    break;
	case MOVED:
	    this.statusMsg = "MOVED";
	    break;
	case BAD_REQUEST:
	    this.statusMsg = "BAD REQUEST";
	    break;
	case FORBIDDEN:
	    this.statusMsg = "FORBIDDEN";
	    break;
	case NOT_FOUND:
	    this.statusMsg = "NOT FOUND";
	    break;
	case INTERNAL_ERROR:
	    this.statusMsg = "INTERNAL ERROR";
	    break;
	case NOT_IMPLEMENTED:
	    this.statusMsg = "NOT IMPLEMENTED";
	    break;
	case BAD_GATEWAY:
	    this.statusMsg = "BAD GATEWAY";
	    break;
	case SERVICE_UNAVAILABLE:
	    this.statusMsg = "SERVICE UNAVAILABLE";
	    break;

	}
    }

    public void setStatusCode(int i, String msg) {
	this.statusCode = i;
	this.statusMsg = msg;
    }

    private void writeHeaders() throws IOException {
	String h = "";

	// - if (resultURI != null) {
	// - h = "Location:" + resultURI.toExternalForm() + CRLF;
	// - }
	h += "Content-Type:" + this.content_type + CRLF;

	// - "Content-Language:" + content_language + CRLF +
	// - "Content-Encoding:" + content_encoding + CRLF;
	int len = h.length();

	for (int i = 0; i < len; i++) {
	    this.out.write(h.charAt(i));
	}
    }

    private void writeStatusLine() throws IOException {
	String h = "HTTP/1.0 " + (this.statusCode + 100) + " " + this.statusMsg
		+ CRLF;
	int len = h.length();

	for (int i = 0; i < len; i++) {
	    this.out.write(h.charAt(i));
	}
    }
}
