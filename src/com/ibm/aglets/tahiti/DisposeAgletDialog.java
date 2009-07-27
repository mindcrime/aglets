package com.ibm.aglets.tahiti;

/*
 * @(#)DisposeAgletDialog.java
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

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * Class RemoveAgletDialog represents the dialog for removing an Aglet
 * instance.
 * 
 * @version     1.05    96/03/28
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

public class DisposeAgletDialog extends TahitiDialog 
	implements ActionListener {

	/*
	 * The proxies of the Aglet that is to be disposed.
	 */
	private AgletProxy[] _proxies = null;

	/*
	 * Constructs the remove Aglet window.
	 */
	DisposeAgletDialog(MainWindow parent, AgletProxy proxies[]) {
		super(parent, bundle.getString("dialog.dispose.title"), false);

		if(proxies==null || proxies.length==0){
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.dispose.error.proxy"),bundle.getString("dialog.dispose.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
		    return;
		}
		
		String msg[] = new String[proxies.length];

		for (int i = 0; i < proxies.length; i++) {
		    msg[i] = this.getAgletName(proxies[i]);
		} 

		
		
		
		this.getContentPane().add("North", new JLabel(bundle.getString("dialog.dispose.message"), JLabel.CENTER));
		this.getContentPane().add("Center", new MessagePanel(msg,JLabel.LEFT,false));

		// add buttons
		this.addJButton(bundle.getString("dialog.dispose.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.dispose.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
		
		_proxies = proxies;
	}
	/*
	 * Changes the look of the remove Aglet window to an error message window.
	 * void setError(String message) {
	 * setMessage("ERROR\n" + message);
	 * setButtons( OKAY );
	 * }
	 */

	/**
	 * Manage events from buttons.
	 * @param event the event to deal with
	 */
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();

	    if(command.equals(TahitiCommandStrings.OK_COMMAND) && this._proxies!=null && this._proxies.length>0){
	        for(int i=0; i<this._proxies.length;i++){
	            this.getMainWindow().disposeAglet(this._proxies[i]);
	        }
	    }
	    
	    this.setVisible(false);
	    this.dispose();
	}
}
