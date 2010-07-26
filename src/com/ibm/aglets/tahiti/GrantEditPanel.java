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
    SecurityConfigDialog _dialog = null;

    GrantEditPanel(SecurityConfigDialog dialog, List list, Editor editor) {
	super(null, list, editor);
	this._dialog = dialog;
    }

    @Override
    protected void addItemIntoList(String item) {
	this._dialog.addGrantPanel(item);
	this.selectItem(item);
	this._dialog.showGrantPanel(item);
    }

    @Override
    protected void removeItemFromList() {
	final int idx = this.getSelectedIndex();
	final String item = this.getSelectedItem();

	super.removeItemFromList();
	this._dialog.removeGrantPanel(idx, item);
	this._dialog.showGrantPanel(item);
    }
}
