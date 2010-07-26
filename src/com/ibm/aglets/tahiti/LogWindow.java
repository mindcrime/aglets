package com.ibm.aglets.tahiti;

/*
 * @(#)LogWindow.java
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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * <tt> LogWindow </tt>
 * 
 * @version 1.10 97/03/21
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

final class LogWindow extends Frame implements ActionListener {

    /*
     * The text area in which log messages are shown.
     */
    private TextArea _logList = new TextArea();

    /*
     * Layouts all components
     */
    GridBagLayout grid = new GridBagLayout();

    private int lastPos = 0; // for the performance reason.

    // @see java.awt.TextArea#appendText
    static final int MAX_LEN = 400 * 40;

    /*
     * Constructs a log window.
     */
    LogWindow() {
	super("Log Information");
	Util.setBackground(this);
	Util.setFixedFont(this._logList);

	// setBackground( DefaultResource.getBackground() );
	// _logList.setFont( DefaultResource.getFixedFont() );
	this._logList.setBackground(Color.white);
	this._logList.setEditable(false);

	GridBagConstraints cns = new GridBagConstraints();

	this.setLayout(this.grid);

	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.insets = new Insets(5, 5, 5, 5);

	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;
	this.addCmp(this._logList, cns);

	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.HORIZONTAL;
	this.addCmp(this.makeButtonPanel(), cns);

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent ev) {
		LogWindow.this.setVisible(false);
	    }
	});
    }

    public void actionPerformed(ActionEvent ev) {
	String cmd = ev.getActionCommand();

	if ("close".equals(cmd)) {
	    this.setVisible(false);
	} else if ("clear".equals(cmd)) {
	    this.clearLog();
	}
    }

    private void addCmp(Component cmp, GridBagConstraints cns) {
	this.grid.setConstraints(cmp, cns);
	this.add(cmp);
    }

    public void appendText(String line) {
	line = line + '\n';
	this._logList.insert(line, this.lastPos);
	this.lastPos += line.length();
	if (this.lastPos > MAX_LEN) {
	    int m = this.lastPos / 2;

	    String str = this._logList.getText();

	    for (int i = m; i < this.lastPos; i++) {
		if (str.charAt(m) == '\n') {
		    break;
		}
	    }
	    this._logList.replaceRange("", 0, m);
	    this.lastPos = this._logList.getText().length();
	}
    }

    /*
     * Clears logs shown in the console.
     */
    public void clearLog() {
	synchronized (this._logList) {
	    this.lastPos = 0;
	    this._logList.setText("");
	}
    }

    private Panel makeButtonPanel() {
	Panel buttonPanel = new Panel();

	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	Button c = new Button("Close");

	c.setActionCommand("close");
	c.addActionListener(this);
	buttonPanel.add(c);

	c = new Button("Clear Log");
	c.setActionCommand("clear");
	c.addActionListener(this);
	buttonPanel.add(c);
	return buttonPanel;
    }
}
