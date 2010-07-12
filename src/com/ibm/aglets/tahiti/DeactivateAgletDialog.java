package com.ibm.aglets.tahiti;

/*
 * @(#)DeactivateAgletDialog.java
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

import com.ibm.aglets.*;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;

/**
 * Class DeactivateAgletDialog is the dialog for deactivating an Aglet.
 * 
 * @version     1.02    $Date: 2009/07/27 10:31:40 $
 * @author	Mitsuru Oshima
 */

final class DeactivateAgletDialog extends TahitiDialog 
	implements ActionListener {

	/*
	 * The proxy to be deactivated
	 */
	private AgletProxy proxy = null;

	/*
	 * GUI components
	 */
	private JTextField _time = new JTextField(5);

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	DeactivateAgletDialog(MainWindow parent, AgletProxy proxy) {
		super( parent );
		this.proxy = proxy;

		// set the layout of this window
		this.getContentPane().setLayout(new BorderLayout());
		
		this.getContentPane().add("Center", makePanel());

		// add the button panel
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton(translator.translate("dialog.deactivate.button.ok"),IconRepository.getIcon("ok"));
		ok.addActionListener(this);
		ok.setActionCommand(TahitiCommandStrings.OK_COMMAND);
		JButton cancel = new JButton(translator.translate("dialog.deactivate.button.cancel"),IconRepository.getIcon("cancel"));
		cancel.addActionListener(this);
		cancel.setActionCommand(TahitiCommandStrings.CANCEL_COMMAND);
		p.add(ok);
		p.add(cancel);
		this.getContentPane().add("South",p);
		this.pack();
	}
	
	
	/**
	 * Manages events from the buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();
	    
	    if(command.equals(TahitiCommandStrings.OK_COMMAND)){
			if (proxy == null) {
				JOptionPane.showMessageDialog(this,translator.translate("dialog.deactivate.error.proxy"),
								   translator.translate("dialog.deactivate.error.proxy"),
								   JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
			} 
			if (!"".equals(_time.getText())) {
				long time = Integer.parseInt(_time.getText());
				if(time<0){
				    return;
				}
	
				getMainWindow().deactivateAglet(proxy, time);
				dispose();
				return;
			} 
			
	    }
	    
	    this.setVisible(false);
	    this.dispose();
	}
	/*
	 * Layouts all Components
	 */
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();

		GridBagConstraints cns = new GridBagConstraints();

		cns.insets = new Insets(5, 5, 5, 5);
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;

		
		// try to get the aglet name from its proxy
		String agletname = "Invalid Aglet";
		try {
			agletname = (proxy == null ? "No Aglet" 
						 : proxy.getAgletClassName());
		} catch (InvalidAgletException ex) {
		    // cannot get the aglet name
		    JOptionPane.showMessageDialog(this,
			     			  translator.translate("dialog.deactivate.error"),
			     			  translator.translate("dialog.deactivate.error"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
		}
		p.add(new JLabel(agletname, JLabel.CENTER), cns);

		/*
		 * Time to sleep
		 */
		cns.gridwidth = 1;
		cns.weightx = 0.0;
		p.add(new JLabel(translator.translate("dialog.deactivate.time")));

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		p.add(_time);

		_time.addActionListener(this);
		_time.setText("0");

		return p;
	}
	
	
}
