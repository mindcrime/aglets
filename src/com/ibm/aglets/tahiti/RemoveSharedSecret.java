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

    AgentListPanel list;

    JTextField password;

    SharedSecrets secrets;

    NetworkConfigDialog parent;

    RemoveSharedSecret(JFrame f, SharedSecrets secs, NetworkConfigDialog net) {
	super(f);
	this.secrets = secs;
	this.getContentPane().add("North", new JLabel(this.translator.translate("dialog.removesharedsecret.label"), SwingConstants.CENTER));

	GridBagPanel p = new GridBagPanel();

	this.getContentPane().add(p);

	this.list = new AgentListPanel();
	this.password = new JPasswordField(20);

	GridBagConstraints cns = new GridBagConstraints();

	cns.fill = GridBagPanel.HORIZONTAL;
	cns.anchor = GridBagConstraints.WEST;
	cns.gridwidth = GridBagConstraints.REMAINDER;

	p.setConstraints(cns);
	p.addLabeled(this.translator.translate("dialog.removesharedsecret.label.list"), this.list);
	p.addLabeled(this.translator.translate("dialog.removesharedsecret.label.password"), this.password);

	Enumeration domains = this.secrets.getDomainNames();

	if (domains != null) {
	    while (domains.hasMoreElements()) {
		String domain = (String) domains.nextElement();

		this.list.addItem(domain);
	    }
	}

	// add the buttons
	this.addButton(this.translator.translate("dialog.removesharedsecret.button.ok"), this);
	this.addButton(this.translator.translate("dialog.removesharedsecret.button.cancel"), this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	String domainName = this.list.getSelectedItem();
	String command = ev.getActionCommand();

	if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
	    if ((domainName == null) || domainName.equals("")) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.removesharedsecret.error.nulldomain"), this.translator.translate("dialog.removesharedsecret.error.nulldomain.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }
	    SharedSecret secret = this.secrets.getSharedSecret(domainName);

	    if (secret == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.removesharedsecret.error.nullsecret"), this.translator.translate("dialog.removesharedsecret.error.nullsecret.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }
	    AgletRuntime rt = AgletRuntime.getAgletRuntime();

	    // Certificate ownerCert = rt.getOwnerCertificate();
	    String ownerName = rt.getOwnerName();

	    if (rt.authenticateOwner(ownerName, this.password.getText()) == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.removesharedsecret.error.password"), this.translator.translate("dialog.removesharedsecret.error.password.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		this.password.setText("");
		return;
	    }

	    this.secrets.removeSharedSecret(domainName);
	    this.secrets.save();
	}

	this.setVisible(false);
	this.dispose();
    }
};
