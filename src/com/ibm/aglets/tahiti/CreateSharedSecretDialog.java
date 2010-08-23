package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

/**
 * Dialog for shared secret creation.
 * 
 * @author: Hideki Tai
 */
class CreateSharedSecretDialog extends TahitiDialog implements
java.awt.event.ActionListener {

    /**
     * GUI Components
     */
    private JTextField domain = null;
    private JTextField creatorAlias = null;
    private JTextField creatorPassword = null;

    CreateSharedSecretDialog(JFrame parent) {
	super(parent);

	// create the gui components
	this.domain = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".domain");
	this.creatorAlias = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".alias");
	this.creatorPassword = JComponentBuilder.createJTextField(20, null, null);

	// create a panel to display the components
	JPanel centerPanel = new JPanel(new GridLayout(0, 2));

	JLabel label = JComponentBuilder.createJLabel(this.baseKey
		+ ".domain.label");
	centerPanel.add(label);
	centerPanel.add(this.domain);

	label = JComponentBuilder.createJLabel(this.baseKey + ".alias.label");
	centerPanel.add(label);
	centerPanel.add(this.creatorAlias);

	label = JComponentBuilder.createJLabel(this.baseKey + ".password.label");
	centerPanel.add(label);
	centerPanel.add(this.creatorPassword);

	this.contentPanel.add(centerPanel, BorderLayout.CENTER);
	this.pack();
    }

    /**
     * Handles "OK" button - creates a new shared secret and registers it.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	String command = event.getActionCommand();

	// if the ok button has been pressed then
	// it is time to create the shared secret
	if (GUICommandStrings.OK_COMMAND.equals(command)) {

	    // get the data the user has entered
	    String domainName = this.domain.getText();
	    String keyAlias = this.creatorAlias.getText();
	    String keyPassword = this.creatorPassword.getText();

	    // check and prompt the user for valid strings

	    if ((domainName == null) || (domainName.length() == 0)) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".error.domain"), this.translator.translate(this.baseKey
				+ ".error.domain.title"), JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    if ((keyAlias == null) || (keyAlias.length() == 0)) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".error.alias"), this.translator.translate(this.baseKey
				+ ".error.alias.title"), JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    if ((keyPassword == null) || (keyPassword.length() == 0)) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".error.password"), this.translator.translate(this.baseKey
				+ ".error.password.title"), JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    // now get the shared secrets and check if a secret already exists
	    // for the specified domain
	    SharedSecrets secrets = SharedSecrets.getSharedSecrets();
	    SharedSecret secret = secrets.getSharedSecret(domainName);

	    if (secret != null) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".error.domainExists"), this.translator.translate(this.baseKey
				+ ".error.domainExists.title"), JOptionPane.ERROR_MESSAGE);
		return;
	    }

	    // create and check the shared secret
	    secret = SharedSecret.createNewSharedSecret(domainName, keyAlias, keyPassword);

	    if (secret == null)
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".error.secret"), this.translator.translate(this.baseKey
				+ ".error.secret.title"), JOptionPane.ERROR_MESSAGE);
	    else {
		// add the secret to the shared secrets
		secrets.addSharedSecret(secret);
		secrets.save();
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".secret"), this.translator.translate(this.baseKey
				+ ".secret.title"), JOptionPane.INFORMATION_MESSAGE);
	    }

	    this.setVisible(false);
	    this.dispose();
	} else
	    super.actionPerformed(event);

    }
}
