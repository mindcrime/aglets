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
	super( f );
        this.parent=net;
        this.getContentPane().add("North", new JLabel(translator.translate("dialog.importsharedsecret.label"), JLabel.CENTER));
        GridBagPanel p = new GridBagPanel();

        this.getContentPane().add(p);

        filename = new JTextField(30);

        GridBagConstraints cns = new GridBagConstraints();

        cns.fill = GridBagPanel.HORIZONTAL;
        cns.anchor = GridBagConstraints.WEST;
        cns.gridwidth = GridBagConstraints.REMAINDER;

        p.setConstraints(cns);
        p.addLabeled(translator.translate("dialog.importsharedsecret.filename"), filename);
        filename.addActionListener(this);
        
        // add the buttons
        this.addButton(translator.translate("dialog.importsharedsecret.button.ok"),this);
        this.addButton(translator.translate("dialog.importsharedsecret.button.cancel"),this);
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
                JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.error.message"),translator.translate("dialog.importsharedsecret.error.title"),JOptionPane.ERROR_MESSAGE,null);
                return;
            }
        

	        String owner = LoginData.getUsername();
	        String workDir = FileUtils.getWorkDirectoryForUser(owner);
	        String secretFilename = workDir + File.separator + filename;
	        SharedSecret secret = null;
	
	        try {
	            secret = SharedSecret.load(secretFilename);
	        } catch (FileNotFoundException excpt) {
	            JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.error.filenotfound"),translator.translate("dialog.importsharedsecret.error.filenotfound.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        } catch (IOException excpt) {
	            JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.error.io"),translator.translate("dialog.importsharedsecret.error.io.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        
	        if (secret == null) {
	            JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.error.secret"),translator.translate("dialog.importsharedsecret.error.secret.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        String domainName = secret.getDomainName();
	        SharedSecrets secrets = SharedSecrets.getSharedSecrets();
	        SharedSecret sec = secrets.getSharedSecret(domainName);
	
	        if (sec != null) {
	            JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.error.alreadyexists"),translator.translate("dialog.importsharedsecret.error.alreadyexists.title"),JOptionPane.ERROR_MESSAGE,null);
	            return;
	        }
	        secrets.addSharedSecret(secret);
	        secrets.save();
	        JOptionPane.showMessageDialog(this,translator.translate("dialog.importsharedsecret.ok"),translator.translate("dialog.importsharedsecret.ok.title"),JOptionPane.INFORMATION_MESSAGE,null);
	        }
	        
        this.setVisible(false);
        dispose();
    }
}