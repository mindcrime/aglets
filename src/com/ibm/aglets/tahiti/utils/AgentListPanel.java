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
     * The jlist that displayes the agents
     */
    private JList _agentList;

    /**
     * The JList model used to dynamically add and remove components to the
     * list.
     * 
     */
    private DefaultListModel _listModel;

    /**
     * Default constructor. Creates a ScrollPane and a list model and place the
     * jlist in this panel.
     * 
     */
    public AgentListPanel() {
	super();
	this._listModel = new DefaultListModel();
	this._agentList = new JList(this._listModel);
	this._agentList.setBackground(Color.WHITE);
	this._agentList.setForeground(Color.BLUE);
	this._agentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	// this.setLayout(new BorderLayout());
	this.add(new JScrollPane(this._agentList));

	this.setVisible(true);
    }

    /**
     * Constructor with a parent element, used to get the size of the list
     * automatically.
     * 
     * @param parent
     *            the container of this panel
     */
    public AgentListPanel(JComponent parent) {
	this();
	this.setSize(parent.getSize());
    }

    /**
     * Construct the panel setting the maximum visible elements.
     * 
     * @param elements
     *            the number of elements the list will show
     * 
     */
    public AgentListPanel(int elements) {
	this();
	this._agentList.setVisibleRowCount(elements);
    }

    /**
     * A method to get the selected indexes in the current list.
     * 
     * @return an array of int that contains locations of the selected indexes.
     */
    public int[] getSelectedIndexes() {
	return this._agentList.getSelectedIndices();
    }

    /**
     * Add a particular element in the last position.
     * 
     * @param toAdd
     *            the parameter to add to the list
     * 
     */
    public void addItem(String toAdd) {
	if (toAdd == null) {
	    return;
	}
	this._listModel.addElement(toAdd);
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
    public void addItem(String toAdd, int index) {
	if ((toAdd == null) || (index < 0)) {
	    return;
	}
	this._listModel.add(index, toAdd);
    }

    /**
     * Get the selected item string.
     * 
     * @return the selected item string
     */
    public String getSelectedItem() {
	return (String) this._agentList.getSelectedValue();
    }

    /**
     * Gets the list of all elements as a single string, space separated.
     * 
     * @return the compound string
     */
    public String getAllItems() {
	StringBuffer buf = new StringBuffer(100);
	Enumeration elements = this._listModel.elements();
	while ((elements != null) && elements.hasMoreElements()) {
	    buf.append(" ");
	    buf.append((String) elements.nextElement());
	}

	return new String(buf);
    }

    /**
     * Returns the first index selected in the list.
     * 
     * @return the first index selected in the list
     * 
     * 
     */
    public int getSelectedIndex() {
	return this._agentList.getSelectedIndex();
    }

    /**
     * Remove an item from the list by its index.
     * 
     * @param index
     *            the index of the element to remove
     * 
     * 
     */
    public void removeItem(int index) {
	this._listModel.remove(index);
    }

    /**
     * Get an item from its index.
     * 
     * @param index
     *            the index of the item
     * @return the item
     * 
     */
    public String getItem(int index) {
	return (String) this._listModel.elementAt(index);
    }

    /**
     * Remove the selected item.
     * 
     */
    public void removeSelectedItem() {
	this.removeItem(this.getSelectedIndex());
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
    public void replaceItem(int firstIndex, int secondIndex) {
	if ((firstIndex < 0) || (secondIndex < 0)
		|| (firstIndex == secondIndex)) {
	    return;
	}

	// get the element at the first index
	String first = (String) this._listModel.elementAt(firstIndex);
	this._listModel.add(firstIndex, this._listModel.elementAt(secondIndex));
	this._listModel.add(secondIndex, first);
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
    public void replaceItem(String newItem, int index) {
	this.addItem(newItem, index);
    }

    /**
     * Selects a specific item in the list
     * 
     * @param index
     *            the index of the element to select
     * 
     * 
     */
    public void selectItem(int index) {
	this._agentList.setSelectedIndex(index);
    }

    /**
     * Remove all items from the list.
     * 
     */
    public void removeAllItems() {
	this._listModel.removeAllElements();
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
    public void addListSelectionListener(ListSelectionListener listener) {
	this._agentList.addListSelectionListener(listener);
    }

    /**
     * Get all items as a string array.
     * 
     * @return the string array that contains each item
     * 
     */
    public String[] getItems() {
	String ret[] = new String[this._listModel.size()];
	for (int i = 0; i < this._listModel.size(); i++) {
	    ret[i] = (String) this._listModel.elementAt(i);
	}

	return ret;
    }

    /**
     * Selects an entry in the list.
     * 
     * @param index
     *            the index to emphasize
     * 
     */
    public void select(int index) {
	this._agentList.setSelectedIndex(index);
    }

    /**
     * Gets the number of elements in the list.
     * 
     * @return the item count
     * 
     */
    public int getItemCount() {
	return this._listModel.size();
    }

    /**
     * A method to set up colors of the list.
     * 
     * @param background
     *            the background color
     * @param foregroung
     *            the foreground color
     */
    public void setColors(Color background, Color foreground) {
	this._agentList.setForeground(foreground);
	this._agentList.setBackground(background);
    }

    public void setSelectionColors(Color background, Color foreground) {
	this._agentList.setSelectionBackground(background);
	this._agentList.setSelectionForeground(foreground);
    }

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(500, 100);
    }

    /**
     * Sets the dimension of the panel
     * 
     */
    public void setListDimension(Dimension dim) {
	this._agentList.setSize(dim);
	this.setSize(dim);
    }
}
