/*
 * Created on Oct 16, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;

/**
 * 
 */
public class KillAgletDialog extends TahitiDialog implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -310269321700342540L;
    /**
     * The proxy to kill.
     */
    protected AgletProxy _proxies[] = null;

    /**
     * Default constructor.
     * 
     * @param parent
     *            the main window of this dialog
     * @param proxies
     *            an array of proxies to kill
     */
    public KillAgletDialog(MainWindow parent, AgletProxy[] proxies) {
	super(parent);

	if ((proxies == null) || (proxies.length == 0)) {
	    JOptionPane.showMessageDialog(this, this.translator.translate("dialog.kill.error.proxy"), this.translator.translate("dialog.kill.title"), JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("proxy"));
	    return;
	}

	String msg[] = new String[proxies.length];

	JTextArea area = new JTextArea(msg.length, 100);
	for (int i = 0; i < proxies.length; i++) {
	    msg[i] = this.getAgletName(proxies[i]);
	    area.append(msg[i]);
	    area.append("\n");
	}

	this.getContentPane().add("North", JComponentBuilder.createJLabel("dialog.kill.message"));

	this.getContentPane().add("Center", area);

	// add buttons
	JButton okButton = JComponentBuilder.createJButton("dialog.kill.button.ok", TahitiCommandStrings.OK_COMMAND, this);
	JButton cancelButton = JComponentBuilder.createJButton("dialog.kill.button.cancel", TahitiCommandStrings.CANCEL_COMMAND, this);
	this.getContentPane().add(okButton);
	this.getContentPane().add(cancelButton);

	this._proxies = proxies;
    }

    private String getAgletName(AgletProxy agletProxy) {
	StringBuffer agletName = new StringBuffer(100);
	try {
	    AgletInfo info = agletProxy.getAgletInfo();
	    agletName.append("Classname:");
	    agletName.append(info.getAgletClassName());
	    agletName.append(" - Owner:");
	    agletName.append(info.getAuthorityName());
	    agletName.append(" - From:");
	    agletName.append(info.getOrigin());
	    agletName.append(" - Address:");
	    agletName.append(info.getAddress());
	} catch (InvalidAgletException e) {
	    this.logger.error("Cannot get the name of the aglet", e);
	} finally {
	    return agletName.toString();
	}
    }

    /**
     * Manage events from buttons.
     * 
     * @param event
     *            the event to deal with
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if (command.equals(TahitiCommandStrings.OK_COMMAND)
		&& (this._proxies != null) && (this._proxies.length > 0)) {
	    try {
		for (AgletProxy _proxie : this._proxies) {
		    AgletRuntime.getAgletRuntime().killAglet(_proxie);
		}
	    } catch (Exception ex) {

		this.logger.error("Exception caught while killing agents", ex);

		this.getMainWindow().showException(ex);

	    }

	}

	this.setVisible(false);
	this.dispose();
    }
}
