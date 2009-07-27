package com.ibm.aglets.tahiti;

/*
 * @(#)CreateAgletDialog.java
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

import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Color;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;

import java.util.StringTokenizer;

import com.ibm.awb.misc.Resource;
import com.ibm.aglets.tahiti.utils.*;
import java.awt.*;
import java.util.*;
import javax.swing.event.*;
/**
 * Class CreateAgletDialog represents the dialog for creating a new Aglet
 * instance. The class uses a CardLayout to handle the GUI differences
 * between creating an Aglet for a system class, local class file, remote
 * URL, and the hotlist of recently used Aglet classes.
 * 
 * @version     1.04    $Date: 2009/07/27 10:31:40 $
 * @author      Danny B. Lange
 */

final class CreateAgletDialog extends TahitiDialog implements ActionListener, 
		ListSelectionListener {

	/*
	 * Singleton instance reference.
	 */
	private static CreateAgletDialog _instance = null;

	/*
	 * GUI components
	 */
	private JTextField _classField = new JTextField(20);
	private JTextField _urlField = new JTextField(20);
	private AgentListPanel agentList;

	/**
	 * Constructs a new Aglet creation dialog.
	 * @param parent the parent frame.
	 */
	private CreateAgletDialog(MainWindow parent) {
		super(parent, bundle.getString("dialog.create.title"), false);

	
		// set the layout of this window
		this.getContentPane().setLayout(new BorderLayout());
		
		// create the panel with the agent name and sourcebase
		this.getContentPane().add("North",this.createUpperPanel());
		// add the agent list
		this.getContentPane().add("Center",this.createAgentListPanel());
		
		
		// create the button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton create = new JButton(bundle.getString("dialog.create.button.create"),IconRepository.getIcon("create"));
		create.setActionCommand(TahitiCommandStrings.CREATE_COMMAND);
		create.addActionListener(this);
		buttonPanel.add("South",create);
		JButton clearcreate = new JButton(bundle.getString("dialog.create.button.clearcreate"),IconRepository.getIcon("create"));
		clearcreate.setActionCommand(TahitiCommandStrings.CLEAR_CREATE_COMMAND);
		clearcreate.addActionListener(this);
		buttonPanel.add("South",clearcreate);
		JButton add = new JButton(bundle.getString("dialog.create.button.add"),IconRepository.getIcon("add"));
		add.setActionCommand(TahitiCommandStrings.ADD_COMMAND);
		add.addActionListener(this);
		buttonPanel.add("North",add);
		JButton remove = new JButton(bundle.getString("dialog.create.button.remove"),IconRepository.getIcon("remove"));
		remove.setActionCommand(TahitiCommandStrings.REMOVE_COMMAND);
		remove.addActionListener(this);
		buttonPanel.add("North",remove);
		JButton close = new JButton(bundle.getString("dialog.create.button.close"),IconRepository.getIcon("close"));
		close.setActionCommand(TahitiCommandStrings.CANCEL_COMMAND);
		close.addActionListener(this);
		buttonPanel.add("South",close);
		
		this.getContentPane().add("South",buttonPanel);
		
		this.pack();
				
	}
	
	/**
	 * Creates the upper panel, with fields for the agent name and code base
	 * @return the panel.
	 */
	protected JPanel createUpperPanel(){
	    JPanel ret = new JPanel();
	    ret.setLayout(new BorderLayout());
	    
	    // nested panels
	    JPanel p1 = new JPanel();
	    p1.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    p1.add(new JLabel(bundle.getString("dialog.create.label.agletname")));
	    p1.add(this._classField);
	    
	    JPanel p2 = new JPanel();
	    p2.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    p2.add(new JLabel(bundle.getString("dialog.create.label.codebase")));
	    p2.add(this._urlField);
	    
	    ret.add("North",p1);
	    ret.add("South",p2);
	    
	    return ret;
	}
	
	/**
	 * Creates a panel that contains the list of the agents.
	 * @return the panel
	 */
	protected JPanel createAgentListPanel(){
	    	    
	    // get a new agentlist
	    AgentListPanel ret = new AgentListPanel();
	    

	    
	    // add this class as listselection listener
	    ret.addListSelectionListener(this);
	    // store the panel
	    this.agentList = ret;
	    
	    // get the list of agents from the property file
	    this.updateList();
	    
	    return ret;
	    
	}
	
	/**
	 * Manage events from buttons
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		
		if(command!=null && command.equals(TahitiCommandStrings.CREATE_COMMAND) && this._classField.getText()!=null){
		    // create the specified aglet
		    createAglet(false);
		}
		else
		if(command!=null && command.equals(TahitiCommandStrings.ADD_COMMAND) && this._classField.getText()!=null){
		    this.agentList.addItem(this._classField.getText());
		    return;
		}
		else
		if(command != null && command.equals(TahitiCommandStrings.REMOVE_COMMAND)){
		    this.agentList.removeSelectedItem();
		    return;
		}
		else
		if(command !=null && command.equals(TahitiCommandStrings.CLEAR_CREATE_COMMAND) && this._classField.getText()!=null){
		    createAglet(true);
		}

		this.setVisible(false);
		this.dispose();
	}
	
	
	
	/*
	 * Creates an agent. 
	 * @param reload if true forces a creation with a new classloader.
	 */
	synchronized void createAglet(boolean reload) {
		
	    if((this._classField.getText()==null || this._classField.getText().equals("")) &&
		        this.agentList.getSelectedItem()!=null){
		    this._classField.setText(this.agentList.getSelectedItem());
		}
	    
		this.setVisible(false);
		String classname = _classField.getText().trim();
		String codebase = _urlField.getText().trim();

		// check if there's data
		if(classname ==null){
		    JOptionPane.showMessageDialog(this,"Please specify an aglet class name!");
		    return;
		}
		
		
		getMainWindow().createNewAglet(codebase, classname, reload);
	}
	
	
	/*
	 * Singleton method to get the instance
	 */
	static synchronized CreateAgletDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new CreateAgletDialog(parent);
		} 
		
		return _instance;
	}
	
	/**
	 * Handles list events
	 * @param event the event to manage
	 */
	public void valueChanged(ListSelectionEvent event){
	    if(event==null){
	        return;
	    }
	    
	    String selectedItem = this.agentList.getSelectedItem();
	   
	    if (selectedItem.toLowerCase().startsWith("http://") 
				|| selectedItem.toLowerCase().startsWith("https://") 
				|| selectedItem.toLowerCase().startsWith("atps://") 
				|| selectedItem.toLowerCase().startsWith("atp://") 
				|| selectedItem.toLowerCase().startsWith("file://")) {
			int delimiter = selectedItem.lastIndexOf('/');

			_classField.setText(selectedItem.substring(delimiter + 1));
			_urlField.setText(selectedItem.substring(0, delimiter));
		} else {
			_classField.setText(selectedItem);
			_urlField.setText("");
		} 
	    
	}
	
		/*
	 * Updates the hotlist
	 */
	private void updateList() {
		Resource res = Resource.getResourceFor("aglets");
		String list = res.getString("aglets.agletsList");

		StringTokenizer stz = new StringTokenizer(list," ");
	    while(stz.hasMoreTokens()){
	        this.agentList.addItem(stz.nextToken());
	    }
	    
	}
	private void updateProperty() {
		synchronized (this) {
		    String agletsList = this.agentList.getAllItems();
		    		    
			Resource res = Resource.getResourceFor("aglets");
			res.setResource("aglets.agletsList", agletsList);
			res.save("Tahiti");
		} 
	}
	
}
