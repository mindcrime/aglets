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
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

import java.awt.event.*;
import java.awt.*;


class RemoveSharedSecret extends TahitiDialog implements ActionListener {

    AgentListPanel list;

    JTextField password;

    SharedSecrets secrets;
    
    NetworkConfigDialog parent;

    RemoveSharedSecret(JFrame f, SharedSecrets secs, NetworkConfigDialog net) {
        super(f, bundle.getString("dialog.removesharedsecret.title"), true);
        secrets = secs;
        this.getContentPane().add("North", new JLabel(bundle.getString("dialog.removesharedsecret.label"), JLabel.CENTER));
        
        GridBagPanel p = new GridBagPanel();

        this.getContentPane().add(p);

        list = new AgentListPanel();
        password = new JPasswordField(20);
        

        GridBagConstraints cns = new GridBagConstraints();

        cns.fill = GridBagPanel.HORIZONTAL;
        cns.anchor = GridBagConstraints.WEST;
        cns.gridwidth = GridBagConstraints.REMAINDER;

        p.setConstraints(cns);
        p.addLabeled(bundle.getString("dialog.removesharedsecret.label.list"), list);
        p.addLabeled(bundle.getString("dialog.removesharedsecret.label.password"), password);
        
        Enumeration domains = secrets.getDomainNames();

        if (domains != null) {
            while (domains.hasMoreElements()) {
                String domain = (String) domains.nextElement();

                list.addItem(domain);
            }
        }
        
        
        // add the buttons
        this.addJButton(bundle.getString("dialog.removesharedsecret.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
        this.addJButton(bundle.getString("dialog.removesharedsecret.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
    }

    
    
    public void actionPerformed(ActionEvent ev) {
        String domainName = list.getSelectedItem();
        String command = ev.getActionCommand();

        if(command.equals(TahitiCommandStrings.OK_COMMAND)){
	        if (domainName == null || domainName.equals("")) {
	           JOptionPane.showMessageDialog(this,bundle.getString("dialog.removesharedsecret.error.nulldomain"),bundle.getString("dialog.removesharedsecret.error.nulldomain.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
	            return;
	        }
	        SharedSecret secret = secrets.getSharedSecret(domainName);
	
	        if (secret == null) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.removesharedsecret.error.nullsecret"),bundle.getString("dialog.removesharedsecret.error.nullsecret.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
	            return;
	        }
	        AgletRuntime rt = AgletRuntime.getAgletRuntime();
	
	        // Certificate ownerCert = rt.getOwnerCertificate();
	        String ownerName = rt.getOwnerName();
	
	        if (rt.authenticateOwner(ownerName, password.getText()) == null) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.removesharedsecret.error.password"),bundle.getString("dialog.removesharedsecret.error.password.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
	            password.setText("");
	            return;
	        }
	
	        
	        secrets.removeSharedSecret(domainName);
	        secrets.save();
        }
        
        this.setVisible(false);
        dispose();
    }
}
;
