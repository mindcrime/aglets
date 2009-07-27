package com.ibm.aglets.tahiti;

/*
 * @(#)CloneAgletDialog.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletInfo;

import com.ibm.aglets.*;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Label;
import javax.swing.*;
import java.awt.*;

/**
 * Class CloneAgletDialog is the dialog for cloning an Aglet instance.
 * 
 * @version     1.05    96/03/28
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */

final class CloneAgletDialog extends TahitiDialog implements ActionListener {

	/*
	 * The proxy of the Aglet that is to be cloned
	 */
	private AgletProxy _proxy = null;

	/*
	 * Constructs the clone Aglet window.
	 * @param parent the TahitiWindow parent of this dialog window
	 * @param proxy the proxy of the aglet to clone
	 */
	CloneAgletDialog(MainWindow parent, AgletProxy proxy) {
		super(parent, bundle.getString("dialog.clone.title"), true);
		
		_proxy = proxy;

		String msg = "Invalid Aglet";

		try {
			AgletInfo info = proxy.getAgletInfo();
			msg = (proxy == null ? "No Aglet selected" : info.getAgletClassName());
		} catch (InvalidAgletException ex) {
		    ex.printStackTrace();
		}

		this.getContentPane().add("North",new JLabel(bundle.getString("dialog.clone.message"),JLabel.CENTER));
		this.getContentPane().add("Center", new MessagePanel(msg));

		JButton clone = new JButton(bundle.getString("dialog.clone.button.clone"),IconRepository.getIcon("clone"));
		clone.setActionCommand(TahitiCommandStrings.CLONE_COMMAND);
		clone.addActionListener(this);
		JButton close = new JButton(bundle.getString("dialog.clone.button.cancel"),IconRepository.getIcon("cancel"));
		close.setActionCommand(TahitiCommandStrings.CANCEL_COMMAND);
		close.addActionListener(this);
		
		JPanel buttonPanel  = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(clone);
		buttonPanel.add(close);
		
		this.getContentPane().add("South",buttonPanel);
		this.pack();
	}
	
	/**
	 * Manage action events.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    
	    if(command!=null && command.equals(TahitiCommandStrings.CLONE_COMMAND)){
	        getMainWindow().cloneAglet(_proxy);    
	    }
	    
		setVisible(false);
		dispose();
	}
}
