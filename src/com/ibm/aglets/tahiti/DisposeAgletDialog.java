package com.ibm.aglets.tahiti;

/*
 * @(#)DisposeAgletDialog.java
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

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class RemoveAgletDialog represents the dialog for removing an Aglet
 * instance.
 * 
 * @version     1.05    96/03/28
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class DisposeAgletDialog extends TahitiDialog 
	implements ActionListener {

	/*
	 * The proxy of the Aglet that is to be disposed.
	 */
	private AgletProxy[] _proxies = null;

	/*
	 * Constructs the remove Aglet window.
	 */
	DisposeAgletDialog(MainWindow parent, AgletProxy proxies[]) {
		super(parent, "Dispose an Aglet", false);

		String msg[] = new String[proxies.length];

		for (int i = 0; i < proxies.length; i++) {
			try {
				msg[i] = proxies[i].getAgletClassName();
			} catch (InvalidAgletException ex) {
				msg[i] = "Invalid Aglet";
			} 
		} 

		add("North", new Label("Dispose Aglet", Label.CENTER));
		add("Center", new MessagePanel(msg, Label.LEFT, false));

		addButton("Dispose", this);
		addCloseButton(null);

		_proxies = proxies;
	}
	/*
	 * Changes the look of the remove Aglet window to an error message window.
	 * void setError(String message) {
	 * setMessage("ERROR\n" + message);
	 * setButtons( OKAY );
	 * }
	 */

	/*
	 * Disposes the selected Aglet.
	 */
	public void actionPerformed(ActionEvent ev) {
		if (_proxies != null && _proxies.length > 0) {
			getMainWindow().disposeAglet(_proxies[0]);
			dispose();
		} 
	}
}
