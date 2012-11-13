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

import net.sourceforge.aglets.log.AgletsLogger;
import net.sourceforge.aglets.util.AgletsTranslator;
import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;
import net.sourceforge.aglets.util.gui.WindowManager;

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

	/**
	 * 
	 */
	private static final long serialVersionUID = -623077255997728363L;

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

	protected TahitiWindow() {
		this(true);
	}

	/**
	 * Default constructor, does simple layout. The layout of the window is the
	 * following: a twin panel is added to the south, with an ok/cancel button
	 * panel and a generic button panel that the user can use to add other
	 * buttons. Then there is a border layout over the whole window that can be
	 * used to display other components.
	 */
	protected TahitiWindow(final boolean inizializeGUI) {
		super();
		translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

		setTitle(this.getClass().getName()); // translate the title to the
		// appropriate one
		windowManager = new WindowManager(this); // add the standard
		// management event system
		addWindowListener(windowManager);

		if (inizializeGUI) {
			// set the layout to the borderlayout and add the content to the
			// center, the
			// button panels to the south
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			final String className = this.getClass().getName();
			final String okKey = className + ".okButton";
			final String cancelKey = className + ".cancelButton";
			okButtonPanel = JComponentBuilder.createOkCancelButtonPanel(okKey, cancelKey, this);

			setLayout(new BorderLayout());
			final JPanel southPanel = new JPanel();
			southPanel.setLayout(new BorderLayout());
			southPanel.add(buttonPanel, BorderLayout.CENTER);
			southPanel.add(okButtonPanel, BorderLayout.SOUTH);

			this.add(southPanel, BorderLayout.SOUTH);

			// set the default button operation
			getRootPane().setDefaultButton(okButtonPanel.getOkButton());

		}

		// pack the components
		pack();
	}

	/**
	 * Manages events related to buttons, menues, etc.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		// check params
		if (event == null)
			return;

		final String command = event.getActionCommand();

		// if the button is the cancel, close the window
		if (GUICommandStrings.CANCEL_COMMAND.equals(command)) {
			setVisible(false);
			dispose();
		}

	}

	/**
	 * Adds an already built button to the button panel.
	 * 
	 * @param button
	 *            the button to add.
	 * @return the button added
	 */
	protected JButton addButton(final JButton button) {
		if (button != null)
			buttonPanel.add(button);

		return button;
	}

	/**
	 * Adds a button to the button panel of this window.
	 * 
	 * @param key
	 * @return created button
	 */
	protected JButton addButton(final String key) {
		final JButton button = this.addButton(key, this);
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
	protected JButton addButton(final String key, final ActionListener listener) {
		// check params
		if (key == null)
			return null;

		// create the button
		// by default the button will have the key as the action command
		// and will be associated to the specified listener
		final JButton button = JComponentBuilder.createJButton(key, null, listener);

		// add the button to the panel
		if (button != null)
			buttonPanel.add(button);

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
	protected JButton addButton(
	                            final String key,
	                            final ActionListener listener,
	                            final KeyListener keyListener) {
		final JButton button = this.addButton(key, listener);

		// add the key listener
		if (button != null)
			button.addKeyListener(keyListener);

		// all done
		return button;
	}

	/**
	 * Gets back the baseKey.
	 * 
	 * @return the baseKey
	 */
	public final String getBaseKey() {
		return baseKey;
	}

	/**
	 * Gets back the okButtonPanel.
	 * 
	 * @return the okButtonPanel
	 */
	protected final JPanel getOkButtonPanel() {
		return okButtonPanel;
	}

	/**
	 * Gets back the translator.
	 * 
	 * @return the translator
	 */
	public final AgletsTranslator getTranslator() {
		return translator;
	}

	/**
	 * Centers this window in the screen.
	 */
	public void popupAtCenterOfScreen() {
		// get the dimension of the screen
		final Dimension d = getToolkit().getScreenSize();
		// get the size of this window
		final Dimension s = this.getSize();

		final int x = (int) (d.getWidth() - s.getWidth()) / 2;
		final int y = (int) (d.getHeight() - s.getHeight()) / 2;

		// center in the screen
		this.setLocation(x, y);
	}

	/**
	 * Gets back the shouldExitOnClosing.
	 * 
	 * @return the shouldExitOnClosing
	 */
	public synchronized final boolean shouldExitOnClosing() {
		return shouldExitOnClosing;
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
