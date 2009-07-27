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
import java.util.ResourceBundle;

import javax.swing.*;
import com.ibm.aglets.tahiti.utils.*;
import java.awt.*;

/**
 * <tt> LogWindow </tt>
 * 
 * @version     1.10    97/03/21
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

final class LogWindow extends TahitiWindow implements ActionListener {

    
    
    
	/*
	 * The text area in which log messages are shown.
	 */
	private JTextArea _logList = new JTextArea();

	

	private int lastPos = 0;	// for the performance reason.

	// @see java.awt.TextArea#appendText
	static final int MAX_LEN = 400 * 40;

	/*
	 * Constructs a log window.
	 */
	LogWindow() {
	    super(bundle.getString("window.log.title"));
		_logList.setBackground(Color.yellow);
		this._logList.setForeground(Color.BLACK);
		_logList.setEditable(false);

		// add the log panel
		this.getContentPane().add("Center",new JScrollPane(this._logList));
		
		
		
		// button panel
		this.addJButton(bundle.getString("window.log.button.clear"),TahitiCommandStrings.CLEAR_CACHE_COMMAND,IconRepository.getIcon("clear_cache"),this,bundle.getString("window.log.tooltip.clear"));
		this.addJButton(bundle.getString("window.log.button.close"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this,bundle.getString("window.log.tooltip.close"));
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
			    
				setVisible(false);
				dispose();
			} 
		});
		
		// set the window visible
		this.pack();
	}
	
	
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();

		if(command.equals(TahitiCommandStrings.CLEAR_CACHE_COMMAND)){
		    // clear the log panel
		    this.clearLog();
		}
		else
		if(command.equals(TahitiCommandStrings.CANCEL_COMMAND)){
		    this.setVisible(false);
		}
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
	public synchronized void clearLog() {
			lastPos = 0;
			_logList.setText("");
		 
	}
	
	

}
