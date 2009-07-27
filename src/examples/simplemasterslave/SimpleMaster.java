package examples.simplemasterslave;

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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.*;
import com.ibm.agletx.patterns.*;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SimpleMaster extends Aglet {
	Vector urllist = null;
	String SlaveClassName = "examples.simplemasterslave.SimpleSlave";

	private void addURL(URL url) {
		urllist.addElement(url);
	}
	private void createGUI() {
		CommandWindow cm = new CommandWindow(getProxy());

		cm.pack();
		cm.setSize(cm.getPreferredSize());
		cm.setVisible(true);
	}
	private void createSlave() {
		try {
			Slave.create(getCodeBase(), SlaveClassName, getAgletContext(), 
						 this, getURLList(), new String());

		} catch (Exception e) {
			System.out.println("Error:" + e.getMessage());
		} 
	}
	private Vector getURLList() {
		return urllist;
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("go")) {
			createSlave();
			return true;
		} else if (msg.sameKind("add")) {
			addURL((URL)msg.getArg());
			return true;
		} else if (msg.sameKind("remove")) {
			removeURL(((Integer)msg.getArg()).intValue());
			return true;
		} else if (msg.sameKind("getlist")) {
			msg.sendReply(getURLList());
			return true;
		} else {
			return false;
		}
	}
	public void onCreation(Object o) {
		urllist = new Vector();
		createGUI();
	}
	private void removeURL(int i) {
		urllist.removeElementAt(i);
	}
}
