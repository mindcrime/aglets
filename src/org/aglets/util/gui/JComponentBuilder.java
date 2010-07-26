package org.aglets.util.gui;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.aglets.log.AgletsLogger;
import org.aglets.util.AgletsTranslator;

import com.ibm.aglets.tahiti.OkCancelButtonPanel;

/**
 * This class represents an utility class which aim is to build a standardized
 * JCOmponent, like a JLabel or a JButton to use in the GUI of the platform
 * (i.e., the Tahiti server) or in a swing application/agent.
 * 
 * The idea of this class is that for each main JComponent you want to create,
 * you can get one localized thru a resource bundle or a property file. By
 * default this class works with the Tahiti property file
 * (lib/tahiti.properties) for the localization.
 * 
 * You should use this class thru a set of keys, that are strings that
 * represents a component of your dialog window, thus the system can
 * automatically bind the icon for such key. For instance, for a JLabel in the
 * status memoryBar com.ibm.aglets.tahiti.MainWindow object, you should use a
 * key like com.ibm.aglets.tahiti.MainWindow.statusBarLabel (or something
 * similar) and provide in the tahiti.properties file the translation and the
 * icon as follows: com.ibm.aglets.tahiti.MainWindow.statusBarLabel =
 * "Text for the status memoryBar"
 * com.ibm.aglets.tahiti.MainWindow.statusBarLabel.icon = "/path/to/the/icon"
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         06/set/07
 */
public class JComponentBuilder {

    /**
     * Static keys for the default.
     */
    public static String OK_BUTTON_KEY = "OK_BUTTON_KEY";
    public static String CANCEL_BUTTON_KEY = "CANCEL_BUTTON_KEY";

    /**
     * The suffix of the key of a component for the key specification.
     */
    public static String ICON_KEY_SUFFIX = ".icon";

    /**
     * The suffix for the tooltip key.
     */
    public static String TOOLTIP_KEY_SUFFIX = ".tooltip";

    /**
     * The title suffix.
     */
    public static String TITLE_KEY_SUFFIX = ".title";

    /**
     * The initial content of a component, for example the initial text of a
     * textfield.
     */
    public static String INITIAL_CONTENT_KEY_SUFFIX = ".initial";

    /**
     * The suffix for a message.
     */
    public static String MESSAGE_KEY_SUFFIX = ".message";

    /**
     * The logo of the project.
     */
    public static String LOGO_FILE = "img/logo.png";
    public static String LOGO_SMALL_FILE = "img/logo_small.png";

    /**
     * The translator to use for texts and other stuff.
     */
    private static AgletsTranslator translator = AgletsTranslator.getInstance("tahiti", Locale.getDefault());

    /**
     * The logger of this class.
     */
    private static AgletsLogger logger = AgletsLogger.getLogger(JComponentBuilder.class.getName());

    /**
     * Returns an incon for the specified key, appended with the ".icon" suffix.
     * This means that if your icon key is something like
     * com.ibm.aglets.tahiti.MainWindow.createButton, then the image file will
     * be searched in the translator with the key
     * com.ibm.aglets.tahiti.MainWindow.createButton.icon
     * 
     * Please note that if the file does not exists, then the icon returned will
     * be null.
     * 
     * @param key
     *            the key that identifies the component to search the icon for,
     *            without the .icon suffix.
     * @return the created icon
     */
    public static Icon getIcon(String key) {
	String iconPath = translator.translate(key + ICON_KEY_SUFFIX);

	File iconFile = new File(iconPath);

	// check if I found something
	if ((iconPath == null) || iconPath.equals(key + ICON_KEY_SUFFIX)
		|| (!(iconFile.exists())))
	    return null;
	else {
	    ImageIcon icon = new ImageIcon(iconPath);
	    return icon;
	}
    }

