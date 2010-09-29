/*
 * Created on Oct 3, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A simple status bar to be shown at the bottom of the Tahiti window.
 */
public class TahitiStatusBar extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -7347272840438355180L;

    /**
     * A label containing the current message.
     * 
     */
    private JLabel message;

    /**
     * A memory panel to show the memory usage
     */
    private MemoryPanel memPanel;

    /**
     * Build the status bar.
     * 
     * @param text
     *            the initial text of the status bar
     */
    public TahitiStatusBar(String text) {
	super();
	this.setBackground(Color.BLACK);
	this.message = new JLabel(text, SwingConstants.CENTER);
	this.message.setForeground(Color.YELLOW);
	this.message.setBackground(Color.BLACK);
	this.memPanel = new MemoryPanel(200, 200, true, false);
	this.setLayout(new BorderLayout());
	this.add("South", this.message);
	this.add("Center", this.memPanel);
    }

    /**
     * Changes the text of the status bar.
     * 
     * @param newText
     *            the new text to display
     * 
     */
    public void setText(String newText) {
	this.message.setText(newText);
    }

}
