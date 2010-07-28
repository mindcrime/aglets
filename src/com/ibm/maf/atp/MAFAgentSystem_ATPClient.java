package com.ibm.maf.atp;

/*
 * @(#)MAFAgentSystem_ATPClient.java
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

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import com.ibm.aglet.QoC;
import com.ibm.aglet.Ticket;
import com.ibm.atp.AtpConstants;
import com.ibm.atp.auth.SharedSecret;
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
import com.ibm.maf.FinderNotFound;
import com.ibm.maf.MAFAgentSystem;
import com.ibm.maf.MAFExtendedException;
import com.ibm.maf.MAFFinder;
import com.ibm.maf.MessageEx;
import com.ibm.maf.Name;
import com.ibm.maf.NotHandled;
import com.ibm.maf.ResumeFailed;
import com.ibm.maf.SuspendFailed;
import com.ibm.maf.TerminateFailed;

public class MAFAgentSystem_ATPClient extends MAFAgentSystem implements
	AtpConstants {
    private Ticket _ticket = null;
    private URL _url_address = null;
    private String _address = null;

    // static String user_agent_name = "Aglets/1.0b";
    // static String agent_system_name = "aglets";
    static String content_type = "application/x-aglets";

    class WaitThread extends Thread {
	MAFAgentSystem _local;
	AtpConnectionImpl _conn;
	long _return_id;

	WaitThread(MAFAgentSystem local, AtpConnectionImpl conn, long return_id) {
	    super("FutureReplyWaiter");
	    this._local = local;
	    this._conn = conn;
	    this._return_id = return_id;
	    this.setDaemon(true);
	}

	@Override
	public void run() {
	    try {
		byte b[] = MAFAgentSystem_ATPClient.this.receive_reply_internal(this._conn);

		this._local.receive_future_reply(this._return_id, b);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    } finally {
		try {
		    this._conn.close();
		} catch (IOException ex) {
		}
	    }
	}
    }

    //
    java.util.Hashtable handlers = new java.util.Hashtable();

    public MAFAgentSystem_ATPClient(Ticket ticket) {
	this._ticket = ticket;
	this._url_address = ticket.getDestination();
	this._address = ticket.getHost();
    }

    public MAFAgentSystem_ATPClient(String address) {
	this._ticket = null;
	this._address = address;
	try {
	    this._url_address = new URL("atp://" + this._address);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    @Override
    public Name create_agent(
			     Name agent_name,
			     AgentProfile agent_profile,
			     byte[] agent,
			     String place_name,
			     Object[] arguments,
			     ClassName[] class_names,
			     String code_base,
			     MAFAgentSystem class_provider)
							   throws ClassUnknown,
							   ArgumentInvalid,
							   DeserializationFailed,
							   MAFExtendedException {

	/*
	 * Message msg = new Message("createAglet"); msg.setArg("codebase",
	 * code_base); msg.setArg("classname", class_names[0]);
	 * msg.setArg("init", arguments[0]); try { AgletProxy proxy =
	 * broker.sendMessage(place_name, msg); return new
	 * Name(agent_name.authority, proxy.getAgletID().toByteArray(),
	 * agent_name.agent_system_type); } catch (InvalidAgletException ex) {
	 * throw new MAFExtendedException("Place Not Found:" + ex.getMessage());
	 * } return null;
	 */

	throw new MAFExtendedException("Not Supported");
    }

    @Override
    public byte[][] fetch_class(
				ClassName[] class_name_list,
				String code_base,
				AgentProfile agent_profile)
							   throws ClassUnknown,
							   MAFExtendedException {
	if ((class_name_list != null) && (class_name_list.length != 1)) {
	    throw new MAFExtendedException("Multiple classes not supported");
	}

	byte bytecode[][] = new byte[1][];
	InputStream is = null;

	try {

	    // REMIND: !!
	    int content_length = -1;
	    URL url = new URL(code_base);

	    // fetch
	    AtpConnectionImpl connection = new AtpConnectionImpl(url);

	    connection.setRequestType(FETCH);
	    connection.setAgentProfile(agent_profile);

	    /*
	     * connection.setSender(class_sender.getAddress());
	     * connection.setRequestProperty("user-agent", user_agent_name);
	     * connection.setRequestProperty("agent-system", agent_system_name);
	     * connection.setRequestProperty("agent-language", "java");
	     * connection.setDoInput(true); connection.setUseCaches(false);
	     */
	    connection.connect();
	    is = connection.getInputStream();

	    content_length = connection.getContentLength();

	    if (content_length < 0) {
		content_length = is.available();
	    }
	    if (content_length == 0) {
		return null;
	    }

	    bytecode[0] = new byte[content_length];

	    int offset = 0;

	    while (content_length > 0) {
		int read = is.read(bytecode[0], offset, content_length);

		offset += read;
		content_length -= read;
	    }
	    is.close();
	} catch (IOException ex) {
	    ex.printStackTrace();
	    return null;
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (Exception ex) {
		}
	    }
	}
	return bytecode;
    }

    @Override
    public String find_nearby_agent_system_of_profile(AgentProfile profile)
									   throws EntryNotFound {
	return null;
    }

    @Override
    public AgentStatus get_agent_status(Name agent_name) throws AgentNotFound {
	return null;
    }

    @Override
    public AgentSystemInfo get_agent_system_info() {
	return null;
    }

    @Override
    public AuthInfo get_authinfo(Name agent_name) throws AgentNotFound {
	return null;
    }

    @Override
    public MAFFinder get_MAFFinder() throws FinderNotFound {
	return null;
    }

    @Override
    public String getAddress() {
	return this._address;
    }

    @Override
    public Name[] list_all_agents() {
	return null;
    }

    @Override
    public Name[] list_all_agents_of_authority(byte[] authority) {
	return null;
    }

    @Override
    public String[] list_all_places() {
	return null;
    }

    @Override
    public void receive_agent(
			      Name agent_name,
			      AgentProfile agent_profile,
			      byte[] agent,
			      String place_name,
			      ClassName[] class_names,
			      String code_base,
			      MAFAgentSystem class_sender)
							  throws ClassUnknown,
							  DeserializationFailed,
							  MAFExtendedException {
	try {
	    final Name fAgentName = agent_name;
	    final AgentProfile fAgentProfile = agent_profile;
	    final byte[] fAgent = agent;
	    final String fPlaceName = place_name;
	    final ClassName[] fClassNames = class_names;
	    final String fCodeBase = code_base;
	    final MAFAgentSystem fClassSender = class_sender;

	    AccessController.doPrivileged(new PrivilegedExceptionAction() {
		public Object run()
				   throws ClassUnknown,
				   DeserializationFailed,
				   MAFExtendedException {
		    MAFAgentSystem_ATPClient.this.receive_agent0(fAgentName, fAgentProfile, fAgent, fPlaceName, fClassNames, fCodeBase, fClassSender);
		    return null;
		}
	    });
	} catch (PrivilegedActionException ex) {
	    Exception e = ex.getException();

	    if (e instanceof ClassUnknown) {
		throw (ClassUnknown) e;
	    } else if (e instanceof DeserializationFailed) {
		throw (DeserializationFailed) e;
	    } else if (e instanceof MAFExtendedException) {
		throw (MAFExtendedException) e;
	    } else {
		ex.printStackTrace();
	    }
	}
    }

    private void receive_agent0(
				Name agent_name,
				AgentProfile agent_profile,
				byte[] agent,
				String place_name,
				ClassName[] class_names,
				String code_base,
				MAFAgentSystem class_sender)
							    throws ClassUnknown,
							    DeserializationFailed,
							    MAFExtendedException {
	AtpConnectionImpl connection = null;

	try {
	    URL url = new URL(this._url_address, place_name);
	    System.out.println("***** Addr: " + this._url_address + " place: "
		    + place_name);
	    connection = new AtpConnectionImpl(url);
	    connection.setRequestType(DISPATCH);
	    connection.setAgentProfile(agent_profile);
	    connection.setAgentName(agent_name);
	    connection.setSender(class_sender.getAddress());
	    connection.connect();
	    if (this._ticket != null) {
		QoC qoc = this._ticket.getQoC();

		if ((qoc != null)
			&& qoc.getIntegrity().equals(QoC.NORMALINTEGRITY)) {
		    SharedSecret secret = connection.getSharedSecret();

		    if (secret == null) {
			System.out.println("No integrity check because no security domain is authenticated.");
		    } else {
			byte[] mic = ConnectionHandler.calculateMIC(secret, agent);

			connection.setMIC(mic);
		    }
		}
	    }
	    DataOutput out = new DataOutputStream(connection.getOutputStream());

	    // CodeBase
	    out.writeUTF(code_base);

	    // ClassNames
	    out.writeInt(class_names.length);
	    for (ClassName class_name : class_names) {
		out.writeUTF(class_name.name);
		out.writeInt(class_name.descriminator.length);
		out.write(class_name.descriminator);
	    }

	    // Agent
	    out.writeInt(agent.length);
	    out.write(agent);
	    connection.sendRequest();
	    if ((connection.getStatusCode() != OKAY)
		    && (connection.getStatusCode() != MOVED)) {
		System.out.println("code = " + connection.getStatusCode());
		throw new MAFExtendedException(connection.getReasonPhase());
	    }
	    if (content_type.equalsIgnoreCase(connection.getContentType()) == false) {
		throw new MAFExtendedException(connection.getReasonPhase());
	    }
	    DataInput in = new DataInputStream(connection.getInputStream());

	    // REMIND : this data must be used to represent the
	    // remote proxy.
	    int content_length = connection.getContentLength();

	    if (content_length > 0) {
		byte[] content = new byte[connection.getContentLength()];

		in.readFully(content);
	    }
	} catch (SocketException ex) {
	    throw new MAFExtendedException("SocketException: "
		    + this._url_address);
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new MAFExtendedException("IOException: " + this._url_address);
	} finally {
	    if (connection != null) {
		try {
		    connection.close();
		} catch (IOException ex) {
		}
	    }
	}
    }

    @Override
    public long receive_future_message(
				       Name agent_name,
				       byte[] msg,
				       MAFAgentSystem sender)
							     throws AgentNotFound,
							     ClassUnknown,
							     DeserializationFailed,
							     MAFExtendedException {
	AtpConnectionImpl connection = this.send_message_internal(agent_name, msg, FUTURE, sender);
	long l = System.currentTimeMillis();

	new WaitThread(sender, connection, l).start();
	return l;
    }

    @Override
    synchronized public void receive_future_reply(long return_id, byte[] reply)
									       throws EntryNotFound,
									       ClassUnknown,
									       DeserializationFailed,
									       MAFExtendedException {
	Long id = new Long(return_id);

	ConnectionHandler handler = (ConnectionHandler) this.handlers.get(id);

	while (handler == null) {
	    try {
		this.wait();
	    } catch (InterruptedException ex) {
	    }
	    handler = (ConnectionHandler) this.handlers.get(id);
	}
	handler.sendFutureReply(reply);
    }

    /**
     * Messaging
     */
    @Override
    public byte[] receive_message(Name agent_name, byte[] msg)
							      throws AgentNotFound,
							      NotHandled,
							      MessageEx,
							      ClassUnknown,
							      DeserializationFailed,
							      MAFExtendedException {
	AtpConnectionImpl connection = this.send_message_internal(agent_name, msg, SYNC, null);

	try {
	    InputStream in = connection.getInputStream();
	    byte type = (byte) in.read();

	    switch (type) {
	    case HANDLED:
		int length = connection.getContentLength() - 1;
		byte b[] = new byte[length];

		new DataInputStream(in).readFully(b);
		return b;
	    case NOT_HANDLED:
		throw new NotHandled();
	    case EXCEPTION:
		throw MessageEx.read(new DataInputStream(connection.getInputStream()));
	    default:
		throw new MAFExtendedException("Unkonown Return Type");
	    }
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new MAFExtendedException("Error in receiving reply");
	} finally {
	    try {
		connection.close();
	    } catch (IOException ex) {
	    }
	    ;
	}
    }

    @Override
    public void receive_oneway_message(Name agent_name, byte[] msg)
								   throws AgentNotFound,
								   ClassUnknown,
								   DeserializationFailed,
								   MAFExtendedException {
	try {
	    this.send_message_internal(agent_name, msg, ONEWAY, null).close();
	} catch (IOException ex) {
	    throw new MAFExtendedException("Unexpected Exception " + ex);
	}
    }

    byte[] receive_reply_internal(AtpConnectionImpl connection)
							       throws IOException {
	try {
	    int length = connection.getContentLength();
	    InputStream in = connection.getInputStream();

	    if (length <= 0) {
		length = in.available();
	    }

	    // System.out.println("length = " + length);
	    if (length > 0) {
		byte b[] = new byte[length];
		DataInputStream din = new DataInputStream(in);

		din.readFully(b);
		return b;
	    } else {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int i;

		while ((i = in.read()) >= 0) {
		    out.write(i);
		}
		return out.toByteArray();
	    }
	} finally {

	    // Just to make sure
	    if (connection != null) {
		connection.close();
	    }
	}
    }

    synchronized public void registerFutureReply(
						 ConnectionHandler handler,
						 long id) {
	this.handlers.put(new Long(id), handler);
	this.notify();
    }

    @Override
    public void resume_agent(Name agent_name)
					     throws AgentNotFound,
					     ResumeFailed,
					     AgentIsRunning {
    }

    @Override
    public byte[] retract_agent(Name agent_name)
						throws AgentNotFound,
						MAFExtendedException {
	try {
	    final Name fAgentName = agent_name;

	    return (byte[]) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		public Object run() throws AgentNotFound, MAFExtendedException {
		    return MAFAgentSystem_ATPClient.this.retract_agent0(fAgentName);
		}
	    });
	} catch (PrivilegedActionException ex) {
	    Exception e = ex.getException();

	    if (e instanceof AgentNotFound) {
		throw (AgentNotFound) e;
	    } else if (e instanceof MAFExtendedException) {
		throw (MAFExtendedException) e;
	    } else {
		ex.printStackTrace();
		return null;
	    }
	}
    }

    private byte[] retract_agent0(Name agent_name)
						  throws AgentNotFound,
						  MAFExtendedException {
	AtpConnectionImpl connection = null;

	try {
	    connection = new AtpConnectionImpl(this._url_address);
	    connection.setRequestType(RETRACT);
	    connection.setAgentName(agent_name);

	    // connection.setAgentProfile(agent_profile);
	    // connection.setCodeBase(code_base);
	    // connection.setSender(class_sender.getAddress());

	    connection.connect();
	    connection.sendRequest();
	    if ((connection.getStatusCode() != OKAY)
		    && (connection.getStatusCode() != MOVED)) {
		System.out.println("code = " + connection.getStatusCode());
		throw new MAFExtendedException(connection.getReasonPhase());
	    }
	    if (content_type.equalsIgnoreCase(connection.getContentType()) == false) {
		throw new MAFExtendedException(connection.getReasonPhase());
	    }
	    InputStream is = connection.getInputStream();
	    DataInput in = new DataInputStream(is);

	    // REMIND : this data must be used to represent the
	    // remote proxy.
	    int content_length = connection.getContentLength();

	    if (content_length <= 0) {
		content_length = is.available();
	    }
	    if (content_length > 0) {
		byte[] content = new byte[connection.getContentLength()];

		in.readFully(content);
		return content;
	    } else {
		throw new AgentNotFound(agent_name.toString());
	    }
	} catch (SocketException ex) {
	    throw new MAFExtendedException("SocketException: "
		    + this._url_address);
	} catch (IOException ex) {
	    throw new MAFExtendedException("IOException: " + this._url_address);
	} finally {
	    if (connection != null) {
		try {
		    connection.close();
		} catch (IOException ex) {
		}
	    }
	}
    }

    //
    // Utilities
    //
    private AtpConnectionImpl send_message_internal(
						    Name agent_name,
						    byte[] msg,
						    int type,
						    MAFAgentSystem sender)
									  throws AgentNotFound,
									  MAFExtendedException {
	try {
	    final Name fAgentName = agent_name;
	    final byte[] fMsg = msg;
	    final int fType = type;
	    final MAFAgentSystem fSender = sender;

	    return (AtpConnectionImpl) AccessController.doPrivileged(new PrivilegedExceptionAction() {
		public Object run() throws AgentNotFound, MAFExtendedException {
		    return MAFAgentSystem_ATPClient.this.send_message_internal0(fAgentName, fMsg, fType, fSender);
		}
	    });
	} catch (PrivilegedActionException ex) {
	    Exception e = ex.getException();

	    if (e instanceof AgentNotFound) {
		throw (AgentNotFound) e;
	    } else if (e instanceof MAFExtendedException) {
		throw (MAFExtendedException) e;
	    } else {
		ex.printStackTrace();
		return null;
	    }
	}
    }

    //
    // Utilities
    //
    private AtpConnectionImpl send_message_internal0(
						     Name agent_name,
						     byte[] msg,
						     int type,
						     MAFAgentSystem sender)
									   throws AgentNotFound,
									   MAFExtendedException {
	AtpConnectionImpl connection = null;

	try {
	    connection = new AtpConnectionImpl(this._url_address);
	    connection.setRequestType(MESSAGE);
	    connection.setAgentName(agent_name);
	    if (sender != null) {
		connection.setSender(sender.getAddress());
	    }

	    // connection.setAgentProfile(agent_profile);
	    // connection.setContentType(content_type);
	    connection.connect();
	    OutputStream out = connection.getOutputStream();

	    out.write(type & 0xFF); // byte
	    out.write(msg);
	    connection.sendRequest();
	    if (connection.getStatusCode() != AtpConstants.OKAY) {
		System.out.println("code = " + connection.getStatusCode());
		throw new AgentNotFound(connection.getReasonPhase());
	    }
	    if (content_type.equalsIgnoreCase(connection.getContentType()) == false) {
		throw new MAFExtendedException(connection.getReasonPhase());
	    }
	    return connection;
	} catch (IOException ex) {
	    ex.printStackTrace();
	    throw new AgentNotFound(connection != null ? connection.getReasonPhase()
		    : agent_name.toString());
	}
    }

    @Override
    public void setAddress(String name) {
	throw new NoSuchMethodError();
    }

    @Override
    public void suspend_agent(Name agent_name)
					      throws AgentNotFound,
					      SuspendFailed,
					      AgentIsSuspended {
    }

    @Override
    public void terminate_agent(Name agent_name)
						throws AgentNotFound,
						TerminateFailed {
    }
}