    /**
     * Provides the title for a window, that is the translation for the .title
     * key.
     * 
     * @param key
     *            the key of the window
     * @return the translated title or null
     */
    public static String getTitle(String key) {
	// check params
	if (key == null)
	    return null;
	else
	    return translator.translate(key + TITLE_KEY_SUFFIX);
    }

    /**
     * Provides the mnemonic shortcut for a menu entry.
     * 
     * @param key
     *            the base key of the menu
     * @return the char for the memo entry
     */
    public static char getMnemonic(String key) {
	// check params
	if (key == null)
	    return ' ';
	else {
	    String memo = translator.translate(key + ".mnemonic");
	    if ((memo != null) && (memo.length() == 1))
		return memo.charAt(0);
	    else
		return ' ';
	}

    }

    /**
     * Provides the tooltip text of the specified key, already translated and
     * localized thru the translator. The tooltip for a component must be
     * specified thru the key of the component itself and the TOOLTIP_KEY_SUFFIX
     * suffix. For instance, the tooltip for the component
     * com.ibm.aglets.tahiti.mainWindow.OkButton is specfied thru
     * com.ibm.aglets.tahiti.mainWindow.OkButton.
     * 
     * @param key
     *            the key of the component
     * @return the localized tooltip string (if any)
     */
    public static String getTooltipText(String key) {
	if (key == null)
	    return null;
	else
	    return translator.translate(key + TOOLTIP_KEY_SUFFIX);
    }

    /**
     * Creates a new JLabel with the specified text (not to be translated but
     * already translated) and icon.
     * 
     * @param text
     *            the text to display in the label
     * @param icon
     *            the icon to show
     * @return the JLabel created
     */
    protected static final JLabel createJLabel(String text, Icon icon) {
	JLabel label = new JLabel(text, icon, SwingConstants.CENTER);
	return label;
    }

    /**
     * Builds a JLabel for the specified key. You should specify the key the
     * method should use to refers to your JLabel, and specify in the property
     * file or ResourceBundle. The text to display will be took from the
     * ResourceBundle/property file, the icon from the same file/bundle but with
     * the ".icon" suffix appended.
     * 
     * @param key
     *            the key to use for the lookup in the property file/resource
     *            bundle
     * @return the created JLabel, with the specified icon and translated text.
     */
    public static final JLabel createJLabel(String key) {
	String text = translator.translate(key);
	Icon icon = getIcon(key);
	return createJLabel(text, icon);
    }

    /**
     * Creates a specific JButton with an associated icon and tooltip, specified
     * from the key string. In other word the string key is used to lookup in
     * the resource bundle for the resource specified, and the .icon and
     * .tooltip keys are also looked up.
     * 
     * @param key
     *            the key for this entry, you should also specify the key.icon
     *            and key.tooltip properties
     * @param actionCommand
     *            the action command for this button, if not specified it will
     *            be set equal to the specified key
     * @param listener
     *            the action listener associated to this button (if there's one)
     * @return
     */
    public static final JButton createJButton(String key, String actionCommand,
	    ActionListener listener) {
	// check params
	if (key == null)
	    return null;

	// get the localized text
	String text = translator.translate(key);

	// get the icon of this button
	Icon icon = getIcon(key);

	// create the button
	JButton button = new JButton();
	button.setText(text);

	if (icon != null)
	    button.setIcon(icon);

	// set the tooltip
	button.setToolTipText(getTooltipText(key));

	// set the action command for the button
	if (actionCommand == null)
	    actionCommand = key;

	button.setActionCommand(actionCommand);

	if (listener != null)
	    button.addActionListener(listener);

	// all done
	return button;
    }

