package com.ibm.aglets.tahiti;

/*
 * @(#)SecurityConfigDialog.java
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
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.aglets.security.PolicyDB;
import com.ibm.aglets.security.PolicyFileReader;
import com.ibm.aglets.security.PolicyFileWriter;
import com.ibm.aglets.security.PolicyGrant;
import com.ibm.aglets.security.PolicyPermission;
import com.ibm.awb.misc.MalformedURIPatternException;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.URIPattern;

/**
 * Class SecurityConfigDialog represents the dialog for
 * 
 * @version 1.03 96/04/15
 * @author Danny B. Lange
 */

// TODO testare a modo

final class SecurityConfigDialog extends TahitiDialog implements
ActionListener, ItemListener, ListSelectionListener {

    /*
     * Singleton instance reference.
     */
    private static SecurityConfigDialog mySelf = null;

    private PolicyDB _db = null;

    private List _grantList = new List(5, false);
    private GrantEditor _grantEditor = new GrantEditor();
    private GrantEditPanel _grantPanel = null;
    GridBagPanel _panel = new GridBagPanel();
    Panel setting_panel = new Panel();
    CardLayout layout = new CardLayout();
    Hashtable privileges = new Hashtable();

    /**
     * A list of stored code bases.
     */
    private AgletListPanel<PolicyGrant> codeBaseList = null;

    /**
     * A list of permissions.
     */
    private AgletListPanel<PolicyPermission> permissionList = null;

    /**
     * Text fields related to the code bases.
     */
    private JTextField codeSourceField = null;
    private JTextField signedByField = null;
    private JTextField ownedByField = null;

    /**
     * Permission properties that the user can edit.
     */
    private JTextField permissionSigner = null;
    private JTextField permissionActions = null;
    private JTextField permissionObjective = null;
    private JTextField permissionClassName = null;

    private SecurityConfigDialog(MainWindow parentWindow) {
	super(parentWindow);

	// create a panel for the code source
	JPanel codeSourcePanel = new JPanel();
	codeSourcePanel.setLayout(new BorderLayout());

	JPanel codeSourceFieldPanel = new JPanel();
	codeSourceFieldPanel.setLayout(new GridLayout(0, 2));
	JLabel label = JComponentBuilder.createJLabel(this.baseKey
		+ ".codeSource");
	this.codeSourceField = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".codeSourceField");
	codeSourceFieldPanel.add(label);
	codeSourceFieldPanel.add(this.codeSourceField);

	this.signedByField = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".signedByField");
	label = JComponentBuilder.createJLabel(this.baseKey + ".signedBy");
	codeSourceFieldPanel.add(label);
	codeSourceFieldPanel.add(this.signedByField);

	this.ownedByField = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".ownedByField");
	label = JComponentBuilder.createJLabel(this.baseKey + ".ownedBy");
	codeSourceFieldPanel.add(label);
	codeSourceFieldPanel.add(this.ownedByField);

	this.codeBaseList = new AgletListPanel<PolicyGrant>();
	this.codeBaseList.setTitleBorder(this.translator.translate(this.baseKey
		+ ".codeSourceList"));
	this.codeBaseList.addListSelectionListener(this);
	this.codeBaseList.setRenderer(new PolicyGrantPermissionRenderer());

	JButton addButton = JComponentBuilder.createJButton(this.baseKey
		+ ".addButton", GUICommandStrings.ADD_CODE_SOURCE_COMMAND, this);

	JButton removeButton = JComponentBuilder.createJButton(this.baseKey
		+ ".removeButton", GUICommandStrings.REMOVE_CODE_SOURCE_COMMAND, this);

	JPanel codeSourceButtonPanel = new JPanel();
	codeSourceButtonPanel.setLayout(new GridLayout(1, 2));
	codeSourceButtonPanel.add(addButton);
	codeSourceButtonPanel.add(removeButton);
	codeSourcePanel.add(codeSourceButtonPanel);

	codeSourcePanel.add(codeSourceFieldPanel, BorderLayout.NORTH);
	codeSourcePanel.add(this.codeBaseList, BorderLayout.CENTER);
	codeSourcePanel.add(codeSourceButtonPanel, BorderLayout.SOUTH);
	this.contentPanel.add(codeSourcePanel, BorderLayout.WEST);

	// make a panel for editing the single permissions
	JPanel rightPanel = new JPanel();
	rightPanel.setLayout(new BorderLayout());

	// the permission will be displayed in a list
	this.permissionList = new AgletListPanel<PolicyPermission>();
	this.permissionList.setTitleBorder(this.translator.translate(this.baseKey
		+ ".permissionList"));
	this.permissionList.addListSelectionListener(this);
	this.permissionList.setRenderer(new PolicyGrantPermissionRenderer());

	// a panel with the active components of a permission
	//
	// Show the class name of the permission that is going to be edited and
	// three text components: action, codebase and objective
	JPanel permissionPanel = new JPanel();
	permissionPanel.setLayout(new GridLayout(0, 2));
	label = JComponentBuilder.createJLabel(this.baseKey
		+ ".permissionClassName");
	permissionPanel.add(label);
	this.permissionClassName = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".permissionClassName");
	this.permissionClassName.setEditable(false);
	permissionPanel.add(this.permissionClassName);

	label = JComponentBuilder.createJLabel(this.baseKey
		+ ".permissionActionsLabel");
	this.permissionActions = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".permissionSigner");

	permissionPanel.add(label);
	permissionPanel.add(this.permissionActions);

	this.permissionObjective = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".permissionObjective");
	label = JComponentBuilder.createJLabel(this.baseKey
		+ ".permissionObjectiveLabel");
	permissionPanel.add(label);
	permissionPanel.add(this.permissionObjective);

	label = JComponentBuilder.createJLabel(this.baseKey
		+ ".permissionSignerLabel");
	this.permissionSigner = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".permissionActions");
	permissionPanel.add(label);
	permissionPanel.add(this.permissionSigner);

	// a panel with the button for adding and removing the permissions
	JPanel permissionButtonPanel = new JPanel();
	permissionButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JButton addPermissionButton = JComponentBuilder.createJButton(this.baseKey
		+ ".addPermissionButton", GUICommandStrings.ADD_PERMISSION_COMMAND, this);

	JButton removePermissionButton = JComponentBuilder.createJButton(this.baseKey
		+ ".removePermissionButton", GUICommandStrings.REMOVE_PERMISSION_COMMAND, this);
	permissionButtonPanel.add(addPermissionButton);
	permissionButtonPanel.add(removePermissionButton);

	// place each component in the permission panel
	rightPanel.add(this.permissionList, BorderLayout.NORTH);
	rightPanel.add(permissionPanel, BorderLayout.CENTER);
	rightPanel.add(permissionButtonPanel, BorderLayout.SOUTH);

	// all done
	this.contentPanel.add(rightPanel, BorderLayout.EAST);

	// load the policy db
	this.loadPolicyFile();
	this.showPolicyGrants();

	// all done
	this.pack();

    }

    public void addGrantPanel(String name) {
	this.addGrantPanel(name, null);
    }

    private void addGrantPanel(String name, PolicyGrant grant) {
	if (!this.hasGrant(name)) {
	    this.codeBaseList.addItem(grant);
	    SecurityConfigPanel panel = new SecurityConfigPanel(name, grant);

	    this.privileges.put(name, panel);
	    this.setting_panel.add(name, panel);
	    panel.setupPanels();
	}
    }

    private static String getGrantName(PolicyGrant grant) {
	final URIPattern codeBase = grant.getCodeBase();
	final String signers = grant.getSignerNames();
	final String owners = grant.getOwnerNames();
	final boolean cb = codeBase != null;
	final boolean s = (signers != null) && !signers.equals("");
	final boolean o = (owners != null) && !owners.equals("");
	Vector args = new Vector();

	if (cb || s || o) {
	    args.addElement(codeBase.toString());
	}
	if (s || o) {
	    args.addElement(signers);
	}
	if (o) {
	    args.addElement(owners);
	}
	return EditorPanel.toText(args);
    }

    /**
     * Singleton method to get the instance of this window.
     * 
     * @param parent
     *            the window onwer of this dialog
     * @return the instance of the dialog
     */
    protected static synchronized SecurityConfigDialog getInstance(
                                                                   MainWindow parent) {
	if (mySelf == null) {
	    mySelf = new SecurityConfigDialog(parent);
	}

	mySelf.updateValues();
	return mySelf;
    }

    private boolean hasGrant(String name) {
	String[] grants = this._grantList.getItems();
	int i = 0;

	for (i = 0; i < grants.length; i++) {
	    if (name.equals(grants[i])) {
		return true;
	    }
	}
	return false;
    }

    /*
     * Handles the events
     */
    @Override
    public void itemStateChanged(ItemEvent ev) {
	String item = this._grantList.getSelectedItem();

	if (item != null) {
	    this.showGrantPanel(item);
	}
    }

    /**
     * Loads the policy file and initializes the db.
     * 
     */
    private void loadPolicyFile() {
	final String file = PolicyFileReader.getUserPolicyFilename();
	this.logger.info("Loading the policy file " + file);
	PolicyFileReader reader = new PolicyFileReader(file);
	this._db = reader.getPolicyDB();
    }

    /*
     * Layouts all components.
     */
    /*
     * void makePanel() { GridBagConstraints cns = new GridBagConstraints();
     * 
     * cns.weightx = 0.0; cns.weighty = 1.0; cns.fill =
     * GridBagConstraints.VERTICAL; cns.ipadx = cns.ipady = 5; cns.insets = new
     * Insets(10, 5, 5, 5);
     * 
     * _panel.setConstraints(cns); BorderPanel grantPanel = new
     * BorderPanel(LABEL_GRANT_PANEL);
     * 
     * _panel.add(grantPanel, 1, 0.0); setupGrantPanel(grantPanel);
     * 
     * cns.fill = GridBagConstraints.BOTH; _panel.add(setting_panel,
     * GridBagConstraints.REMAINDER, 1.0);
     * 
     * setting_panel.setLayout(layout);
     * 
     * PolicyGrant firstGrant = null; Enumeration grants = _db.getGrants();
     * 
     * while (grants.hasMoreElements()) { Object obj = grants.nextElement();
     * 
     * if (obj instanceof PolicyGrant) { PolicyGrant grant = (PolicyGrant)obj;
     * 
     * if (firstGrant == null) { firstGrant = grant; } addGrantPanel(grant); } }
     * 
     * showGrantPanel(firstGrant); }
     */
    public void removeGrantPanel(int idx, String name) {
	if (this.hasGrant(name)) {
	    this._grantList.remove(idx);
	    Object obj = this.privileges.get(name);

	    if (obj instanceof SecurityConfigPanel) {
		SecurityConfigPanel panel = (SecurityConfigPanel) obj;

		this.privileges.remove(obj);
		this.setting_panel.remove(panel);
	    }
	}
    }

    public void save() {
	PolicyDB db = new PolicyDB();
	final int num = this._grantList.getItemCount();
	int idx = 0;

	for (idx = 0; idx < num; idx++) {
	    final String item = this._grantList.getItem(idx);
	    PolicyGrant grant = GrantEditor.toGrant(item);
	    Object obj = this.privileges.get(item);

	    if (obj instanceof SecurityConfigPanel) {
		SecurityConfigPanel panel = (SecurityConfigPanel) obj;

		panel.addPermissions(grant);
	    }
	    db.addGrant(grant);
	}

	try {
	    final String policyFilename = PolicyFileReader.getUserPolicyFilename();
	    File policyFile = new File(policyFilename);

	    if (policyFile.exists()) {
		final String backupFilename = PolicyFileWriter.getBackupFilename(policyFilename);
		File backupFile = new File(backupFilename);

		try {
		    policyFile.renameTo(backupFile);
		} catch (SecurityException excpt) {
		    System.err.println(excpt.toString());
		    return;
		}
	    }
	    PolicyFileWriter.writePolicyDB(policyFilename, db);
	} catch (IOException excpt) {
	    System.err.println(excpt.toString());
	}
    }

    /*
     * setup Grant Panel
     */
    void setupGrantPanel(BorderPanel panel) {
	GridBagConstraints cns = new GridBagConstraints();

	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;
	cns.ipadx = cns.ipady = 3;
	cns.insets = panel.topInsets();
	cns.insets.bottom = panel.bottomInsets().bottom;

	panel.setConstraints(cns);

	cns.weighty = 0.1;
	this._grantPanel = new GrantEditPanel(this, this._grantList, this._grantEditor);
	panel.add(this._grantPanel, GridBagConstraints.REMAINDER, 1.0);
	this._grantList.addItemListener(this);

	cns.insets.top = 0;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.weighty = 0.0;
	panel.add(this._grantEditor, GridBagConstraints.REMAINDER, 0.1);
    }

    public void showGrantPanel(PolicyGrant grant) {
	this.showGrantPanel(getGrantName(grant));
    }

    public void showGrantPanel(String name) {
	if (this._grantList.getSelectedIndex() < 0) {
	    for (int i = 0; i < this._grantList.getItemCount(); i++) {
		if (name.equals(this._grantList.getItem(i))) {
		    this._grantList.select(i);
		    break;
		}
	    }
	}
	this.layout.show(this.setting_panel, name);
    }

    public void updateValues() {
	Resource.getResourceFor("aglets");
	Enumeration e = this.privileges.elements();

	while (e.hasMoreElements()) {
	    ((SecurityConfigPanel) e.nextElement()).updateValues();
	}
    }

    /**
     * Manages selection on the grant list.
     */
    @Override
    public void valueChanged(ListSelectionEvent event) {
	if (event == null)
	    return;

	if (this.codeBaseList.getSelectedItem() != null) {

	    // get the code source from the list panel
	    PolicyGrant selectedGrant = this.codeBaseList.getSelectedItem();

	    if (selectedGrant == null) {
		this.codeSourceField.setText("");
		this.ownedByField.setText("");
		this.signedByField.setText("");
		this.permissionList.removeAllItems();
		this.permissionClassName.setText("");
		this.permissionSigner.setText("");
		this.permissionObjective.setText("");
		this.permissionActions.setText("");
	    } else {
		// show the value of the code source
		this.codeSourceField.setText(selectedGrant.getCodeBase().toString());
		this.ownedByField.setText(selectedGrant.getOwnerNames());
		this.signedByField.setText(selectedGrant.getSignerNames());

		for (Enumeration enumer = selectedGrant.getPermissions(); enumer.hasMoreElements();)
		    this.permissionList.addItem((PolicyPermission) enumer.nextElement());
	    }
	}

	if (this.permissionList.getSelectedItem() != null) {
	    PolicyPermission selectedPermission = this.permissionList.getSelectedItem();

	    if (selectedPermission == null) {
		this.permissionClassName.setText("");
		this.permissionActions.setText("");
		this.permissionObjective.setText("");
		this.permissionSigner.setText("");
	    } else {

		this.permissionClassName.setText(selectedPermission.getPermission().getClass().getName());
		this.permissionActions.setText(selectedPermission.getActions());
		this.permissionObjective.setText(selectedPermission.getTargetName());
		this.permissionSigner.setText(selectedPermission.getSignerNames());
	    }
	}

    }

    /**
     * Gets all the policy grants from the database and display them in the
     * policy grant list.
     * 
     */
    private void showPolicyGrants() {
	if (this._db == null) {
	    this.logger.warn("The PolicyDB has not been initialized");
	    return;
	}

	for (Enumeration enumer = this._db.getGrants(); enumer.hasMoreElements();) {
	    PolicyGrant currentGrant = (PolicyGrant) enumer.nextElement();
	    this.codeBaseList.addItem(currentGrant);
	}
    }

    private PolicyGrant getGrant() throws MalformedURIPatternException {
	// get the values
	String codebase = this.codeSourceField.getText();
	String signedby = this.signedByField.getText();
	String ownedby = this.ownedByField.getText();

	if ((codebase != null) || (signedby != null) || (ownedby != null)) {
	    // produce a grant

	    PolicyGrant grant = new PolicyGrant();
	    grant.setCodeBase(codebase);
	    grant.setSignerNames(signedby);
	    grant.setOwnerNames(ownedby);
	    return grant;
	} else
	    return null;

    }

    @Override
    public void actionPerformed(ActionEvent event) {
	if (event == null)
	    return;

	String command = event.getActionCommand();

	try {

	    if (GUICommandStrings.ADD_CODE_SOURCE_COMMAND.equals(command)
		    || GUICommandStrings.REMOVE_CODE_SOURCE_COMMAND.equals(command)) {

		PolicyGrant grant = this.getGrant();

		if (grant == null)
		    return;

		if (GUICommandStrings.ADD_CODE_SOURCE_COMMAND.equals(command))
		    this._db.addGrant(grant);
		else
		    System.out.println("Not Implemented Yet!");

		this.showPolicyGrants();

	    } else if (GUICommandStrings.ADD_PERMISSION_COMMAND.equals(command)
		    || GUICommandStrings.REMOVE_PERMISSION_COMMAND.equals(command)) {

		// get the values
		String className = this.permissionClassName.getText();
		String actions = this.permissionActions.getText();
		String signers = this.permissionSigner.getText();
		String objective = this.permissionObjective.getText();

		PolicyGrant grant = this.getGrant();
		if (grant == null)
		    return;

		if ((className != null)
			&& ((actions != null) || (signers != null) || (objective != null))) {
		    PolicyPermission permission = new PolicyPermission(className);
		    permission.setActions(actions);
		    permission.setSignerNames(signers);
		    permission.setTargetName(objective);
		    grant.addPermission(permission);
		    this._db.addGrant(grant);
		    this.permissionList.addItem(permission);
		}

	    } else
		super.actionPerformed(event);

	} catch (MalformedURIPatternException e) {
	    this.logger.error("Exception caught while building the policy grant", e);
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".uriError"), this.translator.translate(this.baseKey
			    + ".uriError.title"), JOptionPane.ERROR_MESSAGE);
	} catch (ClassNotFoundException e) {
	    this.logger.error("Exception caught while building the policy permission", e);
	    JOptionPane.showMessageDialog(this, this.translator.translate(this.baseKey
		    + ".permissionError"), this.translator.translate(this.baseKey
			    + ".permissionError.title"), JOptionPane.ERROR_MESSAGE);
	}
    }

}
