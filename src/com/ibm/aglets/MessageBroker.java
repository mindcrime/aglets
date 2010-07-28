package com.ibm.aglets;

/*
 * @(#)MessageBroker.java
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
import java.io.OptionalDataException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.maf.AgentNotFound;
import com.ibm.maf.ClassUnknown;
import com.ibm.maf.DeserializationFailed;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.MessageEx;
import com.ibm.maf.Name;
import com.ibm.maf.NotHandled;

/*
 * This class will be removed.
 */
final class MessageBroker {

    static Hashtable replies = new Hashtable();
    static Hashtable rms = new Hashtable();

    static void delegateMessage(Ticket ticket, Name name, Message msg)
								      throws InvalidAgletException {
	try {
	    MAFAgentSystem maf = MAFAgentSystem.getMAFAgentSystem(ticket);
	    ResourceManager rm = getCurrentResourceManager();

	    synchronized (msg) {
		if (((msg instanceof MessageImpl) == false)
			|| (((MessageImpl) msg).isDelegatable() == false)) {
		    throw new IllegalArgumentException("The message cannot be delegated");
		}

		MessageImpl origin = (MessageImpl) msg;
		origin.clone();

		FutureReplyImpl future = origin.future;
		byte msg_bytes[] = MessageOutputStream.toByteArray(rm, msg);

		MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();
		long return_id = maf.receive_future_message(name, msg_bytes, local);

		waitFutureReply(future, rm, new Long(return_id));

		//
		// okay
		//
		origin.disable(); // disable the message
	    }

	    /*
	     * MAF Exceptions
	     */
	} catch (AgentNotFound ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (ClassUnknown ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (DeserializationFailed ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (UnknownHostException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (IOException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (MAFExtendedException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	}
    }

    static private String getContextName(Ticket ticket) {
	if (ticket == null) {
	    return null;
	}

	String name = ticket.getFile();

	if ((name == null) || (name.length() == 0)) {
	    return "";
	}

	// cut off "/servlet"
	if (name.startsWith("/servlet")) {
	    name = name.substring(9); // cut off "/servlet/"
	    int i = name.indexOf('/', 1);

	    if (i > 0) {
		name = name.substring(i);
	    } else {
		return "";
	    }
	}

	// cut off "/aglets"
	if (name.startsWith("/aglets")) {
	    name = name.substring(7);
	}
	int i = name.indexOf('/', 1);

	if (i > 0) {
	    name = name.substring(name.charAt(0) == '/' ? 1 : 0, i);
	} else {
	    name = name.substring(name.charAt(0) == '/' ? 1 : 0);
	}
	if (name.equals("default") || name.equals("cxt")) { // legacy
	    name = "";
	}
	return name;
    }

    private static ResourceManager getCurrentResourceManager() {

	// REMIND. This should be obtained from context.
	ResourceManagerFactory rm_factory = AgletRuntime.getDefaultResourceManagerFactory();

	return rm_factory.getCurrentResourceManager();
    }

    static FutureReplyImpl getFutureReply(Long return_id) {
	return (FutureReplyImpl) replies.get(return_id);
    }

    static ResourceManager getResourceManager(Long return_id) {
	ResourceManager rm;

	synchronized (MessageBroker.class) {
	    while ((rm = (ResourceManager) rms.get(return_id)) == null) {
		try {
		    MessageBroker.class.wait();
		} catch (InterruptedException ex) {
		    ex.printStackTrace();

		    // correct?
		    return null;
		}
	    }
	}
	return rm;
    }

    static FutureReplyImpl sendFutureMessage(
					     Ticket ticket,
					     Name name,
					     Message msg)
							 throws InvalidAgletException {
	try {
	    MAFAgentSystem maf = MAFAgentSystem.getMAFAgentSystem(ticket);
	    ResourceManager rm = getCurrentResourceManager();

	    byte msg_bytes[] = MessageOutputStream.toByteArray(rm, msg);
	    MAFAgentSystem local = MAFAgentSystem.getLocalMAFAgentSystem();

	    long return_id = maf.receive_future_message(name, msg_bytes, local);

	    FutureReplyImpl reply = new FutureReplyImpl();

	    waitFutureReply(reply, rm, new Long(return_id));
	    return reply;

	} catch (UnknownHostException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new InvalidAgletException(toMessage(ex));

	    /*
	     * MAF Exceptions
	     */

	} catch (AgentNotFound ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (ClassUnknown ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (DeserializationFailed ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (MAFExtendedException ex) {
	    throw new InvalidAgletException(toMessage(ex));
	}
    }

    static Object sendMessage(Ticket ticket, Name name, Message msg)
								    throws InvalidAgletException,
								    MessageException,
								    NotHandledException {

	try {
	    MAFAgentSystem maf = MAFAgentSystem.getMAFAgentSystem(ticket);
	    ResourceManager rm = getCurrentResourceManager();

	    if (name == null) {

		// String cname = ticket.getContextName();
		String cname = getContextName(ticket);

		name = new Name("".getBytes(), cname.getBytes(), MAFUtil.AGENT_SYSTEM_TYPE_AGLETS);
	    }

	    byte msg_bytes[] = MessageOutputStream.toByteArray(rm, msg);

	    byte ret_bytes[] = maf.receive_message(name, msg_bytes);

	    return MessageInputStream.toObject(rm, ret_bytes);

	} catch (OptionalDataException ex) {
	    ex.printStackTrace();
	    throw new NotHandledException(toMessage(ex));

	} catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	    throw new NotHandledException(toMessage(ex));

	} catch (UnknownHostException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new NotHandledException(toMessage(ex));

	    /*
	     * MAF Exceptions
	     */

	} catch (AgentNotFound ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (NotHandled ex) {
	    throw new NotHandledException(toMessage(ex));

	} catch (MessageEx ex) {
	    throw new MessageException(ex.getException(), ex.getMessage());

	} catch (ClassUnknown ex) {
	    throw new NotHandledException(toMessage(ex));

	} catch (DeserializationFailed ex) {
	    throw new NotHandledException(toMessage(ex));

	} catch (MAFExtendedException ex) {
	    throw new NotHandledException(toMessage(ex));
	}
    }

    static void sendOnewayMessage(Ticket ticket, Name name, Message msg)
									throws InvalidAgletException {
	try {
	    MAFAgentSystem maf = MAFAgentSystem.getMAFAgentSystem(ticket);
	    ResourceManager rm = getCurrentResourceManager();

	    byte msg_bytes[] = MessageOutputStream.toByteArray(rm, msg);

	    maf.receive_oneway_message(name, msg_bytes);

	} catch (UnknownHostException ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new InvalidAgletException(toMessage(ex));

	    /*
	     * MAFExceptions
	     */

	} catch (AgentNotFound ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (ClassUnknown ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (DeserializationFailed ex) {
	    throw new InvalidAgletException(toMessage(ex));

	} catch (MAFExtendedException ex) {
	    throw new InvalidAgletException(toMessage(ex));
	}
    }

    static String toMessage(Exception ex) {
	return ex.getClass().getName() + ':' + ex.getMessage();
    }

    static public void waitFutureReply(
				       FutureReplyImpl reply,
				       ResourceManager rm,
				       Long return_id) {
	synchronized (MessageBroker.class) {
	    replies.put(return_id, reply);
	    rms.put(return_id, rm);
	    MessageBroker.class.notify();
	}
    }
}
