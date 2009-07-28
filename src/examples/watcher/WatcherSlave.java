package examples.watcher;

/*
 * @(#)WatcherSlave.java
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
import com.ibm.aglet.message.MessageManager;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      Mitsuru Oshima
 */
public class WatcherSlave extends Aglet {
	AgletProxy master;
	boolean started = false;

	public String getInfo() {
		AgletContext ac = getAgletContext();
		StringBuffer b = new StringBuffer();

		Enumeration e = ac.getAgletProxies(ACTIVE | INACTIVE);

		while (e.hasMoreElements()) {
			try {
				AgletProxy p = (AgletProxy)e.nextElement();
				AgletInfo info = p.getAgletInfo();

				b.append(info.toString());
				b.append("\n---------\n");
			} catch (InvalidAgletException ex) {
				b.append("[InvalidAglet]\n");
				continue;
			} 
		} 
		return b.toString();
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("start")) {

			// to work around a bug in framework..... sorry.
			msg.sendReply();

			start();
		} else if (msg.sameKind("stop")) {
			stop();

		} else if (msg.sameKind("gonext")) {
			try {
				dispatch((URL)msg.getArg());
			} catch (Exception ex) {
				ex.printStackTrace();
			} 

		} else if (msg.sameKind("sleep")) {

			// sleep at most 10 seconds.
			try {
				deactivate(10 * 1000);
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		} else if (msg.sameKind("getInfo")) {
			msg.sendReply(getInfo());

		} else {
			return false;
		}
		return true;
	}
	public void onCreation(Object o) {
		if (o instanceof AgletID) {
			master = getAgletContext().getAgletProxy((AgletID)o);
		} else if (o instanceof AgletProxy) {
			master = (AgletProxy)o;
		} else {
			master = null;
		} 

		// 
		// Activate if these messages arrives.
		// 
		getMessageManager().setPriority("start", 
										MessageManager.ACTIVATE_AGLET);
		getMessageManager().setPriority(Message.DISPOSE, 
										MessageManager.ACTIVATE_AGLET);

		// 
		// event listener
		// 
		addPersistencyListener(new PersistencyAdapter() {
			public void onActivation(PersistencyEvent ev) {
				setText("wakeup");

				// 
				// Start monitoring if it was monitoring
				// 
				if (started) {
					started = false;
					start();
				} 
			} 
		});
		addMobilityListener(new MobilityAdapter() {
			public void onArrival(MobilityEvent ev) {
				setText("arrived");

				// 
				// Start monitoring if it was monitoring
				// 
				if (started) {
					started = false;
					start();
				} 
			} 
		});
	}
	/**
	 * Start monitoring
	 */
	public void start() {
		if (started || master == null) {
			return;
		} 
		setText("started");
		started = true;

		while (started) {
			try {
				master.sendAsyncMessage(new Message("update", getInfo()));
			} catch (InvalidAgletException ex) {
				ex.printStackTrace();
				dispose();
			} 
			waitMessage(2 * 1000);		// wait two seconds.
		} 
		setText("stopped");
	}
	public void stop() {
		started = false;
		notifyMessage();
	}
}
