package examples.watcher;

/*
 * @(#)ProxyWatcher.java
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

import java.net.URL;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import com.ibm.aglet.*;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.*;

/**
 * <tt> ProxyWatcher </tt> is an aglet which dispatches a slave aglet and
 * monitors proxies of the remote context. This is an example of
 * these features in Aglets.
 * 
 * <ol>
 * <li> remote messaging
 * <li> delegation event model
 * <li> concurrency control by <tt> waitMessage()/notifyMessage() </tt>
 * <li> persistency
 * <li> activation by message
 * <li> remote control of an aglet.
 * </ol>
 * Please use JDK1.1 or later to compile these classes.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:40 $
 * @author      Mitsuru Oshima
 * @see examples.watcher.WatcherSlave
 * @updated by Shintaro Kosugi $Date: 98/11/24
 */
public class ProxyWatcher extends Aglet {
	transient AgletContext ac;
	WatcherFrame frame;
	AgletProxy slave = null;

	public void go(String address) {
		try {

			// 
			// Creates another aglet
			// 
			if (slave == null) {
				slave = ac.createAglet(getCodeBase(), 
									   "keio.ics.nak.watcher.WatcherSlave", 
									   getAgletID());
			} 

			// 
			// Obtain the remote proxy.
			// 
			Message gonext = new Message("gonext", new URL(address));

			// update the proxy
			slave = (AgletProxy)slave.sendMessage(gonext);

		} catch (Exception ex) {
			if (slave != null) {
				try {
					slave.dispose();
				} catch (Exception exx) {
					exx.printStackTrace();
				} 
			} 
			ex.printStackTrace();
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("update")) {
			String s = String.valueOf(msg.getArg());

			frame.update(s);
		} else {
			return false;
		}
		return true;
	}
	public void move(String address) {
		try {
			URL dest = new URL(address);

			slave = slave.dispatch(dest);
		} catch (AgletException ex) {
			ex.printStackTrace();
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
		} 
	}
	public void onCreation(Object o) {
		ac = getAgletContext();
		frame = new WatcherFrame(this);
		frame.pack();
		frame.setVisible(true);
	}
	public void sendMessage(Message msg) {
		if (slave != null) {
			try {

				// 
				// Remote messaging.
				// 
				slave.sendAsyncMessage(msg);
			} catch (InvalidAgletException ex) {
				ex.printStackTrace();
			} 
		} 
	}
	public void terminate() {
		if (slave == null) {
			return;
		} 
		try {

			// 
			// you can call the dispose method on the remote aglet.
			// 
			slave.dispose();
		} catch (InvalidAgletException ex) {
			ex.printStackTrace();
		} 
	}
}
