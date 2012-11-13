package net.sourceforge.aglets.examples.protection;

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
import java.net.MalformedURLException;
import java.net.URL;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;

/**
 * <tt>ProtectionAglet</tt> is a test aglet for selecting actions to be
 * protected or not.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class ProtectionAglet extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8866311642773135301L;
	private AgletID _aid = null; // Original Aglet
	private AgletProxy _target = null; // Target Aglet Proxy
	private String _owner = null; // Aglet Owner
	private ProtectionDialog _protectionDialog = null;

	void cloneTarget() {
		try {
			_target.clone();
		} catch (final Exception ex) {
			System.err.println(ex.toString());
		}
	}

	void createTarget() {
		if ((_target != null) && _target.isValid()) {
			try {
				_target.dispose();
			} catch (final Exception ex) {
				System.err.println(ex.toString());
			}
		}
		try {
			_target = getAgletContext().createAglet(null, "examples.protection.TargetAglet", _owner);
		} catch (final Exception ex) {
			System.err.println(ex.toString());
		}
	}

	void deactivateTarget(final long duration) {
		try {
			_target.deactivate(duration);
		} catch (final InvalidAgletException ex) {
			System.err.println(ex.toString());
		} catch (final IOException ex) {
			System.err.println(ex.toString());
		}
	}

	void dispatchTarget(final String destination) {
		URL dest = null;

		try {
			dest = new URL(destination);
		} catch (final MalformedURLException ex) {
			System.err.println(ex.toString());
			return;
		}
		AgletProxy ap = null;

		try {
			ap = _target.dispatch(dest);
		} catch (final IOException ex) {
			System.err.println(ex.toString());
		} catch (final AgletException ex) {
			System.err.println(ex.toString());
		}
		if (ap != null) {
			_target = ap;
		}
	}

	void disposeTarget() {
		try {
			_target.dispose();
		} catch (final InvalidAgletException ex) {
			System.err.println(ex.toString());
		}
	}

	@Override
	public boolean handleMessage(final Message message) {
		if (message.sameKind("dialog")) {
			_protectionDialog.show();
		}
		return true;
	}

	private void init() {
		final AgletInfo info = getAgletInfo();

		_owner = info.getAuthorityName();
		final String label = "Protection Dialog: " + info.getAgletID() + "("
		+ _owner + ")";
		_protectionDialog = new ProtectionDialog(this, label);
		_protectionDialog.pack();
	}

	@Override
	public void onCreation(final Object init) {
		_aid = getAgletID();
		init();
	}

	void retractTarget() {
		AgletInfo info = null;
		URL source = null;

		try {
			info = _target.getAgletInfo();
			source = new URL(_target.getAddress());
		} catch (final InvalidAgletException ex) {
			System.err.println(ex.toString());
			return;
		} catch (final MalformedURLException ex) {
			System.err.println(ex.toString());
			return;
		}
		final AgletID aid = info.getAgletID();
		AgletProxy ap = null;

		try {
			ap = getAgletContext().retractAglet(source, aid);
		} catch (final IOException ex) {
			System.err.println(ex.toString());
		} catch (final AgletException ex) {
			System.err.println(ex.toString());
		}
		if (ap != null) {
			_target = ap;
		}
	}

	void setAgletProtectionActions(String name, final String actions) {
		if (_target != null) {
			try {
				name = _owner;
				final Message msg = new Message("setProtections");
				msg.setArg("name", name);
				msg.setArg("actions", actions);
				_target.sendMessage(msg);
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	void setTarget(final AgletProxy target) {
		_target = target;
	}
}
