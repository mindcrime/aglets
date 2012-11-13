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

	/**
	 * 
	 */
	private static final long serialVersionUID = 3020214045093753722L;

	static MAFAgentSystem_RMI find_rmi_agentsystem(final MAFAgentSystem __maf) {
		if (__maf == null) {
			return null;
		}
		return (MAFAgentSystem_RMI) to_rmi.get(__maf);
	}

	static MAFAgentSystem getLocalAgentSystem(final String address) {
		return (MAFAgentSystem) locals.get(address);
	}

	static private MAFAgentSystem to_maf_agentsystem(final MAFAgentSystem_RMI __rmi) {
		if (__rmi instanceof MAFAgentSystem_RMIImpl) {
			return ((MAFAgentSystem_RMIImpl) __rmi).maf;
		} else {
			return MAFAgentSystem_RMIClient.find_maf_agentsystem(__rmi, null);
		}
	}

	MAFAgentSystem maf = null;

	static Hashtable to_rmi = new Hashtable();

	static Hashtable locals = new Hashtable();

	public MAFAgentSystem_RMIImpl(final MAFAgentSystem __maf) throws RemoteException {
		maf = __maf;
		to_rmi.put(__maf, this);

		try {
			final java.net.URL u = new java.net.URL(__maf.getAddress());
			final String addr = u.getHost() + ":"
			+ (u.getPort() == -1 ? 1099 : u.getPort());

			locals.put(addr, __maf);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public Name create_agent(
	                         final Name agent_name,
	                         final AgentProfile agent_profile,
	                         final byte[] agent,
	                         final String place_name,
	                         final Object[] arguments,
	                         final ClassName[] class_names,
	                         final String code_base,
	                         final MAFAgentSystem_RMI rmi_class_provider)
	throws RemoteException {
		try {
			final MAFAgentSystem class_provider = to_maf_agentsystem(rmi_class_provider);

			return maf.create_agent(agent_name, agent_profile, agent, place_name, arguments, class_names, code_base, class_provider);
		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final ArgumentInvalid ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public byte[][] fetch_class(
	                            final ClassName[] class_name_list,
	                            final String code_base,
	                            final AgentProfile agent_profile)
	throws RemoteException {
		try {
			return maf.fetch_class(class_name_list, code_base, agent_profile);
		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public String find_nearby_agent_system_of_profile(final AgentProfile profile)
	throws RemoteException {
		try {
			return maf.find_nearby_agent_system_of_profile(profile);
		} catch (final EntryNotFound ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public AgentStatus get_agent_status(final Name agent_name) throws RemoteException {
		try {
			return maf.get_agent_status(agent_name);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public AgentSystemInfo get_agent_system_info() throws RemoteException {
		return maf.get_agent_system_info();
	}

	@Override
	public AuthInfo get_authinfo(final Name agent_name) throws RemoteException {
		try {
			return maf.get_authinfo(agent_name);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public MAFFinder get_MAFFinder() throws RemoteException {
		return null;
	}

	@Override
	public Name[] list_all_agents() throws RemoteException {
		return maf.list_all_agents();
	}

	@Override
	public Name[] list_all_agents_of_authority(final byte[] authority)
	throws RemoteException {
		return maf.list_all_agents_of_authority(authority);
	}

	@Override
	public String[] list_all_places() throws RemoteException {
		return maf.list_all_places();
	}

	@Override
	public void receive_agent(
	                          final Name agent_name,
	                          final AgentProfile agent_profile,
	                          final byte[] agent,
	                          final String place_name,
	                          final ClassName[] class_names,
	                          final String code_base,
	                          final MAFAgentSystem_RMI rmi_class_sender)
	throws RemoteException {
		try {
			final MAFAgentSystem class_sender = to_maf_agentsystem(rmi_class_sender);

			maf.receive_agent(agent_name, agent_profile, agent, place_name, class_names, code_base, class_sender);
		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public long receive_future_message(
	                                   final Name agent_name,
	                                   final byte[] msg,
	                                   final MAFAgentSystem_RMI message_sender)
	throws RemoteException {
		try {
			final MAFAgentSystem maf_message_sender = to_maf_agentsystem(message_sender);

			return maf.receive_future_message(agent_name, msg, maf_message_sender);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public void receive_future_reply(final long return_id, final byte[] reply)
	throws RemoteException {
		try {
			maf.receive_future_reply(return_id, reply);
		} catch (final EntryNotFound ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	/**
	 * Messaging
	 */
	@Override
	public byte[] receive_message(final Name agent_name, final byte[] msg)
	throws RemoteException {
		try {
			return maf.receive_message(agent_name, msg);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final NotHandled ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final MessageEx ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public void receive_oneway_message(final Name agent_name, final byte[] msg)
	throws RemoteException {
		try {
			maf.receive_oneway_message(agent_name, msg);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final ClassUnknown ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final DeserializationFailed ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public void resume_agent(final Name agent_name) throws RemoteException {
		try {
			maf.resume_agent(agent_name);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final ResumeFailed ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final AgentIsRunning ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	/*
	 * Aglets Sepcific
	 */
	@Override
	public byte[] retract_agent(final Name agent_name) throws RemoteException {
		try {
			return maf.retract_agent(agent_name);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);

		} catch (final MAFExtendedException ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public void suspend_agent(final Name agent_name) throws RemoteException {
		try {
			maf.suspend_agent(agent_name);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final SuspendFailed ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final AgentIsSuspended ex) {
			throw new RemoteException("Remote:", ex);
		}
	}

	@Override
	public void terminate_agent(final Name agent_name) throws RemoteException {
		try {
			maf.terminate_agent(agent_name);
		} catch (final TerminateFailed ex) {
			throw new RemoteException("Remote:", ex);
		} catch (final AgentNotFound ex) {
			throw new RemoteException("Remote:", ex);
		}
	}
}
