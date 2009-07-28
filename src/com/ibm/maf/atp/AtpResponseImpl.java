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
public final class AtpResponseImpl implements AtpResponse {

	private static final String CRLF = "\r\n";

	private OutputStream out = null;
	private ContentOutputStream bout = null;

	// private URL resultURI = null;
	private boolean request_sent = false;

	private int statusCode = -1;
	private String statusMsg;

	private String content_type = "";
	private String content_language = "";
	private String content_encoding = "";

	public AtpResponseImpl(OutputStream out) throws IOException {
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
		for (int j = 0; j < CRLF.length(); j++) {
			out.write(CRLF.charAt(j));
		} 

		// out.flush();
		if (bout != null) {
			bout.sendContent();
		} 
		out.flush();
		out.close();
	}
	public void sendResponse() throws IOException {
		writeStatusLine();
		writeHeaders();
		if (bout != null) {
			bout.sendContent();
		} 
		out.flush();
	}
	public void setContentType(String type) {
		content_type = type;
	}
	public void setStatusCode(int i) {
		statusCode = i;
		switch (statusCode) {
		case AtpConstants.OKAY:
			statusMsg = "OKAY";
			break;
		case AtpConstants.MOVED:
			statusMsg = "MOVED";
			break;
		case AtpConstants.BAD_REQUEST:
			statusMsg = "BAD REQUEST";
			break;
		case AtpConstants.FORBIDDEN:
			statusMsg = "FORBIDDEN";
			break;
		case AtpConstants.NOT_FOUND:
			statusMsg = "NOT FOUND";
			break;
		case AtpConstants.INTERNAL_ERROR:
			statusMsg = "INTERNAL ERROR";
			break;
		case AtpConstants.NOT_IMPLEMENTED:
			statusMsg = "NOT IMPLEMENTED";
			break;
		case AtpConstants.BAD_GATEWAY:
			statusMsg = "BAD GATEWAY";
			break;
		case AtpConstants.SERVICE_UNAVAILABLE:
			statusMsg = "SERVICE UNAVAILABLE";
			break;
		case AtpConstants.NOT_AUTHENTICATED:
			statusMsg = "NOT AUTHENTICATED";
			break;
		}
	}
	public void setStatusCode(int i, String msg) {
		statusCode = i;
		statusMsg = msg;
	}
	private void writeHeaders() throws IOException {
		String h = "";

		/*
		 * if (resultURI != null) {
		 * h = "Location:" + resultURI.toExternalForm() + CRLF;
		 * }
		 */
		h += "Content-Type:" + content_type + CRLF + "Content-Language:" 
			 + content_language + CRLF + "Content-Encoding:" 
			 + content_encoding + CRLF;

		int len = h.length();

		for (int i = 0; i < len; i++) {
			out.write(h.charAt(i));
		} 
	}
	private void writeStatusLine() throws IOException {
		String h = AtpRequestImpl.ATP_VERSION + " " + statusCode + " " 
				   + statusMsg + CRLF;
		int len = h.length();

		for (int i = 0; i < len; i++) {
			out.write(h.charAt(i));
		} 
	}
}
