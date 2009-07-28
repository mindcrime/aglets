package examples.patterns;

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

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Label;

/**
 * The <tt>MessagePanel</tt> class is a common and generic dialog to display
 * the messages
 * 
 * @version     1.00    96/07/03
 * @author      M.Oshima
 */

public class MessagePanel extends Panel {

	private GridBagConstraints cns = new GridBagConstraints();
	private GridBagLayout grid = new GridBagLayout();

	private boolean raised;
	private int alignment;

	/**
	 * Line separator constants
	 */
	static final public String lineSeparator = "\n";
	static final public char lineSeparatorChar = lineSeparator.charAt(0);

	/*
	 * Constructs a message panel with the message.
	 * @param message
	 * @param alignment
	 * @param raised
	 */
	public MessagePanel(String message, int alignment, boolean raised) {
		this.alignment = alignment;
		this.raised = raised;

		setMessage(message);
	}
	/*
	 * Constructs a MessagePanel with the message and boolean which
	 * specifies the shape of the frame.
	 * @param message
	 * @param raised
	 */
	public MessagePanel(String message, boolean raised) {
		this(message, Label.LEFT, raised);
	}
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.draw3DRect(1, 1, getSize().width - 2, getSize().height - 2, raised);
	}
	/**
	 * Sets aligment
	 * 
	 * @param alignemtn
	 */
	public void setAlignment(int alignment) {
		int t = getComponentCount();

		this.alignment = alignment;
		for (int i = 0; i < t; i++) {
			Component c = getComponent(i);

			if (c instanceof Label) {
				((Label)c).setAlignment(alignment);
			} 
		} 
	}
	/**
	 * Sets message string to be shown.
	 * 
	 * @param message the message
	 */
	public void setMessage(String message) {
		removeAll();

		String messages[] = split(message);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.insets = new Insets(3, 3, 3, 3);
		setLayout(grid);

		for (int i = 0; i < messages.length; i++) {
			Label l = new Label(messages[i], alignment);

			grid.setConstraints(l, cns);
			add(l);
		} 
	}
	private String[] split(String str) {
		String msg[] = new String[50];
		int pos, i, size = lineSeparator.length();

		for (i = 0; (pos = str.indexOf(lineSeparator)) >= 0 && i < 49; i++) {
			msg[i] = str.substring(0, pos);
			str = str.substring(pos + size);
		} 
		msg[i++] = str;

		String ret[] = new String[i];

		System.arraycopy(msg, 0, ret, 0, i);
		return ret;
	}
}
