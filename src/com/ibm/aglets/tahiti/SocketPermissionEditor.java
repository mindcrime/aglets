package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.TextField;
import java.util.Vector;

class SocketPermissionEditor extends PermissionEditor {

    // possible actions are "accept", "connect", "listen" and "resolve"
    private static final String LABEL_HOST = "Host";
    private static final int LENGTH_HOST = 10;
    private static final String LABEL_PORT = "Port";
    private static final int LENGTH_PORT = 5;

    private static final char CHAR_COLON = ':';
    private static final char CHAR_PORT_LEADER = CHAR_COLON;

    private String _hostname = null;
    private String _portno = null;

    private TextField host = new TextField(LENGTH_HOST);
    private TextField port = new TextField(LENGTH_PORT);

    SocketPermissionEditor() {
	GridBagLayout grid = new GridBagLayout();

	this.setLayout(grid);

	GridBagConstraints cns = new GridBagConstraints();

	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.ipadx = cns.ipady = 5;

	Label label = null;

	// host
	label = new Label(LABEL_HOST);
	this.add(label);
	cns.weightx = 0.2;
	cns.gridwidth = GridBagConstraints.RELATIVE;
	grid.setConstraints(label, cns);

	this.add(this.host);
	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.host, cns);

	// port
	label = new Label(LABEL_PORT);
	this.add(label);
	cns.weightx = 0.2;
	cns.gridwidth = GridBagConstraints.RELATIVE;
	grid.setConstraints(label, cns);

	this.add(this.port);
	cns.weightx = 0.5;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.port, cns);

	// actions
	label = new Label(LABEL_ACTIONS);
	this.add(label);
	cns.weightx = 0.2;
	cns.gridwidth = GridBagConstraints.RELATIVE;
	grid.setConstraints(label, cns);

	this.add(this.actions);
	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.actions, cns);
    }

    protected final void clearHostPort() {
	this._hostname = null;
	this._portno = null;
    }

    private final String getHostPort() {
	return getHostPort(this.host.getText(), this.port.getText());
    }

    private static final String getHostPort(String hostname, String portno) {
	String hostport = null;

	if ((hostname == null) || hostname.equals("")) {
	    hostport = "";
	} else if ((portno == null) || portno.equals("")) {
	    hostport = hostname;
	} else {
	    hostport = hostname + CHAR_PORT_LEADER + portno;
	}
	return hostport;
    }

    @Override
    public String getText() {
	Vector args = new Vector();
	final String hostport = this.getHostPort();
	final String acts = this.actions.getText();
	final boolean h = (hostport != null) && !hostport.equals("");
	final boolean a = (acts != null) && !acts.equals("");

	if (h || a) {
	    args.addElement(hostport);
	}
	if (a) {
	    args.addElement(acts);
	}
	return toText(args);
    }

    private final void parseHostPort(String hostport) {
	this.clearHostPort();
	final int portLeaderIndex = hostport.lastIndexOf(CHAR_PORT_LEADER);

	if (portLeaderIndex < 0) {
	    this._hostname = hostport;
	    this._portno = null;
	} else {
	    this._hostname = hostport.substring(0, portLeaderIndex);
	    this._portno = hostport.substring(portLeaderIndex + 1);
	}
    }

    @Override
    public void setText(String text) {
	this.parseText(text);
	final String hostport = this.getArg(0);
	final String acts = this.getArg(1);

	if (hostport != null) {
	    this.parseHostPort(hostport);
	    this.host.setText(this._hostname);
	    this.port.setText(this._portno);
	} else {
	    this.host.setText("");
	    this.port.setText("");
	}
	if (acts != null) {
	    this.actions.setText(acts);
	} else {
	    this.actions.setText("");
	}
    }
}
