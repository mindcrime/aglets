package examples.finder;

/*
 * @(#)HostTravellor.java
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
import java.util.Vector;
import java.util.Enumeration;

/**
 * The HostTravellor carries a list of aglet server names.
 * When it moves to another server, it adds current server address
 * to the list. If there is no HostList aglet is running, it creates
 * an instance of HostList on that server. If a HostList aglet is running,
 * it appends a list of server name to list in the HostList and
 * then get the list belongs to the HostList.
 * 
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      Yoshiaki Mima
 * @see examples.finder.HostList
 */

public class HostTravellor extends Aglet implements MobilityListener {
	Hashtable hostList;
	Vector visitOrder = new Vector();
	int nextVisit = 0;
	int count = 0;

	public void appendList(Hashtable list) {
		for (Enumeration e = list.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();

			if (hostList.get(key) == null) {
				hostList.put(key, "new");
			} 
		} 
	}
	public void goNext() {
		String next = "";

		try {
			while (true) {
				try {
					if (nextVisit >= visitOrder.size()) {
						nextVisit = 0;
						count++;
						setText("reset");
						Thread.sleep(5000);
					} 

					next = (String)visitOrder.elementAt(nextVisit);
					setText(count + "> goto: " + next);
					Thread.sleep(2000);

					dispatch(new URL((String)visitOrder
						.elementAt(nextVisit++)));
				} catch (Exception e) {
					System.out.println("dispatch to " + next + " failed");
				} 
			} 
		} catch (Exception e) {
			System.out.println(e);
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
		} else if (msg.sameKind("shutdown")) {
			try {
				deactivate(0);
			} catch (Exception e) {}
			return true;
		} else {}
		return true;
	}
	public void onArrival(MobilityEvent event) {
		AgletProxy ap = (AgletProxy)getAgletContext().getProperty("hostlist");

		hostList.put(getAgletContext().getHostingURL().toString(), "running");

		try {
			if ((ap == null) ||!ap.isValid()) {
				ap = getAgletContext().createAglet(getCodeBase(), 
												   "examples.finder.HostList", 
												   hostList);
			} else {
				ap.sendMessage(new Message("append", hostList));
				Hashtable list = 
					(Hashtable)ap.sendMessage(new Message("getlist"));

				if (list != null) {
					appendList(list);
					setVisitOrder();
				} 
			} 

			// go next
			goNext();
		} catch (Exception e) {
			System.out.println(e);
		} 
	}
	public void onCreation(Object init) {
		hostList = new Hashtable();
		AgletProxy ap = (AgletProxy)getAgletContext().getProperty("hostlist");

		hostList.put(getAgletContext().getHostingURL().toString(), "running");
		addMobilityListener(this);

		try {
			if ((ap == null) ||!ap.isValid()) {
				ap = getAgletContext().createAglet(getCodeBase(), 
												   "examples.finder.HostList", 
												   hostList);
			} 

			Hashtable list = 
				(Hashtable)ap.sendMessage(new Message("getlist"));

			if (list != null) {
				appendList(list);
			} 

			// go next
			setVisitOrder();
			goNext();
		} catch (Exception e) {
			System.out.println(e);
		} 
	}
	public void onDispatching(MobilityEvent event) {}
	public void onRetraction(MobilityEvent event) {}
	public void onReverting(MobilityEvent event) {}
	public void setVisitOrder() {
		for (Enumeration e = hostList.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();

			if (key instanceof String) {
				if (visitOrder.indexOf(key) == -1) {
					visitOrder.addElement(key);
					System.out.println("add: " + key);
				} 
			} 
		} 
	}
}
