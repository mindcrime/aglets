package com.ibm.aglets.tahiti;

/*
 * @(#)DispatchAgletDialog.java
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletProxy;
import com.ibm.awb.misc.Resource;

/**
 * Class DispatchAgletDialog represents the dialog for dispatching an Aglet.
 * 
 * @version 1.02 $Date: 2009/07/28 07:04:52 $
 * @author Danny B. Lange Mitsuru Oshima
 */

final class DispatchAgletDialog extends TahitiDialog implements ActionListener,
ItemListener {

    /*
     * The proxy to be dispatched.
     */
    private AgletProxy proxy = null;

    private JTextField remoteURL = null;
    private AgletListPanel<URL> urlList = null;
    private JButton addURL = null;
    private JButton removeURL = null;

    /*
     * Constructs a new Aglet dispatch dialog.
     */
    DispatchAgletDialog(MainWindow parent, AgletProxy proxy) {
	super(parent);

	// store the proxy to dispatch
	this.proxy = proxy;

	// create components
	this.remoteURL = JComponentBuilder.createJTextField(40, "atp://", this.baseKey
		+ ".remoteURL");
	this.addURL = JComponentBuilder.createJButton(this.baseKey + ".addURL", GUICommandStrings.ADD_COMMAND, this);
	this.removeURL = JComponentBuilder.createJButton(this.baseKey
		+ ".removeURL", GUICommandStrings.REMOVE_COMMAND, this);
	this.urlList = new AgletListPanel<URL>();

	// create the north panel
	JPanel northPanel = new JPanel();
	northPanel.setLayout(new BorderLayout());
	JPanel northPanel1 = new JPanel();
	northPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JLabel label = JComponentBuilder.createJLabel(this.baseKey + ".URL");
	northPanel1.add(label);
	northPanel1.add(this.remoteURL);
	northPanel.add(northPanel1, BorderLayout.NORTH);
	JPanel northPanel2 = new JPanel();
	northPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
	northPanel2.add(this.addURL);
	northPanel2.add(this.removeURL);
	northPanel.add(northPanel2, BorderLayout.SOUTH);
	this.add(northPanel, BorderLayout.NORTH);

	// the center panel will be the agent list
	this.urlList.setTitleBorder(this.translator.translate(this.baseKey
		+ ".URL.title"));
	this.add(this.urlList, BorderLayout.CENTER);

	this.pack();

    }

    /*
     * Creates an Aglet dispatch dialog.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	try {
	    if (GUICommandStrings.ADD_COMMAND.equals(command)) {
		URL url = new URL(this.remoteURL.getText());
		this.urlList.addItem(url);
	    } else if (GUICommandStrings.REMOVE_COMMAND.equals(command)) {
		URL url = new URL(this.remoteURL.getText());
		this.urlList.removeItem(url);
	    } else if (GUICommandStrings.OK_COMMAND.equals(command)) {
		this.setVisible(false);
		this.getMainWindow().dispatchAglet(this.proxy, new URL(this.remoteURL.getText()));
		this.dispose();
	    } else
		super.actionPerformed(event);

	} catch (MalformedURLException e) {
	    this.logger.error("Exception caught while converting a string to an url", e);
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".error.URL"), this.translator.translate(this.baseKey
			    + ".error.URL.title"), JOptionPane.ERROR_MESSAGE);

	}

    }

    /*
     * Adds an item to the list
     */
    protected final void addURL() {
	try {
	    String url = this.remoteURL.getText();

	    // check if the url is valid
	    if ((url == null) || (url.length() == 0))
		return;
	    else
		this.urlList.addItem(new URL(url));
	} catch (MalformedURLException e) {
	    this.logger.error("Exception caught while converting a string to an url", e);
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".error.URL"), this.translator.translate(this.baseKey
			    + ".error.URL.title"), JOptionPane.ERROR_MESSAGE);
	}
    }

    // Handles list box selections.
    //
    @Override
    public void itemStateChanged(ItemEvent event) {
	if (event == null)
	    return;

	this.remoteURL.setText(this.urlList.getSelectedItem().toString());

    }

    /*
     * Delete an item from the list
     */
    protected final void removeURL() {
	try {
	    String url = this.remoteURL.getText();

	    // check if the url is valid
	    if ((url == null) || (url.length() == 0))
		return;
	    else
		this.urlList.removeItem(new URL(url));
	} catch (MalformedURLException e) {
	    this.logger.error("Exception caught while converting a string to an url", e);
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".error.URL"), this.translator.translate(this.baseKey
			    + ".error.URL.title"), JOptionPane.ERROR_MESSAGE);
	}
    }

    @Override
    public void dispose() {
	this.storeURLList();
	super.dispose();
    }

    private void storeURLList() {
	synchronized (this.urlList) {
	    int num = this.urlList.getItemCount();
	    String addressList = "";

	    for (int i = 0; i < num; i++) {
		addressList += (this.urlList.getItem(i).toString() + " ");
	    }
	    Resource res = Resource.getResourceFor("aglets");

	    res.setResource("aglets.addressbook", addressList);
	    res.save("Tahiti");
	}
    }
}
