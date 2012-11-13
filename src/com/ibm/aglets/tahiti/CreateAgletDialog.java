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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
	protected CreateAgletDialog(final MainWindow parent) {
		super(parent);

		// set the title
		setTitle(JComponentBuilder.getTitle(baseKey));

		// create the gui components
		classField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".agletClassName");
		urlField = JComponentBuilder.createJTextField(20, null, baseKey
				+ ".agletURL");
		selectionList = new AgletListPanel<String>();
		selectionList.setTitleBorder(translator.translate(baseKey
				+ ".border"));
		selectionList.addListSelectionListener(this);
		updateList();

		// add the gui components
		final JPanel northPanel1 = new JPanel();
		northPanel1.setLayout(new GridLayout(2, 2));
		JLabel label = JComponentBuilder.createJLabel(baseKey
				+ ".agletClassName");
		northPanel1.add(label);
		northPanel1.add(classField);
		label = JComponentBuilder.createJLabel(baseKey + ".agletURL");
		northPanel1.add(label);
		northPanel1.add(urlField);
		final JPanel northPanel2 = new JPanel();
		northPanel2.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JButton addButton = JComponentBuilder.createJButton(baseKey
				+ ".addButton", GUICommandStrings.ADD_COMMAND, this);
		final JButton removeButton = JComponentBuilder.createJButton(baseKey
				+ ".removeButton", GUICommandStrings.REMOVE_COMMAND, this);
		northPanel2.add(addButton);
		northPanel2.add(removeButton);

		final JPanel northPanel3 = new JPanel();
		northPanel3.setLayout(new FlowLayout(FlowLayout.RIGHT));
		reload = JComponentBuilder.createJCheckBox(baseKey
				+ ".reload", true, null);
		northPanel3.add(reload);

		final JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
		northPanel.add(northPanel1, BorderLayout.NORTH);
		northPanel.add(northPanel3, BorderLayout.CENTER);
		northPanel.add(northPanel2, BorderLayout.SOUTH);

		JComponentBuilder.createOkCancelButtonPanel(baseKey
				+ ".createButton", baseKey + ".cancelButton", this);
		// add the components to the window
		this.add(northPanel, BorderLayout.NORTH);
		contentPanel.add(selectionList);
		// this.add(okCancelPanel, BorderLayout.SOUTH);

		pack();
	}

	/*
	 * Creation without reloading class.
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		// check params
		if (event == null)
			return;

		// get the command
		final String command = event.getActionCommand();

		// parse the command
		if (GUICommandStrings.OK_COMMAND.equals(command)) {
			// create a new aglet
			createAglet(reload.isSelected());
			setVisible(false);
			dispose();
		} else if (GUICommandStrings.ADD_COMMAND.equals(command)) {
			// add the specified agent to the list of agents
			addAgletToList();
		} else if (GUICommandStrings.REMOVE_COMMAND.equals(command)) {
			// remove the agent from the list
			removeAgletFromList();
		} else
			super.actionPerformed(event);

	}

	/**
	 * Adds a new entry to the aglet list and immediately saves it to the aglet
	 * resource.
	 * 
	 */
	protected void addAgletToList() {
		final String urlFieldText = urlField.getText().trim();
		final String classFieldText = classField.getText().trim();

		// minimal validation for now
		if ((classFieldText.length() == 0) || (urlFieldText.length() == 0)) {
			return;
		}

		// URI of the code base
		URI cbUri = null;
		// string representation of the URI of the aglet's class
		String itemText = null;
		try {
			// parse the entered code base URI 
			cbUri = new URL(urlFieldText).toURI();
			// convert complete URI to text via URL form
			itemText = new URI(
					cbUri.getScheme(),
					cbUri.getUserInfo(),
					cbUri.getHost(),
					cbUri.getPort(),
					cbUri.getPath(),
					classFieldText,
					cbUri.getFragment()
			).normalize().toURL().toString();
		} catch (final MalformedURLException ex) {
			return;
		} catch (final URISyntaxException ex) {
			return;
		}

		// avoid adding a duplicate
		final int num = selectionList.getItemCount();

		for (int i = 0; i < num; i++) {
			if (selectionList.getItem(i).equals(itemText)) {
				return;
			}
		}
		selectionList.addItem(itemText);
		updateProperty();
	}

	/**
	 * Creates a new aglet from the specified classname and codebase. This
	 * method calls the parentwindow createAglet method.
	 * 
	 * @param reload
	 *            true if the agent must be reloaded
	 */
	synchronized void createAglet(final boolean reload) {
		final String classname = classField.getText().trim();
		final String codebase = urlField.getText().trim();

		// System.out.println("createAglet("+codebase+","+classname+","+reload+")");
		getMainWindow().createAglet(codebase, classname, reload);
	}

	/**
	 * Removes the specified item from the list of known agents.
	 * 
	 */
	protected void removeAgletFromList() {
		if (selectionList.getSelectedIndex() != -1) {
			selectionList.removeItem(selectionList.getSelectedIndex());
			classField.setText("");
			urlField.setText("");
			updateProperty();
		}
	}

	/**
	 * Loads all resources from the saved resources and shows them into the
	 * agent list
	 * 
	 */
	protected void updateList() {
		final Resource res = Resource.getResourceFor("aglets");
		final String lists = res.getString("aglets.agletsList");

		selectionList.removeAllItems();

		final String[] sa = lists.split(" ");

		for (final String s: sa) {
			logger.debug("Adding to the agent list: " + s);
			selectionList.addItem(s);
		}

	}

	/**
	 * Saves the list and its content to the aglet resource.
	 * 
	 */
	protected void updateProperty() {
		synchronized (selectionList) {
			final int num = selectionList.getItemCount();
			String agletsList = "";

			for (int i = 0; i < num; i++) {
				agletsList += (selectionList.getItem(i) + " ");
			}
			final Resource res = Resource.getResourceFor("aglets");

			res.setResource("aglets.agletsList", agletsList);
			res.save("Tahiti");
		}
	}

	/**
	 * Manages events from the list. Is called both on mousedown and mouseup.
	 * 
	 * @param e
	 *            the event
	 */
	@Override
	public void valueChanged(final ListSelectionEvent e) {
		// get the list selected in the list
		final String selectedItem = selectionList.getSelectedItem();
		if ((selectedItem == null) || (selectedItem.length() == 0)) {
			return;
		}

		URI selectedUri = null;
		String classFieldText = null;
		String urlFieldText = null;
		try {
			// parse the URI from the user interface
			selectedUri = new URL(selectedItem).toURI();
			// derive a URI that omits the query part
			urlFieldText = new URI(
					selectedUri.getScheme(),
					selectedUri.getUserInfo(),
					selectedUri.getHost(),
					selectedUri.getPort(),
					selectedUri.getPath(),
					null,
					selectedUri.getFragment()
			).normalize().toURL().toString();
			// retrieve just the query part
			classFieldText = selectedUri.getQuery();
		} catch (final MalformedURLException ex) {
			classFieldText = selectedItem;
			urlFieldText = null;
		} catch (final URISyntaxException ex) {
			classFieldText = selectedItem;
			urlFieldText = null;
		}
		classField.setText(classFieldText);
		urlField.setText(urlFieldText);
	}

}
