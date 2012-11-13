package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

/**
 * A dialog to manage the remotion of a shared secret.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         07/nov/07
 */
public class RemoveSharedSecretDialog extends TahitiDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5592252588199141821L;

	/**
	 * The text field where the user must specify the password.
	 */
	private JPasswordField passwordField = null;

	/**
	 * The list of available domains.
	 */
	private AgletListPanel<String> domainList = null;

	public RemoveSharedSecretDialog(final JFrame parentFrame) {
		super(parentFrame);

		// create gui components
		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".infoLabel");
		contentPanel.add(label, BorderLayout.NORTH);

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".domainLabel");
		centerPanel.add(label);
		domainList = new AgletListPanel<String>();
		domainList.setTitleBorder(translator.translate(baseKey
				+ ".domainLabel"));
		centerPanel.add(domainList);
		fillDomainList();
		contentPanel.add(centerPanel, BorderLayout.CENTER);

		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".passwordLabel");
		passwordField = JComponentBuilder.createJPasswordField(20);
		southPanel.add(label);
		southPanel.add(passwordField);
		contentPanel.add(southPanel, BorderLayout.SOUTH);

		pack();

	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// the user wants to remove the shared secret
			final String domain = domainList.getSelectedItem();
			final String password = new String(passwordField.getPassword());

			final SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();
			final SharedSecret selectedSecret = allSecrets.getSharedSecret(domain);

			// check if this exists
			if (selectedSecret == null) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".sharedSecretNotExists"), translator.translate(baseKey
								+ ".sharedSecretNotExists.title"), JOptionPane.ERROR_MESSAGE);
				return;

			}

			// check if the user can be authenticated
			final AgletRuntime runTime = AgletRuntime.getAgletRuntime();
			final String username = runTime.getOwnerName();
			if (runTime.authenticateOwner(username, password) == null) {
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".authError"), translator.translate(baseKey
								+ ".authError.title"), JOptionPane.ERROR_MESSAGE);
				return;

			}

			// ok, now delete the shared secret
			allSecrets.removeSharedSecret(domain);
			allSecrets.save();

		}

		// leave the superclass to manage the events
		super.actionPerformed(event);
	}

	/**
	 * Iterates on the domain list and adds each domain name (as a string) to
	 * the list.
	 * 
	 */
	private void fillDomainList() {
		final SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();

		if (allSecrets == null)
			return;

		for (final Enumeration enumer = allSecrets.getDomainNames(); (enumer != null)
		&& enumer.hasMoreElements();) {
			final String currentDomain = (String) enumer.nextElement();
			domainList.addItem(currentDomain);
		}

	}

}
