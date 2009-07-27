package examples.protection;

/*
 * @(#)ProtectionAglet.java
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

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.security.Protections;
import com.ibm.aglet.security.AgletProtection;

/**
 * <tt>ProtectionAglet</tt> is a test aglet for selecting actions
 * to be protected or not.
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:42 $
 * @author      ONO Kouichi
 */
public class ProtectionAglet extends Aglet {
	private AgletID _aid = null;			// Original Aglet
	private AgletProxy _target = null;		// Target Aglet Proxy
	private String _owner = null;			// Aglet Owner
	private ProtectionDialog _protectionDialog = null;

	void setTarget(AgletProxy target) {
		_target = target;
	}

	void cloneTarget() {
		try {
			_target.clone();
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	void createTarget() {
		if (_target != null && _target.isValid()) {
			try {
				_target.dispose();
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
		try {
			_target = (AgletProxy)
				getAgletContext().createAglet(null,
											  "examples.protection.TargetAglet",
											  _owner);
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	void deactivateTarget(long duration) {
		try {
			_target.deactivate(duration);
		} catch (InvalidAgletException ex) {
			System.err.println(ex.toString());
		} catch (IOException ex) {
			System.err.println(ex.toString());
		} 
	}

	void dispatchTarget(String destination) {
		URL dest = null;

		try {
			dest = new URL(destination);
		} catch (MalformedURLException ex) {
			System.err.println(ex.toString());
			return;
		} 
		AgletProxy ap = null;

		try {
			ap = _target.dispatch(dest);
		} catch (IOException ex) {
			System.err.println(ex.toString());
		} catch (AgletException ex) {
			System.err.println(ex.toString());
		} 
		if (ap != null) {
			_target = ap;
		} 
	}

	void disposeTarget() {
		try {
			_target.dispose();
		} catch (InvalidAgletException ex) {
			System.err.println(ex.toString());
		} 
	}

	void retractTarget() {
		AgletInfo info = null;
		URL source = null;

		try {
			info = _target.getAgletInfo();
			source = new URL(_target.getAddress());
		} catch (InvalidAgletException ex) {
			System.err.println(ex.toString());
			return;
		} catch (MalformedURLException ex) {
			System.err.println(ex.toString());
			return;
		} 
		AgletID aid = info.getAgletID();
		AgletProxy ap = null;

		try {
			ap = getAgletContext().retractAglet(source, aid);
		} catch (IOException ex) {
			System.err.println(ex.toString());
		} catch (AgletException ex) {
			System.err.println(ex.toString());
		} 
		if (ap != null) {
			_target = ap;
		}
	}

	void setAgletProtectionActions(String name, String actions) {
		if (_target != null) {
			try {
				name = _owner;
				Message msg = new Message("setProtections");
				msg.setArg("name", name);
				msg.setArg("actions", actions);
				_target.sendMessage(msg);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void init() {
		AgletInfo info = getAgletInfo();

		_owner = info.getAuthorityName();
		String label = "Protection Dialog: " +
			info.getAgletID() + "(" + _owner + ")";
		_protectionDialog = new ProtectionDialog(this, label);
		_protectionDialog.pack();
	}

	public void onCreation(Object init) {
		_aid = getAgletID();
		init();
	}

	public boolean handleMessage(Message message) {
		if (message.sameKind("dialog")) {
			_protectionDialog.show();
		} 
		return true;
	}
}
