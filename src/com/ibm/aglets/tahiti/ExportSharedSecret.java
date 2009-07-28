/*
 * Created on Oct 22, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JFrame;

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.ibm.aglets.tahiti.utils.*;


class ExportSharedSecret extends TahitiDialog implements
ActionListener {

AgentListPanel list;

JTextField filename;

SharedSecrets secrets;

NetworkConfigDialog parent;

ExportSharedSecret(JFrame f, SharedSecrets secs, NetworkConfigDialog net) {
super(f,bundle.getString("dialog.exportsharedsecret.title"), true);
secrets = secs;
parent = net;
this.getContentPane().add("North",  new JLabel(bundle.getString("dialog.exportsharedsecret.label"), JLabel.CENTER));
GridBagPanel p = new GridBagPanel();

this.getContentPane().add(p);

this.list = new AgentListPanel();
filename = new JTextField(20);

GridBagConstraints cns = new GridBagConstraints();

cns.fill = GridBagPanel.HORIZONTAL;
cns.anchor = GridBagConstraints.WEST;
cns.gridwidth = GridBagConstraints.REMAINDER;

p.setConstraints(cns);
p.addLabeled(bundle.getString("dialog.exportsharedsecret.label.domain"), list);
p.addLabeled(bundle.getString("dialog.exportsharedsecret.label.file"), filename);
Enumeration domains = secrets.getDomainNames();

if (domains != null) {
    while (domains.hasMoreElements()) {
        String domain = (String) domains.nextElement();

        list.addItem(domain);
    }
}



// add the buttons
this.addJButton(bundle.getString("dialog.exportsharedsecret.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
this.addJButton(bundle.getString("dialog.exportsharedsecret.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);

}


public void actionPerformed(ActionEvent event) {
String command = event.getActionCommand();

if(command.equals(TahitiCommandStrings.OK_COMMAND)){
    // export the secret
    String domainName = this.list.getSelectedItem();
    if (domainName == null || domainName.equals("")) {
        JOptionPane.showMessageDialog(this,bundle.getString("dialog.exportsharedsecret.error.domain"),bundle.getString("dialog.exportsharedsecret.error.domain.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
        return;
    }
    
    String filen = filename.getText();

    if (filen == null || filen.equals("")) {
        JOptionPane.showMessageDialog(this,bundle.getString("dialog.exportsharedsecret.error.file"),bundle.getString("dialog.exportsharedsecret.error.file.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
        return;
    }
    
    String owner = NetworkConfigDialog.getOwnerName();
    String workDir = FileUtils.getWorkDirectoryForUser(owner);
    String secretFilename = workDir + File.separator + filen;
    SharedSecret secret = secrets.getSharedSecret(domainName);

    if (secret == null) {
        JOptionPane.showMessageDialog(this,bundle.getString("dialog.exportsharedsecret.error.secret"),bundle.getString("dialog.exportsharedsecret.error.secret.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
        return;
    }
    try {
        secret.save(secretFilename);
    } catch (IOException excpt) {
        JOptionPane.showMessageDialog(this,bundle.getString("dialog.exportsharedsecret.error.io"),bundle.getString("dialog.exportsharedsecret.error.io.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
        return;
    }

}

this.setVisible(false);
dispose();
}


}

