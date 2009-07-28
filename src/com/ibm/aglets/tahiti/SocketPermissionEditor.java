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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Font;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.io.File;
import java.io.IOException;

import java.security.Policy;
import com.ibm.aglets.security.PolicyDB;
import com.ibm.aglets.security.PolicyFileReader;
import com.ibm.aglets.security.PolicyFileWriter;
import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;
import com.ibm.aglets.security.PolicyFileParsingException;

import com.ibm.awb.misc.URIPattern;
import com.ibm.awb.misc.MalformedURIPatternException;
import com.ibm.awb.misc.Resource;

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

		setLayout(grid);

		GridBagConstraints cns = new GridBagConstraints();

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.ipadx = cns.ipady = 5;

		Label label = null;

		// host
		label = new Label(LABEL_HOST);
		add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		add(host);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(host, cns);

		// port
		label = new Label(LABEL_PORT);
		add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		add(port);
		cns.weightx = 0.5;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(port, cns);

		// actions
		label = new Label(LABEL_ACTIONS);
		add(label);
		cns.weightx = 0.2;
		cns.gridwidth = GridBagConstraints.RELATIVE;
		grid.setConstraints(label, cns);

		add(actions);
		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(actions, cns);
	}
	protected final void clearHostPort() {
		_hostname = null;
		_portno = null;
	}
	private final String getHostPort() {
		return getHostPort(host.getText(), port.getText());
	}
	private static final String getHostPort(String hostname, String portno) {
		String hostport = null;

		if (hostname == null || hostname.equals("")) {
			hostport = "";
		} else if (portno == null || portno.equals("")) {
			hostport = hostname;
		} else {
			hostport = hostname + CHAR_PORT_LEADER + portno;
		} 
		return hostport;
	}
	public String getText() {
		Vector args = new Vector();
		final String hostport = getHostPort();
		final String acts = actions.getText();
		final boolean h = hostport != null &&!hostport.equals("");
		final boolean a = acts != null &&!acts.equals("");

		if (h || a) {
			args.addElement(hostport);
		} 
		if (a) {
			args.addElement(acts);
		} 
		return toText(args);
	}
	private final void parseHostPort(String hostport) {
		clearHostPort();
		final int portLeaderIndex = hostport.lastIndexOf(CHAR_PORT_LEADER);

		if (portLeaderIndex < 0) {
			_hostname = hostport;
			_portno = null;
		} else {
			_hostname = hostport.substring(0, portLeaderIndex);
			_portno = hostport.substring(portLeaderIndex + 1);
		} 
	}
	public void setText(String text) {
		parseText(text);
		final String hostport = getArg(0);
		final String acts = getArg(1);

		if (hostport != null) {
			parseHostPort(hostport);
			host.setText(_hostname);
			port.setText(_portno);
		} else {
			host.setText("");
			port.setText("");
		} 
		if (acts != null) {
			actions.setText(acts);
		} else {
			actions.setText("");
		} 
	}
}
