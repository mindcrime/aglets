package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

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

public class ExportSharedSecretDialog extends TahitiDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3129746473663734302L;

	/**
	 * The text field where the user can specify the file to export to.
	 */
	private JTextField fileTextField = null;

	/**
	 * The list of available domains.
	 */
	private AgletListPanel<String> domainList = null;

	/**
	 * A cached file chooser (thus it stores always the latest directory where
	 * the user worked).
	 */
	private static JFileChooser fileChooser = null;

	public ExportSharedSecretDialog(final JFrame parentFrame) {
		super(parentFrame);

		// create gui components
		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".infoLabel");
		contentPanel.add(label, BorderLayout.NORTH);

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".domainLabel");
		centerPanel.add(label);
		domainList = new AgletListPanel<String>();
		domainList.setTitleBorder(translator.translate(baseKey
				+ ".domainLabel"));
		centerPanel.add(domainList);
		fillDomainList();
		contentPanel.add(centerPanel, BorderLayout.CENTER);

		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		label = JComponentBuilder.createJLabel(baseKey + ".fileLabel");
		fileTextField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".fileName");
		final JButton browseButton = JComponentBuilder.createJButton(baseKey
				+ ".browseButton", GUICommandStrings.BROWSE_FILESYSTEM_COMMAND, this);
		southPanel.add(label);
		southPanel.add(fileTextField);
		southPanel.add(browseButton);
		contentPanel.add(southPanel, BorderLayout.SOUTH);

		pack();

	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		if (GUICommandStrings.BROWSE_FILESYSTEM_COMMAND.equals(command)) {
			try {
				// open the brose dialog
				if (fileChooser == null) {
					fileChooser = new JFileChooser();
					fileChooser.setMultiSelectionEnabled(false);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					final String workingDir = FileUtils.getWorkDirectory();
					fileChooser.setCurrentDirectory(new File(workingDir));
				}

				fileChooser.showOpenDialog(this);

				// now get the file
				fileTextField.setText((fileChooser.getSelectedFile()).getCanonicalPath());
			} catch (final IOException e) {
				logger.error("Exception caught while trying to get a certificate from the filesystem (browsing)", e);
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".fileError"), translator.translate(baseKey
								+ ".fileError.title"), JOptionPane.ERROR_MESSAGE);
			}

		} else if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// the user wants to import the shared secret specified by the file
			// name
			try {
				// check for the file name
				final String file = fileTextField.getText();
				final String domain = domainList.getSelectedItem();

				final SharedSecret secret = SharedSecrets.getSharedSecrets().getSharedSecret(domain);

				if (secret == null)
					JOptionPane.showMessageDialog(this, translator.translate(baseKey
							+ ".sharedSecretNotExists"), translator.translate(baseKey
									+ ".sharedSecretNotExists.title"), JOptionPane.ERROR_MESSAGE);
				else
					secret.save(file);

			} catch (final FileNotFoundException e) {
				logger.error("Shared secret file not found, cannot import", e);
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".fileError2"), translator.translate(baseKey
								+ ".fileError2.title"), JOptionPane.ERROR_MESSAGE);

			} catch (final IOException e) {
				logger.error("Exception caught while trying to access the shared secret file", e);
				JOptionPane.showMessageDialog(this, translator.translate(baseKey
						+ ".fileError2"), translator.translate(baseKey
								+ ".fileError2.title"), JOptionPane.ERROR_MESSAGE);
			}

		}

		// leave the management of the GUI to the parent class
		super.actionPerformed(event);
	}

	/**
	 * Iterates on the domain list and adds each domain name (as a string) to
	 * the list.
	 * 
	 */
	private void fillDomainList() {
		final SharedSecrets allSecrets = SharedSecrets.getSharedSecrets();

		if (allSecrets == null)
			return;

		for (final Enumeration enumer = allSecrets.getDomainNames(); (enumer != null)
		&& enumer.hasMoreElements();) {
			final String currentDomain = (String) enumer.nextElement();
			domainList.addItem(currentDomain);
		}

	}

}
