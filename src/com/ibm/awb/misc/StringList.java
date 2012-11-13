package com.ibm.awb.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/*
 * @(#)StringList.java
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

public class StringList implements Enumeration {
	private final Vector _list = new Vector();
	private final int _index = 0;

	public final void addString(final String str) {
		_list.addElement(str);
	}

	@Override
	public boolean hasMoreElements() {
		return _index < _list.size();
	}

	@Override
	public Object nextElement() {
		if (!hasMoreElements()) {
			throw new NoSuchElementException("no more elements.");
		}
		Object obj = null;

		try {
			obj = _list.elementAt(_index);
		} catch (final Exception excpt) {
			throw new NoSuchElementException(excpt.getMessage());
		}
		return obj;
	}
}