    /**
     * Creates a checkbox with the specified label, tooltip, and so on.
     * 
     * @param key
     *            the key for the lookup in the localized resources
     * @param selected
     *            true if the checkbox must be selected by default
     * @param listener
     *            the listener of this checkbox
     * @return the checkbox
     */
    public static final JCheckBox createJCheckBox(String key, boolean selected,
	    ItemListener listener) {
	// check params
	if (key == null)
	    return null;

	// get the texts
	String tooltip = getTooltipText(key);
	String text = translator.translate(key);
	Icon icon = getIcon(key);

	// create the checkbox
	JCheckBox cBox = new JCheckBox();

	if (tooltip != null)
	    cBox.setToolTipText(tooltip);

	if (text != null)
	    cBox.setText(text);

	if (icon != null)
	    cBox.setIcon(icon);

	if (listener != null)
	    cBox.addItemListener(listener);

	cBox.setSelected(selected);

	// all done
	return cBox;

    }

    /**
     * Creates a JTextField with the specified size and the initial text.
     * 
     * @param size
     *            the size of the textfield, must be greated than zero
     * @param initialText
     *            the initial text to display (not mandatory)
     * @param key
     *            the key for the tooltip or initial text or something else
     * @return the created textfield or null if the size is less or equal zero
     */
    public static final JTextField createJTextField(int size,
	    String initialText, String key) {
	// check params
	if (size <= 0)
	    return null;

	// create the textfield
	JTextField field = new JTextField(size);

	// get the values for this textfield
	String initialContent = translator.translate(key
		+ INITIAL_CONTENT_KEY_SUFFIX);
	String tooltip = getTooltipText(key);

	// set the initial text if have it
	if (initialText != null)
	    field.setText(initialText);
	else if ((initialContent != null)
		&& (initialContent.equals(key + INITIAL_CONTENT_KEY_SUFFIX) == false))
	    field.setText(initialContent);

	// set the tooltip
	if (tooltip != null)
	    field.setToolTipText(tooltip);

	// all done
	return field;
    }

    /**
     * Creates a password field with the specified size.
     * 
     * @param size
     *            the size of the field
     * @return the field or null
     */
    public static final JPasswordField createJPasswordField(int size) {
	// check params
	if (size <= 0)
	    return null;

	// create the textfield
	JPasswordField field = new JPasswordField(size);

	return field;
    }

    /**
     * Constructs a generic OK/Cancel two buttons panel with the button
     * associated to the specified listener. Please note that the keys OK_BUTTON
     * and CANCEL_BUTTON (and their derivates) must be present in the resource
     * bundle.
     * 
     * @param listener
     *            the listener to use with these buttons
     * @param okKey
     *            the key used for the button ok, if null it will be used a
     *            default string
     * @param cancelKey
     *            the key for the cancel, if null a default string will be used
     * @return the panel with the two buttons
     */
    public static final OkCancelButtonPanel createOkCancelButtonPanel(
	    String okKey, String cancelKey, ActionListener listener) {
	return new OkCancelButtonPanel(okKey, cancelKey, listener);
	/*
	 * JPanel buttonPanel = new JPanel(); buttonPanel.setLayout(new
	 * FlowLayout(FlowLayout.RIGHT));
	 * 
	 * if( okKey == null || okKey.length() == 0 ||
	 * okKey.equals(translator.translate(okKey)) ) okKey = OK_BUTTON_KEY;
	 * 
	 * if( cancelKey == null || cancelKey.length() == 0 ||
	 * cancelKey.equals(translator.translate(cancelKey)) ) cancelKey =
	 * CANCEL_BUTTON_KEY;
	 * 
	 * JButton okButton = createJButton(okKey, GUICommandStrings.OK_COMMAND,
	 * listener); JButton cancelButton = createJButton(cancelKey,
	 * GUICommandStrings.CANCEL_COMMAND, listener);
	 * 
	 * buttonPanel.add(okButton); buttonPanel.add(cancelButton);
	 * 
	 * return buttonPanel;
	 */
    }

    public static final void showErrorDialog(JFrame parentFrame, String key) {
	// check arguments
	if (key == null)
	    return;

	// try to get the translations for this dialog
	String message = translator.translate(key + MESSAGE_KEY_SUFFIX);
	String title = getTitle(key);
	Icon icon = getIcon(key);

	// create the message dialog
	JOptionPane.showMessageDialog(parentFrame, message, title, JOptionPane.ERROR_MESSAGE, icon);
    }

