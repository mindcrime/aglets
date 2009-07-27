package examples.finder;

/*
 * @(#)HostCollector.java
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
 * The HostCollector carries a list of aglet server names.
 * When it moves to another server, it adds current server address
 * to the list. If there is no HostList aglet is running, it creates
 * an instance of HostList on that server. If a HostList aglet is running,
 * it appends a list of server name to list in the HostList and
 * then get the list belongs to the HostList.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:41 $
 * @author      Yoshiaki Mima
 * @see examples.finder.HostList
 */

public class HostCollector extends Aglet implements MobilityListener {
	Hashtable hostList;

	public void appendList(Hashtable list) {
		for (Enumeration e = list.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();

			if (hostList.get(key) == null) {
				System.out.println("new: " + key);
				hostList.put(key, "new");
			} else {
				System.out.println("key: " + key + " value: " 
								   + hostList.get(key));
			} 
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			try {
				System.out.println(hostList);
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
			} 

			ap.sendMessage(new Message("append", hostList));
			Hashtable list = 
				(Hashtable)ap.sendMessage(new Message("getlist"));

			if (list != null) {
				appendList(list);
			} 
		} catch (Exception e) {
			System.out.println(e);
		} 
	}
	public void onCreation(Object init) {
		hostList = new Hashtable();
		AgletProxy ap = (AgletProxy)getAgletContext().getProperty("hostlist");

		hostList.put(getAgletContext().getHostingURL().toString(), "running");

		try {
			if ((ap == null) ||!ap.isValid()) {
				ap = getAgletContext().createAglet(getCodeBase(), 
												   "examples.finder.HostList", 
												   hostList);
			} 
		} catch (Exception e) {
			System.out.println(e);
		} 

		addMobilityListener(this);
	}
	public void onDispatching(MobilityEvent event) {}
	public void onRetraction(MobilityEvent event) {}
	public void onReverting(MobilityEvent event) {}
}
