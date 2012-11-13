package com.ibm.aglets.tahiti;

/*
 * @(#)MultiList.java
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

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ItemSelectable;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Vector;

class MultiList extends Panel implements ItemSelectable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5850148005894578120L;
	private static final boolean equalItems(final String[] itemsA, final String[] itemsB) {
		if ((itemsA == null) || (itemsB == null)) {
			return false;
		}
		if (itemsA.length != itemsB.length) {
			return false;
		}
		final int num = itemsA.length;
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			final String itemA = itemsA[idx];
			final String itemB = itemsB[idx];

			if ((itemA == null) || (itemB == null)) {
				return false;
			}
			if (!itemA.equals(itemB)) {
				return false;
			}
		}
		return true;
	}
	public static void main(final String arg[]) {
		final int cols = 4;

		final double[] weight = new double[cols];

		weight[0] = 0.10;
		weight[1] = 0.70;
		weight[2] = 0.05;
		weight[3] = 0.15;

		final String[] labels = new String[cols];

		labels[0] = "item 1";
		labels[1] = "item 2";
		labels[2] = "item 3";
		labels[3] = "item 4";

		final MultiList mlist = new MultiList(3, weight, labels);

		// MultiList mlist = new MultiList(3, labels);

		final String[] items = new String[cols];

		items[0] = "ABCDEFGHIJKLMNOPQRSTU";
		items[1] = "xyz";
		items[2] = "---";
		items[3] = "She said.";
		items[0] = "ABCDEFGHIJKLMNOPQRSTU";
		items[1] = "ABCDEFGHIJKLMNOPQRSTU";
		items[2] = "ABCDEFGHIJKLMNOPQRSTU";
		items[3] = "ABCDEFGHIJKLMNOPQRSTU";
		mlist.addItems(items);

		items[0] = "Alpha Beta Gamma Delta";
		items[1] = "Omega";
		items[2] = "   ";
		items[3] = "Yes";
		items[0] = "ABCDEFGHIJKLMNOPQRSTU";
		items[1] = "ABCDEFGHIJKLMNOPQRSTU";
		items[2] = "ABCDEFGHIJKLMNOPQRSTU";
		items[3] = "ABCDEFGHIJKLMNOPQRSTU";
		mlist.addItems(items);

		items[0] = "When I wake up early in the morning,";
		items[1] = "lift up my head, I'm still yawning.";
		items[2] = "When I'm in the middle of the dread,";
		items[3] = "stay in bed, float up stream.";
		items[0] = "ABCDEFGHIJKLMNOPQRSTU";
		items[1] = "ABCDEFGHIJKLMNOPQRSTU";
		items[2] = "ABCDEFGHIJKLMNOPQRSTU";
		items[3] = "ABCDEFGHIJKLMNOPQRSTU";
		mlist.addItems(items);

		items[0] = "Please don't wake me,";
		items[1] = "no, don't shake me,";
		items[2] = "leave me where I am.";
		items[3] = "I'm only sleeping.";
		items[0] = "ABCDEFGHIJKLMNOPQRSTU";
		items[1] = "ABCDEFGHIJKLMNOPQRSTU";
		items[2] = "ABCDEFGHIJKLMNOPQRSTU";
		items[3] = "ABCDEFGHIJKLMNOPQRSTU";
		mlist.addItems(items);

		final Panel buttonPanel = new Panel();

		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(new Button("add"));
		buttonPanel.add(new Button("remove"));

		final Frame frame = new Frame("test");
		final GridBagLayout layout = new GridBagLayout();

		frame.setLayout(layout);
		frame.setSize(800, 150);

		final GridBagConstraints cnst = new GridBagConstraints();

		cnst.gridwidth = GridBagConstraints.REMAINDER;
		cnst.gridheight = 1;
		cnst.gridx = 0;
		cnst.gridy = 0;
		cnst.fill = GridBagConstraints.BOTH;
		cnst.anchor = GridBagConstraints.CENTER;
		cnst.weightx = 1.0;
		cnst.weighty = 1.0;
		cnst.ipadx = 2;
		cnst.ipady = 2;
		layout.setConstraints(mlist, cnst);
		frame.add(mlist);

		cnst.gridy = 1;
		cnst.fill = GridBagConstraints.HORIZONTAL;
		cnst.weighty = 0.0;
		layout.setConstraints(buttonPanel, cnst);
		frame.add(buttonPanel);

		// frame.setSize(400, 150);

		// frame.pack();
		frame.show();
	}
	int _rows = 0;
	int _cols = 0;
	int _line = 0;
	List[] _lists = null;
	String[] _selectedItems = null;
	Vector _listeners = new Vector();
	GridBagLayout _layout = new GridBagLayout();

	private final GridBagConstraints _cnst = new GridBagConstraints();

	Contents _contents = null;

	MultiList(final int rows, final double[] weight) {
		setListSize(rows, weight.length);
		init();
		this.createLists(weight);
	}

	MultiList(final int rows, final double[] weight, final String[] labels) {
		setListSize(rows, weight.length);
		init();
		this.createLabels(labels, weight);
		this.createLists(weight);
	}

	MultiList(final int rows, final int cols) {
		setListSize(rows, cols);
		init();
		this.createLists();
	}

	MultiList(final int rows, final String[] labels) {
		setListSize(rows, labels.length);
		init();
		this.createLabels(labels);
		this.createLists();
	}

	public synchronized void add(final String[] items) {
		this.addItems(items);
	}

	public synchronized void add(final String[] items, final int idx) {
		this.addItems(items, idx);
	}

	@Override
	public void addItemListener(final ItemListener listener) {
		_listeners.addElement(listener);
	}

	public synchronized void addItems(final String[] items) {
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.add(items[i]);
			}
		}
		if (_contents != null) {
			_contents.addElements(items);
		}
	}

	public synchronized void addItems(final String[] items, final int idx) {
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.add(items[i], idx);
			}
		}
		if (_contents != null) {
			_contents.addElements(items, idx);
		}
	}

	protected GridBagConstraints createConstraints() {
		final GridBagConstraints cnst = new GridBagConstraints();

		initializeConstraints(cnst);
		return cnst;
	}

	private void createLabels(final String[] labels) {
		initializeConstraints(_cnst);
		_cnst.gridwidth = 1;
		_cnst.gridheight = 1;
		_cnst.gridy = _line;
		_cnst.fill = GridBagConstraints.NONE;
		_cnst.weightx = 1.0;
		_cnst.weighty = 0.0;

		Label label = null;
		int i = 0;

		for (i = 0; i < _cols; i++) {
			label = new Label(labels[i]);
			_cnst.gridx = i;
			_layout.setConstraints(label, _cnst);
			this.add(label);
		}
		nextComponents();
	}

	private void createLabels(final String[] labels, final double[] weight) {
		initializeConstraints(_cnst);
		_cnst.gridwidth = 1;
		_cnst.gridheight = 1;
		_cnst.gridy = _line;
		_cnst.fill = GridBagConstraints.NONE;
		_cnst.weighty = 0.0;

		Label label = null;
		int i = 0;

		for (i = 0; i < _cols; i++) {
			label = new Label(labels[i]);
			_cnst.gridx = i;
			_cnst.weightx = weight[i];
			_layout.setConstraints(label, _cnst);
			this.add(label);
		}
		nextComponents();
	}

	private void createLists() {
		initializeConstraints(_cnst);
		_cnst.gridwidth = 1;
		_cnst.gridheight = GridBagConstraints.REMAINDER;
		_cnst.gridy = _line;
		_cnst.fill = GridBagConstraints.BOTH;
		_cnst.weightx = 1.0;
		_cnst.weighty = 1.0;

		List list = null;
		int i = 0;

		for (i = 0; i < _cols; i++) {
			list = new List(_rows);
			_lists[i] = list;
			_cnst.gridx = i;
			_layout.setConstraints(list, _cnst);
			this.add(list);
			list.addItemListener(new ListSelector(this, i));
		}
		nextComponents();
	}

	private void createLists(final double[] weight) {
		initializeConstraints(_cnst);
		_cnst.gridwidth = 1;
		_cnst.gridheight = GridBagConstraints.REMAINDER;
		_cnst.gridy = _line;
		_cnst.fill = GridBagConstraints.BOTH;
		_cnst.weighty = 1.0;

		List list = null;
		int i = 0;

		for (i = 0; i < _cols; i++) {
			list = new List(_rows);
			_lists[i] = list;
			_cnst.gridx = i;
			_cnst.weightx = weight[i];
			_layout.setConstraints(list, _cnst);
			this.add(list);
			list.addItemListener(new ListSelector(this, i));
		}
		nextComponents();
	}

	public synchronized void delItems(final int idx) {
		if (idx < 0) {

			// do nothing
			return;
		}
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.remove(idx);
			}
		}
		if (_contents != null) {
			_contents.removeElements(idx);
		}
	}

	public synchronized void delSelectedItems() {
		delItems(getSelectedIndex());
	}

	public synchronized void deselect(final int row) {
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.deselect(row);
				_selectedItems[i] = null;
			}
		}
	}

	public Contents getContents() {
		return _contents;
	}

	public int getItemCount() {
		final List list = getList(0);

		if (list == null) {
			return 0;
		}
		return list.getItemCount();
	}

	public synchronized String[] getItems(final int idx) {
		final String[] items = new String[_cols];
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				items[i] = list.getItem(idx);
			}
		}
		return items;
	}

	public List getList(final int i) {
		return _lists[i];
	}

	public int getRows() {
		return _rows;
	}

	public int getSelectedIndex() {
		final List list = getList(0);

		if (list == null) {
			return -1;
		}
		return list.getSelectedIndex();
	}

	public synchronized String[] getSelectedItems() {
		return _selectedItems;
	}

	@Override
	public synchronized Object[] getSelectedObjects() {
		return _selectedItems;
	}

	private void init() {
		setLayout(_layout);
		_lists = new List[_cols];
		_selectedItems = new String[_cols];
	}

	protected void initializeConstraints(final GridBagConstraints cnst) {
		cnst.gridwidth = 1; // default

		// cnst.gridwidth = GridBagConstraints.RELATIVE;
		cnst.gridheight = 1; // default
		cnst.gridx = GridBagConstraints.RELATIVE; // default
		cnst.gridy = GridBagConstraints.RELATIVE; // default

		// cnst.fill = GridBagConstraints.NONE; // default
		cnst.fill = GridBagConstraints.BOTH;
		cnst.anchor = GridBagConstraints.CENTER; // default

		// cnst.weightx = 0.0; // default
		cnst.weightx = 1.0;

		// cnst.weighty = 0.0; // default
		cnst.weighty = 1.0;
		cnst.ipadx = 0; // default

		// cnst.ipadx = 2;
		cnst.ipady = 0; // default

		// cnst.ipady = 2;
	}

	public boolean isIndexSelected(final int idx) {
		final List list = getList(0);

		if (list == null) {
			return false;
		}
		return list.isIndexSelected(idx);
	}

	public synchronized void moveToLast() {
		this.moveToLast(getSelectedIndex());
	}

	public synchronized void moveToLast(final int idx) {
		if (idx < 0) {

			// do nothing
			return;
		}
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				final String item = list.getItem(idx);

				list.remove(idx);
				list.add(item);
			}
		}
		if (_contents != null) {
			_contents.moveToLast(idx);
		}
	}

	public synchronized void moveToTop() {
		this.moveToTop(getSelectedIndex());
	}

	public synchronized void moveToTop(final int idx) {
		if (idx < 0) {

			// do nothing
			return;
		}
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				final String item = list.getItem(idx);

				list.remove(idx);
				list.add(item, 0);
			}
		}
		if (_contents != null) {
			_contents.moveToTop(idx);
		}
	}

	public void nextComponents() {
		_line++;
	}

	void notifyToItemListeners(final ItemEvent ev) {
		final Enumeration listeners = _listeners.elements();

		while (listeners.hasMoreElements()) {
			final Object obj = listeners.nextElement();

			if (obj != null) {
				if (obj instanceof ItemListener) {
					final ItemListener listener = (ItemListener) obj;

					listener.itemStateChanged(ev);
				}
			}
		}
	}

	@Override
	public synchronized void remove(final int idx) {
		delItems(idx);
	}

	public synchronized void remove(final String[] items) {
		int idx = 0;

		for (idx = 0; idx < _rows; idx++) {
			if (equalItems(items, getItems(idx))) {
				this.remove(idx);
				return;
			}
		}
	}

	@Override
	public synchronized void removeAll() {
		final int num = getItemCount();
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			this.remove(idx);
		}
	}

	@Override
	public void removeItemListener(final ItemListener listener) {
		_listeners.removeElement(listener);
	}

	public synchronized void replaceItems(final String[] items, final int idx) {
		if (items == null) {

			// do nothing
			return;
		}
		if (idx < 0) {

			// do nothing
			return;
		}
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.replaceItem(items[i], idx);
			}
		}
		if (_contents != null) {
			_contents.replaceElements(items, idx);
		}
	}

	public synchronized void select(final int row) {
		int i = 0;

		for (i = 0; i < _cols; i++) {
			final List list = getList(i);

			if (list != null) {
				list.select(row);
				_selectedItems[i] = list.getSelectedItem();
			} else {
				_selectedItems[i] = null;
			}
		}
	}

	public void setContents(final Contents contents) {
		_contents = contents;
	}

	private void setListSize(final int rows, final int cols) {
		_rows = rows;
		_cols = cols;
	}
}
