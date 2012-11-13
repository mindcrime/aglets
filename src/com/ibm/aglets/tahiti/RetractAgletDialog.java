package com.ibm.aglets.tahiti;

/*
 * @(#)RetractAgletDialog.java
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.Aglets;
import com.ibm.awb.misc.Resource;

/**
 * Class RetractAgletDialog represents the dialog for Retracting an Aglet.
 * 
 * @version 1.02 $Date: 2009/07/28 07:04:53 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class RetractAgletDialog extends TahitiDialog implements ActionListener,
ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2767078222262785698L;
	private JComboBox servers = null;
	private AgletListPanel<AgletProxy> agletsList = null;
	private JButton refreshProxies = null;

	private String defaultServerString = null;

	String currentList = null;

	/**
	 * A list of selected proxies that represents the proxy available on the
	 * selected remote context/server.
	 */
	private AgletProxy proxies[] = null;

	/**
	 * The main constructor of this dialog window.
	 * 
	 * @param parent
	 *            the parent frame of the dialog window
	 */
	protected RetractAgletDialog(final MainWindow parent) {
		super(parent);

		// build up a list of agents
		agletsList = new AgletListPanel<AgletProxy>();
		agletsList.setRenderer(new AgletListRenderer(agletsList));

		defaultServerString = translator.translate(baseKey
				+ ".serverHeader");

		// the combobox for the server
		servers = new JComboBox();
		servers.setEditable(true);
		servers.addItemListener(this);
		updateServerList();
		refreshProxies = JComponentBuilder.createJButton(baseKey
				+ ".refreshButton", GUICommandStrings.REFRESH_COMMAND, this);

		// create a north panel
		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".serverLabel");
		northPanel.add(label);
		northPanel.add(servers);
		northPanel.add(refreshProxies);
		this.add(northPanel, BorderLayout.NORTH);

		// add the aglet list panel at the center
		this.add(agletsList, BorderLayout.CENTER);

		// pack
		pack();

	}

	/*
	 * Creates an Aglet retract dialog.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		// if the event comes from the jcombobox (i.e., the user has pressed
		// enter on the combobox) I must refresh the proxy list
		if (event.getSource() == servers) {
			getRemoteProxies();
			updateAgletList();
			return;
		}

		// the action comes from a menu

		final String command = event.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			final AgletProxy selectedProxy = agletsList.getSelectedItem();
			setVisible(false);
			getMainWindow().retractAglet(selectedProxy);
			dispose();
		} else if (GUICommandStrings.REFRESH_COMMAND.equals(command)) {
			getRemoteProxies();
			updateAgletList();
		} else
			super.actionPerformed(event);
	}

	/**
	 * Adds the item editable.
	 * 
	 */
	private void addServerItem() {
		this.addServerItem(servers.getSelectedItem());
	}

	/**
	 * Adds a new item to the server list if not already present.
	 * 
	 * @param serverItem
	 *            the item to add
	 */
	private void addServerItem(final Object serverItem) {
		boolean toInsert = true;

		final int items = servers.getItemCount();
		for (int i = 0; i < items; i++)
			if (servers.getItemAt(i).equals(serverItem))
				toInsert = false;

		if (toInsert)
			servers.addItem(serverItem);

		pack();
	}

	/**
	 * Gets the list of remote proxies.
	 */
	private void getRemoteProxies() {
		try {
			String finalDestination = null;

			final Object selection = servers.getSelectedItem();
			logger.debug("Remote server has been selected as " + selection);

			if (selection instanceof URL)
				finalDestination = ((URL) selection).toExternalForm();
			else if ((selection instanceof String)
					&& (!(defaultServerString.equals(selection))))
				finalDestination = (String) servers.getSelectedItem();
			else
				return;

			// update the aglet proxy list for such URL
			if (finalDestination != null) {
				proxies = Aglets.getAgletProxies(finalDestination);
				// update the list
				updateAgletList();

				// store the remote server in the combobox
				this.addServerItem();
			}

		} catch (final Exception e) {
			logger.error("Exception caught while trying to get the list of remote proxies from "
					+ servers.getSelectedItem(), e);
			agletsList.removeAllItems();
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".error.proxy"), translator.translate(baseKey
							+ ".error.proxy.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Handles the events on the combobox menu.
	 */
	@Override
	public synchronized void itemStateChanged(final ItemEvent event) {
		// check params
		if ((event == null) || (servers.getSelectedItem() == null))
			return;

		// try to get the selected URL
		getRemoteProxies();

	}

	/**
	 * Updates the aglet proxy list supposing the array of proxies has been
	 * already updated.
	 * 
	 * 
	 */
	private void updateAgletList() {
		// supposing I've already got the proxy list, I can update it
		agletsList.removeAllItems();

		if ((proxies != null) && (proxies.length > 0))
			for (final AgletProxy proxie : proxies)
				agletsList.addItem(proxie);

	}

	/**
	 * Updates the server list in the combobox.
	 * 
	 */
	protected void updateServerList() {
		final Resource res = Resource.getResourceFor("aglets");
		final String list = res.getString("aglets.addressbook");

		if ((list != null) && (list.equals(currentList) == false)) {
			currentList = list;
			servers.removeAllItems();
			servers.addItem(defaultServerString);

			// get all the know servers
			final String items[] = res.getStringArray("aglets.addressbook", " ");

			for (final String item : items) {
				try {
					servers.addItem(new URL(item));
				} catch (final MalformedURLException e) {
					logger.error("Exception caught while converting a string to an URL", e);
				}
			}
			/*
			 * JOptionPane.showMessageDialog(this,
			 * this.translator.translate(this.baseKey + ".error.URL"),
			 * this.translator.translate(this.baseKey + ".error.URL.title"),
			 * JOptionPane.ERROR_MESSAGE );
			 */
		}
	}

}
