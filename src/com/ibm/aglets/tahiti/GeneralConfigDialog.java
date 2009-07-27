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


import com.ibm.aglets.*;
import com.ibm.aglets.tahiti.utils.IconRepository;
import com.ibm.aglets.tahiti.utils.TahitiCommandStrings;
import javax.swing.*;


import com.ibm.awb.misc.Resource;




import java.awt.GridBagConstraints;
import java.awt.Insets;
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

	
	/**
	 * Combos for the selection order
	 */
	private JComboBox viewCombo,orderCombo,precisionCombo;
	
	/*
	 * Cache Control
	 */
	private JButton _clearCache = new JButton(bundle.getString("dialog.genprefs.button.clearcache"),IconRepository.getIcon("now"));

	/*
	 * Startup Configuration
	 */
	private JCheckBox _startup = new JCheckBox(bundle.getString("dialog.genprefs.button.startup"));
	private JTextField _startupAglet = new JTextField(20);

	
	private String keyItemList[] = {
		bundle.getString("dialog.genprefs.key.item.event"), bundle.getString("dialog.genprefs.key.item.creation"), bundle.getString("dialog.genprefs.key.item.class")
	};
	private String orderList[] = {
	        bundle.getString("dialog.genprefs.order.ascent"), bundle.getString("dialog.genprefs.order.descent")
	};
	private String precisionList[] = {
	        bundle.getString("dialog.genprefs.precision.complete"), bundle.getString("dialog.genprefs.precision.compact")
	};

	/*
	 * Singleton instance reference.
	 */
	private static GeneralConfigDialog _instance = null;


	/*
	 * Constructs a new Aglet creation dialog.
	 */
	private GeneralConfigDialog(MainWindow parent) {
		super(parent, bundle.getString("dialog.genprefs.title"), true);

		makePanel();

		// add buttons
		this.addJButton(bundle.getString("dialog.genprefs.button.ok"),TahitiCommandStrings.OK_COMMAND,IconRepository.getIcon("ok"),this);
		this.addJButton(bundle.getString("dialog.genprefs.button.default"),TahitiCommandStrings.DEFAULT_COMMAND,IconRepository.getIcon("default"),this);
		this.addJButton(bundle.getString("dialog.genprefs.button.cancel"),TahitiCommandStrings.CANCEL_COMMAND,IconRepository.getIcon("cancel"),this);
		
		this.pack();
	}
	
	
	
	
	
	
	/**
	 * Manage events from buttons.
	 * @param event the event to manage
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		
		if(command.equals(TahitiCommandStrings.OK_COMMAND)){
		    // save current values
		    this.save();
		}
		else
		if(command.equals(TahitiCommandStrings.DEFAULT_COMMAND)){
		    this.restoreDefaults();
		}
		else
		if(command.equals(TahitiCommandStrings.CLEAR_CACHE_COMMAND)){
		    AgletRuntime.clearCache();
		}
		
		this.setVisible(false);
		this.dispose();
	}
	
	
	/**
	 * Disable the startup agent JTextField depending on the value of the check box.
	 *
	 */
	void disabling() {
		this._startupAglet.setEnabled(this._startup.isSelected());
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

		this.getContentPane().add("Center",p);

		GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.ipadx = cns.ipady = 5;
		cns.insets = new Insets(1, 5, 1, 5);
		p.setConstraints(cns);
		
		// view order
		BorderPanel viewPanel = new BorderPanel(bundle.getString("dialog.genprefs.border.view"));
		p.add(viewPanel, GridBagConstraints.REMAINDER);

		// startup agent
		BorderPanel startupPanel = new BorderPanel(bundle.getString("dialog.genprefs.border.startup"));
		p.add(startupPanel, GridBagConstraints.REMAINDER);

		// cache control
		BorderPanel cachePanel = new BorderPanel(bundle.getString("dialog.genprefs.border.cache"));
		p.add(cachePanel, GridBagConstraints.REMAINDER);

		
		setupViewPanel(viewPanel);
		setupCachePanel(cachePanel);
		setupStartupPanel(startupPanel);

		updateValues();
	}
	
	
	
	
	/*
	 * Reverts to the defaults
	 */
	void restoreDefaults() {
	    this._startup.setSelected(false);
		disabling();
		
		this.orderCombo.setSelectedIndex(0);
		this.precisionCombo.setSelectedIndex(0);
		this.viewCombo.setSelectedIndex(0);
	}
	
	
	
	void save() {
		Resource tahiti_res = Resource.getResourceFor("tahiti");
		Resource aglets_res = Resource.getResourceFor("aglets");
		
		// get startup values
		tahiti_res.setResource("tahiti.startup", String.valueOf(_startup.isSelected()));
		tahiti_res.setResource("tahiti.startupAglets", _startupAglet.getText().trim());

		// tahiti items view control
		String key = (String)this.viewCombo.getSelectedItem();

		
		if (key==null || key.equals(bundle.getString("dialog.genprefs.key.item.event"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_LASTUPDATE);
		} else if (key!=null && key.equals(bundle.getString("dialog.genprefs.key.item.creation"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_TIMESTAMP);
		} else if (key!=null && key.equals(bundle.getString("dialog.genprefs.key.item.class"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_CLASSNAME);
		} 
		
		tahiti_res.setResource("tahiti.itemkey", key);

		// order view control
		String order = (String) this.orderCombo.getSelectedItem();

		if (order==null || bundle.getString("dialog.genprefs.order.ascent").equals(order)) {
			TahitiItem.setAscentOrder();
		} else if (order!=null && bundle.getString("dialog.genprefs.order.descent").equals(order)) {
			TahitiItem.setDescentOrder();
		} 
		
		tahiti_res.setResource("tahiti.itemorder", order);

		
		// precision order
		String precision =(String) this.precisionCombo.getSelectedItem();
		if (precision==null || precision.equals(bundle.getString("dialog.genprefs.precision.complete"))) {
			TahitiItem.setPrecision(true);
		} else if (precision!=null && precision.equals(bundle.getString("dialog.genprefs.precision.compact"))) {
			TahitiItem.setPrecision(false);
		} 
		
		tahiti_res.setResource("tahiti.itemprecision", precision);

		tahiti_res.save("Tahiti");
		aglets_res.save("Tahiti");

		
		Util.reset();
		Util.update();

		
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

		_clearCache.setActionCommand(TahitiCommandStrings.CLEAR_CACHE_COMMAND);
		_clearCache.addActionListener(this);
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
		startupPanel.add(new JLabel(bundle.getString("dialog.genprefs.label.startup")), 1, 0.0);
		startupPanel.add(_startup, GridBagConstraints.REMAINDER, 1.0);

		
		cns.insets = startupPanel.bottomInsets();
		startupPanel.add(_startupAglet, GridBagConstraints.REMAINDER, 0.5);
		this._startup.setActionCommand(TahitiCommandStrings.STARTUP_COMMAND);
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

		cns.insets.bottom = viewPanel.bottomInsets().bottom;
		viewPanel.setConstraints(cns);

		

		// build a combobox with the available selections
		viewCombo = new JComboBox(this.keyItemList);
		viewPanel.add(new JLabel(bundle.getString("dialog.genprefs.label.vieworder")), 1, 0.4);
		viewPanel.add(viewCombo, GridBagConstraints.REMAINDER, 1.0);

		// build a combobox with available choices for the view order
		orderCombo = new JComboBox(this.orderList);
		viewPanel.add(new JLabel(bundle.getString("dialog.genprefs.label.sortorder")), 1, 0.4);
		viewPanel.add(orderCombo, GridBagConstraints.REMAINDER, 1.0);

		// precision
		precisionCombo = new JComboBox(this.precisionList);
		viewPanel.add(new JLabel(bundle.getString("dialog.genprefs.label.precision")), 1, 0.4);
		viewPanel.add(precisionCombo, GridBagConstraints.REMAINDER, 1.0);

		cns.insets = viewPanel.bottomInsets();
	}
	
	
	
	
	
	/*
	 * Sets values
	 */
	private void updateValues() {
		Resource tahiti_res = Resource.getResourceFor("tahiti");
		Resource aglets_res = Resource.getResourceFor("aglets");

		if(tahiti_res==null || aglets_res==null){
		    return;
		}
		
		
		String key, order, precision;

		this.viewCombo.setSelectedItem(key = (String) tahiti_res.getString("tahiti.itemkey"));
		this.orderCombo.setSelectedItem(order = (String) tahiti_res.getString("tahiti.itemorder")); 
		this.precisionCombo.setSelectedItem(precision = (String) tahiti_res.getString("tahiti.itemprecision"));

		
				
				
		if (key == null ||  key.equals(bundle.getString("dialog.genprefs.key.item.event"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_LASTUPDATE);
			this.viewCombo.setSelectedItem(bundle.getString("dialog.genprefs.key.item.event"));
		} else if (key !=null && key.equals(bundle.getString("dialog.genprefs.key.item.creation"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_TIMESTAMP);
		} else if (key!=null && key.equals(bundle.getString("dialog.genprefs.key.item.class"))) {
			TahitiItem.setKeyItem(TahitiItem.KEY_CLASSNAME);
		} 

		if (order==null || bundle.getString("dialog.genprefs.order.ascent").equals(order)) {
			TahitiItem.setAscentOrder();
			this.orderCombo.setSelectedItem(bundle.getString("dialog.genprefs.order.ascent"));
		} else if (order!=null && bundle.getString("dialog.genprefs.order.descent").equals(order)) {
			TahitiItem.setDescentOrder();
		} 

		if (precision==null || precision.equals(bundle.getString("dialog.genprefs.precision.complete"))) {
			TahitiItem.setPrecision(true);
			this.precisionCombo.setSelectedItem(bundle.getString("dialog.genprefs.precision.complete"));
		} else if (precision!=null && precision.equals(bundle.getString("dialog.genprefs.precision.compact"))) {
			TahitiItem.setPrecision(false);
		} 
		
		

		this._startup.setSelected(tahiti_res.getBoolean("tahiti.startup",false));
		_startupAglet.setText(tahiti_res.getString("tahiti.startupAglets"));


		disabling();
	}

	
	public boolean windowClosing(WindowEvent ev) {
		return false;
	}
}
