/*
 * Created on Oct 22, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.tahiti.utils.AgentListPanel;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

class RemoveSharedSecret extends TahitiDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4401746296431455597L;

	AgentListPanel list;

	JTextField password;

	SharedSecrets secrets;

	NetworkConfigDialog parent;

	RemoveSharedSecret(final JFrame f, final SharedSecrets secs, final NetworkConfigDialog net) {
		super(f);
		secrets = secs;
		getContentPane().add("North", new JLabel(translator.translate("dialog.removesharedsecret.label"), SwingConstants.CENTER));

		final GridBagPanel p = new GridBagPanel();

		getContentPane().add(p);

		list = new AgentListPanel();
		password = new JPasswordField(20);

		final GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;

		p.setConstraints(cns);
		p.addLabeled(translator.translate("dialog.removesharedsecret.label.list"), list);
		p.addLabeled(translator.translate("dialog.removesharedsecret.label.password"), password);

		final Enumeration domains = secrets.getDomainNames();

		if (domains != null) {
			while (domains.hasMoreElements()) {
				final String domain = (String) domains.nextElement();

				list.addItem(domain);
			}
		}

		// add the buttons
		this.addButton(translator.translate("dialog.removesharedsecret.button.ok"), this);
		this.addButton(translator.translate("dialog.removesharedsecret.button.cancel"), this);
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final String domainName = list.getSelectedItem();
		final String command = ev.getActionCommand();

		if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
			if ((domainName == null) || domainName.equals("")) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.removesharedsecret.error.nulldomain"), translator.translate("dialog.removesharedsecret.error.nulldomain.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}
			final SharedSecret secret = secrets.getSharedSecret(domainName);

			if (secret == null) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.removesharedsecret.error.nullsecret"), translator.translate("dialog.removesharedsecret.error.nullsecret.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				return;
			}
			final AgletRuntime rt = AgletRuntime.getAgletRuntime();

			// Certificate ownerCert = rt.getOwnerCertificate();
			final String ownerName = rt.getOwnerName();

			if (rt.authenticateOwner(ownerName, password.getText()) == null) {
				JOptionPane.showMessageDialog(this, translator.translate("dialog.removesharedsecret.error.password"), translator.translate("dialog.removesharedsecret.error.password.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
				password.setText("");
				return;
			}

			secrets.removeSharedSecret(domainName);
			secrets.save();
		}

		setVisible(false);
		dispose();
	}
};
