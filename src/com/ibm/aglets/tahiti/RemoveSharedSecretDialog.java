package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;

/**
 * A dialog to manage the remotion of a shared secret.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         07/nov/07
 */
public class RemoveSharedSecretDialog extends TahitiDialog {

    /**
     * The text field where the user must specify the password.
     */
    private JPasswordField passwordField = null;

    /**
     * The list of available domains.
     */
    private AgletListPanel<String> domainList = null;

    public RemoveSharedSecretDialog(JFrame parentFrame) {
	super(parentFrame);

	// create gui components
	JLabel label = JComponentBuilder.createJLabel(this.baseKey
		+ ".infoLabel");
	this.contentPanel.add(label, BorderLayout.NORTH);

	JPanel centerPanel = new JPanel();
	centerPanel.setLayout(new FlowLayout());
	label = JComponentBuilder.createJLabel(this.baseKey + ".domainLabel");
	centerPanel.add(label);
	this.domainList = new AgletListPanel<String>();
	this.domainList.setTitleBorder(this.translator.translate(this.baseKey
		+ ".domainLabel"));
	centerPanel.add(this.domainList);
	this.fillDomainList();
	this.contentPanel.add(centerPanel, BorderLayout.CENTER);

	JPanel southPanel = new JPanel();
	southPanel.setLayout(new FlowLayout());
	label = JComponentBuilder.createJLabel(this.baseKey + ".passwordLabel");
	this.passwordField = JComponentBuilder.createJPasswordField(20);
	southPanel.add(label);
	southPanel.add(this.passwordField);
	this.contentPanel.add(southPanel, BorderLayout.SOUTH);

	this.pack();

    }

    /**
     * Iterates on the domain list and adds each domain name (as a string) to
     * the list.
     * 
     */
    private void fillDomainList() {
	SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();

	if (allSecrets == null)
	    return;

	for (Enumeration enumer = allSecrets.getDomainNames(); (enumer != null)
	&& enumer.hasMoreElements();) {
	    String currentDomain = (String) enumer.nextElement();
	    this.domainList.addItem(currentDomain);
	}

    }

    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	if (GUICommandStrings.OK_COMMAND.equals(command)) {
	    // the user wants to remove the shared secret
	    String domain = this.domainList.getSelectedItem();
	    String password = new String(this.passwordField.getPassword());

	    SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();
	    SharedSecret selectedSecret = allSecrets.getSharedSecret(domain);

	    // check if this exists
	    if (selectedSecret == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".sharedSecretNotExists"), this.translator.translate(this.baseKey
				+ ".sharedSecretNotExists.title"), JOptionPane.ERROR_MESSAGE);
		return;

	    }

	    // check if the user can be authenticated
	    AgletRuntime runTime = AgletRuntime.getAgletRuntime();
	    String username = runTime.getOwnerName();
	    if (runTime.authenticateOwner(username, password) == null) {
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".authError"), this.translator.translate(this.baseKey
				+ ".authError.title"), JOptionPane.ERROR_MESSAGE);
		return;

	    }

	    // ok, now delete the shared secret
	    allSecrets.removeSharedSecret(domain);
	    allSecrets.save();

	}

	// leave the superclass to manage the events
	super.actionPerformed(event);
    }

}
