package com.ibm.maf.rmi;

/*
 * @(#)MAFAgentSystem_RMI.java
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

import java.rmi.RemoteException;

import com.ibm.maf.AgentProfile;
import com.ibm.maf.AgentStatus;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.AuthInfo;
import com.ibm.maf.ClassName;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.Name;

public interface MAFAgentSystem_RMI extends java.rmi.Remote {

    Name create_agent(
                      Name agent_name,
                      AgentProfile agent_profile,
                      byte[] agent,
                      String place_name,
                      Object[] arguments,
                      ClassName[] class_names,
                      String code_base,
                      MAFAgentSystem_RMI class_provider) throws RemoteException;

    byte[][] fetch_class(
                         ClassName[] class_name_list,
                         String code_base,
                         AgentProfile agent_profile) throws RemoteException;

    String find_nearby_agent_system_of_profile(AgentProfile profile)
    throws RemoteException;

    AgentStatus get_agent_status(Name agent_name) throws RemoteException;

    AgentSystemInfo get_agent_system_info() throws RemoteException;

    AuthInfo get_authinfo(Name agent_name) throws RemoteException;

    MAFFinder get_MAFFinder() throws RemoteException;

    Name[] list_all_agents() throws RemoteException;

    Name[] list_all_agents_of_authority(byte[] authority)
    throws RemoteException;

    String[] list_all_places() throws RemoteException;

    void receive_agent(
                       Name agent_name,
                       AgentProfile agent_profile,
                       byte[] agent,
                       String place_name,
                       ClassName[] class_names,
                       String code_base,
                       MAFAgentSystem_RMI class_sender) throws RemoteException;

    public long receive_future_message(
                                       Name agent_name,
                                       byte[] msg,
                                       MAFAgentSystem_RMI message_sender)
    throws RemoteException;

    public void receive_future_reply(long return_id, byte[] reply)
    throws RemoteException;

    /**
     * Messaging
     */
    public byte[] receive_message(Name agent_name, byte[] msg)
    throws RemoteException;

    public void receive_oneway_message(Name agent_name, byte[] msg)
    throws RemoteException;

    void resume_agent(Name agent_name) throws RemoteException;

    /**
     * Aglet Specific
     */
    byte[] retract_agent(Name agent_name) throws RemoteException;

    void suspend_agent(Name agent_name) throws RemoteException;

    void terminate_agent(Name agent_name) throws RemoteException;
}
