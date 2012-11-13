package com.ibm.aglets.tahiti;

/*
 * @(#)NetworkConfigDialog.java
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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.awb.misc.Resource;

// TODO compleare gestione eventi in action performed e gestione dei pulsanti
// per l'import/export degli shared secrets

/**
 * Class NetworkConfigDialog represents the dialog for
 * 
 * @version 1.03 96/04/15
 * @author Danny B. Lange
 * @author Yariv Aridor: 97/02/17 - added subscription support
 */

final class NetworkConfigDialog extends TahitiDialog implements ActionListener,
ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1590344113043220301L;
	/*
	 * Singletion method to get the instnace
	 */
	public synchronized static NetworkConfigDialog getInstance(final MainWindow parent) {
		if (mySelf == null)
			mySelf = new NetworkConfigDialog(parent);

		return mySelf;
	}
	/*
	 * Proxy Configuration
	 */
	private JCheckBox useProxy = null;
	private JTextField proxyHost = null;
	private JTextField proxyPort = null;

	private JTextField noProxy = null;
	/*
	 * Http tunnelling.
	 */
	private JCheckBox httpTunneling = null;

	private JCheckBox httpMessaging = null;

	/* authentication panel */
	private JCheckBox atpAuthentication = null;

	private JCheckBox useSecureRandomSeed = null;
	private JButton createSharedSecretButton = null;
	private JButton removeSharedSecretButton = null;
	private JButton importSharedSecretButton = null;

	private JButton exportSharedSecretButton = null;

	/*
	 * Singleton instance reference.
	 */
	private static NetworkConfigDialog mySelf = null;

	/*
	 * Constructs a new Aglet creation dialog.
	 */
	private NetworkConfigDialog(final MainWindow parent) {
		super(parent);

		contentPanel.setLayout(new BorderLayout());

		// a north panel for the http configuration
		final JPanel httpPanel = new JPanel();
		httpPanel.setLayout(new GridLayout(0, 1)); // always one columng
		httpTunneling = JComponentBuilder.createJCheckBox(baseKey
				+ ".httpTunneling", false, this);
		useProxy = JComponentBuilder.createJCheckBox(baseKey
				+ ".useProxy", false, this);
		proxyHost = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".proxyHost");
		proxyPort = JComponentBuilder.createJTextField(6, null, baseKey
				+ ".proxyPort");
		noProxy = JComponentBuilder.createJTextField(30, null, baseKey
				+ ".noProxy");

		JLabel label = null;

		httpPanel.add(httpTunneling);
		httpPanel.add(useProxy);
		final JPanel proxyPanel = new JPanel();
		proxyPanel.setLayout(new FlowLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".proxyHost");
		proxyPanel.add(label);
		proxyPanel.add(proxyHost);
		label = JComponentBuilder.createJLabel(baseKey + ".proxyPort");
		proxyPanel.add(label);
		proxyPanel.add(proxyPort);
		httpPanel.add(proxyPanel);
		label = JComponentBuilder.createJLabel(baseKey + ".noProxy");
		httpPanel.add(label);
		httpPanel.add(noProxy);
		contentPanel.add(httpPanel);

		final TitledBorder border = new TitledBorder(JComponentBuilder.getTitle(baseKey
				+ ".httpPanel"));
		border.setTitleColor(Color.BLUE);
		httpPanel.setBorder(border);

		// add the http panel to the north of the window
		contentPanel.add(httpPanel, BorderLayout.NORTH);

		// authentication panel
		final JPanel authPanel = new JPanel();
		authPanel.setLayout(new GridLayout(0, 1)); // always a column

		atpAuthentication = JComponentBuilder.createJCheckBox(baseKey
				+ ".authenticationMode", false, this);
		useSecureRandomSeed = JComponentBuilder.createJCheckBox(baseKey
				+ ".secureRandomSeed", false, this);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 2));
		createSharedSecretButton = JComponentBuilder.createJButton(baseKey
				+ ".createSharedSecret", GUICommandStrings.CREATE_SHARED_SECRET_COMMAND, this);
		removeSharedSecretButton = JComponentBuilder.createJButton(baseKey
				+ ".removeSharedSecret", GUICommandStrings.REMOVE_SHARED_SECRET_COMMAND, this);
		importSharedSecretButton = JComponentBuilder.createJButton(baseKey
				+ ".importSharedSecret", GUICommandStrings.IMPORT_SHARED_SECRET_COMMAND, this);
		exportSharedSecretButton = JComponentBuilder.createJButton(baseKey
				+ ".exportSharedSecret", GUICommandStrings.EXPORT_SHARED_SECRET_COMMAND, this);
		buttonPanel.add(createSharedSecretButton);
		buttonPanel.add(removeSharedSecretButton);
		buttonPanel.add(importSharedSecretButton);
		buttonPanel.add(exportSharedSecretButton);

		final TitledBorder border3 = new TitledBorder(JComponentBuilder.getTitle(baseKey
				+ ".authPanel"));
		border3.setTitleColor(Color.BLUE);
		border3.setTitlePosition(TitledBorder.CENTER);

		authPanel.add(atpAuthentication);
		authPanel.add(useSecureRandomSeed);
		authPanel.add(buttonPanel);
		authPanel.setBorder(border3);

		contentPanel.add(authPanel, BorderLayout.CENTER);

		// other section
		final JPanel otherPanel = new JPanel();
		otherPanel.setLayout(new GridLayout(0, 1));

		httpMessaging = JComponentBuilder.createJCheckBox(baseKey
				+ ".httpMessaging", false, this);
		otherPanel.add(httpMessaging);
		final TitledBorder border2 = new TitledBorder(JComponentBuilder.getTitle(baseKey
				+ ".otherPanel"));
		border2.setTitleColor(Color.BLUE);
		otherPanel.setBorder(border2);

		contentPanel.add(otherPanel, BorderLayout.SOUTH);

		// load values from the resources
		updateValues();
		updateGUIState();

		// all done
		pack();

	}

	/*
	 * The call back methods
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		// if the user has pressed ok then save the parameters
		if (GUICommandStrings.OK_COMMAND.equals(command))
			save();
		else if (GUICommandStrings.EXPORT_SHARED_SECRET_COMMAND.equals(command))
			exportSharedSecret();
		else if (GUICommandStrings.IMPORT_SHARED_SECRET_COMMAND.equals(command))
			importSharedSecret();
		else if (GUICommandStrings.CREATE_SHARED_SECRET_COMMAND.equals(command))
			createSharedSecret();
		else if (GUICommandStrings.REMOVE_SHARED_SECRET_COMMAND.equals(command))
			removeSharedSecret();
		else
			super.actionPerformed(event);

	}

	/**
	 * Pops up the creation of the shared secret dialog.
	 * 
	 */
	private void createSharedSecret() {
		final BaseAgletsDialog dialog = new CreateSharedSecretDialog(getMainWindow());
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	/**
	 * Pops up the export shared secret dialog.
	 * 
	 */
	private void exportSharedSecret() {
		final BaseAgletsDialog dialog = new ExportSharedSecretDialog(getMainWindow());
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	/**
	 * Pops up the import shared secret dialog.
	 * 
	 */
	private void importSharedSecret() {
		final BaseAgletsDialog dialog = new ImportSharedSecretDialog(getMainWindow());
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	/**
	 * manages checkbox events in order to keep the gui state coherent.
	 */
	@Override
	public void itemStateChanged(final ItemEvent event) {
		if (event == null)
			return;

		// update the gui state
		updateGUIState();
	}

	/**
	 * Pops up the remotion dialog.
	 * 
	 */
	private void removeSharedSecret() {
		final BaseAgletsDialog dialog = new RemoveSharedSecretDialog(getMainWindow());
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	/**
	 * Saves the GUI State in the aglets resources.
	 */
	private boolean save() {
		boolean changed = false;
		String value;
		final Resource system_res = Resource.getResourceFor("system");
		final Resource atp_res = Resource.getResourceFor("atp");

		final boolean use = useProxy.isSelected();

		if (use != atp_res.getBoolean("atp.useHttpProxy", false)) {
			changed = true;
		}
		atp_res.setResource("atp.useHttpProxy", String.valueOf(use));

		value = proxyHost.getText().trim();
		if (value.equals(atp_res.getString("atp.http.proxyHost")) == false) {
			changed = true;
		}
		atp_res.setResource("atp.http.proxyHost", value);
		system_res.setResource("proxyHost", use ? value : "");
		system_res.setResource("http.proxyHost", use ? value : "");

		value = proxyPort.getText().trim();
		if (value.equals(atp_res.getString("atp.http.proxyPort")) == false) {
			changed = true;
		}
		atp_res.setResource("atp.http.proxyPort", value);
		system_res.setResource("proxyPort", use ? value : "");
		system_res.setResource("http.proxyPort", use ? value : "");

		value = noProxy.getText().trim();
		if (value.equals(atp_res.getString("atp.noProxy")) == false) {
			changed = true;
		}
		atp_res.setResource("atp.noProxy", value);
		system_res.setResource("http.nonProxyHosts", value);

		/*
		 * allow/disallow http tunneling/messaging
		 */
		atp_res.setResource("atp.http.tunneling", String.valueOf(httpTunneling.isSelected()));
		atp_res.setResource("atp.http.messaging", String.valueOf(httpMessaging.isSelected()));

		// com.ibm.atp.daemon.Daemon.update();

		/*
		 * authentication
		 */
		final boolean auth = atpAuthentication.isSelected();

		if (auth != atp_res.getBoolean("atp.authentication", false)) {
			changed = true;
		}
		atp_res.setResource("atp.authentication", String.valueOf(auth));
		if (auth) {
			System.out.println("AUTHENTICATION MODE ON.");

			// SharedSecrets.getSharedSecrets();
		} else {
			System.out.println("AUTHENTICATION MODE OFF.");
		}
		final boolean secureseed = useSecureRandomSeed.isSelected();

		atp_res.setResource("atp.secureseed", String.valueOf(secureseed));
		if (secureseed) {
			System.out.println("USE SECURE RANDOM SEED.");
		} else {
			System.out.println("USE UNSECURE PSEUDO RANDOM SEED.");
		}

		// Randoms.setUseSecureRandomSeed(secureseed);
		if (auth) {
			System.out.print("[Generating random seed ... wait for a while ... ");
			if (auth) {

				// Randoms.getRandomGenerator(Challenge.LENGTH);
			}
			System.out.println("done.]");
		}

		final Resource aglets_res = Resource.getResourceFor("aglets");

		aglets_res.save("Tahiti");
		atp_res.save("Tahiti");

		// REMIND: needs update
		// com.ibm.atp.protocol.http.HttpProxy.update();
		return changed;
	}

	/**
	 * Enables or disables the gui components depending on user selections.
	 * 
	 */
	private void updateGUIState() {
		if (useProxy.isSelected()) {
			proxyHost.setEnabled(true);
			proxyPort.setEnabled(true);
			noProxy.setEnabled(true);
		} else {
			proxyHost.setEnabled(false);
			proxyPort.setEnabled(false);
			noProxy.setEnabled(false);
		}
	}

	/**
	 * Loads values from the aglet resources and enables/disables the gui
	 * components
	 * 
	 */
	private void updateValues() {

		final Resource atp_res = Resource.getResourceFor("atp");
		assert (atp_res == null);
		proxyHost.setText(atp_res.getString("atp.http.proxyHost", ""));
		proxyPort.setText(atp_res.getString("atp.http.proxyPort", ""));
		noProxy.setText(atp_res.getString("atp.noProxy", ""));
		useProxy.setSelected(atp_res.getBoolean("atp.useHttpProxy", false));
		final Resource res = Resource.getResourceFor("aglets");
		assert (res == null);
		httpTunneling.setSelected(atp_res.getBoolean("atp.http.tunneling", false));
		httpMessaging.setSelected(atp_res.getBoolean("atp.http.messaging", false));
		atpAuthentication.setSelected(atp_res.getBoolean("atp.authentication", false));
		useSecureRandomSeed.setSelected(atp_res.getBoolean("atp.secureseed", false));

	}

}
