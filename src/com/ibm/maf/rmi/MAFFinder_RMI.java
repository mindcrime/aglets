package com.ibm.maf.rmi;

/*
 * @(#)MAFFinder_RMI.java
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

/*
 * File: ./CfMAF/MAFFinder.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

import java.rmi.RemoteException;

import com.ibm.maf.AgentProfile;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.EntryNotFound;
import com.ibm.maf.Name;
import com.ibm.maf.NameInvalid;

public interface MAFFinder_RMI extends java.rmi.Remote {

	String[] lookup_agent(Name agent_name, AgentProfile agent_profile)
	throws RemoteException,
	EntryNotFound;

	String[] lookup_agent_system(
	                             Name agent_system_name,
	                             AgentSystemInfo agent_system_info)
	throws RemoteException,
	EntryNotFound;

	String[] lookup_place(String place_name)
	throws RemoteException,
	EntryNotFound;

	void register_agent(
	                    Name agent_name,
	                    String agent_location,
	                    AgentProfile agent_profile)
	throws RemoteException,
	NameInvalid;

	void register_agent_system(
	                           Name agent_system_name,
	                           String agent_system_location,
	                           AgentSystemInfo agent_system_info)
	throws RemoteException,
	NameInvalid;

	void register_place(String place_name, String place_location)
	throws RemoteException,
	NameInvalid;

	void unregister_agent(Name agent_name)
	throws RemoteException,
	EntryNotFound;

	void unregister_agent_system(Name agent_system_name)
	throws RemoteException,
	EntryNotFound;

	void unregister_place(Name place_name)
	throws RemoteException,
	EntryNotFound;
}
