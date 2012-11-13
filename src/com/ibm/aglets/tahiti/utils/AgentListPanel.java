/*
 * Created on Oct 2, 2004
 *
 * @author Luca Ferrari
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * A class that embeds a JList to show agent data in the main Tahiti window.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class AgentListPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3849702890166914054L;

	/**
	 * The JList that displays the agents
	 */
	private final JList _agentList;

	/**
	 * The JList model used to dynamically add and remove components to the
	 * list.
	 * 
	 */
	private final DefaultListModel _listModel;

	/**
	 * Default constructor. Creates a ScrollPane and a list model and place the
	 * JList in this panel.
	 * 
	 */
	public AgentListPanel() {
		super();
		_listModel = new DefaultListModel();
		_agentList = new JList(_listModel);
		_agentList.setBackground(Color.WHITE);
		_agentList.setForeground(Color.BLUE);
		_agentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// this.setLayout(new BorderLayout());
		this.add(new JScrollPane(_agentList));

		setVisible(true);
	}

	/**
	 * Construct the panel setting the maximum visible elements.
	 * 
	 * @param elements
	 *            the number of elements the list will show
	 * 
	 */
	public AgentListPanel(final int elements) {
		this();
		_agentList.setVisibleRowCount(elements);
	}

	/**
	 * Constructor with a parent element, used to get the size of the list
	 * automatically.
	 * 
	 * @param parent
	 *            the container of this panel
	 */
	public AgentListPanel(final JComponent parent) {
		this();
		this.setSize(parent.getSize());
	}

	/**
	 * Add a particular element in the last position.
	 * 
	 * @param toAdd
	 *            the parameter to add to the list
	 * 
	 */
	public void addItem(final String toAdd) {
		if (toAdd == null) {
			return;
		}
		_listModel.addElement(toAdd);
	}

	/**
	 * Add a particular element at the specified index.
	 * 
	 * @param toAdd
	 *            the element to be added
	 * @param index
	 *            the index to which the element must be placed
	 * 
	 */
	public void addItem(final String toAdd, final int index) {
		if ((toAdd == null) || (index < 0)) {
			return;
		}
		_listModel.add(index, toAdd);
	}

	/**
	 * Adds the listlistener to the list object.
	 * 
	 * @param listener
	 *            the listener to add
	 * 
	 * 
	 * 
	 */
	public void addListSelectionListener(final ListSelectionListener listener) {
		_agentList.addListSelectionListener(listener);
	}

	/**
	 * Gets the list of all elements as a single string, space separated.
	 * 
	 * @return the compound string
	 */
	public String getAllItems() {
		final StringBuffer buf = new StringBuffer(100);
		final Enumeration elements = _listModel.elements();
		while ((elements != null) && elements.hasMoreElements()) {
			buf.append(" ");
			buf.append((String) elements.nextElement());
		}

		return new String(buf);
	}

	/**
	 * Get an item from its index.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the item
	 * 
	 */
	public String getItem(final int index) {
		return (String) _listModel.elementAt(index);
	}

	/**
	 * Gets the number of elements in the list.
	 * 
	 * @return the item count
	 * 
	 */
	public int getItemCount() {
		return _listModel.size();
	}

	/**
	 * Get all items as a string array.
	 * 
	 * @return the string array that contains each item
	 * 
	 */
	public String[] getItems() {
		final String ret[] = new String[_listModel.size()];
		for (int i = 0; i < _listModel.size(); i++) {
			ret[i] = (String) _listModel.elementAt(i);
		}

		return ret;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 100);
	}

	/**
	 * Returns the first index selected in the list.
	 * 
	 * @return the first index selected in the list
	 * 
	 * 
	 */
	public int getSelectedIndex() {
		return _agentList.getSelectedIndex();
	}

	/**
	 * A method to get the selected indexes in the current list.
	 * 
	 * @return an array of int that contains locations of the selected indexes.
	 */
	public int[] getSelectedIndexes() {
		return _agentList.getSelectedIndices();
	}

	/**
	 * Get the selected item string.
	 * 
	 * @return the selected item string
	 */
	public String getSelectedItem() {
		return (String) _agentList.getSelectedValue();
	}

	/**
	 * Remove all items from the list.
	 * 
	 */
	public void removeAllItems() {
		_listModel.removeAllElements();
	}

	/**
	 * Remove an item from the list by its index.
	 * 
	 * @param index
	 *            the index of the element to remove
	 * 
	 * 
	 */
	public void removeItem(final int index) {
		_listModel.remove(index);
	}

	/**
	 * Remove the selected item.
	 * 
	 */
	public void removeSelectedItem() {
		removeItem(getSelectedIndex());
	}

	/**
	 * Replaces two elements in the list.
	 * 
	 * @param firstIndex
	 *            the index of the first element to move
	 * @param secondIndex
	 *            the element of the second index to move
	 * 
	 */
	public void replaceItem(final int firstIndex, final int secondIndex) {
		if ((firstIndex < 0) || (secondIndex < 0)
				|| (firstIndex == secondIndex)) {
			return;
		}

		// get the element at the first index
		final String first = (String) _listModel.elementAt(firstIndex);
		_listModel.add(firstIndex, _listModel.elementAt(secondIndex));
		_listModel.add(secondIndex, first);
	}

	/**
	 * Replaces the item at the specified index with the new one.
	 * 
	 * @param newItem
	 *            the new item to place in the list
	 * @param index
	 *            the index to which the item must be placed.
	 * 
	 * 
	 */
	public void replaceItem(final String newItem, final int index) {
		this.addItem(newItem, index);
	}

	/**
	 * Selects an entry in the list.
	 * 
	 * @param index
	 *            the index to emphasize
	 * 
	 */
	public void select(final int index) {
		_agentList.setSelectedIndex(index);
	}

	/**
	 * Selects a specific item in the list
	 * 
	 * @param index
	 *            the index of the element to select
	 * 
	 * 
	 */
	public void selectItem(final int index) {
		_agentList.setSelectedIndex(index);
	}

	/**
	 * A method to set up colors of the list.
	 * 
	 * @param background
	 *            the background color
	 * @param foreground
	 *            the foreground color
	 */
	public void setColors(final Color background, final Color foreground) {
		_agentList.setForeground(foreground);
		_agentList.setBackground(background);
	}

	/**
	 * Sets the dimension of the panel
	 * 
	 */
	public void setListDimension(final Dimension dim) {
		_agentList.setSize(dim);
		this.setSize(dim);
	}

	public void setSelectionColors(final Color background, final Color foreground) {
		_agentList.setSelectionBackground(background);
		_agentList.setSelectionForeground(foreground);
	}
}
