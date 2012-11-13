package com.ibm.aglets.tahiti;

/*
 * @(#)TahitiDialog.java
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import net.sourceforge.aglets.util.AgletsTranslator;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;

public class TahitiDialog extends BaseAgletsDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3185078843794397732L;

	static final public String lineSeparator = "\n";

	/**
	 * Prepars an alert dialog window.
	 * 
	 * @param frame
	 *            the owner of the dialog
	 * @param message
	 *            the message to show (it must be already translated)
	 * @return the dialog window
	 */
	public static TahitiDialog alert(final JFrame frame, final String message) {
		// check params
		if ((frame == null) || (message == null))
			return null;

		// build a new TahitiDialog
		final TahitiDialog dialog = new TahitiDialog(frame);

		dialog.setTitle(JComponentBuilder.getTitle(dialog.baseKey + ".alert"));

		// add the message to a textarea
		final JTextArea text = new JTextArea();
		text.setText(message);
		text.setEditable(false);

		// now add the text area to the content pane of the
		// tahiti dialog
		dialog.addContent(text);
		dialog.pack();

		// all done
		return dialog;
	}

	protected static final TahitiDialog info(
	                                         final JFrame parentFrame,
	                                         final AgletProxy proxy) {
		// check params
		if (proxy == null)
			return null;

		// build a new tahiti dialog
		TahitiDialog dialog = null;
		try {
			dialog = new TahitiDialog(parentFrame);
			dialog.setTitle(JComponentBuilder.getTitle(dialog.baseKey + ".info"));
			dialog.addContent(dialog.buildAgletInfoTable(proxy.getAgletInfo()));
			dialog.pack();
			return dialog;
		} catch (final Exception e) {
			if (dialog != null)
				dialog.logger.error("Exception caught while trying to build an aglet info dialog", e);
			return null;
		}

	}

	/**
	 * Builds a tahiti dialog with a message.
	 * 
	 * @param frame
	 *            the frame owner of the dialog
	 * @param title
	 *            the title (could be null) already translated
	 * @param msg
	 *            the message to show
	 * @return the dialog
	 */
	public static TahitiDialog message(final JFrame frame, final String title, final String msg) {
		// check params
		if ((frame == null) || (msg == null))
			return null;

		// build a new TahitiDialog
		final TahitiDialog dialog = new TahitiDialog(frame);

		if (title != null)
			dialog.setTitle(title);
		else
			dialog.setTitle(JComponentBuilder.getTitle(dialog.baseKey
					+ ".message"));

		// add the message to a textarea
		final JTextArea text = new JTextArea();
		text.setText(msg);
		text.setEditable(false);

		// now add the text area to the content pane of the
		// tahiti dialog
		dialog.addContent(text);
		dialog.pack();

		// all done
		return dialog;

	}

	public static String[] split(String str) {
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

	/**
	 * The main window of this dialog.
	 */
	protected MainWindow mainWindow = null;

	/**
	 * A panel for containing the buttons.
	 */
	private JPanel buttonPanel = null;

	/**
	 * The panel for the ok/close buttons.
	 */
	private OkCancelButtonPanel okCancelButtonPanel = null;

	/**
	 * A panel for the content.
	 */
	protected JPanel contentPanel = null;

	/**
	 * A message area for a text message to display in the content panel.
	 */
	protected JTextArea messageArea = null;

	/**
	 * Creates the dialog window displaying the main components. Please note
	 * that the dialog window will have a main component, called content panel,
	 * at the center and a button panel at the south where user can add buttons.
	 * Moreover there will be standard buttons at the south for managing the
	 * ok/cancel operation.
	 * 
	 * @param parentFrame
	 *            the frame owner of this dialog.
	 */
	protected TahitiDialog(final JFrame parentFrame) {
		super(parentFrame);

		// store the main window if it is a main window
		if (parentFrame instanceof MainWindow)
			mainWindow = (MainWindow) parentFrame;

		// create the translator
		translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

		// set the layout for this dialog
		setLayout(new BorderLayout());

		// set the title
		setTitle(JComponentBuilder.getTitle(baseKey));

		// create the ok/cancel button panel
		okCancelButtonPanel = JComponentBuilder.createOkCancelButtonPanel(baseKey
				+ ".okButton", baseKey + ".cancelButton", this);

		// create the button panel
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// add the button panels
		final JPanel bPanel = new JPanel();
		bPanel.setLayout(new BorderLayout());
		bPanel.add(buttonPanel, BorderLayout.NORTH);
		bPanel.add(okCancelButtonPanel, BorderLayout.SOUTH);
		this.add(bPanel, BorderLayout.SOUTH);

		// create the content panel
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		this.add(contentPanel, BorderLayout.CENTER);

		// set the default button
		getRootPane().setDefaultButton(okCancelButtonPanel.getOkButton());

		pack();

	}

	/**
	 * Builds the dialog window with the specified title and modal way.
	 * 
	 * @param parentFrame
	 *            the fram owner of this dialog
	 * @param title
	 *            the title of this window
	 * @param modal
	 *            the modal or not
	 */
	@Deprecated
	protected TahitiDialog(final JFrame parentFrame, final String title, final boolean modal) {
		this(parentFrame);
		setTitle(title);
		setModal(modal);
	}

	/**
	 * Adds the specified button to the button panel
	 * 
	 * @param key
	 *            the key for the button
	 * @return the button, for further evaluation
	 */
	public JButton addButton(final String key) {
		return this.addButton(key, null, null);
	}

	/**
	 * Adds the button to the button panel.
	 * 
	 * @param key
	 *            the key of the button
	 * @param listener
	 *            the listener for this button
	 * @return the button for further evaluation
	 */
	public JButton addButton(final String key, final ActionListener listener) {
		return this.addButton(key, listener, null);
	}

	/**
	 * Adds a button to the button panel.
	 * 
	 * @param key
	 *            the key for looking up the button locales
	 * @param listener
	 *            the listener to associate to the button
	 * @param keyListener
	 *            the key listener for the button
	 * @return the button for further evaluation
	 */
	public JButton addButton(
	                         final String key,
	                         final ActionListener listener,
	                         final KeyListener keyListener) {
		// check params
		if (key == null)
			return null;

		// get the button
		final JButton button = JComponentBuilder.createJButton(key, key, listener);
		button.addKeyListener(keyListener);

		// add the button to the panel
		buttonPanel.add(button);

		// return the button
		return button;
	}

	/**
	 * Creates a generic "close" button.
	 * 
	 * @param key
	 * @return the created button
	 */
	public final JButton addCloseButton(String key) {
		if (key == null)
			key = JComponentBuilder.CANCEL_BUTTON_KEY;

		return this.addButton(key);
	}

	/**
	 * Adds the specified component to the content panel.
	 * 
	 * @param component
	 *            the component to show
	 */
	protected final void addContent(final JComponent component) {
		// check params
		if (component == null)
			return;
		else
			contentPanel.add(component);
	}

	/**
	 * Builds a table with the iformation about an agent.
	 * 
	 * @param info
	 *            the aglet info
	 * @return a scrollpane with the table to display
	 */
	private JScrollPane buildAgletInfoTable(final AgletInfo info) {
		// display all the information about the aglet
		final String address = info.getAddress();
		final String className = info.getAgletClassName();
		final String codeBase = info.getCodeBase().toExternalForm();
		final String creationTime = "" + info.getCreationTime();
		final String AgletID = info.getAgletID().toString();
		final String APIMajor = "" + info.getAPIMajorVersion();
		final String APIMinor = "" + info.getAPIMinorVersion();
		final String origin = info.getOrigin();
		final String certificate = info.getAuthorityCertificate().toString();

		final String infoes[][] = new String[9][2];

		infoes[0][0] = translator.translate("AgletID");
		infoes[0][1] = AgletID;
		infoes[1][0] = translator.translate("AgletCodeBase");
		infoes[1][1] = codeBase;
		infoes[2][0] = translator.translate("AgletClassName");
		infoes[2][1] = className;
		infoes[3][0] = translator.translate("AgletAddress");
		infoes[3][1] = address;
		infoes[4][0] = translator.translate("AgletOrigin");
		infoes[4][1] = origin;
		infoes[5][0] = translator.translate("AgletCertificate");
		infoes[5][1] = certificate;
		infoes[6][0] = translator.translate("AgletAPIMajor");
		infoes[6][1] = APIMajor;
		infoes[7][0] = translator.translate("AgletAPIMinor");
		infoes[7][1] = APIMinor;
		infoes[8][0] = translator.translate("AgletCreationTime");
		infoes[8][1] = creationTime;

		final String header[] = new String[2];
		header[0] = translator.translate("AgletPropertyName");
		header[1] = translator.translate("AgletPropertyValue");

		// build a jtable for the info
		final JTable table = new JTable(infoes, header);

		// add the table to the content pane
		table.setEnabled(false);
		final JScrollPane scrollTable = new JScrollPane(table);
		final TitledBorder border = new TitledBorder(translator.translate(baseKey
				+ ".agletInfo.title"));
		border.setTitleJustification(TitledBorder.RIGHT);
		border.setTitleColor(Color.BLUE);
		scrollTable.setBorder(border);
		return scrollTable;
	}

	/**
	 * Gets back the mainWindow.
	 * 
	 * @return the mainWindow
	 */
	public synchronized final MainWindow getMainWindow() {
		return mainWindow;
	}

	/**
	 * Makes the dialog window visible (if it is not already).
	 * 
	 */
	@Deprecated
	public void popup() {
		if (!isVisible()) {
			pack();
			setVisible(true);
		}
	}

	/**
	 * Makes the dialog window visible at the center of the parent window.
	 * 
	 * 
	 */
	@Deprecated
	public void popupAtCenterOfParent() {
		// check params
		if (isVisible() || (getParent() == null))
			return;

		final Dimension d = getToolkit().getScreenSize();
		final JFrame parent = (JFrame) getParent();

		final Point ploc = parent.getLocationOnScreen();
		final Dimension psize = parent.getSize();

		final Dimension size = this.getSize();

		int x = (psize.width - size.width) / 2 + ploc.x;
		int y = (psize.height - size.height) / 2 + ploc.y;

		if (x < 0) {
			x = 0;
		}
		if (x > d.width - size.width) {
			x = d.width - size.width;

		}
		if (y < 0) {
			y = 0;
		}
		if (y > d.height - size.height) {
			y = d.height - size.height;

		}
		this.setLocation(x, y);

		setVisible(true);
	}

	/**
	 * Makes the dialog visible at the center of the screen.
	 * 
	 */
	@Deprecated
	public void popupAtCenterOfScreen() {
		if (isVisible())
			return;

		final Dimension d = getToolkit().getScreenSize();

		this.setLocation((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2);
		setVisible(true);
	}

	/**
	 * Sets the mainWindow value.
	 * 
	 * @param mainWindow
	 *            the mainWindow to set
	 */
	public synchronized final void setMainWindow(final MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	/**
	 * Builds a table with a list of the aglet info properties and their values.
	 * The infoes are shown at the bottom of the content pane.
	 * 
	 * @param info
	 *            the aglet info for the agent
	 */
	protected final void showAgletInfo(final AgletInfo info) {
		// check params
		if (info == null)
			return;

		final JScrollPane scrollTable = buildAgletInfoTable(info);
		contentPanel.add(scrollTable, BorderLayout.SOUTH);
	}

	/**
	 * Adds a panel showing each aglet in the dialog window.
	 * 
	 * @param proxies
	 *            the list of proxies to show
	 */
	protected final void showAgletProxies(final LinkedList<AgletProxy> proxies) {
		// check params
		if ((proxies == null) || (proxies.size() == 0))
			return;

		// add an aglet list with the proxies
		final AgletListPanel<AgletProxy> agletPanel = new AgletListPanel<AgletProxy>(proxies.size());
		agletPanel.setRenderer(new AgletListRenderer(agletPanel));

		// add each proxy
		final Iterator iter = proxies.iterator();
		while ((iter != null) && iter.hasNext()) {
			final AgletProxy currentProxy = (AgletProxy) iter.next();
			agletPanel.addItem(currentProxy);

		}

		// add the panel to the content panel
		addContent(agletPanel);
	}

	/**
	 * Adds the specified message to the content panel using a JLabel. This
	 * method is suitable for small messages. The message is shown at the top of
	 * the content pane.
	 * 
	 * @param message
	 *            the short message to display
	 */
	protected final void showMessage(final String message) {
		// check params
		if ((message == null) || (message.length() == 0))
			return;
		else
			contentPanel.add(new JLabel(message), BorderLayout.NORTH);
	}

	/**
	 * Shows the specified message into a text area within the content panel.
	 * Please note that only one message can be shown per time. The message is
	 * shown at the center of the content pane.
	 * 
	 * @param message
	 *            the message to show (must be not-null)
	 */
	protected final void showText(final String message) {
		// check params
		if ((message == null) || (message.length() == 0))
			return;
		else {
			// check if the message area has been already created
			if (messageArea == null) {
				messageArea = new JTextArea();
				contentPanel.add(messageArea, BorderLayout.CENTER);
				messageArea.setEnabled(false);
			}

			// add the text message
			messageArea.setText(message);

		}
	}
}
