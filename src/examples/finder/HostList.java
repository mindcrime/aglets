package examples.finder;

/*
 * @(#)HostList.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;

import java.net.URL;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The HostList keeps a list of aglet server names.
 * When it moves to another server, it automatically disappears.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:41 $
 * @author      Yoshiaki Mima
 * @see examples.finder.HostCollector
 */

public class HostList extends Aglet implements PersistencyListener, 
		MobilityListener {
	Hashtable hostList;

	public void appendList(Hashtable list) {
		for (Enumeration e = list.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();

			if (list.get(key) instanceof String) {
				if (hostList.get(key) == null) {
					System.out.println("new: " + key);
					hostList.put(key, "new");
				} else {

					// System.out.println("key: " + key + " value: " + hostList.get(key));
				} 
			} 
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			try {
				int i = 0;

				System.out.println("HostList -- begin");
				for (Enumeration e = hostList.keys(); e.hasMoreElements(); ) {
					System.out.println(i++ + ": " + e.nextElement());
				} 
				System.out.println("HostList -- end");
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
			return true;
		} else if (msg.sameKind("register")) {
			if (msg.getArg() instanceof String) {
				hostList.put(msg.getArg(), "running");
			} 
			return true;
		} else if (msg.sameKind("append")) {
			if (msg.getArg() instanceof Hashtable) {
				appendList((Hashtable)msg.getArg());
			} 
			return true;
		} else if (msg.sameKind("getlist")) {
			msg.sendReply(hostList);
			return true;
		} else if (msg.sameKind("shutdown")) {
			try {
				deactivate(0);
			} catch (Exception e) {}
			return true;
		} else if (msg.sameKind("dispose")) {
			dispose();
			return true;
		} else {}
		return true;
	}
	public void onActivation(PersistencyEvent event) {
		AgletContext ac = getAgletContext();
		AgletProxy proxy = getProxy();
		AgletProxy ap;

		// check if another HostList registration
		ap = (AgletProxy)ac.getProperty("hostlist");
		if ((ap != null) && (ap.isValid())) {
			try {
				ap.sendMessage(new Message("append", hostList));
			} catch (Exception e) {
				System.out.println(e);
			} 
			dispose();
		} else {
			ac.setProperty("hostlist", proxy);
		} 
	}
	public void onArrival(MobilityEvent event) {
		dispose();
	}
	public void onCreation(Object init) {
		AgletContext ac = getAgletContext();
		AgletProxy proxy = getProxy();
		AgletProxy ap;

		addMobilityListener(this);
		addPersistencyListener(this);

		// check if another HostList registration
		ap = (AgletProxy)ac.getProperty("hostlist");
		if ((ap != null) && (ap.isValid())) {
			if (init instanceof Hashtable) {
				try {
					ap.sendMessage(new Message("append", init));
				} catch (Exception e) {
					System.out.println(e);
				} 
			} 
			dispose();
		} else {
			ac.setProperty("hostlist", proxy);

			if (init instanceof Hashtable) {
				hostList = (Hashtable)((Hashtable)init).clone();
			} else {
				hostList = new Hashtable();
			} 
			hostList.put(ac.getHostingURL().toString(), "running");
		} 
	}
	public void onDeactivating(PersistencyEvent event) {

		// unregistration
		getAgletContext().setProperty("hostlist", null);
	}
	public void onDispatching(MobilityEvent event) {}
	public void onRetraction(MobilityEvent event) {}
	public void onReverting(MobilityEvent event) {}
}
