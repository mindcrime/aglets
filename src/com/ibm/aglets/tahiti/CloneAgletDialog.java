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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Label;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.aglets.util.gui.*;

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
	private AgletProxy proxy = null;

	/*
	 * Constructs the clone Aglet window.
	 */
	CloneAgletDialog(MainWindow parent, AgletProxy proxy) {
	    super((JFrame) parent);
	    
	    // set the title
	    this.setTitle(JComponentBuilder.getTitle(this.baseKey));
	    
	    // store the proxy
	    this.proxy = proxy;

	    // try to understand if this is a valid agent
	    String message = null;
	    AgletInfo info = null;
	    try{
	    if( this.proxy == null || (info = this.proxy.getAgletInfo()) == null ){
		// show an error message
		message = this.translator.translate(this.baseKey + ".selectionError");
		this.showMessage(message);
	    }
	    else
		// try to get the information about this agent
		message = this.translator.translate(this.baseKey + ".confirmMessage");
		this.showMessage(message);
		this.showAgletInfo(info);
		
	    }catch(InvalidAgletException ex){
		JOptionPane.showMessageDialog(this,
			                      this.translator.translate(this.baseKey + ".infoError"),
			                      this.translator.translate(this.baseKey + ".infoError.title"),
			                      JOptionPane.ERROR_MESSAGE
			                      );
	    }
	    finally{
		this.pack();
	    }
	    

	}
	
	
	/**
	 * Manages events from the buttons and other components.
	 * If the command comes from the OK/Clone event that means the user wants
	 * to clone the agent, and thus I clone it and dispose this window, otherwise
	 * I leave the parent frame to manage the event.
	 */
	public void actionPerformed(ActionEvent ev) {
	    // check params
	    if( ev == null )
		return;
	    
	    String command = ev.getActionCommand();
	    
	    if( GUICommandStrings.OK_COMMAND.equals(command) ){
		// the user wants to clone the agent
		this.getMainWindow().cloneAglet(this.proxy);
		this.setVisible(false);
		this.dispose();
	    }
	    else
		super.actionPerformed(ev);
	}
}
