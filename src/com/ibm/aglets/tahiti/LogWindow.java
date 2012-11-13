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

	/**
	 * 
	 */
	private static final long serialVersionUID = 4394807357766075062L;

	/*
	 * The text area in which log messages are shown.
	 */
	private final TextArea _logList = new TextArea();

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
		Util.setFixedFont(_logList);

		// setBackground( DefaultResource.getBackground() );
		// _logList.setFont( DefaultResource.getFixedFont() );
		_logList.setBackground(Color.white);
		_logList.setEditable(false);

		final GridBagConstraints cns = new GridBagConstraints();

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
			@Override
			public void windowClosing(final WindowEvent ev) {
				LogWindow.this.setVisible(false);
			}
		});
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final String cmd = ev.getActionCommand();

		if ("close".equals(cmd)) {
			setVisible(false);
		} else if ("clear".equals(cmd)) {
			clearLog();
		}
	}

	private void addCmp(final Component cmp, final GridBagConstraints cns) {
		grid.setConstraints(cmp, cns);
		this.add(cmp);
	}

	public void appendText(String line) {
		line = line + '\n';
		_logList.insert(line, lastPos);
		lastPos += line.length();
		if (lastPos > MAX_LEN) {
			final int m = lastPos / 2;

			final String str = _logList.getText();

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

	private Panel makeButtonPanel() {
		final Panel buttonPanel = new Panel();

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
