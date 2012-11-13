/*
 * Created on Oct 22, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;

/**
 * 
 */
class ImportSharedSecret extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6985591177449002078L;

	/**
	 * A text field for the store file name
	 */
	JTextField filename;

	/**
	 * The parent dialog.
	 */
	NetworkConfigDialog parent;

	ImportSharedSecret(final JFrame f, final NetworkConfigDialog net) {
		super(f);
		parent = net;
		getContentPane().add("North", new JLabel(translator.translate("dialog.importsharedsecret.label"), SwingConstants.CENTER));
		final GridBagPanel p = new GridBagPanel();

		getContentPane().add(p);

		filename = new JTextField(30);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;

		p.setConstraints(cns);
		p.addLabeled(translator.translate("dialog.importsharedsecret.filename"), filename);
		filename.addActionListener(this);

		// add the buttons
		this.addButton(translator.translate("dialog.importsharedsecret.button.ok"), this);
		this.addButton(translator.translate("dialog.importsharedsecret.button.cancel"), this);
	}

	/**
	 * Manage events from buttons.
	 * 
	 * @param event
	 *            the event to manage
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
			final String filename = this.filename.getText();

			if ((filename == null) || filename.equals("")) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.error.message"), translator.translate("dialog.importsharedsecret.error.title"), JOptionPane.ERROR_MESSAGE, null);
				return;
			}

			final String owner = LoginData.getUsername();
			final String workDir = FileUtils.getWorkDirectoryForUser(owner);
			final String secretFilename = workDir + File.separator + filename;
			SharedSecret secret = null;

			try {
				secret = SharedSecret.load(secretFilename);
			} catch (final FileNotFoundException excpt) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.error.filenotfound"), translator.translate("dialog.importsharedsecret.error.filenotfound.title"), JOptionPane.ERROR_MESSAGE, null);
				return;
			} catch (final IOException excpt) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.error.io"), translator.translate("dialog.importsharedsecret.error.io.title"), JOptionPane.ERROR_MESSAGE, null);
				return;
			}

			if (secret == null) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.error.secret"), translator.translate("dialog.importsharedsecret.error.secret.title"), JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			final String domainName = secret.getDomainName();
			final SharedSecrets secrets = SharedSecrets.getSharedSecrets();
			final SharedSecret sec = secrets.getSharedSecret(domainName);

			if (sec != null) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.error.alreadyexists"), translator.translate("dialog.importsharedsecret.error.alreadyexists.title"), JOptionPane.ERROR_MESSAGE, null);
				return;
			}
			secrets.addSharedSecret(secret);
			secrets.save();
			JOptionPane.showMessageDialog(this, translator.translate("dialog.importsharedsecret.ok"), translator.translate("dialog.importsharedsecret.ok.title"), JOptionPane.INFORMATION_MESSAGE, null);
		}

		setVisible(false);
		dispose();
	}
}