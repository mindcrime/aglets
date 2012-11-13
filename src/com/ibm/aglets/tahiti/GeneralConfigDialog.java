package com.ibm.aglets.tahiti;

/*
 * @(#)GeneralConfigDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglets.AgletRuntime;
import com.ibm.awb.misc.Resource;

/**
 * Class GeneralConfigDialog represents the dialog for
 * 
 * @version 1.01 96/03/28
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 * @author Yoshiaki Mima
 */

final class GeneralConfigDialog extends TahitiDialog implements ActionListener,
ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2800407145792800916L;

	static final String STARTUP_AGLET = "com.ibm.aglets.samples.Writer";

	/*
	 * Singleton instance reference.
	 */
	private static GeneralConfigDialog mySelf = null;

	/*
	 * Singletion method to get the instnace
	 */
	static GeneralConfigDialog getInstance(final MainWindow parent) {
		if (GeneralConfigDialog.mySelf == null) {
			GeneralConfigDialog.mySelf = new GeneralConfigDialog(parent);
		} /*
		 * else { mySelf.updateValues(); }
		 */
		return GeneralConfigDialog.mySelf;
	}

	/**
	 * A checkbox to indicate if the startup agent must be launched.
	 */
	private JCheckBox startUpAgent = null;

	/**
	 * The textfield that must contain the agent to start.
	 */
	private JTextField startUpAgentClass = null;

	/*
	 * Constructs a new Aglet creation dialog.
	 */
	private GeneralConfigDialog(final MainWindow parentFrame) {
		super(parentFrame);

		// set the layout
		contentPanel.setLayout(new BorderLayout());

		// a first panel with the cache settings
		final JPanel cachePanel = new JPanel();
		cachePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JLabel label = JComponentBuilder.createJLabel(baseKey + ".cache");
		cachePanel.add(label);
		final JButton cacheButton = JComponentBuilder.createJButton(baseKey
				+ ".cacheButton", GUICommandStrings.CLEAR_CACHE_COMMAND, this);
		cachePanel.add(cacheButton);
		TitledBorder border = new TitledBorder(JComponentBuilder.getTitle(baseKey
				+ ".cache"));
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitleColor(Color.BLUE);
		cachePanel.setBorder(border);

		// a second panel with the startup agent to launch
		final JPanel startPanel = new JPanel();
		startPanel.setLayout(new GridLayout(2, 0));
		startUpAgent = new JCheckBox(translator.translate(baseKey
				+ ".startupAgent"));
		startUpAgent.addItemListener(this);
		startPanel.add(startUpAgent);
		startUpAgentClass = new JTextField(20);
		startPanel.add(startUpAgentClass);
		border = new TitledBorder(JComponentBuilder.getTitle(baseKey
				+ ".startupAgent"));
		border.setTitleJustification(TitledBorder.CENTER);
		border.setTitleColor(Color.BLUE);
		startPanel.setBorder(border);

		// disable/enable the startup agent
		disabling();
		// load default config
		updateValues();

		// add the two panels to the main one
		contentPanel.add(cachePanel, BorderLayout.NORTH);
		contentPanel.add(startPanel, BorderLayout.CENTER);
		pack();
	}

	/*
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		// check params
		if (event == null)
			return;

		final String command = event.getActionCommand();

		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// save the values and exit
			save();
			setVisible(false);
			dispose();
		} else if (GUICommandStrings.CANCEL_COMMAND.equals(command)) {
			setVisible(false);
			dispose();
		} else if (GUICommandStrings.CLEAR_CACHE_COMMAND.equals(command)) {
			logger.info("Clearing the AgletRuntime Class Cache");
			AgletRuntime.clearCache();
		}

	}

	/**
	 * Enables or disables the startup agent text field depending on the
	 * selection of the "launch at the startup" checkbox.
	 * 
	 */
	private void disabling() {
		startUpAgentClass.setEnabled(startUpAgent.isSelected());
	}

	/**
	 * Manages events from the checkbox.
	 */
	@Override
	public void itemStateChanged(final ItemEvent event) {
		if (event == null)
			return;

		disabling();
	}

	/**
	 * Saves the status of the dialog.
	 * 
	 */
	private void save() {
		logger.debug("Saving the parameters for the aglet general config");
		final Resource tahiti_res = Resource.getResourceFor("tahiti");
		tahiti_res.setResource("tahiti.startup", String.valueOf(startUpAgent.isSelected()));
		tahiti_res.setResource("tahiti.startupAglets", startUpAgentClass.getText());

		Util.reset();
		Util.update();
	}

	/**
	 * Sets the values from the saved parameters
	 * 
	 */
	private void updateValues() {
		final Resource tahiti_res = Resource.getResourceFor("tahiti");
		startUpAgentClass.setText(tahiti_res.getString("tahiti.startupAglets"));
		startUpAgent.setSelected(Boolean.valueOf(tahiti_res.getString("tahiti.startup")));
		disabling();
	}

}
