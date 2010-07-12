/*
 * Created on Oct 22, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import javax.swing.*;
import com.ibm.aglets.tahiti.*;
import java.awt.event.*;
import com.ibm.aglets.tahiti.utils.*;
import java.awt.*;
import com.ibm.atp.auth.*;
import com.ibm.atp.*;
import java.io.*;
import com.ibm.awb.misc.*;


/**
 * 
 */
class ImportSharedSecret extends TahitiDialog implements ActionListener {

    /**
     * A text field for the store file name
     */
    JTextField filename;
    
    
    /**
     * The parent dialog.
     */
    NetworkConfigDialog parent;

    ImportSharedSecret(JFrame f,NetworkConfigDialog net) {
        super(f, bundle.getString("dialog.importsharedsecret.title"), true);
        this.parent=net;
        this.getContentPane().add("North", new JLabel(bundle.getString("dialog.importsharedsecret.label"), JLabel.CENTER));
        GridBagPanel p = new GridBagPanel();

        this.getContentPane().add(p);

        filename = new JTextField(30);

        GridBagConstraints cns = new GridBagConstraints();

        cns.fill = GridBagPanel.HORIZONTAL;
        cns.anchor = GridBagConstraints.WEST;
        cns.gridwidth = GridBagConstraints.REMAINDER;

        p.setConstraints(cns);
        p.addLabeled(bundle.getString("dialog.importsharedsecret.filename"), filename);
        filename.addActionListener(this);
        
        // add the buttons
        this.addJButton(bundle.getString("dialog.importsharedsecret.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
        this.addJButton(bundle.getString("dialog.importsharedsecret.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
    }

    
    /**
     * Manage events from buttons.
     * @param event the event to manage
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        
        if(command.equals(TahitiCommandStrings.OK_COMMAND)){
            String filename= this.filename.getText();
            
            if(filename==null || filename.equals("")){
                JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.error.message"),bundle.getString("dialog.importsharedsecret.error.title"),JOptionPane.ERROR_MESSAGE,null);
                return;
            }
        

	        String owner = parent.getOwnerName();
	        String workDir = FileUtils.getWorkDirectoryForUser(owner);
	        String secretFilename = workDir + File.separator + filename;
	        SharedSecret secret = null;
	
	        try {
	            secret = SharedSecret.load(secretFilename);
	        } catch (FileNotFoundException excpt) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.error.filenotfound"),bundle.getString("dialog.importsharedsecret.error.filenotfound.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        } catch (IOException excpt) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.error.io"),bundle.getString("dialog.importsharedsecret.error.io.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        
	        if (secret == null) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.error.secret"),bundle.getString("dialog.importsharedsecret.error.secret.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        String domainName = secret.getDomainName();
	        SharedSecrets secrets = SharedSecrets.getSharedSecrets();
	        SharedSecret sec = secrets.getSharedSecret(domainName);
	
	        if (sec != null) {
	            JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.error.alreadyexists"),bundle.getString("dialog.importsharedsecret.error.alreadyexists.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        secrets.addSharedSecret(secret);
	        secrets.save();
	        JOptionPane.showMessageDialog(this,bundle.getString("dialog.importsharedsecret.ok"),bundle.getString("dialog.importsharedsecret.ok.title"),JOptionPane.INFORMATION_MESSAGE,null);
	        }
	        
        this.setVisible(false);
        dispose();
    }
}