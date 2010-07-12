/*
 * Created on Oct 16, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

<<<<<<< HEAD
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.aglets.util.gui.JComponentBuilder;

=======
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.ibm.aglets.tahiti.TahitiDialog.MessagePanel;
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
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
<<<<<<< HEAD
		super(parent);

		if(proxies==null || proxies.length==0){
		    JOptionPane.showMessageDialog(this,this.translator.translate("dialog.kill.error.proxy"),this.translator.translate("dialog.kill.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
=======
		super(parent, bundle.getString("dialog.kill.title"), false);

		if(proxies==null || proxies.length==0){
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.kill.error.proxy"),bundle.getString("dialog.kill.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
		    return;
		}
		
		String msg[] = new String[proxies.length];
<<<<<<< HEAD
		JTextArea area = new JTextArea( msg.length, 100);
		for (int i = 0; i < proxies.length; i++) {
		    msg[i] = this.getAgletName(proxies[i]);
		    area.append( msg[i] );
		    area.append( "\n" );
=======

		for (int i = 0; i < proxies.length; i++) {
		    msg[i] = this.getAgletName(proxies[i]);
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
		} 

		
		
		
<<<<<<< HEAD
		this.getContentPane().add("North", JComponentBuilder.createJLabel("dialog.kill.message") );
		
		this.getContentPane().add("Center", area);

		// add buttons
		JButton okButton     = JComponentBuilder.createJButton("dialog.kill.button.ok", TahitiCommandStrings.OK_COMMAND, this );
		JButton cancelButton = JComponentBuilder.createJButton("dialog.kill.button.cancel", TahitiCommandStrings.CANCEL_COMMAND, this );
		this.getContentPane().add( okButton );
		this.getContentPane().add( cancelButton );
=======
		this.getContentPane().add("North", new JLabel(bundle.getString("dialog.kill.message"), JLabel.CENTER));
		this.getContentPane().add("Center", new MessagePanel(msg,JLabel.LEFT,false));

		// add buttons
		this.addJButton(bundle.getString("dialog.kill.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.kill.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
		
		_proxies = proxies;
    }
    
<<<<<<< HEAD
    
    private String getAgletName(AgletProxy agletProxy) {
	StringBuffer agletName = new StringBuffer(100);
	try {
	    AgletInfo info = agletProxy.getAgletInfo();
	    agletName.append( "Classname:" );
	    agletName.append( info.getAgletClassName() );
	    agletName.append( " - Owner:" );
	    agletName.append( info.getAuthorityName() );
	    agletName.append( " - From:" );
	    agletName.append( info.getOrigin() );
	    agletName.append(" - Address:" );
	    agletName.append( info.getAddress() );
	} catch (InvalidAgletException e) {
	    logger.error("Cannot get the name of the aglet", e);
	}
	finally{
	    return agletName.toString();
	}
    }

=======
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
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
<<<<<<< HEAD
			    logger.error("Exception caught while killing agents", ex);
=======
			    this.getMainWindow().showException(ex);
>>>>>>> 218a26853c9210d659d7703d268e4b377f579a3b
			} 

	    }
	    
	    this.setVisible(false);
	    this.dispose();
	}
}