    /**
     * Creats a panel that contains the logo of the aglets project. In the case
     * the logo cannot be found, then null is returned.
     * 
     * @return the image panel or null
     */
    public static ImagePanel createLogoPanel() {

	// test if the logo exists
	File logo = new File(LOGO_FILE);
	if (logo.exists()) {
	    ImagePanel panel = new ImagePanel(LOGO_FILE);
	    return panel;
	} else
	    return null;

    }

    /**
     * Creates a small logo panel with the small logo image.
     * 
     * @return the image panel or null
     */
    public static ImagePanel createSmallLogoPanel() {
	// test if the logo exists
	File logo = new File(LOGO_SMALL_FILE);
	if (logo.exists()) {
	    ImagePanel panel = new ImagePanel(LOGO_SMALL_FILE);
	    return panel;
	} else
	    return null;

    }

    /**
     * Creates a specific menu entry.
     * 
     * @param key
     *            the key to lookup for the translation
     * @param actionCommand
     *            the action command to associate to the item
     * @param listener
     *            the listener to add to the menu entry
     * @return the menu entry
     */
    public static JMenuItem createJMenuItem(String key, String actionCommand,
	    ActionListener listener) {
	// check params
	if ((key == null) || (key.length() == 0))
	    return null;

	// create the menu item
	JMenuItem item = new JMenuItem();

	// set the mnemonic accelerator
	char memo = getMnemonic(key);
	if (memo != ' ')
	    item.setMnemonic(memo);

	// get the translated text
	String text = translator.translate(key);
	// set the text
	if (text != null)
	    item.setText(text);
	else
	    item.setText(key);

	// get the icon
	Icon icon = getIcon(key);
	// set the icon
	if (icon != null)
	    item.setIcon(icon);

	// get the tooltip
	String tooltip = getTooltipText(key);
	// set the tooltip
	if (tooltip != null)
	    item.setToolTipText(tooltip);

	// set the action
	if (actionCommand != null)
	    item.setActionCommand(actionCommand);
	else
	    item.setActionCommand(key);

	// and if i've got the listener add it
	if (listener != null)
	    item.addActionListener(listener);

	// all done
	return item;
    }

    /**
     * Builds a JMenu with the translated text (if available).
     * 
     * @param key
     *            the key for the lookup of the text
     * @return the menu or null if it cannot be built.
     */
    public static JMenu createJMenu(String key) {
	// check params
	if ((key == null) || (key.length() == 0))
	    return null;

	// create the menu looking up in the translations
	JMenu menu = new JMenu();
	String text = translator.translate(key);

	// set the mnemonic accelerator
	char memo = getMnemonic(key);
	menu.setMnemonic(memo);

	if (text != null)
	    menu.setText(text);
	else
	    menu.setText(key);

	// all done
	return menu;
    }

    /**
     * Gets back the translator.
     * 
     * @return the translator
     */
    public static synchronized final AgletsTranslator getTranslator() {
	return translator;
    }

    /**
     * Returns a text area.
     * 
     * @param fileName
     *            if specified and not null, the file is read and each line is
     *            appended to the text area. If the file cannot be read, than
     *            the text area cotnent is set to empty.
     * @return the text area (empty of with the file content).
     */
    public static JTextArea createJTextArea(String fileName) {
	JTextArea text = new JTextArea(10, 30);

	// try to load the file if specified
	if (fileName != null) {
	    try {
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = null;
		do {
		    logger.debug("Reading a new line from file <" + file + ">");
		    line = reader.readLine();
		    text.append(line);
		    text.append("\n");	// new line in order to report the same text structure as in the file
		} while (line != null);

	    } catch (IOException e) {
		logger.error("Exception caught while loading file content to display in JTextArea "
			+ fileName, e);
		text.setText(e.getMessage());
	    }
	}

	// all done
	return text;
    }
}
