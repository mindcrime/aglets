package com.ibm.maf.atp;

/*
 * @(#)AtpResponse.java
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

interface AtpResponse {

	public OutputStream getOutputStream() throws IOException;

	public int getStatusCode();

	public void sendError(int i) throws IOException;

	public void sendResponse() throws IOException;

	public void setContentType(String string);

	// public void setContentLanguage(String lang);
	// public void setContentEncoding(String lang);
	// public void setAgentName(Name name);
	// public void setAgentProfile(AgentProfile profile);

	public void setStatusCode(int i);

	public void setStatusCode(int i, String msg);
}
