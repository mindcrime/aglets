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

import java.util.*;
import java.io.*;

// import java.awt.Event;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.maf.MAFUtil;
import java.util.Enumeration;

/**
 * The <tt>AgletTimer</tt> class is the time manager.
 * 
 * @version     1.00    96/07/08
 * @author      Gaku Yamamoto
 * @author	Mitsuru Oshima
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
	AgletTimer(AgletContextImpl context) {
		_context = context;
		top = new DeactivationInfo(null, 0, null);
		top.wakeup = 0;
		top.next = null;
	}
	synchronized void add(DeactivationInfo dinfo) {

		if (dinfo.wakeup == 0) {

			// ignore
			// this aglet will be activated after rebooting.
			return;
		} 

		DeactivationInfo tmp;

		for (tmp = top; tmp.next != null && tmp.next.wakeup < dinfo.wakeup; 
				tmp = tmp.next) {}
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
	public void list(PrintStream out) {
		System.out.println("======= Deactivated Aglets ==========");
		for (DeactivationInfo t = top.next; t != null; t = t.next) {
			System.out.println(t.key + " : " + new Date(t.wakeup));
		} 
	}
	synchronized DeactivationInfo popInfo() {
		if (top.next != null) {
			DeactivationInfo info = top.next;

			top.next = top.next.next;
			return info;
		} 
		return null;
	}
	/*
	 * recover timer state from
	 */
	void recoverTimer(Persistence persistence) throws AgletException {
		Enumeration e = persistence.entryKeys();

		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			PersistentEntry entry = persistence.getEntry(key);
			ObjectInputStream in = null;

			try {
				in = new ObjectInputStream(entry.getInputStream());

				// tentative
				DeactivationInfo dinfo = (DeactivationInfo)in.readObject();

				// 
				// Construct message manager for deactivated aglet
				// 

				MessageManagerImpl mm = (MessageManagerImpl)in.readObject();

				mm.setState(MessageManagerImpl.DEACTIVATED);

				LocalAgletRef ref = new LocalAgletRef(_context);

				ref.setName(dinfo.agent_name);

				ref.setMessageManager(mm);

				// Class Names
				in.readObject();

				int len = in.readInt();
				byte[] agent = new byte[len];

				in.readFully(agent);

				AgletReader reader = new AgletReader(agent);

				reader.readInfo(ref);

				if (dinfo.wakeup == 0) {
					dinfo.wakeup = System.currentTimeMillis();
				} 

				/*
				 * if (key.equalsIgnoreCase(ref.info.getAgletID().toString() )
				 * == false) {
				 * System.out.println("wrong AgletID in reading:" +
				 * key + " != " +
				 * ref.info.getAgletID());
				 * // in.close();
				 * continue;
				 * }
				 */
				ref.proxy = new AgletProxyImpl(ref);
				ref.validate(_context, LocalAgletRef.INACTIVE);

				add(dinfo);

				// new Event(key, dinfo.wakeup, 0, 0, 0, 0, 0));
				// in.close();
			} catch (StreamCorruptedException ex) {
				continue;
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
			finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ex) {}
				} 
			} 
		} 
	}
	synchronized void removeInfo(String target_key) {
		DeactivationInfo tmp;

		for (tmp = top; tmp.next != null; tmp = tmp.next) {

			if (tmp.next.key.equals(target_key)) {
				tmp.next = tmp.next.next;
				return;
			} 
		} 
	}
	public void run() {
		long sleeptime = 0;

		while (true) {
			DeactivationInfo info = topInfo();

			sleeptime = info.wakeup - System.currentTimeMillis();
			if (sleeptime > 0) {
				synchronized (this) {
					try {
						wait(sleeptime);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					} 
				} 
			} else {
				AgletID id = MAFUtil.toAgletID(info.agent_name);

				try {
					com.ibm.aglet.AgletProxy proxy = 
						_context.getAgletProxy(id);

					if (proxy != null) {
						proxy.activate();
					} else {
						System.out.println("Proxy Not Found");
						removeInfo(info.key);
					} 
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (AgletException ex) {
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
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			} 
		} 
		return top.next;
	}
}
