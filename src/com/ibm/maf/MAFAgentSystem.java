package com.ibm.maf;

/*
 * @(#)MAFAgentSystem.java
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
 * File: ./CfMAF/MAFAgentSystem.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Hashtable;

import com.ibm.aglet.Ticket;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.Opt;
import com.ibm.awb.misc.Resource;

public abstract class MAFAgentSystem {

    public static final Opt option_defs[] = {
	    Opt.Entry("-defaultport", "atp.defaultport", null),
	    Opt.Entry("-port", "atp.port", "    -port <port>         set the port used by deamon"),
	    Opt.Entry("-resolve", "atp.resolve", "true", "    -resolve             resolve hostname by reverse lookup"),
	    Opt.Entry("-domain", "atp.domain", "    -domain <domainname> set the domain name"),
	    Opt.Entry("-offline", "atp.offline", "true", null),
	    Opt.Entry("-noauthentication", "atp.authentication", "false", "    -noauthentication    do not authenticate"),
	    Opt.Entry("-authentication", "atp.authentication", "true", "    -authentication      do authenticate"),
	    Opt.Entry("-pseudoseed", "atp.secureseed", "false", "    -pseudoseed          use pseudo random seed"),
	    Opt.Entry("-secureseed", "atp.secureseed", "true", "    -secureseed          use secure random seed"), };

    static Hashtable handlers = new Hashtable();

    static private MAFAgentSystem local = null;

    static boolean enabled = false;

    /*
	 * 
	 */
    public abstract Name create_agent(Name agent_name,
	    AgentProfile agent_profile, byte[] agent, String place_name,
	    Object[] arguments, ClassName[] class_names, String code_base,
	    MAFAgentSystem class_provider) throws ClassUnknown,
	    ArgumentInvalid, DeserializationFailed, MAFExtendedException /*
									  * ,
									  * RequestRefused
									  */;

    synchronized private static void createResource(String protocol) {
	AgletRuntime runtime = AgletRuntime.getAgletRuntime();

	if (runtime == null) {
	    return;
	}
	String username = runtime.getOwnerName();

	if (username == null) {
	    return;
	}
	Resource protocol_res = Resource.getResourceFor(protocol);

	if (protocol_res == null) {
	    try {
		String propfile = FileUtils.getPropertyFilenameForUser(username, protocol);

		protocol_res = Resource.createResource(protocol, propfile, null);
	    } catch (SecurityException ex) {
		protocol_res = null;
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}

	if (protocol_res == null) {
	    System.out.println("[No resource for " + protocol.toUpperCase()
		    + " found: use defaults]");
	    protocol_res = Resource.createResource(protocol, null);
	}

	protocol_res.importOptionProperties(protocol);
	protocol_res.setDefaultResource(protocol + ".addressbook", "");
    }

    private static void enableMAFURLStreamHandlers() {
	if (enabled) {
	    return;
	}
	enabled = true;

	try {
	    URLStreamHandlerFactory fac = new URLStreamHandlerFactory() {
		public URLStreamHandler createURLStreamHandler(String protocol) {
		    try {
			return (URLStreamHandler) Class.forName("com.ibm.net.protocol."
				+ protocol + ".Handler").newInstance();
		    } catch (Exception ex) {
			return null;
		    }
		}
	    };

	    URL.setURLStreamHandlerFactory(fac);
	} catch (SecurityException ex) {
	    ex.printStackTrace();
	} catch (Throwable ex) {
	    ex.printStackTrace();
	}
    }

    public abstract byte[][] fetch_class(ClassName[] class_name_list,
	    String code_base, AgentProfile agent_profile) throws ClassUnknown,
	    MAFExtendedException /* , RequestRefused */;

    public abstract String find_nearby_agent_system_of_profile(
	    AgentProfile profile) throws EntryNotFound;

    public abstract AgentStatus get_agent_status(Name agent_name)
	    throws AgentNotFound;

    public abstract AgentSystemInfo get_agent_system_info();

    public abstract AuthInfo get_authinfo(Name agent_name) throws AgentNotFound;

    public abstract MAFFinder get_MAFFinder() throws FinderNotFound;

    // public abstract void initialize(String a[]);

    public abstract String getAddress();

    synchronized private static AgentSystemHandler getHandler(String scheme) {
	AgentSystemHandler handler = (AgentSystemHandler) handlers.get(scheme);

	if (handler == null) {
	    String class_name = "com.ibm.maf." + scheme.toLowerCase()
		    + ".Handler";

	    try {
		handler = (AgentSystemHandler) Class.forName(class_name).newInstance();
	    } catch (Exception ex) {
		ex.printStackTrace();
		return null;
	    }
	    handlers.put(scheme, handler);
	}

	return handler;
    }

    static public MAFAgentSystem getLocalMAFAgentSystem() {
	return local;
    }

    static public MAFAgentSystem getMAFAgentSystem(Ticket ticket)
	    throws java.net.UnknownHostException {

	// tentative
	// String scheme = ticket.getDestination().getProtocol();
	String scheme = ticket.getProtocol();

	return getHandler(scheme).getMAFAgentSystem(ticket);
    }

    /*
     * This needs to be removed, and all MAFAgentSystem objects should be
     * associated with Ticket object.
     */
    static public MAFAgentSystem getMAFAgentSystem(String address)
	    throws java.net.MalformedURLException,
	    java.net.UnknownHostException {

	int c = address.indexOf(':');
	int s = address.indexOf('/');
	String scheme = "atp"; // default;

	if ((c > 0) && (s > c)) {
	    scheme = address.substring(0, c);
	    address = address.substring(c + 1);
	}

	return getHandler(scheme).getMAFAgentSystem(address);
    }

    public synchronized static void initMAFAgentSystem(MAFAgentSystem l,
	    String protocol) throws MAFExtendedException {
	if ((local != null) && (local != l)) {
	    throw new MAFExtendedException("Local Agent system cannot be set twice");
	}

	createResource(protocol);

	enableMAFURLStreamHandlers();
	initURLStreamHandlers(protocol);
	if (protocol == null) {
	    protocol = "mafiiop";
	}
	AgentSystemHandler handler = getHandler(protocol);

	local = l;
	handler.initMAFAgentSystem(local);
    }

    private static void initURLStreamHandlers(String protocol) {

	/*
	 * To make sure that the classes are loaded before aglets use them
	 */
	try {
	    new URL("HTTP://www.ibm.com");
	    new URL("http://www.ibm.com");
	    new URL("file://www.ibm.com");
	    new URL("FILE://www.ibm.com");
	    new URL(protocol.toLowerCase() + "://www.ibm.com");
	    new URL(protocol.toUpperCase() + "://www.ibm.com");
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public abstract Name[] list_all_agents();

    public abstract Name[] list_all_agents_of_authority(byte[] authority);

    public abstract String[] list_all_places();

    public abstract void receive_agent(Name agent_name,
	    AgentProfile agent_profile, byte[] agent, String place_name,
	    ClassName[] class_names, String code_base,
	    MAFAgentSystem class_sender) throws ClassUnknown,
	    DeserializationFailed, MAFExtendedException /* , RequestRefused */;

    public abstract long receive_future_message(Name agent_name, byte[] msg,
	    MAFAgentSystem message_sender) throws AgentNotFound, ClassUnknown,
	    DeserializationFailed, MAFExtendedException;

    public abstract void receive_future_reply(long return_id, byte[] reply)
	    throws EntryNotFound, ClassUnknown, DeserializationFailed,
	    MAFExtendedException;

    /**
     * Messaging
     */
    public abstract byte[] receive_message(Name agent_name, byte[] msg)
	    throws AgentNotFound, NotHandled, MessageEx, ClassUnknown,
	    DeserializationFailed, MAFExtendedException;

    public abstract void receive_oneway_message(Name agent_name, byte[] msg)
	    throws AgentNotFound, ClassUnknown, DeserializationFailed,
	    MAFExtendedException;

    public abstract void resume_agent(Name agent_name) throws AgentNotFound,
	    ResumeFailed, AgentIsRunning;

    /*
     * Aglets Specific
     */
    public abstract byte[] retract_agent(Name agent_name) throws AgentNotFound,
	    MAFExtendedException;

    public abstract void setAddress(String name);

    public synchronized static void startMAFAgentSystem(MAFAgentSystem l,
	    String protocol) throws MAFExtendedException {
	AgentSystemHandler handler = getHandler(protocol);

	handler.startMAFAgentSystem(l);
    }

    public abstract void suspend_agent(Name agent_name) throws AgentNotFound,
	    SuspendFailed, AgentIsSuspended;

    public abstract void terminate_agent(Name agent_name) throws AgentNotFound,
	    TerminateFailed;
}
