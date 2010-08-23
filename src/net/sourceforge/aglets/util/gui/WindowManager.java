package net.sourceforge.aglets.util.gui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JFrame;

import com.ibm.aglets.tahiti.MainWindow;
import com.ibm.aglets.tahiti.TahitiWindow;

/**
 * This class is used to manager window events (e.g., closing events) for a set
 * of windows (at least one). It could manage more than one window at time, thus
 * to avoid a lot of object creation.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         10/set/07
 */
public class WindowManager extends WindowAdapter {

    /**
     * The owners list.
     */
    protected LinkedList<JFrame> owners = null;

    /**
     * Initializes this window closer for the specified frame.
     * 
     * @param owner
     *            the owner of this window closer
     */
    public WindowManager(JFrame owner) {
	super();
	this.owners = new LinkedList<JFrame>();
	this.add(owner);
    }

    /**
     * Adds a new JWindow object to manage thru this manager.
     * 
     * @param toAdd
     *            the JWindow object to add
     * @return true if it has been added, false otherwise
     */
    public boolean add(JFrame toAdd) {
	if ((toAdd != null) && (!(this.owners.contains(toAdd)))) {
	    this.owners.add(toAdd);
	    return true;
	} else
	    return false;
    }

    /**
     * Close all the windows managed from this manager.
     */
    @Override
    public void windowClosing(WindowEvent event) {
	if ((event == null) || (this.owners == null)
		|| (this.owners.size() == 0))
	    return;
	else {
	    // dispose each window managed
	    Iterator iter = this.owners.iterator();
	    while ((iter != null) && iter.hasNext()) {
		JFrame window = (JFrame) iter.next();

		// check if this window is a tahiti window and should
		// exit on closing
		if ((window instanceof TahitiWindow)
			&& ((TahitiWindow) window).shouldExitOnClosing()) {

		    TahitiWindow tWindow = (TahitiWindow) window;
		    tWindow.getTranslator();
		    tWindow.getBaseKey();

		    // don't ask the user to confirm the exiting, since it will
		    // be done by the tahiti window itself

		    // send an exit event
		    ActionEvent exit = new ActionEvent(this, 1, GUICommandStrings.EXIT_COMMAND);
		    ((MainWindow) tWindow).actionPerformed(exit);

		}

		window.setVisible(false);
		window.dispose();
	    }
	}

    }

}
