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
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <tt> LogWindow </tt>
 * 
 * @version     1.10    97/03/21
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
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

	private int lastPos = 0;	// for the performance reason.

	// @see java.awt.TextArea#appendText
	static final int MAX_LEN = 400 * 40;

	/*
	 * Constructs a log window.
	 */
	LogWindow() {
		super("Log Information");
		Util.setBackground(this);
		Util.setFixedFont(_logList);

		// setBackground( DefaultResource.getBackground() );
		// _logList.setFont( DefaultResource.getFixedFont() );
		_logList.setBackground(Color.white);
		_logList.setEditable(false);

		GridBagConstraints cns = new GridBagConstraints();

		setLayout(grid);

		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.insets = new Insets(5, 5, 5, 5);

		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		addCmp(_logList, cns);

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		addCmp(makeButtonPanel(), cns);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				setVisible(false);
			} 
		});
	}
	public void actionPerformed(ActionEvent ev) {
		String cmd = ev.getActionCommand();

		if ("close".equals(cmd)) {
			setVisible(false);
		} else if ("clear".equals(cmd)) {
			clearLog();
		} 
	}
	private void addCmp(Component cmp, GridBagConstraints cns) {
		grid.setConstraints(cmp, cns);
		add(cmp);
	}
	public void appendText(String line) {
		line = line + '\n';
		_logList.insert(line, lastPos);
		lastPos += line.length();
		if (lastPos > MAX_LEN) {
			int m = lastPos / 2;

			String str = _logList.getText();

			for (int i = m; i < lastPos; i++) {
				if (str.charAt(m) == '\n') {
					break;
				} 
			} 
			_logList.replaceRange("", 0, m);
			lastPos = _logList.getText().length();
		} 
	}
	/*
	 * Clears logs shown in the console.
	 */
	public void clearLog() {
		synchronized (_logList) {
			lastPos = 0;
			_logList.setText("");
		} 
	}
	/*
	 * Closes log window
	 */
	private void closeLog() {
		setVisible(false);
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
