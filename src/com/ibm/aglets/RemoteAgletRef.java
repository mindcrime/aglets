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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.NotHandledException;
import com.ibm.aglet.Ticket;
import com.ibm.aglet.message.FutureReply;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.message.MessageException;
import com.ibm.awb.weakref.Ref;
import com.ibm.awb.weakref.WeakRef;
import com.ibm.awb.weakref.WeakRefTable;
import com.ibm.maf.Name;

/**
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
public final class RemoteAgletRef extends WeakRef implements AgletRef {

	static final class RefKey {
		static public boolean equals(final Name n1, final Name n2) {
			if ((n1.authority.length == n2.authority.length)
					&& (n1.identity.length == n2.identity.length)
					&& (n1.agent_system_type == n2.agent_system_type)) {
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
		String address;
		Name name;

		int hash = 0;

		RefKey(final String a, final Name n) {
			address = a;
			name = n;
			for (final byte element : n.identity) {
				hash += (hash * 37) + element;
			}
			hash += a.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof RefKey) {
				final RefKey rk = (RefKey) obj;

				return address.equals(rk.address)
				&& equals(rk.name, name);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	static class RemoteRefTable extends WeakRefTable {
		synchronized public RemoteAgletRef getRef(final Ticket ticket, final Name n) {
			final Object key = RemoteAgletRef.getRefID(ticket.getDestination(), n);
			RemoteAgletRef ref = (RemoteAgletRef) getWeakRef(key);

			if (ref == null) {
				ref = new RemoteAgletRef(ticket, n);
				super.add(ref);
			}
			ref.referenced();
			return ref;
		}
	}

	private static RemoteRefTable _table = new RemoteRefTable();

	static public RemoteAgletRef getAgletRef(final Ticket ticket, final Name n) {
		return _table.getRef(ticket, n);
	}
	static Object getRefID(final URL address, final Name n) {
		return new RefKey(address.toString(), n);
	}
	static public void showRefTable(final PrintStream out) {
		out.println(_table.toString());
	}

	private Ticket _ticket = null; // target server address

	private AgletInfo _info = null; // info is immutable.

	private Name _name;

	static Message _get_info_message = new Message("_getAgletInfo");

	// # /**
	// # * Gets the allowance: availability of the aglet's resources.
	// # * @return an Allowance object
	// # */
	// # public Allowance getAllowance() {
	// # throw new RuntimeException("Remote Reference doens't have allowance.");
	// # }

	/*
	 * Used by WeakReferefence.
	 */
	public RemoteAgletRef() {
		super(_table);
	}

	/*
	 * Creates an remote aglet ref with a name and address of the aglet
	 */
	RemoteAgletRef(final Ticket t, final Name n) {
		super(_table);
		_ticket = t;
		_name = n;
	}

	/*
	 * Activates the aglet
	 */
	@Override
	public void activate() throws IOException, AgletException {

		// REMIND:
		throw new IllegalAccessError("Cannot activate remote Aglet (for now)");
	}

	@Override
	public void checkValidation() throws InvalidAgletException {
		if (_info == null) {
			getInfo();
		}
	}

	/**
	 * Delegates a message
	 */
	@Override
	synchronized public void delegateMessage(final Message msg)
	throws InvalidAgletException {
		MessageBroker.delegateMessage(_ticket, _name, msg);
	}

	/*
	 * Reference Management
	 * 
	 * @see com.ibm.awb.weakref.WeakRef
	 */
	@Override
	protected Ref findRef() {
		return LocalAgletRef.getAgletRef(_name);
	}

	/*
	 * Class Method
	 */
	@Override
	protected Ref findRef(final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {
		final Name t_name = (Name) s.readObject();
		final String address = (String) s.readObject();

		// lookup local references
		final AgletRef ref = LocalAgletRef.getAgletRef(t_name);

		if (ref != null) {
			return ref;
		}

		// lookup remote references
		return RemoteAgletRef.getAgletRef(new Ticket(address), t_name);
	}

	/**
	 * Gets the address of the target aglet
	 */
	@Override
	public String getAddress() throws InvalidAgletException {
		if (_info == null) {
			getInfo();
		}
		return _ticket.getDestination().toString();
	}

	/**
	 * Gets the aglet. If the aglet is access protected it will require the
	 * right key to get access.
	 * 
	 * @return the aglet
	 * @exception SecurityException
	 *                if the current execution is not allowed.
	 */
	@Override
	public Aglet getAglet() throws InvalidAgletException {
		throw new InvalidAgletException("Remote Reference doens't have aglet");
	}

	/**
	 * Gets the information of the aglet
	 * 
	 * @return the AgletInfo of the aglet
	 */
	@Override
	public AgletInfo getAgletInfo() {
		if (_info == null) {
			try {
				getInfo();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
		return _info;
	}

	private void getInfo() throws InvalidAgletException {
		try {
			_info = (AgletInfo) sendMessage(_get_info_message);

			// update
			// _ticket = new Ticket(_info.getDestination(), _ticket.getQoC());
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.out.println("Ticket = " + _ticket);
			System.out.println("name = " + _name.toString());
			throw new InvalidAgletException("Not Found");
		}
	}

	@Override
	public String getRefClassName() {
		return "com.ibm.aglets.RemoteAgletRef";
	}

	@Override
	protected Object getRefID() {
		return getRefID(_ticket.getDestination(), _name);
	}

	/*
	 * 
	 */
	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	/**
	 * Check the state
	 */
	@Override
	public boolean isState(final int s) {
		return false;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	/*
	 * Resumes the aglet
	 */
	@Override
	public void resume() throws AgletException {

		// REMIND:
		throw new IllegalAccessError("Cannot resume remote Aglet (for now)");
	}

	/**
	 * Sends a message in asynchronous way.
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	synchronized public FutureReply sendFutureMessage(final Message msg)
	throws InvalidAgletException {
		return MessageBroker.sendFutureMessage(_ticket, _name, msg);
	}

	/**
	 * Sends a message in synchronous way.
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	public Object sendMessage(final Message msg)
	throws MessageException,
	InvalidAgletException,
	NotHandledException {
		return MessageBroker.sendMessage(_ticket, _name, msg);
	}

	/**
	 * Sends a oneway message
	 * 
	 * @param msg
	 *            the message to send
	 */
	@Override
	synchronized public void sendOnewayMessage(final Message msg)
	throws InvalidAgletException {
		MessageBroker.sendOnewayMessage(_ticket, _name, msg);
	}

	/**
	 * set info
	 */

	/* package */
	void setAgletInfo(final AgletInfo i) {
		_info = i;
	}

	/*
	 * Validation management. Aglet which is not valid is not accessible.
	 */
	@Override
	public String toString() {
		return "RemoteAgletRef : " + String.valueOf(_info) + " .. "
		+ super.toString();
	}

	/*
	 * Reference Management
	 * 
	 * @see com.ibm.awb.weakref.Ref
	 */
	@Override
	public void writeInfo(final ObjectOutputStream s) throws IOException {
		try {
			s.writeObject(_name);
			s.writeObject(_ticket.getDestination().toString());
		} catch (final Exception ex) {
			s.writeObject(null);
			s.writeObject(null);
		}
	}
}
