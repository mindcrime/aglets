package com.ibm.maf;

/*
 * @(#)AgentSystemHandler.java
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

import com.ibm.aglet.Ticket;

public interface AgentSystemHandler {

    public MAFAgentSystem getMAFAgentSystem(Ticket ticket)
    throws java.net.UnknownHostException;

    public MAFAgentSystem getMAFAgentSystem(String address)
    throws java.net.UnknownHostException;

    public void initMAFAgentSystem(MAFAgentSystem local)
    throws MAFExtendedException;

    public void startMAFAgentSystem(MAFAgentSystem local)
    throws MAFExtendedException;
}
