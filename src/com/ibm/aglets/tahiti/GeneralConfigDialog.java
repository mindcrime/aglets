package com.ibm.aglets.tahiti;

/*
 * @(#)GeneralConfigDialog.java
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

import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglets.*;

// import com.ibm.aglets.security.User;
// - import com.ibm.aglets.security.UserAuthenticator;
// import com.ibm.aglets.security.UserAdministrator;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;

import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Enumeration;
import java.util.ResourceBundle;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;

/**
 * Class GeneralConfigDialog represents the dialog for
 * 
 * @version     1.01    96/03/28
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 * @author      Yoshiaki Mima
 */

final class GeneralConfigDialog extends TahitiDialog 
	implements ActionListener, ItemListener {

	static final String STARTUP_AGLET = "com.ibm.aglets.samples.Writer";

	/*
	 * Font Configuration
	 */
	private Choice _fontChoice = new Choice();
	private Choice _fontSizeChoice = new Choice();
	private Choice _fontStyleChoice = new Choice();

	private Choice _fixedFontChoice = new Choice();
	private Choice _fixedFontSizeChoice = new Choice();
	private Choice _fixedFontStyleChoice = new Choice();

	/*
	 * List View Order
	 */
	private Choice _viewKeyItemChoice = new Choice();
	private Choice _viewOrderChoice = new Choice();
	private Choice _viewPrecisionChoice = new Choice();

	/*
	 * Cache Control
	 */
	private Button _clearCache = new Button("Clear Class Cache Now");

	/*
	 * Startup Configuration
	 */
	private Checkbox _startup = new Checkbox("Launch Startup Aglet");
	private TextField _startupAglet = new TextField(20);

	private String[] fontList = 
		GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();

	// Toolkit.getDefaultToolkit().getFontList();
	private String styleList[] = {
		"plain", "bold", "italic", "bolditalic"
	};
	private String sizeList[] = {
		"6", "8", "10", "12", "14", "15", "16", "18", "20", "22", "24", "26", 
		"28", "30"
	};
	private String keyItemList[] = {
		"event order", "creation time", "class name"
	};
	private String orderList[] = {
		"ascent", "descent"
	};
	private String precisionList[] = {
		"complete", "compact"
	};

	/*
	 * Singleton instance reference.
	 */
	private static GeneralConfigDialog _instance = null;

	static final String ACTION_OK = "OK";
	static final String ACTION_CLEAR_CACHE = "Clear Cache";
	static final String ACTION_RESTORE_DEFAULTS = "Restore Defaults";
	static final String ACTION_CHANGE_PROFILE = "Change Profile";
	static final String ACTION_CHANGE_PASSWORD = "Change Password";
	static final String ACTION_CREATE_USER = "Create User";
	static final String ACTION_REMOVE_USER = "Remove User";
	static final String ACTION_IMPORT_USER = "Import User";
	static final String ACTION_EXPORT_USER = "Export User";

	/*
	 * Constructs a new Aglet creation dialog.
	 */
	private GeneralConfigDialog(MainWindow parent) {
		super(parent, "General Preferences", true);

		makePanel();

		addButton(ACTION_OK, this);
		addCloseButton(null);
		addButton(ACTION_RESTORE_DEFAULTS, this);
	}
	/*
	 */
	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();

		if (ACTION_RESTORE_DEFAULTS.equals(cmd)) {
			restoreDefaults();
		} else if (ACTION_CLEAR_CACHE.equals(cmd)) {
			AgletRuntime.clearCache();
		} else if (ACTION_OK.equals(cmd)) {
			save();
			dispose();
		} 
	}
	void disabling() {
		_startupAglet.setEditable(_startup.getState());
	}
	/*
	 * Singletion method to get the instnace
	 */
	static GeneralConfigDialog getInstance(MainWindow parent) {
		if (_instance == null) {
			_instance = new GeneralConfigDialog(parent);
		} else {
			_instance.updateValues();
		} 
		return _instance;
	}
	public void itemStateChanged(ItemEvent ev) {
		disabling();
	}
	/*
	 * Layouts all components.
	 */
	protected void makePanel() {
		GridBagPanel p = new GridBagPanel();

		add("Center", p);

		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.ipadx = cns.ipady = 5;
		cns.insets = new Insets(1, 5, 1, 5);
		p.setConstraints(cns);
		BorderPanel fontPanel = new BorderPanel("Font");

		p.add(fontPanel, GridBagConstraints.REMAINDER);

		BorderPanel viewPanel = new BorderPanel("List View");

		p.add(viewPanel, GridBagConstraints.REMAINDER);

		BorderPanel startupPanel = new BorderPanel("Startup");

		p.add(startupPanel, GridBagConstraints.REMAINDER);

		BorderPanel cachePanel = new BorderPanel("Cache Control");

		p.add(cachePanel, GridBagConstraints.REMAINDER);

		setupFontPanel(fontPanel);
		setupViewPanel(viewPanel);
		setupCachePanel(cachePanel);
		setupStartupPanel(startupPanel);

		updateValues();
	}
	/*
	 * Reverts to the defaults
	 */
	void restoreDefaults() {
		_fontChoice.select("TimesRoman");
		_fontChoice.select("plain");
		_fontSizeChoice.select("12");

		_fixedFontChoice.select("Courier");
		_fixedFontChoice.select("plain");
		_fixedFontSizeChoice.select("12");

		_viewKeyItemChoice.select("event order");
		_viewOrderChoice.select("ascent");
		_viewPrecisionChoice.select("complete");

		_startup.setState(true);
		_startupAglet.setText(STARTUP_AGLET);
		disabling();
	}
	void save() {
		Resource tahiti_res = Resource.getResourceFor("tahiti");
		Resource aglets_res = Resource.getResourceFor("aglets");
		String name = _fontChoice.getSelectedItem();
		String style = _fontStyleChoice.getSelectedItem();
		int size = Integer.parseInt(_fontSizeChoice.getSelectedItem());

		tahiti_res.setResource("tahiti.font", 
							   name + '-' + style + '-' + size);

		name = _fixedFontChoice.getSelectedItem();
		style = _fixedFontStyleChoice.getSelectedItem();
		size = Integer.parseInt(_fixedFontSizeChoice.getSelectedItem());

		tahiti_res.setResource("tahiti.fixedFont", 
							   name + '-' + style + '-' + size);

		tahiti_res.setResource("tahiti.startup", 
							   String.valueOf(_startup.getState()));
		tahiti_res.setResource("tahiti.startupAglets", 
							   _startupAglet.getText().trim());

		// tahiti items view control
		String key = _viewKeyItemChoice.getSelectedItem();

		if (key.equals("event order")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_LASTUPDATE);
		} else if (key.equals("creation time")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_TIMESTAMP);
		} else if (key.equals("class name")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_CLASSNAME);
		} 
		;
		tahiti_res.setResource("tahiti.itemkey", key);

		String order = _viewOrderChoice.getSelectedItem();

		if (order.equals("ascent")) {
			TahitiItem.setAscentOrder();
		} else if (order.equals("descent")) {
			TahitiItem.setDescentOrder();
		} 
		;
		tahiti_res.setResource("tahiti.itemorder", order);

		String precision = _viewPrecisionChoice.getSelectedItem();

		if (precision.equals("complete")) {
			TahitiItem.setPrecision(true);
		} else if (precision.equals("compact")) {
			TahitiItem.setPrecision(false);
		} 
		;
		tahiti_res.setResource("tahiti.itemprecision", precision);

		tahiti_res.save("Tahiti");
		aglets_res.save("Tahiti");

		Font font = tahiti_res.getFont("tahiti.font", null);

		Util.reset();
		Util.update();

		// getParent().setFont(font);
	}
	/*
	 * Cache Control
	 */
	private void setupCachePanel(BorderPanel cachePanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 1.0;
		cns.insets = cachePanel.topInsets();
		cns.insets.bottom = cachePanel.bottomInsets().bottom;

		cachePanel.setConstraints(cns);

		cns.fill = GridBagConstraints.NONE;
		cachePanel.add(_clearCache, GridBagConstraints.REMAINDER, 0.5);

		_clearCache.setActionCommand(ACTION_CLEAR_CACHE);
		_clearCache.addActionListener(this);
	}
	/*
	 * Creates the Font setup panel
	 */
	private void setupFontPanel(BorderPanel fontPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 1.0;
		cns.insets = fontPanel.topInsets();
		cns.insets.bottom = fontPanel.bottomInsets().bottom;
		fontPanel.setConstraints(cns);

		/*
		 * Proportional
		 */
		fontPanel.add(new Label("Proportional Font:"), 1, 0.1);

		/* font list */
		_fontChoice.addItem("serif");
		_fontChoice.addItem("sanserif");
		_fontChoice.addItem("monospaced");
		for (int i = 0; i < fontList.length; i++) {
			_fontChoice.addItem(fontList[i]);
		} 
		fontPanel.add(_fontChoice, 1, 1.0);

		/* font style */
		for (int i = 0; i < styleList.length; i++) {
			_fontStyleChoice.addItem(styleList[i]);
		} 

		fontPanel.add(_fontStyleChoice, 1, 0.4);

		/* font size */
		fontPanel.add(_fontSizeChoice, GridBagConstraints.REMAINDER, 1.0);
		for (int i = 0; i < sizeList.length; i++) {
			_fontSizeChoice.addItem(sizeList[i]);
		} 
		cns.insets = fontPanel.bottomInsets();

		/*
		 * Fixed
		 */
		fontPanel.add(new Label("Fixed Font:"), 1);

		/* font list */
		_fixedFontChoice.addItem("serif");
		_fixedFontChoice.addItem("sanserif");
		_fixedFontChoice.addItem("monospaced");
		for (int i = 0; i < fontList.length; i++) {
			_fixedFontChoice.addItem(fontList[i]);
		} 
		fontPanel.add(_fixedFontChoice, 1, 1.0);

		/* font style */
		for (int i = 0; i < styleList.length; i++) {
			_fixedFontStyleChoice.addItem(styleList[i]);
		} 
		fontPanel.add(_fixedFontStyleChoice, 1, 0.4);

		/* font size */
		cns.weightx = 1.0;
		fontPanel.add(_fixedFontSizeChoice, GridBagConstraints.REMAINDER);
		for (int i = 0; i < sizeList.length; i++) {
			_fixedFontSizeChoice.addItem(sizeList[i]);
		} 
	}
	/*
	 * Creates the startup panel
	 */
	private void setupStartupPanel(BorderPanel startupPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 1.0;
		cns.insets = startupPanel.topInsets();
		cns.insets.bottom = startupPanel.bottomInsets().bottom;

		startupPanel.setConstraints(cns);
		startupPanel.add(new Label("On Startup:"), 1, 0.0);
		startupPanel.add(_startup, GridBagConstraints.REMAINDER, 1.0);

		cns.insets = startupPanel.bottomInsets();
		startupPanel.add(_startupAglet, GridBagConstraints.REMAINDER, 0.5);

		_startup.addItemListener(this);
	}
	/*
	 * Creates the List View setup panel
	 */
	private void setupViewPanel(BorderPanel viewPanel) {
		GridBagConstraints cns = new GridBagConstraints();

		cns.anchor = GridBagConstraints.WEST;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 1.0;
		cns.insets = viewPanel.topInsets();

		// cns.insets = new Insets(3,5,3,5);

		cns.insets.bottom = viewPanel.bottomInsets().bottom;
		viewPanel.setConstraints(cns);

		/*
		 * Sorting Order
		 */
		viewPanel.add(new Label("Order Key:"), 1, 0.4);

		/* key item choice */
		for (int i = 0; i < keyItemList.length; i++) {
			_viewKeyItemChoice.addItem(keyItemList[i]);
		} 

		viewPanel.add(_viewKeyItemChoice, GridBagConstraints.REMAINDER, 1.0);

		/* sort order */
		viewPanel.add(new Label("Sort Order:"), 1, 0.4);

		for (int i = 0; i < orderList.length; i++) {
			_viewOrderChoice.addItem(orderList[i]);
		} 

		viewPanel.add(_viewOrderChoice, GridBagConstraints.REMAINDER, 1.0);

		/* precision */
		viewPanel.add(new Label("Display Precision:"), 1, 0.4);

		viewPanel.add(_viewPrecisionChoice, GridBagConstraints.REMAINDER, 
					  1.0);
		for (int i = 0; i < precisionList.length; i++) {
			_viewPrecisionChoice.addItem(precisionList[i]);
		} 

		cns.insets = viewPanel.bottomInsets();
	}
	/*
	 * Sets values
	 */
	private void updateValues() {
		Resource tahiti_res = Resource.getResourceFor("tahiti");
		Resource aglets_res = Resource.getResourceFor("aglets");

		Font f = tahiti_res.getFont("tahiti.font", null);

		_fontChoice.select(f.getName());
		_fontStyleChoice.select(styleList[f.getStyle()]);
		_fontSizeChoice.select(String.valueOf(f.getSize()));

		f = tahiti_res.getFont("tahiti.fixedFont", null);

		_fixedFontChoice.select(f.getName());
		_fixedFontStyleChoice.select(styleList[f.getStyle()]);
		_fixedFontSizeChoice.select(String.valueOf(f.getSize()));

		String key, order, precision;

		_viewKeyItemChoice.select(key = tahiti_res.getString("tahiti.itemkey", 
				"event order"));
		_viewOrderChoice.select(order = 
			tahiti_res.getString("tahiti.itemorder", "ascent"));
		_viewPrecisionChoice.select(precision = 
			tahiti_res.getString("tahiti.itemprecision", "complete"));

		// tahiti items view control
		if (key.equals("event order")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_LASTUPDATE);
		} else if (key.equals("creation time")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_TIMESTAMP);
		} else if (key.equals("class name")) {
			TahitiItem.setKeyItem(TahitiItem.KEY_CLASSNAME);
		} 
		;

		if (order.equals("ascent")) {
			TahitiItem.setAscentOrder();
		} else if (order.equals("descent")) {
			TahitiItem.setDescentOrder();
		} 
		;

		if (precision.equals("complete")) {
			TahitiItem.setPrecision(true);
		} else if (precision.equals("compact")) {
			TahitiItem.setPrecision(false);
		} 
		;

		// getMainWindow().updateProxyList();

		_startup.setState(tahiti_res.getBoolean("tahiti.startup", false));
		_startupAglet.setText(tahiti_res.getString("tahiti.startupAglets"));

		/*
		 * Cache Control
		 * if (aglets_res.getBoolean("aglets.class.reload", false)) {
		 * _reloadClass.setLabel("Always Reload Classes");
		 * _reloadClass.setState(true);
		 * } else {
		 * _reloadClass.setLabel("Use Cached Classes");
		 * _reloadClass.setState(false);
		 * }
		 */

		disabling();
	}
	public boolean windowClosing(WindowEvent ev) {
		return false;
	}
}
