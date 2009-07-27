/*
 * Created on Oct 16, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.ibm.aglets.tahiti.TahitiDialog.MessagePanel;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import com.ibm.aglet.*;
import com.ibm.aglet.system.AgletRuntime;

import java.awt.event.*;


/**
 * 
 */
public class KillAgletDialog extends TahitiDialog implements ActionListener {

    
    /**
     * The proxy to kill.
     */
    protected AgletProxy _proxies[]=null;
    
    /**
     * Default constructor.
     * @param parent the main window of this dialog
     * @param proxies an array of proxies to kill
     */
    public KillAgletDialog(MainWindow parent,AgletProxy[] proxies){
		super(parent, bundle.getString("dialog.kill.title"), false);

		if(proxies==null || proxies.length==0){
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.kill.error.proxy"),bundle.getString("dialog.kill.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
		    return;
		}
		
		String msg[] = new String[proxies.length];

		for (int i = 0; i < proxies.length; i++) {
		    msg[i] = this.getAgletName(proxies[i]);
		} 

		
		
		
		this.getContentPane().add("North", new JLabel(bundle.getString("dialog.kill.message"), JLabel.CENTER));
		this.getContentPane().add("Center", new MessagePanel(msg,JLabel.LEFT,false));

		// add buttons
		this.addJButton(bundle.getString("dialog.kill.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.kill.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
		
		_proxies = proxies;
    }
    
	/**
	 * Manage events from buttons.
	 * @param event the event to deal with
	 */
	public void actionPerformed(ActionEvent event) {
	    String command = event.getActionCommand();

	    if(command.equals(TahitiCommandStrings.OK_COMMAND) && this._proxies!=null && this._proxies.length>0){
	         try {
		        for(int i=0; i<this._proxies.length;i++){
								AgletRuntime.getAgletRuntime().killAglet(this._proxies[i]);
		        }
			} catch (Exception ex) {
			    this.getMainWindow().showException(ex);
			} 

	    }
	    
	    this.setVisible(false);
	    this.dispose();
	}
}
