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
    int _rows = 0;
    int _cols = 0;
    int _line = 0;
    List[] _lists = null;
    String[] _selectedItems = null;
    Vector _listeners = new Vector();
    GridBagLayout _layout = new GridBagLayout();
    private GridBagConstraints _cnst = new GridBagConstraints();
    Contents _contents = null;

    MultiList(int rows, double[] weight) {
	this.setListSize(rows, weight.length);
	this.init();
	this.createLists(weight);
    }

    MultiList(int rows, double[] weight, String[] labels) {
	this.setListSize(rows, weight.length);
	this.init();
	this.createLabels(labels, weight);
	this.createLists(weight);
    }

    MultiList(int rows, String[] labels) {
	this.setListSize(rows, labels.length);
	this.init();
	this.createLabels(labels);
	this.createLists();
    }

    MultiList(int rows, int cols) {
	this.setListSize(rows, cols);
	this.init();
	this.createLists();
    }

    public synchronized void add(String[] items) {
	this.addItems(items);
    }

    public synchronized void add(String[] items, int idx) {
	this.addItems(items, idx);
    }

    public void addItemListener(ItemListener listener) {
	this._listeners.addElement(listener);
    }

    public synchronized void addItems(String[] items) {
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.add(items[i]);
	    }
	}
	if (this._contents != null) {
	    this._contents.addElements(items);
	}
    }

    public synchronized void addItems(String[] items, int idx) {
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.add(items[i], idx);
	    }
	}
	if (this._contents != null) {
	    this._contents.addElements(items, idx);
	}
    }

    protected GridBagConstraints createConstraints() {
	GridBagConstraints cnst = new GridBagConstraints();

	this.initializeConstraints(cnst);
	return cnst;
    }

    private void createLabels(String[] labels) {
	this.initializeConstraints(this._cnst);
	this._cnst.gridwidth = 1;
	this._cnst.gridheight = 1;
	this._cnst.gridy = this._line;
	this._cnst.fill = GridBagConstraints.NONE;
	this._cnst.weightx = 1.0;
	this._cnst.weighty = 0.0;

	Label label = null;
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    label = new Label(labels[i]);
	    this._cnst.gridx = i;
	    this._layout.setConstraints(label, this._cnst);
	    this.add(label);
	}
	this.nextComponents();
    }

    private void createLabels(String[] labels, double[] weight) {
	this.initializeConstraints(this._cnst);
	this._cnst.gridwidth = 1;
	this._cnst.gridheight = 1;
	this._cnst.gridy = this._line;
	this._cnst.fill = GridBagConstraints.NONE;
	this._cnst.weighty = 0.0;

	Label label = null;
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    label = new Label(labels[i]);
	    this._cnst.gridx = i;
	    this._cnst.weightx = weight[i];
	    this._layout.setConstraints(label, this._cnst);
	    this.add(label);
	}
	this.nextComponents();
    }

    private void createLists() {
	this.initializeConstraints(this._cnst);
	this._cnst.gridwidth = 1;
	this._cnst.gridheight = GridBagConstraints.REMAINDER;
	this._cnst.gridy = this._line;
	this._cnst.fill = GridBagConstraints.BOTH;
	this._cnst.weightx = 1.0;
	this._cnst.weighty = 1.0;

	List list = null;
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    list = new List(this._rows);
	    this._lists[i] = list;
	    this._cnst.gridx = i;
	    this._layout.setConstraints(list, this._cnst);
	    this.add(list);
	    list.addItemListener(new ListSelector(this, i));
	}
	this.nextComponents();
    }

    private void createLists(double[] weight) {
	this.initializeConstraints(this._cnst);
	this._cnst.gridwidth = 1;
	this._cnst.gridheight = GridBagConstraints.REMAINDER;
	this._cnst.gridy = this._line;
	this._cnst.fill = GridBagConstraints.BOTH;
	this._cnst.weighty = 1.0;

	List list = null;
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    list = new List(this._rows);
	    this._lists[i] = list;
	    this._cnst.gridx = i;
	    this._cnst.weightx = weight[i];
	    this._layout.setConstraints(list, this._cnst);
	    this.add(list);
	    list.addItemListener(new ListSelector(this, i));
	}
	this.nextComponents();
    }

    public synchronized void delItems(int idx) {
	if (idx < 0) {

	    // do nothing
	    return;
	}
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.remove(idx);
	    }
	}
	if (this._contents != null) {
	    this._contents.removeElements(idx);
	}
    }

    public synchronized void delSelectedItems() {
	this.delItems(this.getSelectedIndex());
    }

    public synchronized void deselect(int row) {
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.deselect(row);
		this._selectedItems[i] = null;
	    }
	}
    }

    private static final boolean equalItems(String[] itemsA, String[] itemsB) {
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

    public Contents getContents() {
	return this._contents;
    }

    public int getItemCount() {
	List list = this.getList(0);

	if (list == null) {
	    return 0;
	}
	return list.getItemCount();
    }

    public synchronized String[] getItems(int idx) {
	String[] items = new String[this._cols];
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		items[i] = list.getItem(idx);
	    }
	}
	return items;
    }

    public List getList(int i) {
	return this._lists[i];
    }

    public int getRows() {
	return this._rows;
    }

    public int getSelectedIndex() {
	List list = this.getList(0);

	if (list == null) {
	    return -1;
	}
	return list.getSelectedIndex();
    }

    public synchronized String[] getSelectedItems() {
	return this._selectedItems;
    }

    public synchronized Object[] getSelectedObjects() {
	return this._selectedItems;
    }

    private void init() {
	this.setLayout(this._layout);
	this._lists = new List[this._cols];
	this._selectedItems = new String[this._cols];
    }

    protected void initializeConstraints(GridBagConstraints cnst) {
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

    public boolean isIndexSelected(int idx) {
	List list = this.getList(0);

	if (list == null) {
	    return false;
	}
	return list.isIndexSelected(idx);
    }

    public static void main(String arg[]) {
	final int cols = 4;

	double[] weight = new double[cols];

	weight[0] = 0.10;
	weight[1] = 0.70;
	weight[2] = 0.05;
	weight[3] = 0.15;

	String[] labels = new String[cols];

	labels[0] = "item 1";
	labels[1] = "item 2";
	labels[2] = "item 3";
	labels[3] = "item 4";

	MultiList mlist = new MultiList(3, weight, labels);

	// MultiList mlist = new MultiList(3, labels);

	String[] items = new String[cols];

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

	Panel buttonPanel = new Panel();

	buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	buttonPanel.add(new Button("add"));
	buttonPanel.add(new Button("remove"));

	Frame frame = new Frame("test");
	GridBagLayout layout = new GridBagLayout();

	frame.setLayout(layout);
	frame.setSize(800, 150);

	GridBagConstraints cnst = new GridBagConstraints();

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

    public synchronized void moveToLast() {
	this.moveToLast(this.getSelectedIndex());
    }

    public synchronized void moveToLast(int idx) {
	if (idx < 0) {

	    // do nothing
	    return;
	}
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		final String item = list.getItem(idx);

		list.remove(idx);
		list.add(item);
	    }
	}
	if (this._contents != null) {
	    this._contents.moveToLast(idx);
	}
    }

    public synchronized void moveToTop() {
	this.moveToTop(this.getSelectedIndex());
    }

    public synchronized void moveToTop(int idx) {
	if (idx < 0) {

	    // do nothing
	    return;
	}
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		final String item = list.getItem(idx);

		list.remove(idx);
		list.add(item, 0);
	    }
	}
	if (this._contents != null) {
	    this._contents.moveToTop(idx);
	}
    }

    public void nextComponents() {
	this._line++;
    }

    void notifyToItemListeners(ItemEvent ev) {
	Enumeration listeners = this._listeners.elements();

	while (listeners.hasMoreElements()) {
	    Object obj = listeners.nextElement();

	    if (obj != null) {
		if (obj instanceof ItemListener) {
		    ItemListener listener = (ItemListener) obj;

		    listener.itemStateChanged(ev);
		}
	    }
	}
    }

    public synchronized void remove(String[] items) {
	int idx = 0;

	for (idx = 0; idx < this._rows; idx++) {
	    if (equalItems(items, this.getItems(idx))) {
		this.remove(idx);
		return;
	    }
	}
    }

    @Override
    public synchronized void remove(int idx) {
	this.delItems(idx);
    }

    @Override
    public synchronized void removeAll() {
	final int num = this.getItemCount();
	int idx = 0;

	for (idx = 0; idx < num; idx++) {
	    this.remove(idx);
	}
    }

    public void removeItemListener(ItemListener listener) {
	this._listeners.removeElement(listener);
    }

    public synchronized void replaceItems(String[] items, int idx) {
	if (items == null) {

	    // do nothing
	    return;
	}
	if (idx < 0) {

	    // do nothing
	    return;
	}
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.replaceItem(items[i], idx);
	    }
	}
	if (this._contents != null) {
	    this._contents.replaceElements(items, idx);
	}
    }

    public synchronized void select(int row) {
	int i = 0;

	for (i = 0; i < this._cols; i++) {
	    List list = this.getList(i);

	    if (list != null) {
		list.select(row);
		this._selectedItems[i] = list.getSelectedItem();
	    } else {
		this._selectedItems[i] = null;
	    }
	}
    }

    public void setContents(Contents contents) {
	this._contents = contents;
    }

    private void setListSize(int rows, int cols) {
	this._rows = rows;
	this._cols = cols;
    }
}
