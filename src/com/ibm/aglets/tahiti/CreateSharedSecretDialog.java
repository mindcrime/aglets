package com.ibm.aglets.tahiti;

import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.atp.auth.SharedSecret;

/**
 * Dialog for shared secret creation.
 * @author: Hideki Tai
 */
class CreateSharedSecretDialog extends TahitiDialog 
	implements java.awt.event.ActionListener {
	private static final String DIALOG_TITLE = "Create a new shared secret";
	private static final String ACTION_OK = "OK";
	private TextField domain;

	private TextField creatorAlias = null;
	private TextField creatorPassword = null;

	CreateSharedSecretDialog(Frame f) {
		super(f, DIALOG_TITLE, true);

		// Sets the title label
		add("North", new Label(DIALOG_TITLE, Label.CENTER));
		GridBagPanel p = new GridBagPanel();

		add(p);

		// Sets the input field for domain name
		domain = new TextField(20);
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		p.setConstraints(cns);
		p.addLabeled("Domain name", domain);
		domain.addActionListener(this);

		// Sets the input field for creator's alias
		creatorAlias = new TextField(20);
		p.addLabeled("Creator's key alias", creatorAlias);

		// Sets the input field for creator's password
		creatorPassword = new TextField(20);
		p.addLabeled("Creator's key password", creatorPassword);

		// Sets the OK button
		addButton(ACTION_OK, this);

		// Sets the Cancel button
		addCloseButton("Cancel");
	}
	/**
	 * Handles "OK" button - creates a new shared secret and registers it.
	 */
	public void actionPerformed(ActionEvent ev) {

		// Checks if the domain name is set
		String domainName = domain.getText();
		String keyAlias = creatorAlias.getText();
		String keyPassword = creatorPassword.getText();

		if (domainName == null || domainName.equals("")) {
			TahitiDialog.alert(getMainWindow(), 
							   "Specify domain name").popupAtCenterOfParent();
			return;
		} 
		if (keyAlias == null || keyAlias.equals("")) {
			TahitiDialog.alert(getMainWindow(), "Specify creator's key alias")
				.popupAtCenterOfParent();
			return;
		} 

		// Checks if the secret of the domain name is already exists.
		SharedSecrets secrets = SharedSecrets.getSharedSecrets();
		SharedSecret secret = secrets.getSharedSecret(domainName);

		if (secret != null) {
			TahitiDialog.alert(getMainWindow(), 
							   "Already exists").popupAtCenterOfParent();
			return;
		} 

		// Creates a new shared secret.
		secret = SharedSecret.createNewSharedSecret(domainName, keyAlias, 
													keyPassword);
		if (secret == null) {
			TahitiDialog
				.alert(getMainWindow(), "Shared secret is not created")
					.popupAtCenterOfParent();
		} else {
			secrets.addSharedSecret(secret);
			secrets.save();
			TahitiDialog.message(getMainWindow(), "Created", 
								 "Shared secret of domain '" + domainName 
								 + "' is created").popupAtCenterOfParent();
		} 
		dispose();
	}
}
