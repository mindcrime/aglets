package net.sourceforge.aglets.examples.watcher;

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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;

/**
 * <tt> ProxyWatcher </tt> is an aglet which dispatches a slave aglet and
 * monitors proxies of the remote context. This is an example of these features
 * in Aglets.
 * 
 * <ol>
 * <li>remote messaging
 * <li>delegation event model
 * <li>concurrency control by <tt> waitMessage()/notifyMessage() </tt>
 * <li>persistency
 * <li>activation by message
 * <li>remote control of an aglet.
 * </ol>
 * Please use JDK1.1 or later to compile these classes.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author Shintaro Kosugi
 * @see net.sourceforge.aglets.examples.watcher.WatcherSlave
 */
public class ProxyWatcher extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6509197929548594627L;
	transient AgletContext ac;
	WatcherFrame frame;
	AgletProxy slave = null;

	public void go(final String address) {
		try {

			//
			// Creates another aglet
			//
			if (slave == null) {
				slave = ac.createAglet(getCodeBase(), "keio.ics.nak.watcher.WatcherSlave", getAgletID());
			}

			//
			// Obtain the remote proxy.
			//
			final Message gonext = new Message("gonext", new URL(address));

			// update the proxy
			slave = (AgletProxy) slave.sendMessage(gonext);

		} catch (final Exception ex) {
			if (slave != null) {
				try {
					slave.dispose();
				} catch (final Exception exx) {
					exx.printStackTrace();
				}
			}
			ex.printStackTrace();
		}
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("update")) {
			final String s = String.valueOf(msg.getArg());

			frame.update(s);
		} else {
			return false;
		}
		return true;
	}

	public void move(final String address) {
		try {
			final URL dest = new URL(address);

			slave = slave.dispatch(dest);
		} catch (final AgletException ex) {
			ex.printStackTrace();
		} catch (final java.io.IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void onCreation(final Object o) {
		ac = getAgletContext();
		frame = new WatcherFrame(this);
		frame.pack();
		frame.setVisible(true);
	}

	public void sendMessage(final Message msg) {
		if (slave != null) {
			try {

				//
				// Remote messaging.
				//
				slave.sendAsyncMessage(msg);
			} catch (final InvalidAgletException ex) {
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
		} catch (final InvalidAgletException ex) {
			ex.printStackTrace();
		}
	}
}
