package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)MessageDialog.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;

/**
 * The <tt>MessageDialog</tt> class is a common and generic dialog to display
 * the messages
 * 
 * @version 1.00 96/07/03
 * @author M.Oshima
 */

public class MessageDialog extends GeneralDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2419734851714972830L;
	/*
	 * Message panel used in this dialog
	 */
	private MessagePanel message_panel = null;

	/*
	 * Constructs the remove Aglet window.
	 * 
	 * @param parent
	 * 
	 * @param title
	 * 
	 * @param message
	 * 
	 * @param alignment
	 * 
	 * @param object
	 * 
	 * @param modal
	 */
	public MessageDialog(final Frame parent, final Component callback_component,
	                     final String title, final String message, final int alignment, final Object object,
	                     final boolean modal) {

		super(parent, callback_component, title, object, modal);

		message_panel = new MessagePanel(message, alignment, false);
		layoutComponents();
	}

	/*
	 * Constructs a message dialog window
	 * 
	 * @param parent
	 * 
	 * @param title
	 * 
	 * @param message
	 */
	public MessageDialog(final Frame parent, final String title, final String message) {
		this(parent, parent, title, message, Label.CENTER, null, true);
	}

	/*
	 * Constructs a message dialog window
	 * 
	 * @param parent
	 * 
	 * @param title
	 * 
	 * @param message
	 * 
	 * @param object
	 */
	public MessageDialog(final Frame parent, final String title, final String message,
	                     final Object object) {
		this(parent, parent, title, message, Label.CENTER, object, true);
	}

	/*
	 * Builds message dialog specific panel. Called when the doLayout method in
	 * the superclass is invoked.
	 */
	@Override
	protected void makePanel(final GridBagLayout grid) {
		final GridBagConstraints cns = new GridBagConstraints();

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		message_panel.doLayout();
		message_panel.setSize(message_panel.getPreferredSize());
		addCmp(message_panel, grid, cns);
	}

	/**
	 * Sets the message
	 */
	public void setMessage(final String msg) {
		message_panel.setMessage(msg);
		message_panel.doLayout();
		doLayout();
		this.setSize(getPreferredSize());
	}
}
