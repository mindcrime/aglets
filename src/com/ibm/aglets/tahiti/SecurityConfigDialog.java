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

	/**
	 * 
	 */
	private static final long serialVersionUID = 8567450432518947469L;

	/*
	 * Singleton instance reference.
	 */
	private static SecurityConfigDialog mySelf = null;

	private static String getGrantName(final PolicyGrant grant) {
		final URIPattern codeBase = grant.getCodeBase();
		final String signers = grant.getSignerNames();
		final String owners = grant.getOwnerNames();
		final boolean cb = codeBase != null;
		final boolean s = (signers != null) && !signers.equals("");
		final boolean o = (owners != null) && !owners.equals("");
		final Vector args = new Vector();

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
	                                                               final MainWindow parent) {
		if (mySelf == null) {
			mySelf = new SecurityConfigDialog(parent);
		}

		mySelf.updateValues();
		return mySelf;
	}
	private PolicyDB _db = null;
	private final List _grantList = new List(5, false);
	private final GrantEditor _grantEditor = new GrantEditor();
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

	private SecurityConfigDialog(final MainWindow parentWindow) {
		super(parentWindow);

		// create a panel for the code source
		final JPanel codeSourcePanel = new JPanel();
		codeSourcePanel.setLayout(new BorderLayout());

		final JPanel codeSourceFieldPanel = new JPanel();
		codeSourceFieldPanel.setLayout(new GridLayout(0, 2));
		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".codeSource");
		codeSourceField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".codeSourceField");
		codeSourceFieldPanel.add(label);
		codeSourceFieldPanel.add(codeSourceField);

		signedByField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".signedByField");
		label = JComponentBuilder.createJLabel(baseKey + ".signedBy");
		codeSourceFieldPanel.add(label);
		codeSourceFieldPanel.add(signedByField);

		ownedByField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".ownedByField");
		label = JComponentBuilder.createJLabel(baseKey + ".ownedBy");
		codeSourceFieldPanel.add(label);
		codeSourceFieldPanel.add(ownedByField);

		codeBaseList = new AgletListPanel<PolicyGrant>();
		codeBaseList.setTitleBorder(translator.translate(baseKey
				+ ".codeSourceList"));
		codeBaseList.addListSelectionListener(this);
		codeBaseList.setRenderer(new PolicyGrantPermissionRenderer());

		final JButton addButton = JComponentBuilder.createJButton(baseKey
				+ ".addButton", GUICommandStrings.ADD_CODE_SOURCE_COMMAND, this);

		final JButton removeButton = JComponentBuilder.createJButton(baseKey
				+ ".removeButton", GUICommandStrings.REMOVE_CODE_SOURCE_COMMAND, this);

		final JPanel codeSourceButtonPanel = new JPanel();
		codeSourceButtonPanel.setLayout(new GridLayout(1, 2));
		codeSourceButtonPanel.add(addButton);
		codeSourceButtonPanel.add(removeButton);
		codeSourcePanel.add(codeSourceButtonPanel);

		codeSourcePanel.add(codeSourceFieldPanel, BorderLayout.NORTH);
		codeSourcePanel.add(codeBaseList, BorderLayout.CENTER);
		codeSourcePanel.add(codeSourceButtonPanel, BorderLayout.SOUTH);
		contentPanel.add(codeSourcePanel, BorderLayout.WEST);

		// make a panel for editing the single permissions
		final JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());

		// the permission will be displayed in a list
		permissionList = new AgletListPanel<PolicyPermission>();
		permissionList.setTitleBorder(translator.translate(baseKey
				+ ".permissionList"));
		permissionList.addListSelectionListener(this);
		permissionList.setRenderer(new PolicyGrantPermissionRenderer());

		// a panel with the active components of a permission
		//
		// Show the class name of the permission that is going to be edited and
		// three text components: action, codebase and objective
		final JPanel permissionPanel = new JPanel();
		permissionPanel.setLayout(new GridLayout(0, 2));
		label = JComponentBuilder.createJLabel(baseKey
				+ ".permissionClassName");
		permissionPanel.add(label);
		permissionClassName = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".permissionClassName");
		permissionClassName.setEditable(false);
		permissionPanel.add(permissionClassName);

		label = JComponentBuilder.createJLabel(baseKey
				+ ".permissionActionsLabel");
		permissionActions = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".permissionSigner");

		permissionPanel.add(label);
		permissionPanel.add(permissionActions);

		permissionObjective = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".permissionObjective");
		label = JComponentBuilder.createJLabel(baseKey
				+ ".permissionObjectiveLabel");
		permissionPanel.add(label);
		permissionPanel.add(permissionObjective);

		label = JComponentBuilder.createJLabel(baseKey
				+ ".permissionSignerLabel");
		permissionSigner = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".permissionActions");
		permissionPanel.add(label);
		permissionPanel.add(permissionSigner);

		// a panel with the button for adding and removing the permissions
		final JPanel permissionButtonPanel = new JPanel();
		permissionButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JButton addPermissionButton = JComponentBuilder.createJButton(baseKey
				+ ".addPermissionButton", GUICommandStrings.ADD_PERMISSION_COMMAND, this);

		final JButton removePermissionButton = JComponentBuilder.createJButton(baseKey
				+ ".removePermissionButton", GUICommandStrings.REMOVE_PERMISSION_COMMAND, this);
		permissionButtonPanel.add(addPermissionButton);
		permissionButtonPanel.add(removePermissionButton);

		// place each component in the permission panel
		rightPanel.add(permissionList, BorderLayout.NORTH);
		rightPanel.add(permissionPanel, BorderLayout.CENTER);
		rightPanel.add(permissionButtonPanel, BorderLayout.SOUTH);

		// all done
		contentPanel.add(rightPanel, BorderLayout.EAST);

		// load the policy db
		loadPolicyFile();
		showPolicyGrants();

		// all done
		pack();

	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		if (event == null)
			return;

		final String command = event.getActionCommand();

		try {

			if (GUICommandStrings.ADD_CODE_SOURCE_COMMAND.equals(command)
					|| GUICommandStrings.REMOVE_CODE_SOURCE_COMMAND.equals(command)) {

				final PolicyGrant grant = getGrant();

				if (grant == null)
					return;

				if (GUICommandStrings.ADD_CODE_SOURCE_COMMAND.equals(command))
					_db.addGrant(grant);
				else
					System.out.println("Not Implemented Yet!");

				showPolicyGrants();

			} else if (GUICommandStrings.ADD_PERMISSION_COMMAND.equals(command)
					|| GUICommandStrings.REMOVE_PERMISSION_COMMAND.equals(command)) {

				// get the values
				final String className = permissionClassName.getText();
				final String actions = permissionActions.getText();
				final String signers = permissionSigner.getText();
				final String objective = permissionObjective.getText();

				final PolicyGrant grant = getGrant();
				if (grant == null)
					return;

				if ((className != null)
						&& ((actions != null) || (signers != null) || (objective != null))) {
					final PolicyPermission permission = new PolicyPermission(className);
					permission.setActions(actions);
					permission.setSignerNames(signers);
					permission.setTargetName(objective);
					grant.addPermission(permission);
					_db.addGrant(grant);
					permissionList.addItem(permission);
				}

			} else
				super.actionPerformed(event);

		} catch (final MalformedURIPatternException e) {
			logger.error("Exception caught while building the policy grant", e);
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".uriError"), translator.translate(baseKey
							+ ".uriError.title"), JOptionPane.ERROR_MESSAGE);
		} catch (final ClassNotFoundException e) {
			logger.error("Exception caught while building the policy permission", e);
			JOptionPane.showMessageDialog(this, translator.translate(baseKey
					+ ".permissionError"), translator.translate(baseKey
							+ ".permissionError.title"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void addGrantPanel(final String name) {
		this.addGrantPanel(name, null);
	}

	private void addGrantPanel(final String name, final PolicyGrant grant) {
		if (!hasGrant(name)) {
			codeBaseList.addItem(grant);
			final SecurityConfigPanel panel = new SecurityConfigPanel(name, grant);

			privileges.put(name, panel);
			setting_panel.add(name, panel);
			panel.setupPanels();
		}
	}

	private PolicyGrant getGrant() throws MalformedURIPatternException {
		// get the values
		final String codebase = codeSourceField.getText();
		final String signedby = signedByField.getText();
		final String ownedby = ownedByField.getText();

		if ((codebase != null) || (signedby != null) || (ownedby != null)) {
			// produce a grant

			final PolicyGrant grant = new PolicyGrant();
			grant.setCodeBase(codebase);
			grant.setSignerNames(signedby);
			grant.setOwnerNames(ownedby);
			return grant;
		} else
			return null;

	}

	private boolean hasGrant(final String name) {
		final String[] grants = _grantList.getItems();
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
	public void itemStateChanged(final ItemEvent ev) {
		final String item = _grantList.getSelectedItem();

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
		logger.info("Loading the policy file " + file);
		final PolicyFileReader reader = new PolicyFileReader(file);
		_db = reader.getPolicyDB();
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
	public void removeGrantPanel(final int idx, final String name) {
		if (hasGrant(name)) {
			_grantList.remove(idx);
			final Object obj = privileges.get(name);

			if (obj instanceof SecurityConfigPanel) {
				final SecurityConfigPanel panel = (SecurityConfigPanel) obj;

				privileges.remove(obj);
				setting_panel.remove(panel);
			}
		}
	}

	public void save() {
		final PolicyDB db = new PolicyDB();
		final int num = _grantList.getItemCount();
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			final String item = _grantList.getItem(idx);
			final PolicyGrant grant = GrantEditor.toGrant(item);
			final Object obj = privileges.get(item);

			if (obj instanceof SecurityConfigPanel) {
				final SecurityConfigPanel panel = (SecurityConfigPanel) obj;

				panel.addPermissions(grant);
			}
			db.addGrant(grant);
		}

		try {
			final String policyFilename = PolicyFileReader.getUserPolicyFilename();
			final File policyFile = new File(policyFilename);

			if (policyFile.exists()) {
				final String backupFilename = PolicyFileWriter.getBackupFilename(policyFilename);
				final File backupFile = new File(backupFilename);

				try {
					policyFile.renameTo(backupFile);
				} catch (final SecurityException excpt) {
					System.err.println(excpt.toString());
					return;
				}
			}
			PolicyFileWriter.writePolicyDB(policyFilename, db);
		} catch (final IOException excpt) {
			System.err.println(excpt.toString());
		}
	}

	/*
	 * setup Grant Panel
	 */
	void setupGrantPanel(final BorderPanel panel) {
		final GridBagConstraints cns = new GridBagConstraints();

		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;
		cns.ipadx = cns.ipady = 3;
		cns.insets = panel.topInsets();
		cns.insets.bottom = panel.bottomInsets().bottom;

		panel.setConstraints(cns);

		cns.weighty = 0.1;
		_grantPanel = new GrantEditPanel(this, _grantList, _grantEditor);
		panel.add(_grantPanel, GridBagConstraints.REMAINDER, 1.0);
		_grantList.addItemListener(this);

		cns.insets.top = 0;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weighty = 0.0;
		panel.add(_grantEditor, GridBagConstraints.REMAINDER, 0.1);
	}

	public void showGrantPanel(final PolicyGrant grant) {
		this.showGrantPanel(getGrantName(grant));
	}

	public void showGrantPanel(final String name) {
		if (_grantList.getSelectedIndex() < 0) {
			for (int i = 0; i < _grantList.getItemCount(); i++) {
				if (name.equals(_grantList.getItem(i))) {
					_grantList.select(i);
					break;
				}
			}
		}
		layout.show(setting_panel, name);
	}

	/**
	 * Gets all the policy grants from the database and display them in the
	 * policy grant list.
	 * 
	 */
	private void showPolicyGrants() {
		if (_db == null) {
			logger.warn("The PolicyDB has not been initialized");
			return;
		}

		for (final Enumeration enumer = _db.getGrants(); enumer.hasMoreElements();) {
			final PolicyGrant currentGrant = (PolicyGrant) enumer.nextElement();
			codeBaseList.addItem(currentGrant);
		}
	}

	public void updateValues() {
		Resource.getResourceFor("aglets");
		final Enumeration e = privileges.elements();

		while (e.hasMoreElements()) {
			((SecurityConfigPanel) e.nextElement()).updateValues();
		}
	}

	/**
	 * Manages selection on the grant list.
	 */
	@Override
	public void valueChanged(final ListSelectionEvent event) {
		if (event == null)
			return;

		if (codeBaseList.getSelectedItem() != null) {

			// get the code source from the list panel
			final PolicyGrant selectedGrant = codeBaseList.getSelectedItem();

			if (selectedGrant == null) {
				codeSourceField.setText("");
				ownedByField.setText("");
				signedByField.setText("");
				permissionList.removeAllItems();
				permissionClassName.setText("");
				permissionSigner.setText("");
				permissionObjective.setText("");
				permissionActions.setText("");
			} else {
				// show the value of the code source
				codeSourceField.setText(selectedGrant.getCodeBase().toString());
				ownedByField.setText(selectedGrant.getOwnerNames());
				signedByField.setText(selectedGrant.getSignerNames());

				for (final Enumeration enumer = selectedGrant.getPermissions(); enumer.hasMoreElements();)
					permissionList.addItem((PolicyPermission) enumer.nextElement());
			}
		}

		if (permissionList.getSelectedItem() != null) {
			final PolicyPermission selectedPermission = permissionList.getSelectedItem();

			if (selectedPermission == null) {
				permissionClassName.setText("");
				permissionActions.setText("");
				permissionObjective.setText("");
				permissionSigner.setText("");
			} else {

				permissionClassName.setText(selectedPermission.getPermission().getClass().getName());
				permissionActions.setText(selectedPermission.getActions());
				permissionObjective.setText(selectedPermission.getTargetName());
				permissionSigner.setText(selectedPermission.getSignerNames());
			}
		}

	}

}
