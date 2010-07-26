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

import org.aglets.util.AgletsTranslator;
import org.aglets.util.gui.JComponentBuilder;

import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;

public class TahitiDialog extends BaseAgletsDialog implements ActionListener {
    static final public String lineSeparator = "\n";

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
    protected TahitiDialog(JFrame parentFrame) {
	super(parentFrame);

	// store the main window if it is a main window
	if (parentFrame instanceof MainWindow)
	    this.mainWindow = (MainWindow) parentFrame;

	// create the translator
	this.translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

	// set the layout for this dialog
	this.setLayout(new BorderLayout());

	// set the title
	this.setTitle(JComponentBuilder.getTitle(this.baseKey));

	// create the ok/cancel button panel
	this.okCancelButtonPanel = JComponentBuilder.createOkCancelButtonPanel(this.baseKey
		+ ".okButton", this.baseKey + ".cancelButton", this);

	// create the button panel
	this.buttonPanel = new JPanel();
	this.buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

	// add the button panels
	JPanel bPanel = new JPanel();
	bPanel.setLayout(new BorderLayout());
	bPanel.add(this.buttonPanel, BorderLayout.NORTH);
	bPanel.add(this.okCancelButtonPanel, BorderLayout.SOUTH);
	this.add(bPanel, BorderLayout.SOUTH);

	// create the content panel
	this.contentPanel = new JPanel();
	this.contentPanel.setLayout(new BorderLayout());
	this.add(this.contentPanel, BorderLayout.CENTER);

	// set the default button
	this.getRootPane().setDefaultButton(this.okCancelButtonPanel.getOkButton());

	this.pack();

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
    protected TahitiDialog(JFrame parentFrame, String title, boolean modal) {
	this(parentFrame);
	this.setTitle(title);
	this.setModal(modal);
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
    public JButton addButton(String key, ActionListener listener,
	    KeyListener keyListener) {
	// check params
	if (key == null)
	    return null;

	// get the button
	JButton button = JComponentBuilder.createJButton(key, key, listener);
	button.addKeyListener(keyListener);

	// add the button to the panel
	this.buttonPanel.add(button);

	// return the button
	return button;
    }

    /**
     * Creates a generic "close" button.
     * 
     * @param key
     * @return
     */
    public final JButton addCloseButton(String key) {
	if (key == null)
	    key = JComponentBuilder.CANCEL_BUTTON_KEY;

	return this.addButton(key);
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
    public JButton addButton(String key, ActionListener listener) {
	return this.addButton(key, listener, null);
    }

    /**
     * Adds the specified button to the button panel
     * 
     * @param key
     *            the key for the button
     * @return the button, for further evaluation
     */
    public JButton addButton(String key) {
	return this.addButton(key, null, null);
    }

    /**
     * Adds the specified component to the content panel.
     * 
     * @param component
     *            the component to show
     */
    protected final void addContent(JComponent component) {
	// check params
	if (component == null)
	    return;
	else
	    this.contentPanel.add(component);
    }

    /**
     * Shows the specified message into a text area within the content panel.
     * Please note that only one message can be shown per time. The message is
     * shown at the center of the content pane.
     * 
     * @param message
     *            the message to show (must be not-null)
     */
    protected final void showText(String message) {
	// check params
	if ((message == null) || (message.length() == 0))
	    return;
	else {
	    // check if the message area has been already created
	    if (this.messageArea == null) {
		this.messageArea = new JTextArea();
		this.contentPanel.add(this.messageArea, BorderLayout.CENTER);
		this.messageArea.setEnabled(false);
	    }

	    // add the text message
	    this.messageArea.setText(message);

	}
    }

    /**
     * Adds the specified message to the content panel using a JLabel. This
     * method is suitable for small messages. The message is shown at the top of
     * the content pane.
     * 
     * @param message
     *            the short message to display
     */
    protected final void showMessage(String message) {
	// check params
	if ((message == null) || (message.length() == 0))
	    return;
	else
	    this.contentPanel.add(new JLabel(message), BorderLayout.NORTH);
    }

    /**
     * Builds a table with a list of the aglet info properties and their values.
     * The infoes are shown at the bottom of the content pane.
     * 
     * @param info
     *            the aglet info for the agent
     */
    protected final void showAgletInfo(AgletInfo info) {
	// check params
	if (info == null)
	    return;

	JScrollPane scrollTable = this.buildAgletInfoTable(info);
	this.contentPanel.add(scrollTable, BorderLayout.SOUTH);
    }

    /**
     * Builds a table with the iformation about an agent.
     * 
     * @param info
     *            the aglet info
     * @return a scrollpane with the table to display
     */
    private JScrollPane buildAgletInfoTable(AgletInfo info) {
	// display all the information about the aglet
	String address = info.getAddress();
	String className = info.getAgletClassName();
	String codeBase = info.getCodeBase().toExternalForm();
	String creationTime = "" + info.getCreationTime();
	String AgletID = info.getAgletID().toString();
	String APIMajor = "" + info.getAPIMajorVersion();
	String APIMinor = "" + info.getAPIMinorVersion();
	String origin = info.getOrigin();
	String certificate = info.getAuthorityCertificate().toString();

	String infoes[][] = new String[9][2];

	infoes[0][0] = this.translator.translate("AgletID");
	infoes[0][1] = AgletID;
	infoes[1][0] = this.translator.translate("AgletCodeBase");
	infoes[1][1] = codeBase;
	infoes[2][0] = this.translator.translate("AgletClassName");
	infoes[2][1] = className;
	infoes[3][0] = this.translator.translate("AgletAddress");
	infoes[3][1] = address;
	infoes[4][0] = this.translator.translate("AgletOrigin");
	infoes[4][1] = origin;
	infoes[5][0] = this.translator.translate("AgletCertificate");
	infoes[5][1] = certificate;
	infoes[6][0] = this.translator.translate("AgletAPIMajor");
	infoes[6][1] = APIMajor;
	infoes[7][0] = this.translator.translate("AgletAPIMinor");
	infoes[7][1] = APIMinor;
	infoes[8][0] = this.translator.translate("AgletCreationTime");
	infoes[8][1] = creationTime;

	String header[] = new String[2];
	header[0] = this.translator.translate("AgletPropertyName");
	header[1] = this.translator.translate("AgletPropertyValue");

	// build a jtable for the info
	JTable table = new JTable(infoes, header);

	// add the table to the content pane
	table.setEnabled(false);
	JScrollPane scrollTable = new JScrollPane(table);
	TitledBorder border = new TitledBorder(this.translator.translate(this.baseKey
		+ ".agletInfo.title"));
	border.setTitleJustification(TitledBorder.RIGHT);
	border.setTitleColor(Color.BLUE);
	scrollTable.setBorder(border);
	return scrollTable;
    }

    /**
     * Prepars an alert dialog window.
     * 
     * @param frame
     *            the owner of the dialog
     * @param message
     *            the message to show (it must be already translated)
     * @return the dialog window
     */
    public static TahitiDialog alert(JFrame frame, String message) {
	// check params
	if ((frame == null) || (message == null))
	    return null;

	// build a new TahitiDialog
	TahitiDialog dialog = new TahitiDialog(frame);

	dialog.setTitle(JComponentBuilder.getTitle(dialog.baseKey + ".alert"));

	// add the message to a textarea
	JTextArea text = new JTextArea();
	text.setText(message);
	text.setEditable(false);

	// now add the text area to the content pane of the
	// tahiti dialog
	dialog.addContent(text);
	dialog.pack();

	// all done
	return dialog;
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
    public static TahitiDialog message(JFrame frame, String title, String msg) {
	// check params
	if ((frame == null) || (msg == null))
	    return null;

	// build a new TahitiDialog
	TahitiDialog dialog = new TahitiDialog(frame);

	if (title != null)
	    dialog.setTitle(title);
	else
	    dialog.setTitle(JComponentBuilder.getTitle(dialog.baseKey
		    + ".message"));

	// add the message to a textarea
	JTextArea text = new JTextArea();
	text.setText(msg);
	text.setEditable(false);

	// now add the text area to the content pane of the
	// tahiti dialog
	dialog.addContent(text);
	dialog.pack();

	// all done
	return dialog;

    }

    protected static final TahitiDialog info(JFrame parentFrame,
	    AgletProxy proxy) {
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
	} catch (Exception e) {
	    if (dialog != null)
		dialog.logger.error("Exception caught while trying to build an aglet info dialog", e);
	    return null;
	}

    }

    /**
     * Adds a panel showing each aglet in the dialog window.
     * 
     * @param proxies
     *            the list of proxies to show
     */
    protected final void showAgletProxies(LinkedList<AgletProxy> proxies) {
	// check params
	if ((proxies == null) || (proxies.size() == 0))
	    return;

	// add an aglet list with the proxies
	AgletListPanel<AgletProxy> agletPanel = new AgletListPanel<AgletProxy>(proxies.size());
	agletPanel.setRenderer(new AgletListRenderer(agletPanel));

	// add each proxy
	Iterator iter = proxies.iterator();
	while ((iter != null) && iter.hasNext()) {
	    AgletProxy currentProxy = (AgletProxy) iter.next();
	    agletPanel.addItem(currentProxy);

	}

	// add the panel to the content panel
	this.addContent(agletPanel);
    }

    /**
     * Gets back the mainWindow.
     * 
     * @return the mainWindow
     */
    public synchronized final MainWindow getMainWindow() {
	return this.mainWindow;
    }

    /**
     * Sets the mainWindow value.
     * 
     * @param mainWindow
     *            the mainWindow to set
     */
    public synchronized final void setMainWindow(MainWindow mainWindow) {
	this.mainWindow = mainWindow;
    }

    /**
     * Makes the dialog window visible (if it is not already).
     * 
     */
    @Deprecated
    public void popup() {
	if (!this.isVisible()) {
	    this.pack();
	    this.setVisible(true);
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
	if (this.isVisible() || (this.getParent() == null))
	    return;

	Dimension d = this.getToolkit().getScreenSize();
	JFrame parent = (JFrame) this.getParent();

	Point ploc = parent.getLocationOnScreen();
	Dimension psize = parent.getSize();

	Dimension size = this.getSize();

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

	this.setVisible(true);
    }

    /**
     * Makes the dialog visible at the center of the screen.
     * 
     */
    @Deprecated
    public void popupAtCenterOfScreen() {
	if (this.isVisible())
	    return;

	Dimension d = this.getToolkit().getScreenSize();

	this.setLocation((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2);
	this.setVisible(true);
    }

    public static String[] split(String str) {
	String msg[] = new String[50];
	int pos, i, size = lineSeparator.length();

	for (i = 0; ((pos = str.indexOf(lineSeparator)) >= 0) && (i < 49); i++) {
	    msg[i] = str.substring(0, pos);
	    str = str.substring(pos + size);
	}
	msg[i++] = str;

	String ret[] = new String[i];

	System.arraycopy(msg, 0, ret, 0, i);
	return ret;
    }
}
