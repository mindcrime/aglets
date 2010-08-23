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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import net.sourceforge.aglets.util.gui.GUICommandStrings;

import com.ibm.aglet.AgletProxy;

/**
 * Class RemoveAgletDialog represents the dialog for removing an Aglet instance.
 * 
 * @version 1.05 96/03/28
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class DisposeAgletDialog extends TahitiDialog implements ActionListener {

    /**
     * A list of the agent proxies that must be disposed.
     */
    private LinkedList<AgletProxy> proxies = null;

    /*
     * Constructs the remove Aglet window.
     */
    DisposeAgletDialog(MainWindow parent, AgletProxy proxies[]) {
	super(parent);

	// build up a list from the proxy array
	if ((proxies != null) && (proxies.length > 0)) {
	    LinkedList<AgletProxy> proxyList = new LinkedList<AgletProxy>();

	    for (AgletProxy proxie : proxies)
		proxyList.add(proxie);

	    // add the information about the proxies
	    this.showAgletProxies(proxyList);
	    this.proxies = proxyList;

	    // add a text
	    this.showMessage("Please confirm the dispose operation over the "
		    + proxies.length + " agents");
	    this.pack();
	}
    }

    DisposeAgletDialog(MainWindow parent, LinkedList<AgletProxy> proxies) {
	super(parent);

	// store the list of proxies
	this.proxies = proxies;

	// show the list of the agent that I'm going to work on
	this.showAgletProxies(proxies);

	// add a text
	this.showUserMessage();
	this.pack();

    }

    DisposeAgletDialog(MainWindow parent, AgletProxy proxy) {
	super(parent);

	// create a new list of one element
	this.proxies = new LinkedList<AgletProxy>();
	this.proxies.add(proxy);

	this.showAgletProxies(this.proxies);

	// add a text
	this.showUserMessage();
	this.pack();

    }

    /**
     * Shows a user message for asking confirmation.
     * 
     */
    protected void showUserMessage() {
	String localizedString = this.translator.translate(this.baseKey
		+ ".userMessage");
	this.showMessage(localizedString);
    }

    /*
     * Disposes the selected Aglet.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	if (GUICommandStrings.OK_COMMAND.equals(command)
		&& (this.proxies != null)) {
	    // dispose
	    MainWindow mWindow = this.getMainWindow();
	    this.setVisible(false);

	    // iterate on each aglet
	    Iterator iter = this.proxies.iterator();
	    while ((iter != null) && iter.hasNext()) {
		AgletProxy currentProxy = (AgletProxy) iter.next();
		mWindow.disposeAglet(currentProxy);

	    }

	    this.dispose();

	} else
	    super.actionPerformed(event);

    }
}
