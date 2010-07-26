package com.ibm.maf.atp;

/*
 * @(#)AtpResponseImpl.java
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
public final class AtpResponseImpl implements AtpResponse {

    private static final String CRLF = "\r\n";

    private OutputStream out = null;
    private ContentOutputStream bout = null;

    private int statusCode = -1;
    private String statusMsg;

    private String content_type = "";
    private String content_language = "";
    private String content_encoding = "";

    public AtpResponseImpl(OutputStream out) throws IOException {
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
	for (int j = 0; j < CRLF.length(); j++) {
	    this.out.write(CRLF.charAt(j));
	}

	// out.flush();
	if (this.bout != null) {
	    this.bout.sendContent();
	}
	this.out.flush();
	this.out.close();
    }

    public void sendResponse() throws IOException {
	this.writeStatusLine();
	this.writeHeaders();
	if (this.bout != null) {
	    this.bout.sendContent();
	}
	this.out.flush();
    }

    public void setContentType(String type) {
	this.content_type = type;
    }

    public void setStatusCode(int i) {
	this.statusCode = i;
	switch (this.statusCode) {
	case AtpConstants.OKAY:
	    this.statusMsg = "OKAY";
	    break;
	case AtpConstants.MOVED:
	    this.statusMsg = "MOVED";
	    break;
	case AtpConstants.BAD_REQUEST:
	    this.statusMsg = "BAD REQUEST";
	    break;
	case AtpConstants.FORBIDDEN:
	    this.statusMsg = "FORBIDDEN";
	    break;
	case AtpConstants.NOT_FOUND:
	    this.statusMsg = "NOT FOUND";
	    break;
	case AtpConstants.INTERNAL_ERROR:
	    this.statusMsg = "INTERNAL ERROR";
	    break;
	case AtpConstants.NOT_IMPLEMENTED:
	    this.statusMsg = "NOT IMPLEMENTED";
	    break;
	case AtpConstants.BAD_GATEWAY:
	    this.statusMsg = "BAD GATEWAY";
	    break;
	case AtpConstants.SERVICE_UNAVAILABLE:
	    this.statusMsg = "SERVICE UNAVAILABLE";
	    break;
	case AtpConstants.NOT_AUTHENTICATED:
	    this.statusMsg = "NOT AUTHENTICATED";
	    break;
	}
    }

    public void setStatusCode(int i, String msg) {
	this.statusCode = i;
	this.statusMsg = msg;
    }

    private void writeHeaders() throws IOException {
	String h = "";

	/*
	 * if (resultURI != null) { h = "Location:" + resultURI.toExternalForm()
	 * + CRLF; }
	 */
	h += "Content-Type:" + this.content_type + CRLF + "Content-Language:"
		+ this.content_language + CRLF + "Content-Encoding:"
		+ this.content_encoding + CRLF;

	int len = h.length();

	for (int i = 0; i < len; i++) {
	    this.out.write(h.charAt(i));
	}
    }

    private void writeStatusLine() throws IOException {
	String h = AtpRequestImpl.ATP_VERSION + " " + this.statusCode + " "
		+ this.statusMsg + CRLF;
	int len = h.length();

	for (int i = 0; i < len; i++) {
	    this.out.write(h.charAt(i));
	}
    }
}
