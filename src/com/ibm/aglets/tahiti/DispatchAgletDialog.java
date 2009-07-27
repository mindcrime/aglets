package com.ibm.aglets.tahiti;

/*
 * @(#)DispatchAgletDialog.java
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
import com.ibm.aglets.tahiti.utils.*;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletProxy;

import com.ibm.aglets.*;
import com.ibm.awb.misc.Resource;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Font;
import java.awt.Label;
import java.awt.List;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.StringTokenizer;
import javax.swing.event.*;

/**
 * Class DispatchAgletDialog represents the dialog for dispatching an Aglet.
 * 
 * @version     1.02    $Date: 2009/07/27 10:31:40 $
 * @author      Danny B. Lange
 * Mitsuru Oshima
 */

final class DispatchAgletDialog extends TahitiDialog 
	implements ActionListener,ListSelectionListener {

	/*
	 * The proxy to be dispatched.
	 */
	private AgletProxy proxy = null;

	/*
	 * GUI components
	 */
	private JTextField _arlSelection;
	private AgentListPanel _arlList;
	private JButton _add;
	private JButton _remove;

	/*
	 * Constructs a new Aglet dispatch dialog.
	 */
	DispatchAgletDialog(MainWindow parent, AgletProxy proxy) {
		super(parent, bundle.getString("dialog.dispatch.title"), false);
		this.proxy = proxy;

		// create the agent list panel
		this._arlList = new AgentListPanel();
		this._arlList.addListSelectionListener(this);
		_arlSelection = new JTextField(40);
		
		

		// add the buttons
		this.addJButton(bundle.getString("dialog.dispatch.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.dispatch.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
		
		// other buttons
		_add = new JButton(bundle.getString("dialog.dispatch.button.add"),IconRepository.getIcon("add"));
		_remove = new JButton(bundle.getString("dialog.dispatch.button.remove"),IconRepository.getIcon("remove"));
		_add.setActionCommand(TahitiCommandStrings.ADD_COMMAND);
		_add.addActionListener(this);
		_remove.setActionCommand(TahitiCommandStrings.REMOVE_COMMAND);
		_remove.addActionListener(this);

		
		this.getContentPane().add("Center", makePanel());
		
		
		updateList();
		
		this.pack();
	}
	
	
	/**
	 * Manage events from the buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		
		if(command.equals(TahitiCommandStrings.ADD_COMMAND) && this._arlSelection.getText()!=null){
		    // add an entry to the list
		    this._arlList.addItem(this._arlSelection.getText());
		    return;
		}
		else
		if(command.equals(TahitiCommandStrings.REMOVE_COMMAND)){
		    // remove the selected item
		    this._arlList.removeSelectedItem();
		    return;
		}
		else
		if(command.equals(TahitiCommandStrings.OK_COMMAND) && this._arlSelection.getText()!=null 
		        && this.proxy!=null && this._arlSelection.getText().equals("")==false){
		    // dispatch the agent
		    this.getMainWindow().dispatchAglet(this.proxy,this._arlSelection.getText());
		}
		
		// close the window
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


		p.setConstraints(cns);

		// get the agent name
		String agentName ="CIAO";// this.getAgletName(this.proxy);
		// ask the user for the URL
		p.add(new JLabel(bundle.getString("dialog.dispatch.url")), 1, 0.0);

		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		p.add(_arlSelection);
		_arlSelection.addActionListener(this);

		/*
		 * Hot List
		 */
		cns.weightx = 1.0;
		cns.gridwidth = 1;
		cns.fill = GridBagConstraints.NONE;
		cns.anchor = GridBagConstraints.WEST;
		p.add(new JLabel(bundle.getString("dialog.dispatch.addressbook")));

		cns.anchor = GridBagConstraints.EAST;
		p.add(_add);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		p.add(_remove);

		cns.weighty = 1.0;
		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.BOTH;
		p.add(_arlList);

		

		// _arlList.setFont(DefaultResource.getFixedFont());
		_arlList.setBackground(Color.white);

		return p;
	}

	/*
	 * Updates the addressbook
	 */
	private void updateList() {
		Resource res = Resource.getResourceFor("aglets");
		if(res!=null){
			String items[] = res.getStringArray("aglets.addressbook", " ");
	
				for (int i = 0; i < items.length; i++) {
				this._arlList.addItem(items[i]);
			} 
		}
	}
	
	
	private void updateProperty() {
		synchronized (this) {
			Resource res = Resource.getResourceFor("aglets");
			if(res!=null){
				res.setResource("aglets.addressbook", this._arlList.getAllItems());
				res.save("Tahiti");
			}
		} 
	}
	
	/**
	 * Manage selections over the list.
	 * @param event the event
	 */
	public void valueChanged(ListSelectionEvent event){
	    this._arlSelection.setText(this._arlList.getSelectedItem());
	}
	
	
	
}
