package com.ibm.aglets.tahiti;

import java.awt.*;
import javax.swing.*;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.atp.auth.SharedSecret;

/**
 * Dialog for shared secret creation.
 * @author: Hideki Tai
 */
class CreateSharedSecretDialog extends TahitiDialog 
	implements java.awt.event.ActionListener {
	
	private static final String ACTION_OK = "OK";
	private JTextField domain;

	private JTextField creatorAlias = null;
	private JPasswordField creatorPassword = null;

	CreateSharedSecretDialog(JFrame f) {
		super(f, bundle.getString("dialog.createsharedsecret.title"), true);

		// set the layout for this window
		this.getContentPane().setLayout(new BorderLayout());
		
		// place a label
		this.getContentPane().add("North", new JLabel(bundle.getString("dialog.createsharedsecret.message"), JLabel.CENTER));
		GridBagPanel p = new GridBagPanel();

		this.getContentPane().add("Center",p);

		// Sets the input field for domain name
		domain = new JTextField(20);
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagPanel.HORIZONTAL;
		cns.anchor = GridBagConstraints.WEST;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		p.setConstraints(cns);
		p.addLabeled(bundle.getString("dialog.createsharedsecret.domain"), domain);
		domain.addActionListener(this);

		// Sets the input field for creator's alias
		creatorAlias = new JTextField(20);
		p.addLabeled(bundle.getString("dialog.createsharedsecret.alias"), creatorAlias);

		// Sets the input field for creator's password
		creatorPassword = new JPasswordField(20);
		p.addLabeled(bundle.getString("dialog.createsharedsecret.password"), creatorPassword);

		// create the button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton(bundle.getString("dialog.createsharedsecret.button.ok"), IconRepository.getIcon("ok"));
		JButton cancel = new JButton(bundle.getString("dialog.createsharedsecret.button.cancel"),IconRepository.getIcon("cancel"));
		ok.addActionListener(this);
		ok.setActionCommand(TahitiCommandStrings.OK_COMMAND);
		cancel.addActionListener(this);
		cancel.setActionCommand(TahitiCommandStrings.CANCEL_COMMAND);
		buttonPanel.add(ok);
		buttonPanel.add(cancel);
		this.getContentPane().add("South",buttonPanel);
		    
		this.pack();
		
	}
	
	
	/**
	 * Handles the button actions.
	 * @param event the event to deal with 
	 */
	public void actionPerformed(ActionEvent event) {
	    	String command = event.getActionCommand();
	    	String domainName="";
	    	String keyAlias="";
	    	String keyPassword="";
	    	
	    	if(command.equals(TahitiCommandStrings.CANCEL_COMMAND)){
	    	    this.setVisible(false);
	    	    this.dispose();
	    	    return;
	    	}
	    	else
	    	if(command.equals(TahitiCommandStrings.OK_COMMAND)){
	    	    // I need to create the new shared secret, check if all the data is available
	    	    domainName = domain.getText();
	    		keyAlias = creatorAlias.getText();
	    		keyPassword = new String(creatorPassword.getPassword());
	    
	    		// check values
	    		if(domainName == null || domainName.equals("")){
	    		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.createsharedsecret.error.domain"),bundle.getString("dialog.createsharedsecret.error.domain"),JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("sharedsecret"));
	    		    return;
	    		}
	    		
	    		if(keyAlias == null || keyAlias.equals("")){
	    		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.createsharedsecret.error.alias"),bundle.getString("dialog.createsharedsecret.error.alias"),JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("sharedsecret"));
	    		    return;
	    		}
	    		
	    	}
	    
		// Checks if the secret of the domain name is already exists.
		SharedSecrets secrets = SharedSecrets.getSharedSecrets();
		SharedSecret secret = secrets.getSharedSecret(domainName);

		if (secret != null) {
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.createsharedsecret.error.alreadyexists"),bundle.getString("dialog.createsharedsecret.error.alreadyexists"),JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("sharedsecret"));
		}
		else{

			// Creates a new shared secret.
			secret = SharedSecret.createNewSharedSecret(domainName, keyAlias, 
														keyPassword);
			if (secret == null) {
			    JOptionPane.showMessageDialog(this,bundle.getString("dialog.createsharedsecret.error.notcreated"),bundle.getString("dialog.createsharedsecret.error.notcreated"),JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("sharedsecret"));
			} else {
				secrets.addSharedSecret(secret);
				secrets.save();
				JOptionPane.showMessageDialog(this,bundle.getString("dialog.createsharedsecret.created"),bundle.getString("dialog.createsharedsecret.created"),JOptionPane.OK_OPTION, IconRepository.getIcon("sharedsecret"));
			}
		}
		dispose();
	}
	
	
}
