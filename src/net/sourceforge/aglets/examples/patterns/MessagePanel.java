package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)MessagePanel.java
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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;

/**
 * The <tt>MessagePanel</tt> class is a common and generic dialog to display the
 * messages
 * 
 * @version 1.00 96/07/03
 * @author M.Oshima
 */

public class MessagePanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1845486283000204517L;
	private final GridBagConstraints cns = new GridBagConstraints();
	private final GridBagLayout grid = new GridBagLayout();

	private final boolean raised;
	private int alignment;

	/**
	 * Line separator constants
	 */
	static final public String lineSeparator = "\n";
	static final public char lineSeparatorChar = lineSeparator.charAt(0);

	/*
	 * Constructs a MessagePanel with the message and boolean which specifies
	 * the shape of the frame.
	 * 
	 * @param message
	 * 
	 * @param raised
	 */
	public MessagePanel(final String message, final boolean raised) {
		this(message, Label.LEFT, raised);
	}

	/*
	 * Constructs a message panel with the message.
	 * 
	 * @param message
	 * 
	 * @param alignment
	 * 
	 * @param raised
	 */
	public MessagePanel(final String message, final int alignment, final boolean raised) {
		this.alignment = alignment;
		this.raised = raised;

		setMessage(message);
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, this.getSize().width, this.getSize().height);
		g.draw3DRect(1, 1, this.getSize().width - 2, this.getSize().height - 2, raised);
	}

	/**
	 * Sets alignment
	 * 
	 * @param alignment
	 */
	public void setAlignment(final int alignment) {
		final int t = getComponentCount();

		this.alignment = alignment;
		for (int i = 0; i < t; i++) {
			final Component c = getComponent(i);

			if (c instanceof Label) {
				((Label) c).setAlignment(alignment);
			}
		}
	}

	/**
	 * Sets message string to be shown.
	 * 
	 * @param message
	 *            the message
	 */
	public void setMessage(final String message) {
		removeAll();

		final String messages[] = split(message);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.insets = new Insets(3, 3, 3, 3);
		setLayout(grid);

		for (final String message2 : messages) {
			final Label l = new Label(message2, alignment);

			grid.setConstraints(l, cns);
			this.add(l);
		}
	}

	private String[] split(String str) {
		final String msg[] = new String[50];
		int pos, i;
		final int size = lineSeparator.length();

		for (i = 0; ((pos = str.indexOf(lineSeparator)) >= 0) && (i < 49); i++) {
			msg[i] = str.substring(0, pos);
			str = str.substring(pos + size);
		}
		msg[i++] = str;

		final String ret[] = new String[i];

		System.arraycopy(msg, 0, ret, 0, i);
		return ret;
	}
}
