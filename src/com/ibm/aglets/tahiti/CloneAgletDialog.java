package com.ibm.aglets.tahiti;

/*
 * @(#)CloneAgletDialog.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletInfo;

import com.ibm.aglets.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Label;

/**
 * Class CloneAgletDialog is the dialog for cloning an Aglet instance.
 * 
 * @version     1.05    96/03/28
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */

final class CloneAgletDialog extends TahitiDialog implements ActionListener {

	/*
	 * The proxy of the Aglet that is to be cloned
	 */
	private AgletProxy _proxy = null;

	/*
	 * Constructs the clone Aglet window.
	 */
	CloneAgletDialog(MainWindow parent, AgletProxy proxy) {
		super(parent, "Clone an Aglet", true);
		_proxy = proxy;

		String msg = "Invalid Aglet";

		try {
			AgletInfo info = proxy.getAgletInfo();

			msg = (proxy == null ? "No Aglet selected" 
				   : info.getAgletClassName());
		} catch (InvalidAgletException ex) {}

		add("North", new Label("Clone Aglet", Label.CENTER));
		add("Center", new MessagePanel(msg, Label.CENTER, false));

		addButton("Clone", this);
		addCloseButton("Cancel");
	}
	public void actionPerformed(ActionEvent ev) {
		setVisible(false);
		dispose();
		getMainWindow().cloneAglet(_proxy);
	}
}
