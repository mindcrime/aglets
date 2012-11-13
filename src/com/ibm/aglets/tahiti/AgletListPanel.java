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

import net.sourceforge.aglets.util.gui.JComponentBuilder;

/**
 * A class that embeds a JList to show agent data in the main Tahiti window.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class AgletListPanel<ITEM> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2303995451900847091L;

	/**
	 * The base key for the translations.
	 */
	private final String baseKey = this.getClass().getName();

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
		setLayout(new BorderLayout());
		this.listModel = new DefaultListModel();
		this.agletsList = new JList(this.listModel);
		this.agletsList.setBackground(Color.WHITE);
		this.agletsList.setForeground(Color.BLUE);
		this.agletsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.add(new JScrollPane(this.agletsList), BorderLayout.CENTER);
		this.border = new TitledBorder(JComponentBuilder.getTitle(this.baseKey));
		this.border.setTitleColor(Color.BLUE);
		this.border.setTitleJustification(TitledBorder.CENTER);
		setBorder(this.border);

		setVisible(true);
	}

	/**
	 * Construct the panel setting the maximum visible elements.
	 * 
	 * @param elements
	 *            the number of elements the list will show
	 * 
	 */
	public AgletListPanel(final int elements) {
		this();
		this.agletsList.setVisibleRowCount(elements);
	}

	/**
	 * Constructor with a parent element, used to get the size of the list
	 * automatically.
	 * 
	 * @param parent
	 *            the container of this panel
	 */
	public AgletListPanel(final JComponent parent) {
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
	public void addItem(final ITEM toAdd) {
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
	public void addItem(final ITEM toAdd, final int index) {
		if ((toAdd == null) || (index < 0)) {
			return;
		}
		this.listModel.add(index, toAdd);
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
		this.agletsList.addListSelectionListener(listener);
	}

	/**
	 * Provides the index of an alement.
	 * 
	 * @param item
	 *            the item to search
	 * @return the index at which the element exists
	 */
	protected int getIndexOf(final ITEM item) {
		if ((item == null) || (!(this.listModel.contains(item))))
			return -1;
		else {

			// get all the elements
			final Enumeration<ITEM> elements = (Enumeration<ITEM>) this.listModel.elements();
			int index = 0;
			while ((elements != null) && elements.hasMoreElements()) {
				final ITEM element = elements.nextElement();

				if (item.equals(element))
					return index;

				index++;
			}

			return this.listModel.indexOf(item);
		}
	}

	/**
	 * Get an item from its index.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the item
	 * 
	 */
	public ITEM getItem(final int index) {
		return (ITEM) this.listModel.elementAt(index);
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
	 * Get all the available elements in the list, without regard to the
	 * selection.
	 * 
	 * @return the string array that contains each item
	 * 
	 */
	public LinkedList<ITEM> getItems() {
		final LinkedList<ITEM> elements = new LinkedList<ITEM>();

		for (int i = 0; i < this.listModel.size(); i++) {
			elements.add((ITEM) this.listModel.elementAt(i));
		}

		return elements;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(500, 100);
	}

	/**
	 * Provides the renderer actually used from the list.
	 * 
	 * @return the renderer
	 */
	public final ListCellRenderer getRenderer() {
		return this.agletsList.getCellRenderer();
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
	 * A method to get the selected indexes in the current list.
	 * 
	 * @return an array of int that contains locations of the selected indexes.
	 */
	public int[] getSelectedIndexes() {
		return this.agletsList.getSelectedIndices();
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
	 * Returns the number of selected item in the list.
	 * 
	 * @return the number of selected item count
	 */
	public int getSelectedItemCount() {
		return this.getSelectedItemCount();
	}

	/**
	 * Gets the list of all elements as a single string, space separated.
	 * 
	 * @return the compound string
	 */
	public LinkedList<ITEM> getSelectedItems() {
		final int selected[] = this.getSelectedIndexes();

		// check if I've got a selection
		if ((selected == null) || (selected.length == 0))
			return null;

		// now get the selection objects
		final LinkedList<ITEM> selectedItems = new LinkedList<ITEM>();

		for (int i = 0; i < selected.length; i++)
			selectedItems.add(this.getItem(i));

		// all done
		return selectedItems;
	}

	/**
	 * Remove all items from the list.
	 * 
	 */
	public void removeAllItems() {
		this.listModel.removeAllElements();
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
		this.listModel.remove(index);
	}

	/**
	 * Removes a specific item from the list.
	 * 
	 * @param toRemove
	 *            the item to remove
	 */
	public void removeItem(final ITEM toRemove) {
		if (toRemove == null)
			return;
		else {
			final int index = this.getIndexOf(toRemove);
			this.removeItem(index);
		}
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
	public void replaceItem(final int firstIndex, final int secondIndex) {
		if ((firstIndex < 0) || (secondIndex < 0)
				|| (firstIndex == secondIndex)) {
			return;
		}

		// get the element at the first index
		final String first = (String) this.listModel.elementAt(firstIndex);
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
	public void replaceItem(final ITEM newItem, final int index) {
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
		this.agletsList.setSelectedIndex(index);
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
		this.agletsList.setSelectedIndex(index);
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
		this.agletsList.setForeground(foreground);
		this.agletsList.setBackground(background);
	}

	/**
	 * Sets the dimension of the panel
	 * 
	 */
	public void setListDimension(final Dimension dim) {
		this.agletsList.setSize(dim);
		this.setSize(dim);
	}

	/**
	 * Sets the cell renderer for the list.
	 * 
	 * @param renderer
	 *            the new renderer to use
	 */
	public final void setRenderer(final ListCellRenderer renderer) {
		this.agletsList.setCellRenderer(renderer);
	}

	public void setSelectionColors(final Color background, final Color foreground) {
		this.agletsList.setSelectionBackground(background);
		this.agletsList.setSelectionForeground(foreground);
	}

	/**
	 * Sets the title of the border for this panel.
	 * 
	 * @param value
	 *            the (translated) value of the border
	 */
	public void setTitleBorder(final String value) {
		this.border.setTitle(value);
	}
}
