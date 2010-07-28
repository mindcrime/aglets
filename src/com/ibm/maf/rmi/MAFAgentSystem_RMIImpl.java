package com.ibm.maf.rmi;

/*
 * @(#)MAFAgentSystem_RMIImpl.java
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
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import com.ibm.maf.AgentIsRunning;
import com.ibm.maf.AgentIsSuspended;
import com.ibm.maf.AgentNotFound;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.AgentStatus;
import com.ibm.maf.AgentSystemInfo;
import com.ibm.maf.ArgumentInvalid;
import com.ibm.maf.AuthInfo;
import com.ibm.maf.ClassName;
import com.ibm.maf.ClassUnknown;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.EntryNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MessageEx;
import com.ibm.maf.Name;
import com.ibm.maf.NotHandled;
import com.ibm.maf.ResumeFailed;
import com.ibm.maf.SuspendFailed;
import com.ibm.maf.TerminateFailed;

public class MAFAgentSystem_RMIImpl extends UnicastRemoteObject implements
	MAFAgentSystem_RMI {

    MAFAgentSystem maf = null;

    static Hashtable to_rmi = new Hashtable();

    static Hashtable locals = new Hashtable();

    public MAFAgentSystem_RMIImpl(MAFAgentSystem __maf) throws RemoteException {
	this.maf = __maf;
	to_rmi.put(__maf, this);

	try {
	    java.net.URL u = new java.net.URL(__maf.getAddress());
	    String addr = u.getHost() + ":"
		    + (u.getPort() == -1 ? 1099 : u.getPort());

	    locals.put(addr, __maf);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public Name create_agent(
			     Name agent_name,
			     AgentProfile agent_profile,
			     byte[] agent,
			     String place_name,
			     Object[] arguments,
			     ClassName[] class_names,
			     String code_base,
			     MAFAgentSystem_RMI rmi_class_provider)
								   throws RemoteException {
	try {
	    MAFAgentSystem class_provider = to_maf_agentsystem(rmi_class_provider);

	    return this.maf.create_agent(agent_name, agent_profile, agent, place_name, arguments, class_names, code_base, class_provider);
	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (ArgumentInvalid ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public byte[][] fetch_class(
				ClassName[] class_name_list,
				String code_base,
				AgentProfile agent_profile)
							   throws RemoteException {
	try {
	    return this.maf.fetch_class(class_name_list, code_base, agent_profile);
	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public String find_nearby_agent_system_of_profile(AgentProfile profile)
									   throws RemoteException {
	try {
	    return this.maf.find_nearby_agent_system_of_profile(profile);
	} catch (EntryNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    static MAFAgentSystem_RMI find_rmi_agentsystem(MAFAgentSystem __maf) {
	if (__maf == null) {
	    return null;
	}
	return (MAFAgentSystem_RMI) to_rmi.get(__maf);
    }

    public AgentStatus get_agent_status(Name agent_name) throws RemoteException {
	try {
	    return this.maf.get_agent_status(agent_name);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public AgentSystemInfo get_agent_system_info() throws RemoteException {
	return this.maf.get_agent_system_info();
    }

    public AuthInfo get_authinfo(Name agent_name) throws RemoteException {
	try {
	    return this.maf.get_authinfo(agent_name);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public MAFFinder get_MAFFinder() throws RemoteException {
	return null;
    }

    static MAFAgentSystem getLocalAgentSystem(String address) {
	return (MAFAgentSystem) locals.get(address);
    }

    public Name[] list_all_agents() throws RemoteException {
	return this.maf.list_all_agents();
    }

    public Name[] list_all_agents_of_authority(byte[] authority)
								throws RemoteException {
	return this.maf.list_all_agents_of_authority(authority);
    }

    public String[] list_all_places() throws RemoteException {
	return this.maf.list_all_places();
    }

    public void receive_agent(
			      Name agent_name,
			      AgentProfile agent_profile,
			      byte[] agent,
			      String place_name,
			      ClassName[] class_names,
			      String code_base,
			      MAFAgentSystem_RMI rmi_class_sender)
								  throws RemoteException {
	try {
	    MAFAgentSystem class_sender = to_maf_agentsystem(rmi_class_sender);

	    this.maf.receive_agent(agent_name, agent_profile, agent, place_name, class_names, code_base, class_sender);
	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public long receive_future_message(
				       Name agent_name,
				       byte[] msg,
				       MAFAgentSystem_RMI message_sender)
									 throws RemoteException {
	try {
	    MAFAgentSystem maf_message_sender = to_maf_agentsystem(message_sender);

	    return this.maf.receive_future_message(agent_name, msg, maf_message_sender);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public void receive_future_reply(long return_id, byte[] reply)
								  throws RemoteException {
	try {
	    this.maf.receive_future_reply(return_id, reply);
	} catch (EntryNotFound ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    /**
     * Messaging
     */
    public byte[] receive_message(Name agent_name, byte[] msg)
							      throws RemoteException {
	try {
	    return this.maf.receive_message(agent_name, msg);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (NotHandled ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (MessageEx ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public void receive_oneway_message(Name agent_name, byte[] msg)
								   throws RemoteException {
	try {
	    this.maf.receive_oneway_message(agent_name, msg);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (ClassUnknown ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (DeserializationFailed ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public void resume_agent(Name agent_name) throws RemoteException {
	try {
	    this.maf.resume_agent(agent_name);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (ResumeFailed ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (AgentIsRunning ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    /*
     * Aglets Sepcific
     */
    public byte[] retract_agent(Name agent_name) throws RemoteException {
	try {
	    return this.maf.retract_agent(agent_name);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);

	} catch (MAFExtendedException ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public void suspend_agent(Name agent_name) throws RemoteException {
	try {
	    this.maf.suspend_agent(agent_name);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (SuspendFailed ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (AgentIsSuspended ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    public void terminate_agent(Name agent_name) throws RemoteException {
	try {
	    this.maf.terminate_agent(agent_name);
	} catch (TerminateFailed ex) {
	    throw new RemoteException("Remote:", ex);
	} catch (AgentNotFound ex) {
	    throw new RemoteException("Remote:", ex);
	}
    }

    static private MAFAgentSystem to_maf_agentsystem(MAFAgentSystem_RMI __rmi) {
	if (__rmi instanceof MAFAgentSystem_RMIImpl) {
	    return ((MAFAgentSystem_RMIImpl) __rmi).maf;
	} else {
	    return MAFAgentSystem_RMIClient.find_maf_agentsystem(__rmi, null);
	}
    }
}
