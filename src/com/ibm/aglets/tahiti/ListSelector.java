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

import java.util.Vector;
import java.util.Enumeration;
import java.awt.List;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Panel;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.awt.Button;

class ListSelector implements ItemListener {
	MultiList _mlist = null;
	int _col = -1;

	ListSelector(MultiList mlist, int col) {
		_mlist = mlist;
		_col = col;
	}
	public void itemStateChanged(ItemEvent ev) {
		if (_mlist != null) {
			List list = _mlist.getList(_col);

			if (list != null) {
				final int idx = list.getSelectedIndex();

				if (idx >= 0) {
					_mlist.select(idx);
					_mlist.notifyToItemListeners(ev);
				} 
			} 
		} 
	}
}
