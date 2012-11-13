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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;

/**
 * Class CloneAgletDialog is the dialog for cloning an Aglet instance.
 * 
 * @version 1.05 96/03/28
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class CloneAgletDialog extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9035713424701354881L;
	/*
	 * The proxy of the Aglet that is to be cloned
	 */
	private AgletProxy proxy = null;

	/*
	 * Constructs the clone Aglet window.
	 */
	CloneAgletDialog(final MainWindow parent, final AgletProxy proxy) {
		super(parent);

		// set the title
		setTitle(JComponentBuilder.getTitle(baseKey));

		// store the proxy
		this.proxy = proxy;

		// try to understand if this is a valid agent
		String message = null;
		AgletInfo info = null;
		try {
			if ((this.proxy == null)
					|| ((info = this.proxy.getAgletInfo()) == null)) {
				// show an error message
				message = translator.translate(baseKey
						+ ".selectionError");
				showMessage(message);
			} else
				// try to get the information about this agent
				message = translator.translate(baseKey
						+ ".confirmMessage");
			showMessage(message);
			showAgletInfo(info);

		} catch (final InvalidAgletException ex) {
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".infoError"), translator.translate(baseKey
							+ ".infoError.title"), JOptionPane.ERROR_MESSAGE);
		} finally {
			pack();
		}

	}

	/**
	 * Manages events from the buttons and other components. If the command
	 * comes from the OK/Clone event that means the user wants to clone the
	 * agent, and thus I clone it and dispose this window, otherwise I leave the
	 * parent frame to manage the event.
	 */
	@Override
	public void actionPerformed(final ActionEvent ev) {
		// check params
		if (ev == null)
			return;

		final String command = ev.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// the user wants to clone the agent
			getMainWindow().cloneAglet(proxy);
			setVisible(false);
			dispose();
		} else
			super.actionPerformed(ev);
	}
}
