package com.ibm.aglets.tahiti;

/*
 * @(#)DispatchAgletDialog.java
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletProxy;
import com.ibm.awb.misc.Resource;

/**
 * Class DispatchAgletDialog represents the dialog for dispatching an Aglet.
 * 
 * @version 1.02 $Date: 2009/07/28 07:04:52 $
 * @author Danny B. Lange Mitsuru Oshima
 */

final class DispatchAgletDialog extends TahitiDialog implements ActionListener,
ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7705281778334775710L;

	/*
	 * The proxy to be dispatched.
	 */
	private AgletProxy proxy = null;

	private JTextField remoteURL = null;
	private AgletListPanel<URL> urlList = null;
	private JButton addURL = null;
	private JButton removeURL = null;

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	DispatchAgletDialog(final MainWindow parent, final AgletProxy proxy) {
		super(parent);

		// store the proxy to dispatch
		this.proxy = proxy;

		// create components
		remoteURL = JComponentBuilder.createJTextField(40, "atp://", baseKey
				+ ".remoteURL");
		addURL = JComponentBuilder.createJButton(baseKey + ".addURL", GUICommandStrings.ADD_COMMAND, this);
		removeURL = JComponentBuilder.createJButton(baseKey
				+ ".removeURL", GUICommandStrings.REMOVE_COMMAND, this);
		urlList = new AgletListPanel<URL>();

		// create the north panel
		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		final JPanel northPanel1 = new JPanel();
		northPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JLabel label = JComponentBuilder.createJLabel(baseKey + ".URL");
		northPanel1.add(label);
		northPanel1.add(remoteURL);
		northPanel.add(northPanel1, BorderLayout.NORTH);
		final JPanel northPanel2 = new JPanel();
		northPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
		northPanel2.add(addURL);
		northPanel2.add(removeURL);
		northPanel.add(northPanel2, BorderLayout.SOUTH);
		this.add(northPanel, BorderLayout.NORTH);

		// the center panel will be the agent list
		urlList.setTitleBorder(translator.translate(baseKey
				+ ".URL.title"));
		this.add(urlList, BorderLayout.CENTER);

		pack();

	}

	/*
	 * Creates an Aglet dispatch dialog.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		try {
			if (GUICommandStrings.ADD_COMMAND.equals(command)) {
				final URL url = new URL(remoteURL.getText());
				urlList.addItem(url);
			} else if (GUICommandStrings.REMOVE_COMMAND.equals(command)) {
				final URL url = new URL(remoteURL.getText());
				urlList.removeItem(url);
			} else if (GUICommandStrings.OK_COMMAND.equals(command)) {
				setVisible(false);
				getMainWindow().dispatchAglet(proxy, new URL(remoteURL.getText()));
				dispose();
			} else
				super.actionPerformed(event);

		} catch (final MalformedURLException e) {
			logger.error("Exception caught while converting a string to an url", e);
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".error.URL"), translator.translate(baseKey
							+ ".error.URL.title"), JOptionPane.ERROR_MESSAGE);

		}

	}

	/*
	 * Adds an item to the list
	 */
	protected final void addURL() {
		try {
			final String url = remoteURL.getText();

			// check if the url is valid
			if ((url == null) || (url.length() == 0))
				return;
			else
				urlList.addItem(new URL(url));
		} catch (final MalformedURLException e) {
			logger.error("Exception caught while converting a string to an url", e);
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".error.URL"), translator.translate(baseKey
							+ ".error.URL.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void dispose() {
		storeURLList();
		super.dispose();
	}

	// Handles list box selections.
	//
	@Override
	public void itemStateChanged(final ItemEvent event) {
		if (event == null)
			return;

		remoteURL.setText(urlList.getSelectedItem().toString());

	}

	/*
	 * Delete an item from the list
	 */
	protected final void removeURL() {
		try {
			final String url = remoteURL.getText();

			// check if the url is valid
			if ((url == null) || (url.length() == 0))
				return;
			else
				urlList.removeItem(new URL(url));
		} catch (final MalformedURLException e) {
			logger.error("Exception caught while converting a string to an url", e);
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".error.URL"), translator.translate(baseKey
							+ ".error.URL.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	private void storeURLList() {
		synchronized (urlList) {
			final int num = urlList.getItemCount();
			String addressList = "";

			for (int i = 0; i < num; i++) {
				addressList += (urlList.getItem(i).toString() + " ");
			}
			final Resource res = Resource.getResourceFor("aglets");

			res.setResource("aglets.addressbook", addressList);
			res.save("Tahiti");
		}
	}
}
