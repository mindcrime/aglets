package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.atp.auth.SharedSecret;
import com.ibm.atp.auth.SharedSecrets;
import com.ibm.awb.misc.FileUtils;

/**
 * A dialog to import a new shared secret.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         05/nov/07
 */
public class ImportSharedSecretDialog extends TahitiDialog {

    /**
     * A text field with the file name of the shared secret.
     */
    private JTextField fileName = null;

    /**
     * A static file chooser. I cache it in order to show to the user always the
     * last directory where it was working.
     */
    private static JFileChooser fileChooser = null;

    public ImportSharedSecretDialog(JFrame parentFrame) {
	super(parentFrame);

	// create a panel with the jtextfield and a browse button
	JPanel panel = new JPanel();
	panel.setLayout(new FlowLayout());
	JButton browseButton = JComponentBuilder.createJButton(this.baseKey
		+ ".browseButton", GUICommandStrings.BROWSE_FILESYSTEM_COMMAND, this);

	this.fileName = JComponentBuilder.createJTextField(30, null, this.baseKey
		+ ".fileName");
	JLabel label = JComponentBuilder.createJLabel(this.baseKey
		+ ".fileLabel");
	panel.add(label);
	panel.add(this.fileName);
	panel.add(browseButton);

	this.contentPanel.add(panel, BorderLayout.CENTER);
	this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	if (GUICommandStrings.BROWSE_FILESYSTEM_COMMAND.equals(command)) {
	    try {
		// open the brose dialog
		if (fileChooser == null) {
		    fileChooser = new JFileChooser();
		    fileChooser.setMultiSelectionEnabled(false);
		    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		    String workingDir = FileUtils.getWorkDirectory();
		    fileChooser.setCurrentDirectory(new File(workingDir));
		}

		fileChooser.showOpenDialog(this);

		// now get the file
		this.fileName.setText((fileChooser.getSelectedFile()).getCanonicalPath());
	    } catch (IOException e) {
		this.logger.error("Exception caught while trying to get a certificate from the filesystem (browsing)", e);
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".fileError"), this.translator.translate(this.baseKey
				+ ".fileError.title"), JOptionPane.ERROR_MESSAGE);
	    }

	} else if (GUICommandStrings.OK_COMMAND.equals(command)) {
	    // the user wants to import the shared secret specified by the file
	    // name
	    try {
		// check for the file name
		String file = this.fileName.getText();
		SharedSecret secret = null;

		// try to load the shared secret
		secret = SharedSecret.load(file);

		// check if the shared secret for this domain already exists
		SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();
		if ((allSecrets.getSharedSecret(secret.getDomainName())) != null) {
		    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			    + ".sharedSecretExists"), this.translator.translate(this.baseKey
				    + ".sharedSecretExists.title"), JOptionPane.ERROR_MESSAGE);
		} else {
		    // ok, add the shared secret
		    allSecrets.addSharedSecret(secret);
		    allSecrets.save();
		}

	    } catch (FileNotFoundException e) {
		this.logger.error("Shared secret file not found, cannot import", e);
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".fileError2"), this.translator.translate(this.baseKey
				+ ".fileError2.title"), JOptionPane.ERROR_MESSAGE);

	    } catch (IOException e) {
		this.logger.error("Exception caught while trying to access the shared secret file", e);
		JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
			+ ".fileError2"), this.translator.translate(this.baseKey
				+ ".fileError2.title"), JOptionPane.ERROR_MESSAGE);
	    }

	}

	// leave the management of the GUI to the parent class
	super.actionPerformed(event);
    }
}
