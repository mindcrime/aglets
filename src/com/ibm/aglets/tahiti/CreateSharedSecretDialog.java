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
 * @author Hideki Tai
 */
class CreateSharedSecretDialog extends TahitiDialog implements
java.awt.event.ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9072770323713204653L;
	/**
	 * GUI Components
	 */
	private JTextField domain = null;
	private JTextField creatorAlias = null;
	private JTextField creatorPassword = null;

	CreateSharedSecretDialog(final JFrame parent) {
		super(parent);

		// create the gui components
		domain = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".domain");
		creatorAlias = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".alias");
		creatorPassword = JComponentBuilder.createJTextField(20, null, null);

		// create a panel to display the components
		final JPanel centerPanel = new JPanel(new GridLayout(0, 2));

		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".domain.label");
		centerPanel.add(label);
		centerPanel.add(domain);

		label = JComponentBuilder.createJLabel(baseKey + ".alias.label");
		centerPanel.add(label);
		centerPanel.add(creatorAlias);

		label = JComponentBuilder.createJLabel(baseKey + ".password.label");
		centerPanel.add(label);
		centerPanel.add(creatorPassword);

		contentPanel.add(centerPanel, BorderLayout.CENTER);
		pack();
	}

	/**
	 * Handles "OK" button - creates a new shared secret and registers it.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		// check params
		if (event == null)
			return;

		final String command = event.getActionCommand();

		// if the ok button has been pressed then
		// it is time to create the shared secret
		if (GUICommandStrings.OK_COMMAND.equals(command)) {

			// get the data the user has entered
			final String domainName = domain.getText();
			final String keyAlias = creatorAlias.getText();
			final String keyPassword = creatorPassword.getText();

			// check and prompt the user for valid strings

			if ((domainName == null) || (domainName.length() == 0)) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".error.domain"), translator.translate(baseKey
								+ ".error.domain.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if ((keyAlias == null) || (keyAlias.length() == 0)) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".error.alias"), translator.translate(baseKey
								+ ".error.alias.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			if ((keyPassword == null) || (keyPassword.length() == 0)) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".error.password"), translator.translate(baseKey
								+ ".error.password.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// now get the shared secrets and check if a secret already exists
			// for the specified domain
			final SharedSecrets secrets = SharedSecrets.getSharedSecrets();
			SharedSecret secret = secrets.getSharedSecret(domainName);

			if (secret != null) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".error.domainExists"), translator.translate(baseKey
								+ ".error.domainExists.title"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// create and check the shared secret
			secret = SharedSecret.createNewSharedSecret(domainName, keyAlias, keyPassword);

			if (secret == null)
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".error.secret"), translator.translate(baseKey
								+ ".error.secret.title"), JOptionPane.ERROR_MESSAGE);
			else {
				// add the secret to the shared secrets
				secrets.addSharedSecret(secret);
				secrets.save();
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".secret"), translator.translate(baseKey
								+ ".secret.title"), JOptionPane.INFORMATION_MESSAGE);
			}

			setVisible(false);
			dispose();
		} else
			super.actionPerformed(event);

	}
}
