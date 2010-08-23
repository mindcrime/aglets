package com.ibm.awb.launcher;

/*
 * @(#)Console.java
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
import java.awt.Button;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Console extends Frame implements ActionListener {
    private Button _clear_button = new Button("Clear");
    private Button _close_button = new Button("Close");
    private TextArea _log_text_area = new TextArea(15, 82);

    public Console() {
	super("Aglets Daemon Console");

	this.redirect();

	this.add("Center", this._log_text_area);
	Panel p = new Panel();

	p.setLayout(new BorderLayout());
	p.add("West", this._clear_button);
	p.add("East", this._close_button);
	this.add("South", p);
	this.pack();

	this._clear_button.addActionListener(this);
	this._close_button.addActionListener(this);

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent ev) {
		Console.this.setVisible(false);
	    }
	});
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	if (this._close_button.getActionCommand().equals(ev.getActionCommand())) {
	    this.setVisible(false);
	} else if (this._clear_button.getActionCommand().equals(ev.getActionCommand())) {

	    // String str = _log_text_area.getText();
	    this._log_text_area.setText("");
	}
    }

    public void redirect() {
	LogWriter lw = new LogWriter(this._log_text_area);
	java.io.PrintStream ps = new java.io.PrintStream(lw);

	System.setOut(ps);
	System.setErr(ps);
    }
}
