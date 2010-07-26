package com.ibm.maf.atp;

/*
 * @(#)AtpRequest.java
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
import java.io.InputStream;

import com.ibm.maf.AgentProfile;
import com.ibm.maf.Name;

interface AtpRequest {

    public Name getAgentName();

    public String getAgentNameAsString();

    public AgentProfile getAgentProfile();

    public int getContentLength();

    public String getFetchClassFile();

    public InputStream getInputStream();

    public int getMethod();

    public String getPlaceName();

    public String getRequestLine();

    public String getRequestParameter(String key);

    // public String getAgentLanguage();

    public String getSender();

    public void parseHeaders() throws IOException;
}
