package com.ibm.aglets;

/*
 * @(#)AgletTimer.java
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
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Date;
import java.util.Enumeration;

import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.maf.MAFUtil;

/**
 * The <tt>AgletTimer</tt> class is the time manager.
 * 
 * @version 1.00 96/07/08
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */

final class AgletTimer implements Runnable {

	/*
	 * aglet context
	 */
	private AgletContextImpl _context = null;

	/*
	 * Timer request chain.
	 */
	private DeactivationInfo top = null;

	/*
	 * Handler
	 */
	Thread handler = null;

	// -- Create an aglet timer.
	AgletTimer(final AgletContextImpl context) {
		_context = context;
		top = new DeactivationInfo(null, 0, null);
		top.wakeup = 0;
		top.next = null;
	}

	synchronized void add(final DeactivationInfo dinfo) {

		if (dinfo.wakeup == 0) {

			// ignore
			// this aglet will be activated after rebooting.
			return;
		}

		DeactivationInfo tmp;

		for (tmp = top; (tmp.next != null)
		&& (tmp.next.wakeup < dinfo.wakeup); tmp = tmp.next) {
		}
		dinfo.next = tmp.next;
		tmp.next = dinfo;
		notifyAll();
	}

	synchronized void destroy() {
		if (handler != null) {
			handler.stop();
			handler = null;
		}
	}

	public void list(final PrintStream out) {
		System.out.println("======= Deactivated Aglets ==========");
		for (DeactivationInfo t = top.next; t != null; t = t.next) {
			System.out.println(t.key + " : " + new Date(t.wakeup));
		}
	}

	synchronized DeactivationInfo popInfo() {
		if (top.next != null) {
			final DeactivationInfo info = top.next;

			top.next = top.next.next;
			return info;
		}
		return null;
	}

	/*
	 * recover timer state from
	 */
	void recoverTimer(final Persistence persistence) throws AgletException {
		final Enumeration e = persistence.entryKeys();

		while (e.hasMoreElements()) {
			final String key = (String) e.nextElement();
			final PersistentEntry entry = persistence.getEntry(key);
			ObjectInputStream in = null;

			try {
				in = new ObjectInputStream(entry.getInputStream());

				// tentative
				final DeactivationInfo dinfo = (DeactivationInfo) in.readObject();

				//
				// Construct message manager for deactivated aglet
				//

				final MessageManagerImpl mm = (MessageManagerImpl) in.readObject();

				mm.setState(MessageManagerImpl.DEACTIVATED);

				final LocalAgletRef ref = new LocalAgletRef(_context);

				ref.setName(dinfo.agent_name);

				ref.setMessageManager(mm);

				// Class Names
				in.readObject();

				final int len = in.readInt();
				final byte[] agent = new byte[len];

				in.readFully(agent);

				final AgletReader reader = new AgletReader(agent);

				reader.readInfo(ref);

				if (dinfo.wakeup == 0) {
					dinfo.wakeup = System.currentTimeMillis();
				}

				/*
				 * if (key.equalsIgnoreCase(ref.info.getAgletID().toString() )
				 * == false) { System.out.println("wrong AgletID in reading:" +
				 * key + " != " + ref.info.getAgletID()); // in.close();
				 * continue; }
				 */
				ref.proxy = new AgletProxyImpl(ref);
				ref.validate(_context, LocalAgletRef.INACTIVE);

				add(dinfo);

				// new Event(key, dinfo.wakeup, 0, 0, 0, 0, 0));
				// in.close();
			} catch (final StreamCorruptedException ex) {
				continue;
			} catch (final ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (final IOException ex) {
				ex.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (final Exception ex) {
					}
				}
			}
		}
	}

	synchronized void removeInfo(final String target_key) {
		DeactivationInfo tmp;

		for (tmp = top; tmp.next != null; tmp = tmp.next) {

			if (tmp.next.key.equals(target_key)) {
				tmp.next = tmp.next.next;
				return;
			}
		}
	}

	@Override
	public void run() {
		long sleeptime = 0;

		while (true) {
			final DeactivationInfo info = topInfo();

			sleeptime = info.wakeup - System.currentTimeMillis();
			if (sleeptime > 0) {
				synchronized (this) {
					try {
						this.wait(sleeptime);
					} catch (final InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			} else {
				final AgletID id = MAFUtil.toAgletID(info.agent_name);

				try {
					final com.ibm.aglet.AgletProxy proxy = _context.getAgletProxy(id);

					if (proxy != null) {
						proxy.activate();
					} else {
						System.out.println("Proxy Not Found");
						removeInfo(info.key);
					}
				} catch (final IOException ex) {
					ex.printStackTrace();
				} catch (final AgletException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	synchronized void start() {
		if (handler == null) {
			handler = new Thread(this);
			handler.start();
		}
	}

	synchronized DeactivationInfo topInfo() {
		while (top.next == null) {
			try {
				this.wait();
			} catch (final InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return top.next;
	}
}
