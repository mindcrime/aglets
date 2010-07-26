/*
 * Created on Oct 18, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.ibm.aglets.tahiti.MainWindow;
import com.ibm.aglets.tahiti.TahitiDialog;
import com.ibm.aglets.tahiti.TahitiWindow;

/**
 * A generic class to close the window of the Tahiti viewer.
 * 
 * @since 2.1.0
 */
public class WindowCloser extends WindowAdapter {

    /**
     * A reference to the TahitiWindow to close
     * 
     */
    protected TahitiWindow tWindow = null;

    /**
     * A reference to the TahitiDialog to close
     * 
     */
    protected TahitiDialog dWindow = null;

    /**
     * A reference to a JFrame to close
     * */
    protected JFrame fWindow = null;

    /**
     * Constructs the closer for a window.
     * 
     * @param window
     *            the TahitiWindow to close
     */
    public WindowCloser(TahitiWindow window) {
	super();
	this.tWindow = window;
    }

    /**
     * Constructs the closer for a dialog.
     * 
     * @param dialog
     *            the dialog window to close
     */
    public WindowCloser(TahitiDialog dialog) {
	super();
	this.dWindow = dialog;
    }

    /**
     * Constructs a closer for a JFrame
     * 
     * @param frame
     *            the frame to close
     */
    public WindowCloser(JFrame frame) {
	super();
	this.fWindow = frame;
    }

    public WindowCloser(MainWindow mainWindow) {
	this((JFrame) mainWindow);
    }

    /**
     * Manage the window closing.
     * 
     * @param event
     *            the event sent to close the window
     */
    @Override
    public void windowClosing(WindowEvent event) {
	if (this.tWindow != null) {
	    this.tWindow.setVisible(false);
	    this.tWindow.dispose();
	}

	if (this.dWindow != null) {
	    this.dWindow.setVisible(false);
	    this.dWindow.dispose();
	}

	if ((this.fWindow != null) && !(this.fWindow instanceof MainWindow)) {
	    this.fWindow.setVisible(false);
	    this.fWindow.dispose();
	} else if ((this.fWindow != null)
		&& (this.fWindow instanceof MainWindow)) {
	    // send an exit event
	    ActionEvent exit = new ActionEvent(this, 1, TahitiCommandStrings.EXIT_COMMAND);
	    ((MainWindow) this.fWindow).actionPerformed(exit);
	}
    }

}
