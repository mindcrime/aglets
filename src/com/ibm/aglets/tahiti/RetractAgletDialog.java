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

import java.awt.BorderLayout;
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

import java.util.Vector;
import java.util.Enumeration;

import java.net.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.Aglets;
import com.ibm.awb.misc.Resource;

/**
 * Class RetractAgletDialog represents the dialog for Retracting an Aglet.
 * 
 * @version     1.02    $Date: 2009/07/28 07:04:53 $
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class RetractAgletDialog extends TahitiDialog implements ActionListener, 
		ItemListener {


	
	private JComboBox servers = null;
	private AgletListPanel<AgletProxy> agletsList = null;
	private JButton refreshProxies = null;
	
	private String defaultServerString = null;


	String currentList = null;

	/**
	 * A list of selected proxies that represents the proxy available on the
	 * selected remote context/server.
	 */
	private AgletProxy proxies[] = null;



	/**
	 * The main constructor of this dialog window.
	 * @param parent the parent frame of the dialog window
	 */
	protected RetractAgletDialog(MainWindow parent) {
	    super(parent);
	    
	    // build up a list of agents
	    this.agletsList = new AgletListPanel<AgletProxy>();
	    this.agletsList.setRenderer(new AgletListRenderer(this.agletsList));
	    
	    this.defaultServerString = this.translator.translate(this.baseKey + ".serverHeader");
	    
	    // the combobox for the server
	    this.servers = new JComboBox();
	    this.servers.setEditable(true);
	    this.servers.addItemListener(this);
	    this.updateServerList();
	    this.refreshProxies = JComponentBuilder.createJButton(this.baseKey + ".refreshButton",
		    					          GUICommandStrings.REFRESH_COMMAND,
		    					          this);
	    
	    // create a north panel
	    JPanel northPanel = new JPanel();
	    northPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    JLabel label = JComponentBuilder.createJLabel(this.baseKey + ".serverLabel");
	    northPanel.add(label);
	    northPanel.add(this.servers);
	    northPanel.add(this.refreshProxies);
	    this.add(northPanel, BorderLayout.NORTH);
	    
	    // add the aglet list panel at the center
	    this.add(this.agletsList, BorderLayout.CENTER);
	    
	    // pack
	    this.pack();    
	    
	}
	/*
	 * Creates an Aglet retract dialog.
	 */
	public void actionPerformed(ActionEvent event) {
	    if( event == null )
		return;
	    
	    // if the event comes from the jcombobox (i.e., the user has pressed
	    // enter on the combobox) I must refresh the proxy list
	    if( event.getSource() == this.servers ){
		this.getRemoteProxies();
		this.updateAgletList();
		return;
	    }
	    
	    
	    // the action comes from a menu
	    
	    String command = event.getActionCommand();
	    
	    if( GUICommandStrings.OK_COMMAND.equals(command)){
		AgletProxy selectedProxy = (AgletProxy) this.agletsList.getSelectedItem();
		this.setVisible(false);
		this.getMainWindow().retractAglet(selectedProxy);
		this.dispose();
	    }
	    else
	    if( GUICommandStrings.REFRESH_COMMAND.equals(command)){
		this.getRemoteProxies();
		this.updateAgletList();
	    }
	    else
		super.actionPerformed(event);	
	}
	
	
	
	
	/**
	 * Handles the events on the combobox menu.
	 */
	public synchronized void itemStateChanged(ItemEvent event) {
	    // check params
	    if( event == null || this.servers.getSelectedItem() == null )
		return;
	    
	    // try to get the selected URL
	    this.getRemoteProxies();
	    
	}

	/**
	 * Gets the list of remote proxies. 
	 */
	private void getRemoteProxies() {
	    try{
		String finalDestination = null;
		
		Object selection = this.servers.getSelectedItem();
		this.logger.debug("Remote server has been selected as " + selection);
		
		if( selection instanceof URL )
		    finalDestination = (String) ((URL) selection).toExternalForm();
		else
		if( selection instanceof String && (!(this.defaultServerString.equals((String) selection))) )
		    finalDestination = (String) this.servers.getSelectedItem();
		else
		    return;
	    
	    
		// update the aglet proxy list for such URL
		if( finalDestination != null ){
		    this.proxies = Aglets.getAgletProxies(finalDestination);
		    // update the list
		    this.updateAgletList();
		    
		    // store the remote server in the combobox
		    this.addServerItem();
		}
		
	    }catch(Exception e){
		this.logger.error("Exception caught while trying to get the list of remote proxies from "  + this.servers.getSelectedItem(), e);
		this.agletsList.removeAllItems();
		JOptionPane.showMessageDialog(this,
  	                  this.translator.translate(this.baseKey + ".error.proxy"),
  	                  this.translator.translate(this.baseKey + ".error.proxy.title"),
  	                  JOptionPane.ERROR_MESSAGE
  	                  );
	    }
	}
	
	/**
	 *Updates the aglet proxy list supposing the array of proxies has
	 *been already updated.
	 *
	 *
	 */
	private void updateAgletList() {
	    // supposing I've already got the proxy list, I can update it
	    this.agletsList.removeAllItems();
	    
	    if( this.proxies != null && this.proxies.length > 0 )
		for( int i=0; i< this.proxies.length; i++ )
		    this.agletsList.addItem(this.proxies[i]);
	    
	}
	
	
	
	/**
	 * Adds a new item to the server list if not already present.
	 * @param serverItem the item to add
	 */
	private void addServerItem(Object serverItem){
	    boolean toInsert = true;
	    
	    int items = this.servers.getItemCount();
	    for(int i=0; i<items; i++)
		if( this.servers.getItemAt(i).equals(serverItem))
		    toInsert = false;
	    
	    if( toInsert )
		this.servers.addItem(serverItem);
	    
	    this.pack();
	}
	
	/**
	 * Adds the item editable.
	 *
	 */
	private void addServerItem(){
	    this.addServerItem( this.servers.getSelectedItem() );
	}
	
	
	/**
	 * Updates the server list in the combobox.
	 *
	 */
	protected void updateServerList() {
		Resource res = Resource.getResourceFor("aglets");
		String list = res.getString("aglets.addressbook");

		if (list != null && list.equals(currentList) == false) {
			this.currentList = list;
			this.servers.removeAllItems();
			this.servers.addItem( this.defaultServerString );
			
			
			// get all the know servers
			String items[] = res.getStringArray("aglets.addressbook", " ");

			
			for (int i = 0; i < items.length; i++) {
			    try{
				this.servers.addItem( new URL(items[i]) );
			    }catch(MalformedURLException e){
				this.logger.error("Exception caught while converting a string to an URL", e);
			    }
			}
			    /*JOptionPane.showMessageDialog(this,
				    	                  this.translator.translate(this.baseKey + ".error.URL"),
				    	                  this.translator.translate(this.baseKey + ".error.URL.title"),
				    	                  JOptionPane.ERROR_MESSAGE
				    	                  );
				    	              */
		} 
	}

	

}
