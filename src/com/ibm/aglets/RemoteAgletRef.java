package com.ibm.aglets;

/*
 * @(#)RemoteAgletRef.java
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

// - //= import java.security.Permissions;
// - import com.ibm.awb.security.Permissions;
// # import com.ibm.aglets.security.Allowance;

import com.ibm.aglet.*;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.maf.Name;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.*;
import com.ibm.awb.weakref.*;
import java.util.Hashtable;

/**
 * @version     1.00    $Date: 2009/07/27 10:31:41 $
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
public final class RemoteAgletRef extends WeakRef implements AgletRef {

	static final class RefKey {
		String address;
		Name name;
		int hash = 0;

		RefKey(String a, Name n) {
			address = a;
			name = n;
			for (int i = 0; i < n.identity.length; i++) {
				hash += (hash * 37) + (int)n.identity[i];
			} 
			hash += a.hashCode();
		}
		public int hashCode() {
			return hash;
		} 

		public boolean equals(Object obj) {
			if (obj instanceof RefKey) {
				RefKey rk = (RefKey)obj;

				return address.equals(rk.address) && equals(rk.name, name);
			} 
			return false;
		} 

		static public boolean equals(Name n1, Name n2) {
			if (n1.authority.length == n2.authority.length 
					&& n1.identity.length == n2.identity.length 
					&& n1.agent_system_type == n2.agent_system_type) {
				int l = n1.authority.length;

				for (int i = 0; i < l; i++) {
					if (n1.authority[i] != n2.authority[i]) {
						return false;
					} 
				} 
				l = n1.identity.length;
				for (int i = 0; i < l; i++) {
					if (n1.identity[i] != n2.identity[i]) {
						return false;
					} 
				} 
				return true;
			} 
			return false;
		} 
	}

	static class RemoteRefTable extends WeakRefTable {
		synchronized public RemoteAgletRef getRef(Ticket ticket, Name n) {
			Object key = RemoteAgletRef.getRefID(ticket.getDestination(), n);
			RemoteAgletRef ref = (RemoteAgletRef)getWeakRef(key);

			if (ref == null) {
				ref = new RemoteAgletRef(ticket, n);
				super.add(ref);
			} 
			ref.referenced();
			return ref;
		} 
	}

	private static RemoteRefTable _table = new RemoteRefTable();

	private Ticket _ticket = null;		// target server address
	private AgletInfo _info = null;		// info is immutable.
	private Name _name;

	static Message _get_info_message = new Message("_getAgletInfo");

	/*
	 * Used by WeakReferefence.
	 */
	public RemoteAgletRef() {
		super(_table);
	}
	/*
	 * Creates an remote aglet ref with a name and address of the aglet
	 */
	RemoteAgletRef(Ticket t, Name n) {
		super(_table);
		_ticket = t;
		_name = n;
	}
	/*
	 * Activates the aglet
	 */
	public void activate() throws IOException, AgletException {

		// REMIND:
		throw new IllegalAccessError("Cannot activate remote Aglet (for now)");
	}
	// #     /**
	// #      * Gets the allowance: availability of the aglet's resources.
	// #      * @return an Allowance object
	// #      */
	// #     public Allowance getAllowance() {
	// # 	throw new RuntimeException("Remote Reference doens't have allowance.");
	// #     }

	public void checkValidation() throws InvalidAgletException {
		if (_info == null) {
			getInfo();
		} 
	}
	/**
	 * Delegates a message
	 */
	synchronized public void delegateMessage(Message msg) 
			throws InvalidAgletException {
		MessageBroker.delegateMessage(_ticket, _name, msg);
	}
	/*
	 * Reference Management
	 * @see com.ibm.awb.weakref.WeakRef
	 */
	protected Ref findRef() {
		return LocalAgletRef.getAgletRef(_name);
	}
	/*
	 * Class Method
	 */
	protected Ref findRef(ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		Name t_name = (Name)s.readObject();
		String address = (String)s.readObject();

		// lookup local references
		AgletRef ref = LocalAgletRef.getAgletRef(t_name);

		if (ref != null) {
			return ref;
		} 

		// lookup remote references
		return RemoteAgletRef.getAgletRef(new Ticket(address), t_name);
	}
	/**
	 * Gets the address of the target aglet
	 */
	public String getAddress() throws InvalidAgletException {
		if (_info == null) {
			getInfo();
		} 
		return _ticket.getDestination().toString();
	}
	/**
	 * Gets the aglet. If the aglet is access protected it will require
	 * the right key to get access.
	 * @return the aglet
	 * @exception SecurityException if the current execution is not allowed.
	 */
	public Aglet getAglet() throws InvalidAgletException {
		throw new InvalidAgletException("Remote Reference doens't have aglet");
	}
	/**
	 * Gets the information of the aglet
	 * @return the AgletInfo of the aglet
	 */
	public AgletInfo getAgletInfo() {
		if (_info == null) {
			try {
				getInfo();
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
		return _info;
	}
	static public RemoteAgletRef getAgletRef(Ticket ticket, Name n) {
		return _table.getRef(ticket, n);
	}
	private void getInfo() throws InvalidAgletException {
		try {
			_info = (AgletInfo)sendMessage(_get_info_message);

			// update
			// _ticket = new Ticket(_info.getDestination(), _ticket.getQoC());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Ticket = " + _ticket);
			System.out.println("name = " + _name.toString());
			throw new InvalidAgletException("Not Found");
		} 
	}
	public String getRefClassName() {
		return "com.ibm.aglets.RemoteAgletRef";
	}
	protected Object getRefID() {
		return getRefID(_ticket.getDestination(), _name);
	}
	static Object getRefID(URL address, Name n) {
		return new RefKey(address.toString(), n);
	}
	/*
	 * 
	 */
	public boolean isActive() {
		return false;
	}
	public boolean isRemote() {
		return true;
	}
	/**
	 * Check the state
	 */
	public boolean isState(int s) {
		return false;
	}
	public boolean isValid() {
		return false;
	}
	/*
	 * Resumes the aglet
	 */
	public void resume() throws AgletException {

		// REMIND:
		throw new IllegalAccessError("Cannot resume remote Aglet (for now)");
	}
	/**
	 * Sends a message in asynchronous way.
	 * @param msg the message to send
	 */
	synchronized public FutureReply sendFutureMessage(Message msg) 
			throws InvalidAgletException {
		return MessageBroker.sendFutureMessage(_ticket, _name, msg);
	}
	/**
	 * Sends a message in synchronous way.
	 * @param msg the message to send
	 */
	public Object sendMessage(Message msg) 
			throws MessageException, InvalidAgletException, 
				   NotHandledException {
		return MessageBroker.sendMessage(_ticket, _name, msg);
	}
	/**
	 * Sends a oneway message
	 * @param msg the message to send
	 */
	synchronized public void sendOnewayMessage(Message msg) 
			throws InvalidAgletException {
		MessageBroker.sendOnewayMessage(_ticket, _name, msg);
	}
	/**
	 * set info
	 */

	/* package */
	void setAgletInfo(AgletInfo i) {
		_info = i;
	}
	static public void showRefTable(PrintStream out) {
		out.println(_table.toString());
	}
	/*
	 * Validation management. Aglet which is not valid is not accessible.
	 */
	public String toString() {
		return "RemoteAgletRef : " + String.valueOf(_info) + " .. " 
			   + super.toString();
	}
	/*
	 * Reference Management
	 * @see com.ibm.awb.weakref.Ref
	 */
	public void writeInfo(ObjectOutputStream s) throws IOException {
		try {
			s.writeObject(_name);
			s.writeObject(_ticket.getDestination().toString());
		} catch (Exception ex) {
			s.writeObject(null);
			s.writeObject(null);
		} 
	}
}
