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

import java.awt.List;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class ListSelector implements ItemListener {
    MultiList _mlist = null;
    int _col = -1;

    ListSelector(MultiList mlist, int col) {
	this._mlist = mlist;
	this._col = col;
    }

    public void itemStateChanged(ItemEvent ev) {
	if (this._mlist != null) {
	    List list = this._mlist.getList(this._col);

	    if (list != null) {
		final int idx = list.getSelectedIndex();

		if (idx >= 0) {
		    this._mlist.select(idx);
		    this._mlist.notifyToItemListeners(ev);
		}
	    }
	}
    }
}
