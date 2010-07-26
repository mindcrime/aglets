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

import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.JComponentBuilder;

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

    static final String STARTUP_AGLET = "com.ibm.aglets.samples.Writer";

    /*
     * Singleton instance reference.
     */
    private static GeneralConfigDialog mySelf = null;

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
    private GeneralConfigDialog(MainWindow parentFrame) {
	super(parentFrame);

	// set the layout
	this.contentPanel.setLayout(new BorderLayout());

	// a first panel with the cache settings
	JPanel cachePanel = new JPanel();
	cachePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JLabel label = JComponentBuilder.createJLabel(this.baseKey + ".cache");
	cachePanel.add(label);
	JButton cacheButton = JComponentBuilder.createJButton(this.baseKey
		+ ".cacheButton", GUICommandStrings.CLEAR_CACHE_COMMAND, this);
	cachePanel.add(cacheButton);
	TitledBorder border = new TitledBorder(JComponentBuilder.getTitle(this.baseKey
		+ ".cache"));
	border.setTitleJustification(TitledBorder.CENTER);
	border.setTitleColor(Color.BLUE);
	cachePanel.setBorder(border);

	// a second panel with the startup agent to launch
	JPanel startPanel = new JPanel();
	startPanel.setLayout(new GridLayout(2, 0));
	this.startUpAgent = new JCheckBox(this.translator.translate(this.baseKey
		+ ".startupAgent"));
	this.startUpAgent.addItemListener(this);
	startPanel.add(this.startUpAgent);
	this.startUpAgentClass = new JTextField(20);
	startPanel.add(this.startUpAgentClass);
	border = new TitledBorder(JComponentBuilder.getTitle(this.baseKey
		+ ".startupAgent"));
	border.setTitleJustification(TitledBorder.CENTER);
	border.setTitleColor(Color.BLUE);
	startPanel.setBorder(border);

	// disable/enable the startup agent
	this.disabling();
	// load default config
	this.updateValues();

	// add the two panels to the main one
	this.contentPanel.add(cachePanel, BorderLayout.NORTH);
	this.contentPanel.add(startPanel, BorderLayout.CENTER);
	this.pack();
    }

    /*
	 */
    @Override
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	String command = event.getActionCommand();

	if (GUICommandStrings.OK_COMMAND.equals(command)) {
	    // save the values and exit
	    this.save();
	    this.setVisible(false);
	    this.dispose();
	} else if (GUICommandStrings.CANCEL_COMMAND.equals(command)) {
	    this.setVisible(false);
	    this.dispose();
	} else if (GUICommandStrings.CLEAR_CACHE_COMMAND.equals(command)) {
	    this.logger.info("Clearing the AgletRuntime Class Cache");
	    AgletRuntime.clearCache();
	}

    }

    /**
     * Enables or disables the startup agent text field depending on the
     * selection of the "launch at the startup" checkbox.
     * 
     */
    private void disabling() {
	this.startUpAgentClass.setEnabled(this.startUpAgent.isSelected());
    }

    /*
     * Singletion method to get the instnace
     */
    static GeneralConfigDialog getInstance(MainWindow parent) {
	if (GeneralConfigDialog.mySelf == null) {
	    GeneralConfigDialog.mySelf = new GeneralConfigDialog(parent);
	} /*
	   * else { mySelf.updateValues(); }
	   */
	return GeneralConfigDialog.mySelf;
    }

    /**
     * Manages events from the checkbox.
     */
    public void itemStateChanged(ItemEvent event) {
	if (event == null)
	    return;

	this.disabling();
    }

    /**
     * Saves the status of the dialog.
     * 
     */
    private void save() {
	this.logger.debug("Saving the parameters for the aglet general config");
	Resource tahiti_res = Resource.getResourceFor("tahiti");
	tahiti_res.setResource("tahiti.startup", String.valueOf(this.startUpAgent.isSelected()));
	tahiti_res.setResource("tahiti.startupAglets", this.startUpAgentClass.getText());

	Util.reset();
	Util.update();
    }

    /**
     * Sets the values from the saved parameters
     * 
     */
    private void updateValues() {
	Resource tahiti_res = Resource.getResourceFor("tahiti");
	this.startUpAgentClass.setText(tahiti_res.getString("tahiti.startupAglets"));
	this.startUpAgent.setSelected(Boolean.valueOf(tahiti_res.getString("tahiti.startup")));
	this.disabling();
    }

}
