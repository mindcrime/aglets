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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Console extends Frame implements ActionListener {
	private Button _clear_button = new Button("Clear");
	private Button _close_button = new Button("Close");
	private TextArea _log_text_area = new TextArea(15, 82);
	private int _max_chars;
	private int _cur_chars;

	public Console() {
		super("Aglets Daemon Console");

		redirect();

		add("Center", _log_text_area);
		Panel p = new Panel();

		p.setLayout(new BorderLayout());
		p.add("West", _clear_button);
		p.add("East", _close_button);
		add("South", p);
		pack();

		_clear_button.addActionListener(this);
		_close_button.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				setVisible(false);
			} 
		});
	}
	public void actionPerformed(ActionEvent ev) {
		if (_close_button.getActionCommand().equals(ev.getActionCommand())) {
			setVisible(false);
		} else if (_clear_button.getActionCommand()
		.equals(ev.getActionCommand())) {

			// String str = _log_text_area.getText();
			_log_text_area.setText("");
		} 
	}
	public void redirect() {
		LogWriter lw = new LogWriter(_log_text_area);
		java.io.PrintStream ps = new java.io.PrintStream(lw);

		System.setOut(ps);
		System.setErr(ps);
	}
}
