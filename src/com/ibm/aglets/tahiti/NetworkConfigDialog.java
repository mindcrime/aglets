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
    private NetworkConfigDialog(MainWindow parent) {
	super(parent);

	this.contentPanel.setLayout(new BorderLayout());

	// a north panel for the http configuration
	JPanel httpPanel = new JPanel();
	httpPanel.setLayout(new GridLayout(0, 1)); // always one columng
	this.httpTunneling = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".httpTunneling", false, this);
	this.useProxy = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".useProxy", false, this);
	this.proxyHost = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".proxyHost");
	this.proxyPort = JComponentBuilder.createJTextField(6, null, this.baseKey
		+ ".proxyPort");
	this.noProxy = JComponentBuilder.createJTextField(30, null, this.baseKey
		+ ".noProxy");

	JLabel label = null;

	httpPanel.add(this.httpTunneling);
	httpPanel.add(this.useProxy);
	JPanel proxyPanel = new JPanel();
	proxyPanel.setLayout(new FlowLayout());
	label = JComponentBuilder.createJLabel(this.baseKey + ".proxyHost");
	proxyPanel.add(label);
	proxyPanel.add(this.proxyHost);
	label = JComponentBuilder.createJLabel(this.baseKey + ".proxyPort");
	proxyPanel.add(label);
	proxyPanel.add(this.proxyPort);
	httpPanel.add(proxyPanel);
	label = JComponentBuilder.createJLabel(this.baseKey + ".noProxy");
	httpPanel.add(label);
	httpPanel.add(this.noProxy);
	this.contentPanel.add(httpPanel);

	TitledBorder border = new TitledBorder(JComponentBuilder.getTitle(this.baseKey
		+ ".httpPanel"));
	border.setTitleColor(Color.BLUE);
	httpPanel.setBorder(border);

	// add the http panel to the north of the window
	this.contentPanel.add(httpPanel, BorderLayout.NORTH);

	// authentication panel
	JPanel authPanel = new JPanel();
	authPanel.setLayout(new GridLayout(0, 1)); // always a column

	this.atpAuthentication = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".authenticationMode", false, this);
	this.useSecureRandomSeed = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".secureRandomSeed", false, this);

	JPanel buttonPanel = new JPanel();
	buttonPanel.setLayout(new GridLayout(2, 2));
	this.createSharedSecretButton = JComponentBuilder.createJButton(this.baseKey
		+ ".createSharedSecret", GUICommandStrings.CREATE_SHARED_SECRET_COMMAND, this);
	this.removeSharedSecretButton = JComponentBuilder.createJButton(this.baseKey
		+ ".removeSharedSecret", GUICommandStrings.REMOVE_SHARED_SECRET_COMMAND, this);
	this.importSharedSecretButton = JComponentBuilder.createJButton(this.baseKey
		+ ".importSharedSecret", GUICommandStrings.IMPORT_SHARED_SECRET_COMMAND, this);
	this.exportSharedSecretButton = JComponentBuilder.createJButton(this.baseKey
		+ ".exportSharedSecret", GUICommandStrings.EXPORT_SHARED_SECRET_COMMAND, this);
	buttonPanel.add(this.createSharedSecretButton);
	buttonPanel.add(this.removeSharedSecretButton);
	buttonPanel.add(this.importSharedSecretButton);
	buttonPanel.add(this.exportSharedSecretButton);

	TitledBorder border3 = new TitledBorder(JComponentBuilder.getTitle(this.baseKey
		+ ".authPanel"));
	border3.setTitleColor(Color.BLUE);
	border3.setTitlePosition(TitledBorder.CENTER);

	authPanel.add(this.atpAuthentication);
	authPanel.add(this.useSecureRandomSeed);
	authPanel.add(buttonPanel);
	authPanel.setBorder(border3);

	this.contentPanel.add(authPanel, BorderLayout.CENTER);

	// other section
	JPanel otherPanel = new JPanel();
	otherPanel.setLayout(new GridLayout(0, 1));

	this.httpMessaging = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".httpMessaging", false, this);
	otherPanel.add(this.httpMessaging);
	TitledBorder border2 = new TitledBorder(JComponentBuilder.getTitle(this.baseKey
		+ ".otherPanel"));
	border2.setTitleColor(Color.BLUE);
	otherPanel.setBorder(border2);

	this.contentPanel.add(otherPanel, BorderLayout.SOUTH);

	// load values from the resources
	this.updateValues();
	this.updateGUIState();

	// all done
	this.pack();

    }

    /*
     * The call back methods
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	// if the user has pressed ok then save the parameters
	if (GUICommandStrings.OK_COMMAND.equals(command))
	    this.save();
	else if (GUICommandStrings.EXPORT_SHARED_SECRET_COMMAND.equals(command))
	    this.exportSharedSecret();
	else if (GUICommandStrings.IMPORT_SHARED_SECRET_COMMAND.equals(command))
	    this.importSharedSecret();
	else if (GUICommandStrings.CREATE_SHARED_SECRET_COMMAND.equals(command))
	    this.createSharedSecret();
	else if (GUICommandStrings.REMOVE_SHARED_SECRET_COMMAND.equals(command))
	    this.removeSharedSecret();
	else
	    super.actionPerformed(event);

    }

    /**
     * Pops up the export shared secret dialog.
     * 
     */
    private void exportSharedSecret() {
	BaseAgletsDialog dialog = new ExportSharedSecretDialog(this.getMainWindow());
	dialog.setModal(true);
	dialog.setVisible(true);
    }

    /**
     * Pops up the import shared secret dialog.
     * 
     */
    private void importSharedSecret() {
	BaseAgletsDialog dialog = new ImportSharedSecretDialog(this.getMainWindow());
	dialog.setModal(true);
	dialog.setVisible(true);
    }

    /**
     * Pops up the creation of the shared secret dialog.
     * 
     */
    private void createSharedSecret() {
	BaseAgletsDialog dialog = new CreateSharedSecretDialog(this.getMainWindow());
	dialog.setModal(true);
	dialog.setVisible(true);
    }

    /**
     * Pops up the remotion dialog.
     * 
     */
    private void removeSharedSecret() {
	BaseAgletsDialog dialog = new RemoveSharedSecretDialog(this.getMainWindow());
	dialog.setModal(true);
	dialog.setVisible(true);
    }

    /*
     * Singletion method to get the instnace
     */
    public synchronized static NetworkConfigDialog getInstance(MainWindow parent) {
	if (mySelf == null)
	    mySelf = new NetworkConfigDialog(parent);

	return mySelf;
    }

    /**
     * manages checkbox events in order to keep the gui state coherent.
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
	if (event == null)
	    return;

	// update the gui state
	this.updateGUIState();
    }

    /**
     * Saves the GUI State in the aglets resources.
     */
    private boolean save() {
	boolean changed = false;
	String value;
	Resource system_res = Resource.getResourceFor("system");
	Resource atp_res = Resource.getResourceFor("atp");

	boolean use = this.useProxy.isSelected();

	if (use != atp_res.getBoolean("atp.useHttpProxy", false)) {
	    changed = true;
	}
	atp_res.setResource("atp.useHttpProxy", String.valueOf(use));

	value = this.proxyHost.getText().trim();
	if (value.equals(atp_res.getString("atp.http.proxyHost")) == false) {
	    changed = true;
	}
	atp_res.setResource("atp.http.proxyHost", value);
	system_res.setResource("proxyHost", use ? value : "");
	system_res.setResource("http.proxyHost", use ? value : "");

	value = this.proxyPort.getText().trim();
	if (value.equals(atp_res.getString("atp.http.proxyPort")) == false) {
	    changed = true;
	}
	atp_res.setResource("atp.http.proxyPort", value);
	system_res.setResource("proxyPort", use ? value : "");
	system_res.setResource("http.proxyPort", use ? value : "");

	value = this.noProxy.getText().trim();
	if (value.equals(atp_res.getString("atp.noProxy")) == false) {
	    changed = true;
	}
	atp_res.setResource("atp.noProxy", value);
	system_res.setResource("http.nonProxyHosts", value);

	/*
	 * allow/disallow http tunneling/messaging
	 */
	atp_res.setResource("atp.http.tunneling", String.valueOf(this.httpTunneling.isSelected()));
	atp_res.setResource("atp.http.messaging", String.valueOf(this.httpMessaging.isSelected()));

	// com.ibm.atp.daemon.Daemon.update();

	/*
	 * authentication
	 */
	final boolean auth = this.atpAuthentication.isSelected();

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
	final boolean secureseed = this.useSecureRandomSeed.isSelected();

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

	Resource aglets_res = Resource.getResourceFor("aglets");

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
	if (this.useProxy.isSelected()) {
	    this.proxyHost.setEnabled(true);
	    this.proxyPort.setEnabled(true);
	    this.noProxy.setEnabled(true);
	} else {
	    this.proxyHost.setEnabled(false);
	    this.proxyPort.setEnabled(false);
	    this.noProxy.setEnabled(false);
	}
    }

    /**
     * Loads values from the aglet resources and enables/disables the gui
     * components
     * 
     */
    private void updateValues() {

	Resource atp_res = Resource.getResourceFor("atp");
	assert (atp_res == null);
	this.proxyHost.setText(atp_res.getString("atp.http.proxyHost", ""));
	this.proxyPort.setText(atp_res.getString("atp.http.proxyPort", ""));
	this.noProxy.setText(atp_res.getString("atp.noProxy", ""));
	this.useProxy.setSelected(atp_res.getBoolean("atp.useHttpProxy", false));
	Resource res = Resource.getResourceFor("aglets");
	assert (res == null);
	this.httpTunneling.setSelected(atp_res.getBoolean("atp.http.tunneling", false));
	this.httpMessaging.setSelected(atp_res.getBoolean("atp.http.messaging", false));
	this.atpAuthentication.setSelected(atp_res.getBoolean("atp.authentication", false));
	this.useSecureRandomSeed.setSelected(atp_res.getBoolean("atp.secureseed", false));

    }

}
