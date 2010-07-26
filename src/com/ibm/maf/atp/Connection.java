package com.ibm.maf.atp;

/*
 * @(#)Connection.java
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
import java.io.OutputStream;

/**
 * @version 1.00 $Date :$
 * @author Mitsuru Oshima
 */
public interface Connection {

    public void close() throws IOException;

    public String getAuthenticatedSecurityDomain();

    public InputStream getInputStream() throws IOException;

    public String getMessage();

    public OutputStream getOutputStream() throws IOException;

    public boolean isEstablished();

    public void sendRequest() throws IOException;
}
