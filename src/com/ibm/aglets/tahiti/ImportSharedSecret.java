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

    ImportSharedSecret(JFrame f, NetworkConfigDialog net) {
	super(f);
	this.parent = net;
	this.getContentPane().add("North", new JLabel(this.translator.translate("dialog.importsharedsecret.label"), SwingConstants.CENTER));
	GridBagPanel p = new GridBagPanel();

	this.getContentPane().add(p);

	this.filename = new JTextField(30);

	GridBagConstraints cns = new GridBagConstraints();

	cns.fill = GridBagPanel.HORIZONTAL;
	cns.anchor = GridBagConstraints.WEST;
	cns.gridwidth = GridBagConstraints.REMAINDER;

	p.setConstraints(cns);
	p.addLabeled(this.translator.translate("dialog.importsharedsecret.filename"), this.filename);
	this.filename.addActionListener(this);

	// add the buttons
	this.addButton(this.translator.translate("dialog.importsharedsecret.button.ok"), this);
	this.addButton(this.translator.translate("dialog.importsharedsecret.button.cancel"), this);
    }

    /**
     * Manage events from buttons.
     * 
     * @param event
     *            the event to manage
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(TahitiCommandStrings.OK_COMMAND)) {
	    String filename = this.filename.getText();

	    if ((filename == null) || filename.equals("")) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.error.message"), this.translator.translate("dialog.importsharedsecret.error.title"), JOptionPane.ERROR_MESSAGE, null);
		return;
	    }

	    String owner = LoginData.getUsername();
	    String workDir = FileUtils.getWorkDirectoryForUser(owner);
	    String secretFilename = workDir + File.separator + filename;
	    SharedSecret secret = null;

	    try {
		secret = SharedSecret.load(secretFilename);
	    } catch (FileNotFoundException excpt) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.error.filenotfound"), this.translator.translate("dialog.importsharedsecret.error.filenotfound.title"), JOptionPane.ERROR_MESSAGE, null);
		return;
	    } catch (IOException excpt) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.error.io"), this.translator.translate("dialog.importsharedsecret.error.io.title"), JOptionPane.ERROR_MESSAGE, null);
		return;
	    }

	    if (secret == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.error.secret"), this.translator.translate("dialog.importsharedsecret.error.secret.title"), JOptionPane.ERROR_MESSAGE, null);
		return;
	    }
	    String domainName = secret.getDomainName();
	    SharedSecrets secrets = SharedSecrets.getSharedSecrets();
	    SharedSecret sec = secrets.getSharedSecret(domainName);

	    if (sec != null) {
		JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.error.alreadyexists"), this.translator.translate("dialog.importsharedsecret.error.alreadyexists.title"), JOptionPane.ERROR_MESSAGE, null);
		return;
	    }
	    secrets.addSharedSecret(secret);
	    secrets.save();
	    JOptionPane.showMessageDialog(this, this.translator.translate("dialog.importsharedsecret.ok"), this.translator.translate("dialog.importsharedsecret.ok.title"), JOptionPane.INFORMATION_MESSAGE, null);
	}

	this.setVisible(false);
	this.dispose();
    }
}