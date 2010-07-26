package com.ibm.aglets.tahiti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import org.aglets.util.gui.JComponentBuilder;

/**
 * A class that embeds a JList to show agent data in the main Tahiti window.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class AgletListPanel<ITEM> extends JPanel {

    /**
     * The base key for the translations.
     */
    private String baseKey = this.getClass().getName();

    /**
     * The jlist that displayes the agents
     */
    private JList agletsList;

    /**
     * The JList model used to dynamically add and remove components to the
     * list.
     * 
     */
    private DefaultListModel listModel;

    /**
     * The border of the panel.
     */
    protected TitledBorder border = null;

    /**
     * Default constructor. Creates a ScrollPane and a list model and place the
     * jlist in this panel.
     * 
     */
    public AgletListPanel() {
	super();
	this.setLayout(new BorderLayout());
	this.listModel = new DefaultListModel();
	this.agletsList = new JList(this.listModel);
	this.agletsList.setBackground(Color.WHITE);
	this.agletsList.setForeground(Color.BLUE);
	this.agletsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	this.add(new JScrollPane(this.agletsList), BorderLayout.CENTER);
	this.border = new TitledBorder(JComponentBuilder.getTitle(this.baseKey));
	this.border.setTitleColor(Color.BLUE);
	this.border.setTitleJustification(TitledBorder.CENTER);
	this.setBorder(this.border);

	this.setVisible(true);
    }

    /**
     * Constructor with a parent element, used to get the size of the list
     * automatically.
     * 
     * @param parent
     *            the container of this panel
     */
    public AgletListPanel(JComponent parent) {
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
    public AgletListPanel(int elements) {
	this();
	this.agletsList.setVisibleRowCount(elements);
    }

    /**
     * A method to get the selected indexes in the current list.
     * 
     * @return an array of int that contains locations of the selected indexes.
     */
    public int[] getSelectedIndexes() {
	return this.agletsList.getSelectedIndices();
    }

    /**
     * Add a particular element in the last position.
     * 
     * @param toAdd
     *            the parameter to add to the list
     * 
     */
    public void addItem(ITEM toAdd) {
	if (toAdd == null) {
	    return;
	}
	this.listModel.addElement(toAdd);
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
    public void addItem(ITEM toAdd, int index) {
	if ((toAdd == null) || (index < 0)) {
	    return;
	}
	this.listModel.add(index, toAdd);
    }

    /**
     * Get the selected item string.
     * 
     * @return the selected item string
     */
    public ITEM getSelectedItem() {
	return (ITEM) this.agletsList.getSelectedValue();
    }

    /**
     * Gets the list of all elements as a single string, space separated.
     * 
     * @return the compound string
     */
    public LinkedList<ITEM> getSelectedItems() {
	int selected[] = this.getSelectedIndexes();

	// check if I've got a selection
	if ((selected == null) || (selected.length == 0))
	    return null;

	// now get the selection objects
	LinkedList<ITEM> selectedItems = new LinkedList<ITEM>();

	for (int i = 0; i < selected.length; i++)
	    selectedItems.add(this.getItem(i));

	// all done
	return selectedItems;
    }

    /**
     * Returns the number of selected item in the list.
     * 
     * @return the number of selected item count
     */
    public int getSelectedItemCount() {
	return this.getSelectedItemCount();
    }

    /**
     * Returns the first index selected in the list.
     * 
     * @return the first index selected in the list
     * 
     * 
     */
    public int getSelectedIndex() {
	return this.agletsList.getSelectedIndex();
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
	this.listModel.remove(index);
    }

    /**
     * Get an item from its index.
     * 
     * @param index
     *            the index of the item
     * @return the item
     * 
     */
    public ITEM getItem(int index) {
	return (ITEM) this.listModel.elementAt(index);
    }

    /**
     * Remove the selected item.
     * 
     */
    public void removeSelectedItem() {
	this.removeItem(this.getSelectedIndex());
    }

    /**
     * Removes a specific item from the list.
     * 
     * @param toRemove
     *            the item to remove
     */
    public void removeItem(ITEM toRemove) {
	if (toRemove == null)
	    return;
	else {
	    int index = this.getIndexOf(toRemove);
	    this.removeItem(index);
	}
    }

    /**
     * Provides the index of an alement.
     * 
     * @param item
     *            the item to search
     * @return the index at which the element exists
     */
    protected int getIndexOf(ITEM item) {
	if ((item == null) || (!(this.listModel.contains(item))))
	    return -1;
	else {

	    // get all the elements
	    Enumeration<ITEM> elements = (Enumeration<ITEM>) this.listModel.elements();
	    int index = 0;
	    while ((elements != null) && elements.hasMoreElements()) {
		ITEM element = elements.nextElement();

		if (item.equals(element))
		    return index;

		index++;
	    }

	    return this.listModel.indexOf(item);
	}
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
	String first = (String) this.listModel.elementAt(firstIndex);
	this.listModel.add(firstIndex, this.listModel.elementAt(secondIndex));
	this.listModel.add(secondIndex, first);
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
    public void replaceItem(ITEM newItem, int index) {
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
	this.agletsList.setSelectedIndex(index);
    }

    /**
     * Remove all items from the list.
     * 
     */
    public void removeAllItems() {
	this.listModel.removeAllElements();
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
	this.agletsList.addListSelectionListener(listener);
    }

    /**
     * Get all the available elements in the list, without regard to the
     * selection.
     * 
     * @return the string array that contains each item
     * 
     */
    public LinkedList<ITEM> getItems() {
	LinkedList<ITEM> elements = new LinkedList<ITEM>();

	for (int i = 0; i < this.listModel.size(); i++) {
	    elements.add((ITEM) this.listModel.elementAt(i));
	}

	return elements;
    }

    /**
     * Selects an entry in the list.
     * 
     * @param index
     *            the index to emphasize
     * 
     */
    public void select(int index) {
	this.agletsList.setSelectedIndex(index);
    }

    /**
     * Gets the number of elements in the list.
     * 
     * @return the item count
     * 
     */
    public int getItemCount() {
	return this.listModel.size();
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
	this.agletsList.setForeground(foreground);
	this.agletsList.setBackground(background);
    }

    public void setSelectionColors(Color background, Color foreground) {
	this.agletsList.setSelectionBackground(background);
	this.agletsList.setSelectionForeground(foreground);
    }

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(500, 100);
    }

    /**
     * Sets the title of the border for this panel.
     * 
     * @param value
     *            the (translated) value of the border
     */
    public void setTitleBorder(String value) {
	this.border.setTitle(value);
    }

    /**
     * Sets the dimension of the panel
     * 
     */
    public void setListDimension(Dimension dim) {
	this.agletsList.setSize(dim);
	this.setSize(dim);
    }

    /**
     * Sets the cell renderer for the list.
     * 
     * @param renderer
     *            the new renderer to use
     */
    public final void setRenderer(ListCellRenderer renderer) {
	this.agletsList.setCellRenderer(renderer);
    }

    /**
     * Provides the renderer actually used from the list.
     * 
     * @return the renderer
     */
    public final ListCellRenderer getRenderer() {
	return this.agletsList.getCellRenderer();
    }
}
