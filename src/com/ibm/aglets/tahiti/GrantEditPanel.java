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

import java.awt.List;

class GrantEditPanel extends EditListPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7722004754186234319L;
	SecurityConfigDialog _dialog = null;

	GrantEditPanel(final SecurityConfigDialog dialog, final List list, final Editor editor) {
		super(null, list, editor);
		_dialog = dialog;
	}

	@Override
	protected void addItemIntoList(final String item) {
		_dialog.addGrantPanel(item);
		selectItem(item);
		_dialog.showGrantPanel(item);
	}

	@Override
	protected void removeItemFromList() {
		final int idx = getSelectedIndex();
		final String item = getSelectedItem();

		super.removeItemFromList();
		_dialog.removeGrantPanel(idx, item);
		_dialog.showGrantPanel(item);
	}
}
