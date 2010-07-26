package com.ibm.aglets.tahiti;

/*
 * @(#)TahitiWindow.java
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.aglets.log.AgletsLogger;
import org.aglets.util.AgletsTranslator;
import org.aglets.util.gui.GUICommandStrings;
import org.aglets.util.gui.JComponentBuilder;
import org.aglets.util.gui.WindowManager;

/**
 * A base window for all the windows of the Tahiti GUI. Please note that this
 * class should be used as base for other windows and that the behaviour of such
 * windows must be developed accordingly to the one expressed here. Thus, for
 * instance, you have to manage all events and to pass to this class events you
 * don't manage.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         10/set/07
 */
public class TahitiWindow extends JFrame implements ActionListener {

    static final public String lineSeparator = "\n";

    /**
     * The logger of this (and all the other) windows.
     */
    protected AgletsLogger logger = AgletsLogger.getLogger(this.getClass().getName());

    /**
     * A generic panel, for buttons defined by the user.
     */
    protected JPanel buttonPanel = null;

    /**
     * The button panel for the ok/cancel buttons, always present.
     */
    private OkCancelButtonPanel okButtonPanel = null;

    /**
     * The window manager for this window (and its descendant).
     */
    protected WindowManager windowManager = null;

    /**
     * The translator of this window.
     */
    protected AgletsTranslator translator = null;

    /**
     * The base for the key translation.
     */
    protected String baseKey = this.getClass().getName();

    /**
     * This flag is true if this window can exit the whole application when it
     * is closed. Set it to true when you are working with windows that should
     * not allow users to continue with the application (e.g., login windows).
     */
    protected boolean shouldExitOnClosing = false;

    /**
     * Default constructor, does simple layout. The layout of the window is the
     * following: a twin panel is added to the south, with an ok/cancel button
     * panel and a generic button panel that the user can use to add other
     * buttons. Then there is a border layout over the whole window that can be
     * used to display other components.
     */
    protected TahitiWindow(boolean inizializeGUI) {
	super();
	this.translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

	this.setTitle(this.getClass().getName()); // translate the title to the
						  // appropriate one
	this.windowManager = new WindowManager(this); // add the standard
						      // management event system
	this.addWindowListener(this.windowManager);

	if (inizializeGUI) {
	    // set the layout to the borderlayout and add the content to the
	    // center, the
	    // button panels to the south
	    this.buttonPanel = new JPanel();
	    this.buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	    String className = this.getClass().getName();
	    String okKey = className + ".okButton";
	    String cancelKey = className + ".cancelButton";
	    this.okButtonPanel = JComponentBuilder.createOkCancelButtonPanel(okKey, cancelKey, this);

	    this.setLayout(new BorderLayout());
	    JPanel southPanel = new JPanel();
	    southPanel.setLayout(new BorderLayout());
	    southPanel.add(this.buttonPanel, BorderLayout.CENTER);
	    southPanel.add(this.okButtonPanel, BorderLayout.SOUTH);

	    this.add(southPanel, BorderLayout.SOUTH);

	    // set the default button operation
	    this.getRootPane().setDefaultButton(this.okButtonPanel.getOkButton());

	}

	// pack the components
	this.pack();
    }

    protected TahitiWindow() {
	this(true);
    }

    /**
     * Adds a button to the button panel of this window.
     * 
     * @param key
     * @return
     */
    protected JButton addButton(String key) {
	JButton button = this.addButton(key, this);
	return button;
    }

    /**
     * Adds a button to the panel button of the window using the key and the
     * specified listener.
     * 
     * @param key
     *            the key of the button
     * @param listener
     *            the listener to use
     * @return the button for further evaluation
     */
    protected JButton addButton(String key, ActionListener listener) {
	// check params
	if (key == null)
	    return null;

	// create the button
	// by default the button will have the key as the action command
	// and will be associated to the specified listener
	JButton button = JComponentBuilder.createJButton(key, null, listener);

	// add the button to the panel
	if (button != null)
	    this.buttonPanel.add(button);

	// return the button for further manipulation
	return button;

    }

    /**
     * Adds the button to the button panel.
     * 
     * @param key
     *            the key for the button label and icon
     * @param listener
     *            the listener associated to this button
     * @param keyListener
     *            the keylistener of this button
     * @return the button for further evaluation
     */
    protected JButton addButton(String key, ActionListener listener,
	    KeyListener keyListener) {
	JButton button = this.addButton(key, listener);

	// add the key listener
	if (button != null)
	    button.addKeyListener(keyListener);

	// all done
	return button;
    }

    /**
     * Adds an already built button to the button panel.
     * 
     * @param button
     *            the button to add.
     * @return the button added
     */
    protected JButton addButton(JButton button) {
	if (button != null)
	    this.buttonPanel.add(button);

	return button;
    }

    /**
     * Gets back the shouldExitOnClosing.
     * 
     * @return the shouldExitOnClosing
     */
    public synchronized final boolean shouldExitOnClosing() {
	return this.shouldExitOnClosing;
    }

    /**
     * Centers this window in the screen.
     */
    public void popupAtCenterOfScreen() {
	// get the dimension of the screen
	Dimension d = this.getToolkit().getScreenSize();
	// get the size of this window
	Dimension s = this.getSize();

	int x = (int) (d.getWidth() - s.getWidth()) / 2;
	int y = (int) (d.getHeight() - s.getHeight()) / 2;

	// center in the screen
	this.setLocation(x, y);
    }

    /**
     * Manages events related to buttons, menues, etc.
     */
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	String command = event.getActionCommand();

	// if the button is the cancel, close the window
	if (GUICommandStrings.CANCEL_COMMAND.equals(command)) {
	    this.setVisible(false);
	    this.dispose();
	}

    }

    /**
     * Gets back the okButtonPanel.
     * 
     * @return the okButtonPanel
     */
    protected final JPanel getOkButtonPanel() {
	return this.okButtonPanel;
    }

    /**
     * Gets back the baseKey.
     * 
     * @return the baseKey
     */
    public final String getBaseKey() {
	return this.baseKey;
    }

    /**
     * Gets back the translator.
     * 
     * @return the translator
     */
    public final AgletsTranslator getTranslator() {
	return this.translator;
    }

    /*
     * For testing only!
     */
    /*
     * public static void main(String argv[]){ TahitiWindow window = new
     * TahitiWindow(); window.addButton(new JButton("ciao"));
     * window.setVisible(true); window.pack();
     * 
     * }
     */
}
