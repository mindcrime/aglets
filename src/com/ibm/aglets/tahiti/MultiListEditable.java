package com.ibm.aglets.tahiti;

/*
 * @(#)MultiListEditable.java
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

import java.awt.GridBagConstraints;
import java.awt.ItemSelectable;
import java.awt.TextField;

class MultiListEditable extends MultiList implements ItemSelectable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7048653078325472820L;
	TextField[] _textFields = null;

	MultiListEditable(final int rows, final double[] weight) {
		super(rows, weight);
		_textFields = new TextField[_cols];
		this.createTextFields(weight);
	}

	MultiListEditable(final int rows, final double[] weight, final String[] labels) {
		super(rows, weight, labels);
		_textFields = new TextField[_cols];
		this.createTextFields(weight);
	}

	MultiListEditable(final int rows, final int cols) {
		super(rows, cols);
		_textFields = new TextField[_cols];
		this.createTextFields();
	}

	MultiListEditable(final int rows, final int[] width) {
		super(rows, width.length);
		_textFields = new TextField[_cols];
		this.createTextFields(width);
	}

	MultiListEditable(final int rows, final int[] width, final double[] weight) {
		super(rows, weight);
		_textFields = new TextField[_cols];
		this.createTextFields(weight, width);
	}

	MultiListEditable(final int rows, final int[] width, final double[] weight, final String[] labels) {
		super(rows, weight, labels);
		_textFields = new TextField[_cols];
		this.createTextFields(weight, width);
	}

	MultiListEditable(final int rows, final int[] width, final String[] labels) {
		super(rows, labels);
		_textFields = new TextField[_cols];
		this.createTextFields(width);
	}

	MultiListEditable(final int rows, final String[] labels) {
		super(rows, labels);
		_textFields = new TextField[_cols];
		this.createTextFields();
	}

	public synchronized void addItemsInTextFields() {
		final int num = getItemCount();
		final String[] items = new String[num];
		int i = 0;

		for (i = 0; i < num; i++) {
			items[i] = _textFields[i].getText();
		}
		this.addItems(items);
	}

	public void createTextFields() {
		final GridBagConstraints cnt = createConstraints();

		cnt.fill = GridBagConstraints.HORIZONTAL;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			final TextField textField = new TextField();

			_textFields[i] = textField;
			if (i == _cols - 1) {
				cnt.gridwidth = GridBagConstraints.REMAINDER;
			} else {
				cnt.gridwidth = GridBagConstraints.RELATIVE;
			}
			_layout.setConstraints(textField, cnt);
			this.add(textField);
		}
		nextComponents();
	}

	public void createTextFields(final double[] weight) {
		final GridBagConstraints cnt = createConstraints();

		cnt.gridy = _line;
		cnt.fill = GridBagConstraints.HORIZONTAL;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			final TextField textField = new TextField();

			_textFields[i] = textField;
			cnt.weightx = weight[i];
			_layout.setConstraints(textField, cnt);
			this.add(textField);
		}
		nextComponents();
	}

	public void createTextFields(final double[] weight, final int[] width) {
		final GridBagConstraints cnt = createConstraints();

		cnt.gridy = _line;
		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			final TextField textField = new TextField(width[i]);

			_textFields[i] = textField;
			cnt.weightx = weight[i];
			_layout.setConstraints(textField, cnt);
			this.add(textField);
		}
		nextComponents();
	}

	public void createTextFields(final int[] width) {
		final GridBagConstraints cnt = createConstraints();

		cnt.anchor = GridBagConstraints.WEST;

		int i = 0;

		for (i = 0; i < _cols; i++) {
			final TextField textField = new TextField(width[i]);

			_textFields[i] = textField;
			if (i == _cols - 1) {
				cnt.gridwidth = GridBagConstraints.REMAINDER;
			} else {
				cnt.gridwidth = GridBagConstraints.RELATIVE;
			}
			_layout.setConstraints(textField, cnt);
			this.add(textField);
		}
		nextComponents();
	}
}
