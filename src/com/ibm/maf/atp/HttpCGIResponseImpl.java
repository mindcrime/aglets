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

import com.ibm.atp.AtpConstants;
import com.ibm.atp.ContentOutputStream;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.net.URL;

import java.util.Hashtable;

/**
 * @version     1.10	$Date :$
 * @author	Mitsuru Oshima
 */
final class HttpCGIResponseImpl implements AtpResponse, AtpConstants {

	private static final String CRLF = "\r\n";

	private OutputStream out = null;
	private ContentOutputStream bout = null;
	private boolean request_sent = false;

	private int statusCode = -1;
	private String statusMsg;

	private String content_type = "text/html";

	public HttpCGIResponseImpl(OutputStream out) throws IOException {
		this.out = out;
	}
	public OutputStream getOutputStream() throws IOException {
		if (bout == null) {
			bout = new ContentOutputStream(out, true);
		} 
		return bout;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void sendError(int i) throws IOException {
		setStatusCode(i);
		writeStatusLine();
		String h = "<HTML><BODY>ERROR statusCode = " + i + " : " + statusMsg 
				   + "</BODY></HTML>";

		int len = h.length();

		String ct = "Content-Length: " + len + CRLF + CRLF;

		for (int j = 0; j < ct.length(); j++) {
			out.write(ct.charAt(j));
		} 

		for (int j = 0; j < len; j++) {
			out.write(h.charAt(j));
		} 
		out.flush();
	}
	public void sendResponse() throws IOException {
		writeStatusLine();
		writeHeaders();
		if (bout != null) {
			if (bout.size() == 0) {
				java.io.PrintWriter p = new java.io.PrintWriter(bout);

				p.println("No Response is written by the aglet");
				p.flush();
			} 
			bout.sendContent();
		} 
		out.flush();
	}
	public void setContentType(String type) {
		return;
	}
	public void setStatusCode(int i) {
		statusCode = i;
		switch (statusCode) {
		case OKAY:
			statusMsg = "OKAY";
			break;
		case MOVED:
			statusMsg = "MOVED";
			break;
		case BAD_REQUEST:
			statusMsg = "BAD REQUEST";
			break;
		case FORBIDDEN:
			statusMsg = "FORBIDDEN";
			break;
		case NOT_FOUND:
			statusMsg = "NOT FOUND";
			break;
		case INTERNAL_ERROR:
			statusMsg = "INTERNAL ERROR";
			break;
		case NOT_IMPLEMENTED:
			statusMsg = "NOT IMPLEMENTED";
			break;
		case BAD_GATEWAY:
			statusMsg = "BAD GATEWAY";
			break;
		case SERVICE_UNAVAILABLE:
			statusMsg = "SERVICE UNAVAILABLE";
			break;

		}
	}
	public void setStatusCode(int i, String msg) {
		statusCode = i;
		statusMsg = msg;
	}
	private void writeHeaders() throws IOException {
		String h = "";

		// - 	if (resultURI != null) {
		// - 	    h = "Location:" + resultURI.toExternalForm() + CRLF;
		// - 	}
		h += "Content-Type:" + content_type + CRLF;

		// - 	     "Content-Language:" + content_language + CRLF +
		// - 	     "Content-Encoding:" + content_encoding + CRLF;
		int len = h.length();

		for (int i = 0; i < len; i++) {
			out.write(h.charAt(i));
		} 
	}
	private void writeStatusLine() throws IOException {
		String h = "HTTP/1.0 " + (statusCode + 100) + " " + statusMsg + CRLF;
		int len = h.length();

		for (int i = 0; i < len; i++) {
			out.write(h.charAt(i));
		} 
	}
}
