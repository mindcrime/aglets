package com.ibm.aglets.tahiti;

/*
 * @(#)RetractAgletDialog.java
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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.Choice;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Enumeration;

import java.net.*;

import com.ibm.awb.misc.Resource;
import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * Class RetractAgletDialog represents the dialog for Retracting an Aglet.
 * 
 * @version     1.02    $Date: 2009/07/27 10:31:40 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class RetractAgletDialog extends TahitiDialog implements ActionListener, 
		 Runnable {

    /* Load resources */
    static ResourceBundle bundle = null;
	static {
		bundle = ResourceBundle.getBundle("tahiti");
	} 
    
    
	/*
	 * Singleton instance reference.
	 */
	private static RetractAgletDialog _instance = null;

	
	
	/*
	 * GUI components
	 */
	private JComboBox _servers = new JComboBox();

	private AgentListPanel _agletsList = new AgentListPanel();

	private GridBagLayout grid = new GridBagLayout();

	String currentList = null;

	com.ibm.aglet.AgletProxy proxies[] = null;

	Thread handler = null;

	/*
	 * Constructs a new Aglet retract dialog.
	 */
	private RetractAgletDialog(MainWindow parent) {
		super(parent, bundle.getString("dialog.retract.title"), true);

		this.getContentPane().add("Center",this.makePanel());
		
		this.addJButton(bundle.getString("dialog.retract.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("retract"),this);
		this.addJButton(bundle.getString("dialog.retract.button.cancel"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("cancel"),this);
	}
	
	
	/**
	 * Manages events from the buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
	    String command=event.getActionCommand();
	    
	    if(command.equals(TahitiCommandStrings.OK_COMMAND)){
	        int selected = this._agletsList.getSelectedIndex();
	        
	        if(selected >= 0){
	            this.getMainWindow().retractAglet(proxies[selected]);
	        }
	    }
	    
	    this.setVisible(false);
	    dispose();

	}
	
	/*
	 * Singleton method to obtain the instance
	 */
	static RetractAgletDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new RetractAgletDialog(parent);
		} 
		_instance.buildServerList();
		_instance.run();
		return _instance;
	}
	
	
	
	protected GridBagPanel makePanel() {
		GridBagPanel p = new GridBagPanel();
		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.BOTH;
		cns.anchor = GridBagConstraints.WEST;
		cns.insets = new Insets(5, 5, 5, 5);
		cns.weighty = 0.0;
		cns.gridwidth = 1;
		cns.weightx = 0.1;


		p.add(new JLabel(bundle.getString("dialog.retract.label")), GridBagPanel.REMAINDER);
		
		p.addLabeled(bundle.getString("dialog.retract.label.server"), _servers);

	cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weighty = 1.0;
		p.add(_agletsList, cns);
		return p;
	}
	
	
	/**
	 * A method to retrieve the list of agent on a server. Originally this method was
	 * executed by a separated thread, now it's just a normal object method.
	 */
	public void run() {
	    
	    // get the destination server
		String dest = (String)_servers.getSelectedItem();

		try {
		    // get all proxies on the destination server
			proxies = com.ibm.aglet.system.Aglets.getAgletProxies(dest);

			if (proxies == null) {
				return;
			} 

			// remove all items from the list
			_agletsList.removeAll();

			for (int i = 0; i < proxies.length; i++) {
				com.ibm.aglet.AgletInfo info = proxies[i].getAgletInfo();
				_agletsList.addItem(info.getAgletClassName() + " : " 	+ info.getAgletID());
			}
		} catch (Exception ex) {
		    ex.printStackTrace();
		    return;
		}
 
	}
	
	
	/**
	 * A method to build the aglets servers list.
	 *
	 */
	protected void buildServerList(){
	    // read the server list from the properties
	    Resource res = Resource.getResourceFor("aglets");
		String list = res.getString("aglets.addressbook");

		// add all the items
		if (list != null && list.equals(currentList) == false) {
			currentList = list;
			_servers.removeAll();
			String items[] = res.getStringArray("aglets.addressbook", " ");

			for (int i = 0; i < items.length; i++) {
				_servers.addItem(items[i]);
			} 
		} 
	}
	
	

}
