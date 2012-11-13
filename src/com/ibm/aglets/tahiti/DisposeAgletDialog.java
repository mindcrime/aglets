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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import net.sourceforge.aglets.util.gui.GUICommandStrings;

import com.ibm.aglet.AgletProxy;

/**
 * Class RemoveAgletDialog represents the dialog for removing an Aglet instance.
 * 
 * @version 1.05 96/03/28
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class DisposeAgletDialog extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8146917115337440376L;
	/**
	 * A list of the agent proxies that must be disposed.
	 */
	private LinkedList<AgletProxy> proxies = null;

	/*
	 * Constructs the remove Aglet window.
	 */
	DisposeAgletDialog(final MainWindow parent, final AgletProxy proxies[]) {
		super(parent);

		// build up a list from the proxy array
		if ((proxies != null) && (proxies.length > 0)) {
			final LinkedList<AgletProxy> proxyList = new LinkedList<AgletProxy>();

			for (final AgletProxy proxie : proxies)
				proxyList.add(proxie);

			// add the information about the proxies
			showAgletProxies(proxyList);
			this.proxies = proxyList;

			// add a text
			showMessage("Please confirm the dispose operation over the "
					+ proxies.length + " agents");
			pack();
		}
	}

	DisposeAgletDialog(final MainWindow parent, final AgletProxy proxy) {
		super(parent);

		// create a new list of one element
		proxies = new LinkedList<AgletProxy>();
		proxies.add(proxy);

		showAgletProxies(proxies);

		// add a text
		showUserMessage();
		pack();

	}

	DisposeAgletDialog(final MainWindow parent, final LinkedList<AgletProxy> proxies) {
		super(parent);

		// store the list of proxies
		this.proxies = proxies;

		// show the list of the agent that I'm going to work on
		showAgletProxies(proxies);

		// add a text
		showUserMessage();
		pack();

	}

	/*
	 * Disposes the selected Aglet.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)
				&& (proxies != null)) {
			// dispose
			final MainWindow mWindow = getMainWindow();
			setVisible(false);

			// iterate on each aglet
			final Iterator iter = proxies.iterator();
			while ((iter != null) && iter.hasNext()) {
				final AgletProxy currentProxy = (AgletProxy) iter.next();
				mWindow.disposeAglet(currentProxy);

			}

			dispose();

		} else
			super.actionPerformed(event);

	}

	/**
	 * Shows a user message for asking confirmation.
	 * 
	 */
	protected void showUserMessage() {
		final String localizedString = translator.translate(baseKey
				+ ".userMessage");
		showMessage(localizedString);
	}
}
