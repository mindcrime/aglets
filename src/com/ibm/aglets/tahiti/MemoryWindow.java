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
    private MemoryPanel mem;

    public MemoryWindow() {
	super();
	this.mem = new MemoryPanel(250, 250, false, true);
	this.getContentPane().add(this.mem);
	this.pack();
	this.setVisible(true);

    }

}
