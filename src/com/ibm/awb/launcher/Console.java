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
	/**
	 * 
	 */
	private static final long serialVersionUID = -3813053602916153118L;
	private final Button _clear_button = new Button("Clear");
	private final Button _close_button = new Button("Close");
	private final TextArea _log_text_area = new TextArea(15, 82);

	public Console() {
		super("Aglets Daemon Console");

		redirect();

		this.add("Center", _log_text_area);
		final Panel p = new Panel();

		p.setLayout(new BorderLayout());
		p.add("West", _clear_button);
		p.add("East", _close_button);
		this.add("South", p);
		pack();

		_clear_button.addActionListener(this);
		_close_button.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent ev) {
				Console.this.setVisible(false);
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		if (_close_button.getActionCommand().equals(ev.getActionCommand())) {
			setVisible(false);
		} else if (_clear_button.getActionCommand().equals(ev.getActionCommand())) {

			// String str = _log_text_area.getText();
			_log_text_area.setText("");
		}
	}

	public void redirect() {
		final LogWriter lw = new LogWriter(_log_text_area);
		final java.io.PrintStream ps = new java.io.PrintStream(lw);

		System.setOut(ps);
		System.setErr(ps);
	}
}
