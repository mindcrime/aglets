package com.ibm.maf;

/*
 * @(#)MAFFinder.java
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
import java.util.Hashtable;

public interface MAFFinder extends java.rmi.Remote {

	public Hashtable list_agent_entries() throws RemoteException;
	public Hashtable list_agent_system_entries() throws RemoteException;
	public Hashtable list_place_entries() throws RemoteException;
	public String[] lookup_agent(Name agent_name, AgentProfile agent_profile) 
			throws EntryNotFound, RemoteException;
	public String[] lookup_agent_system(Name agent_system_name, 
										AgentSystemInfo agent_system_info) throws EntryNotFound, 
										RemoteException;
	public String[] lookup_place(String place_name) 
			throws EntryNotFound, RemoteException;
	public void register_agent(Name agent_name, String agent_location, 
							   AgentProfile agent_profile) throws NameInvalid, 
							   RemoteException;
	public void register_agent_system(Name agent_system_name, 
									  String agent_system_location, 
									  AgentSystemInfo agent_system_info) throws NameInvalid, 
									  RemoteException;
	public void register_place(String place_name, String place_location) 
			throws NameInvalid, RemoteException;
	public void unregister_agent(Name agent_name) 
			throws EntryNotFound, RemoteException;
	public void unregister_agent_system(Name agent_system_name) 
			throws EntryNotFound, RemoteException;
	public void unregister_place(String place_name) 
			throws EntryNotFound, RemoteException;
}
