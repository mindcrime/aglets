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
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.ibm.aglets.tahiti.utils.AgentListPanel;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;

class ExportSharedSecret extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8780072653784282549L;

	AgentListPanel list;

	JTextField filename;

	SharedSecrets secrets;

	NetworkConfigDialog parent;

	ExportSharedSecret(final JFrame f, final SharedSecrets secs, final NetworkConfigDialog net) {
		super(f);
		secrets = secs;
		parent = net;
		getContentPane().add("North", new JLabel(translator.translate("dialog.exportsharedsecret.label"), SwingConstants.CENTER));
		final GridBagPanel p = new GridBagPanel();

		getContentPane().add(p);

		list = new AgentListPanel();
		filename = new JTextField(20);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;

		p.setConstraints(cns);
		p.addLabeled(translator.translate("dialog.exportsharedsecret.label.domain"), list);
		p.addLabeled(translator.translate("dialog.exportsharedsecret.label.file"), filename);
		final Enumeration domains = secrets.getDomainNames();

		if (domains != null) {
			while (domains.hasMoreElements()) {
				final String domain = (String) domains.nextElement();

				list.addItem(domain);
			}
		}

		// add the buttons
		this.addButton("dialog.exportsharedsecret.button.ok", this);
		this.addButton("dialog.exportsharedsecret.button.cancel", this);

	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
			// export the secret
			final String domainName = list.getSelectedItem();
			if ((domainName == null) || domainName.equals("")) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.exportsharedsecret.error.domain"), translator.translate("dialog.exportsharedsecret.error.domain.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}

			final String filen = filename.getText();

			if ((filen == null) || filen.equals("")) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.exportsharedsecret.error.file"), translator.translate("dialog.exportsharedsecret.error.file.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}

			final String owner = LoginData.getUsername();
			final String workDir = FileUtils.getWorkDirectoryForUser(owner);
			final String secretFilename = workDir + File.separator + filen;
			final SharedSecret secret = secrets.getSharedSecret(domainName);

			if (secret == null) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.exportsharedsecret.error.secret"), translator.translate("dialog.exportsharedsecret.error.secret.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}
			try {
				secret.save(secretFilename);
			} catch (final IOException excpt) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.exportsharedsecret.error.io"), translator.translate("dialog.exportsharedsecret.error.io.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}

		}

		setVisible(false);
		dispose();
	}

}
