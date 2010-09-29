package com.ibm.aglets.tahiti;

/*
 * @(#)CreateAgletDialog.java
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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

import com.ibm.awb.misc.Resource;

/**
 * Class CreateAgletDialog represents the dialog for creating a new Aglet
 * instance. The class uses a CardLayout to handle the GUI differences between
 * creating an Aglet for a system class, local class file, remote URL, and the
 * hotlist of recently used Aglet classes.
 * 
 * @version 1.04 $Date: 2009/07/28 07:04:52 $
 * @author Danny B. Lange
 */

final class CreateAgletDialog extends TahitiDialog implements ActionListener,
ListSelectionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -9026108849866684010L;
    /*
     * GUI components
     */
    protected JTextField classField = null;
    protected JTextField urlField = null;
    protected AgletListPanel<String> selectionList = null;
    protected JCheckBox reload = null;

    /**
     * Constructs a new Aglet creation dialog.
     * 
     * @param parent
     *            the parent frame.
     */
    protected CreateAgletDialog(MainWindow parent) {
	super(parent);

	// set the title
	this.setTitle(JComponentBuilder.getTitle(this.baseKey));

	// create the gui components
	this.classField = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".agletClassName");
	this.urlField = JComponentBuilder.createJTextField(20, null, this.baseKey
		+ ".agletURL");
	this.selectionList = new AgletListPanel<String>();
	this.selectionList.setTitleBorder(this.translator.translate(this.baseKey
		+ ".border"));
	this.selectionList.addListSelectionListener(this);
	this.updateList();

	// add the gui components
	JPanel northPanel1 = new JPanel();
	northPanel1.setLayout(new GridLayout(2, 2));
	JLabel label = JComponentBuilder.createJLabel(this.baseKey
		+ ".agletClassName");
	northPanel1.add(label);
	northPanel1.add(this.classField);
	label = JComponentBuilder.createJLabel(this.baseKey + ".agletURL");
	northPanel1.add(label);
	northPanel1.add(this.urlField);
	JPanel northPanel2 = new JPanel();
	northPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
	JButton addButton = JComponentBuilder.createJButton(this.baseKey
		+ ".addButton", GUICommandStrings.ADD_COMMAND, this);
	JButton removeButton = JComponentBuilder.createJButton(this.baseKey
		+ ".removeButton", GUICommandStrings.REMOVE_COMMAND, this);
	northPanel2.add(addButton);
	northPanel2.add(removeButton);

	JPanel northPanel3 = new JPanel();
	northPanel3.setLayout(new FlowLayout(FlowLayout.RIGHT));
	this.reload = JComponentBuilder.createJCheckBox(this.baseKey
		+ ".reload", true, null);
	northPanel3.add(this.reload);

	JPanel northPanel = new JPanel();
	northPanel.setLayout(new BorderLayout());
	northPanel.add(northPanel1, BorderLayout.NORTH);
	northPanel.add(northPanel3, BorderLayout.CENTER);
	northPanel.add(northPanel2, BorderLayout.SOUTH);

	JComponentBuilder.createOkCancelButtonPanel(this.baseKey
		+ ".createButton", this.baseKey + ".cancelButton", this);
	// add the components to the window
	this.add(northPanel, BorderLayout.NORTH);
	this.contentPanel.add(this.selectionList);
	// this.add(okCancelPanel, BorderLayout.SOUTH);

	this.pack();
    }

    /*
     * Creation without reloading class.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	// check params
	if (event == null)
	    return;

	// get the command
	String command = event.getActionCommand();

	// parse the command
	if (GUICommandStrings.OK_COMMAND.equals(command)) {
	    // create a new aglet
	    this.createAglet(this.reload.isSelected());
	    this.setVisible(false);
	    this.dispose();
	} else if (GUICommandStrings.ADD_COMMAND.equals(command)) {
	    // add the specified agent to the list of agents
	    this.addAgletToList();
	} else if (GUICommandStrings.REMOVE_COMMAND.equals(command)) {
	    // remove the agent from the list
	    this.removeAgletFromList();
	} else
	    super.actionPerformed(event);

    }

    /**
     * Adds a new entry to the aglet list and immediatly saves it to the aglet
     * resource.
     * 
     */
    protected void addAgletToList() {
	String name = this.urlField.getText().trim();

	if ((name.length() > 0) && (name.charAt(name.length() - 1) != '/')) {
	    name += '/';
	}
	name += this.classField.getText().trim();

	if (name.length() == 0) {
	    return;
	}

	int num = this.selectionList.getItemCount();

	for (int i = 0; i < num; i++) {
	    if (this.selectionList.getItem(i).equals(name)) {
		return;
	    }
	}
	this.selectionList.addItem(name);
	this.updateProperty();
    }

    /**
     * Creates a new aglet from the specified classname and codebase. This
     * method calls the parentwindow createAglet method.
     * 
     * @param reload
     *            true if the agent must be reloaded
     */
    synchronized void createAglet(boolean reload) {
	String classname = this.classField.getText().trim();
	String codebase = this.urlField.getText().trim();

	// System.out.println("createAglet("+codebase+","+classname+","+reload+")");
	this.getMainWindow().createAglet(codebase, classname, reload);
    }

    /**
     * Manages the event produced by the list.
     */
    public void itemStateChanged(ItemEvent ev) {
	// get the list selected in the list
	String selectedItem = this.selectionList.getSelectedItem();
	if ((selectedItem == null) || (selectedItem.length() == 0))
	    return;

	// get the pieces of the string to show
	if (selectedItem.toLowerCase().startsWith("http://")
		|| selectedItem.toLowerCase().startsWith("https://")
		|| selectedItem.toLowerCase().startsWith("atps://")
		|| selectedItem.toLowerCase().startsWith("atp://")
		|| selectedItem.toLowerCase().startsWith("file://")) {
	    int delimiter = selectedItem.lastIndexOf('/');

	    this.classField.setText(selectedItem.substring(delimiter + 1));
	    this.urlField.setText(selectedItem.substring(0, delimiter));
	} else {
	    this.classField.setText(selectedItem);
	    this.urlField.setText("");
	}
    }

    /**
     * Removes the specified item from the list of known agents.
     * 
     */
    protected void removeAgletFromList() {
	if (this.selectionList.getSelectedIndex() != -1) {
	    this.selectionList.removeItem(this.selectionList.getSelectedIndex());
	    this.classField.setText("");
	    this.urlField.setText("");
	    this.updateProperty();
	}
    }

    /**
     * Loads all resources from the saved resources and shows them into the
     * agent list
     * 
     */
    protected void updateList() {
	Resource res = Resource.getResourceFor("aglets");
	String lists = res.getString("aglets.agletsList");

	this.selectionList.removeAllItems();

	StringTokenizer st = new StringTokenizer(lists, " ", false);

	while (st.hasMoreTokens()) {
	    String token = st.nextToken();
	    this.logger.debug("Adding the item " + token + "to the agent list");
	    this.selectionList.addItem(token);
	}

    }

    /**
     * Saves the list and its content to the aglet resource.
     * 
     */
    protected void updateProperty() {
	synchronized (this.selectionList) {
	    int num = this.selectionList.getItemCount();
	    String agletsList = "";

	    for (int i = 0; i < num; i++) {
		agletsList += (this.selectionList.getItem(i) + " ");
	    }
	    Resource res = Resource.getResourceFor("aglets");

	    res.setResource("aglets.agletsList", agletsList);
	    res.save("Tahiti");
	}
    }

    /**
     * Manages events from the list.
     * 
     * @param e
     *            the event
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
	// get the list selected in the list
	String selectedItem = this.selectionList.getSelectedItem();
	if ((selectedItem == null) || (selectedItem.length() == 0))
	    return;

	// get the pieces of the string to show
	if (selectedItem.toLowerCase().startsWith("http://")
		|| selectedItem.toLowerCase().startsWith("https://")
		|| selectedItem.toLowerCase().startsWith("atps://")
		|| selectedItem.toLowerCase().startsWith("atp://")
		|| selectedItem.toLowerCase().startsWith("file://")) {
	    int delimiter = selectedItem.lastIndexOf('/');

	    this.classField.setText(selectedItem.substring(delimiter + 1));
	    this.urlField.setText(selectedItem.substring(0, delimiter));
	} else {
	    this.classField.setText(selectedItem);
	    this.urlField.setText("");
	}
    }

}
