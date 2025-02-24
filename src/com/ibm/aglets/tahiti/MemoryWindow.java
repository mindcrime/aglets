/*
 * Created on Oct 1, 2004
 *
 * @author Luca Ferrari, fluca1978@virgilio.it
 */
package com.ibm.aglets.tahiti;

import javax.swing.JFrame;

/**
 * A window to show the memory amount.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class MemoryWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8080806717004726845L;
	private final MemoryPanel mem;

	public MemoryWindow() {
		super();
		mem = new MemoryPanel(250, 250, false, true);
		getContentPane().add(mem);
		pack();
		setVisible(true);

	}

}
