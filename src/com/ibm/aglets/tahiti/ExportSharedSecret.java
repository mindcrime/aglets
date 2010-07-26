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

    AgentListPanel list;

    JTextField filename;

    SharedSecrets secrets;

    NetworkConfigDialog parent;

    ExportSharedSecret(JFrame f, SharedSecrets secs, NetworkConfigDialog net) {
	super(f);
	this.secrets = secs;
	this.parent = net;
	this.getContentPane().add("North", new JLabel(this.translator.translate("dialog.exportsharedsecret.label"), SwingConstants.CENTER));
	GridBagPanel p = new GridBagPanel();

	this.getContentPane().add(p);

	this.list = new AgentListPanel();
	this.filename = new JTextField(20);

	GridBagConstraints cns = new GridBagConstraints();

	cns.fill = GridBagPanel.HORIZONTAL;
	cns.anchor = GridBagConstraints.WEST;
	cns.gridwidth = GridBagConstraints.REMAINDER;

	p.setConstraints(cns);
	p.addLabeled(this.translator.translate("dialog.exportsharedsecret.label.domain"), this.list);
	p.addLabeled(this.translator.translate("dialog.exportsharedsecret.label.file"), this.filename);
	Enumeration domains = this.secrets.getDomainNames();

	if (domains != null) {
	    while (domains.hasMoreElements()) {
		String domain = (String) domains.nextElement();

		this.list.addItem(domain);
	    }
	}

	// add the buttons
	this.addButton("dialog.exportsharedsecret.button.ok", this);
	this.addButton("dialog.exportsharedsecret.button.cancel", this);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
	    // export the secret
	    String domainName = this.list.getSelectedItem();
	    if ((domainName == null) || domainName.equals("")) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.exportsharedsecret.error.domain"), this.translator.translate("dialog.exportsharedsecret.error.domain.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }

	    String filen = this.filename.getText();

	    if ((filen == null) || filen.equals("")) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.exportsharedsecret.error.file"), this.translator.translate("dialog.exportsharedsecret.error.file.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }

	    String owner = LoginData.getUsername();
	    String workDir = FileUtils.getWorkDirectoryForUser(owner);
	    String secretFilename = workDir + File.separator + filen;
	    SharedSecret secret = this.secrets.getSharedSecret(domainName);

	    if (secret == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.exportsharedsecret.error.secret"), this.translator.translate("dialog.exportsharedsecret.error.secret.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }
	    try {
		secret.save(secretFilename);
	    } catch (IOException excpt) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.exportsharedsecret.error.io"), this.translator.translate("dialog.exportsharedsecret.error.io.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("error"));
		return;
	    }

	}

	this.setVisible(false);
	this.dispose();
    }

}
