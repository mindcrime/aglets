package net.sourceforge.aglets.examples.simplemasterslave;

/*
 * @(#)SimpleMaster.java
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
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.patterns.Slave;

public class SimpleMaster extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2916763871556793792L;
	Vector urllist = null;
	String SlaveClassName = "examples.simplemasterslave.SimpleSlave";

	private void addURL(final URL url) {
		urllist.addElement(url);
	}

	private void createGUI() {
		final CommandWindow cm = new CommandWindow(getProxy());

		cm.pack();
		cm.setSize(cm.getPreferredSize());
		cm.setVisible(true);
	}

	private void createSlave() {
		try {
			Slave.create(getCodeBase(), SlaveClassName, getAgletContext(), this, getURLList(), new String());

		} catch (final Exception e) {
			System.out.println("Error:" + e.getMessage());
		}
	}

	private Vector getURLList() {
		return urllist;
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("go")) {
			createSlave();
			return true;
		} else if (msg.sameKind("add")) {
			addURL((URL) msg.getArg());
			return true;
		} else if (msg.sameKind("remove")) {
			removeURL(((Integer) msg.getArg()).intValue());
			return true;
		} else if (msg.sameKind("getlist")) {
			msg.sendReply(getURLList());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onCreation(final Object o) {
		urllist = new Vector();
		createGUI();
	}

	private void removeURL(final int i) {
		urllist.removeElementAt(i);
	}
}
