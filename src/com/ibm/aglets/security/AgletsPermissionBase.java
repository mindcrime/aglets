package com.ibm.aglets.security;

/*
 * @(#)AgletsPermissionBase.java
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

import java.io.Serializable;
import java.security.Guard;
import java.security.Permission;
import java.util.StringTokenizer;
import java.util.Vector;

//
// Base class for Aglets related Permission and Protection classes,
// i.e. AgletPermission, AgletProtection, MessagePermission, MessageProtection.
// (This class was com.ibm.awb.security.Permission)
//
abstract public class AgletsPermissionBase extends Permission implements Guard,
Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5410882102692333193L;
	protected static final char CHAR_ASTERISK = '*';
	protected static final String STRING_ASTERISK = String.valueOf(CHAR_ASTERISK);
	protected static final char CHAR_HYPHEN = '-';
	protected static final String STRING_HYPHEN = String.valueOf(CHAR_HYPHEN);
	protected static final char CHAR_SLASH = '/';
	protected static final String STRING_SLASH = String.valueOf(CHAR_SLASH);
	protected static final char CHAR_COLON = ':';
	protected static final String STRING_COLON = String.valueOf(CHAR_COLON);
	protected static final char CHAR_DOT = '.';
	protected static final String STRING_DOT = String.valueOf(CHAR_DOT);
	protected static final char CHAR_COMMA = ',';
	protected static final String STRING_COMMA = String.valueOf(CHAR_COMMA);

	protected static final String STRING_WILDCARD = STRING_ASTERISK;
	protected static final String SEPARATOR = STRING_COMMA;
	protected static final String SEPARATORS = " ,\t\n\r";

	private static final String WILDCARD_NAME = STRING_WILDCARD;
	private static final String WILDCARD_SUBNAME = STRING_DOT + WILDCARD_NAME;

	// ----- utilities
	final static protected boolean checkAglet(final String pattern, final String name) {
		return matches(pattern, name);
	}

	protected static final String concat(final String a[]) {
		return concat(a, SEPARATOR);
	}

	protected static final String concat(final String a[], final String separator) {
		final StringBuffer b = new StringBuffer();

		for (int i = 0; i < a.length; i++) {
			b.append(a[i]);
			if (i + 1 < a.length) {
				b.append(separator);
			}
		}
		return b.toString();
	}

	protected static final String concat(final Vector list) {
		return concat(list, SEPARATOR);
	}

	protected static final String concat(final Vector list, final String separator) {
		if (list == null) {
			return null;
		}
		final int num = list.size();
		final StringBuffer buf = new StringBuffer();
		boolean moreElement = false;

		for (int i = 0; i < num; i++) {
			final Object obj = list.elementAt(i);

			if (obj instanceof String) {
				final String str = (String) obj;

				if (moreElement) {
					buf.append(separator);
				} else {
					moreElement = true;
				}
				buf.append(str);
			}
		}
		return buf.toString();
	}

	static final protected boolean includes(final String[] list, final String elem) {
		if (list == null) {
			return false;
		}
		for (final String str : list) {
			if (matches(str, elem)) {
				return true;
			}
		}
		return false;
	}

	static final protected boolean includes(final Vector list, final String elem) {
		if (list == null) {
			return false;
		}
		final int num = list.size();

		for (int i = 0; i < num; i++) {
			final Object obj = list.elementAt(i);

			if (obj instanceof String) {
				final String str = (String) obj;

				if (matches(str, elem)) {
					return true;
				}
			}
		}
		return false;
	}

	static final protected boolean matches(final boolean[] base, final boolean[] target) {
		for (int i = 0; i < base.length; i++) {
			if (target[i] && !base[i]) {
				return false;
			}
		}
		return true;
	}

	static final protected boolean matches(final String list[], final String elems[]) {
		if (list == null) {
			return true;
		}
		for (int i = 0; i < elems.length; i++) {
			if (!includes(list, elems[i])) {
				return false;
			}
		}
		return true;
	}

	static final protected boolean matches(final String base, final String t) {
		if (base.equals(WILDCARD_NAME)) {
			return true;
		} else if (base.endsWith(WILDCARD_SUBNAME)) {
			return t.startsWith(base.substring(0, base.length() - 2));
		} else {
			return base.equals(t);
		}
	}

	static final protected boolean matches(final Vector list, final Vector elems) {
		if (list == null) {
			return true;
		}
		final int num = elems.size();

		for (int i = 0; i < num; i++) {
			final Object obj = elems.elementAt(i);

			if (obj instanceof String) {
				final String elem = (String) obj;

				if (!includes(list, elem)) {
					return false;
				}
			}
		}
		return true;
	}

	protected static void qsort(final String array[]) {
		qsort(array, 0, array.length - 1);
	}

	private static void qsort(final String array[], final int left, final int right) {
		int i, last;

		if (left >= right) {

			/* do nothing if array contains fewer than two */
			return;

			/* two elements */
		}
		swap(array, left, (left + right) / 2);
		last = left;
		for (i = left + 1; i <= right; i++) {
			if (array[i].compareTo(array[left]) < 0) {
				swap(array, ++last, i);
			}
		}
		swap(array, left, last);
		qsort(array, left, last - 1);
		qsort(array, last + 1, right);
	}

	static final protected String select(final String[] label, final boolean[] flag) {
		return select(label, flag, SEPARATOR);
	}

	static final protected String select(
	                                     final String[] label,
	                                     final boolean[] flag,
	                                     final String separator) {
		int num = 0;
		int i = 0;

		for (i = 0; i < flag.length; i++) {
			if (flag[i]) {
				num++;
			}
		}
		final String[] selected = new String[num];

		num = 0;
		for (i = 0; i < flag.length; i++) {
			if (flag[i]) {
				selected[num] = label[i];
				num++;
			}
		}
		return concat(selected, separator);
	}

	static final protected String[] split(final String v, final String sep) {
		return split(v, sep, true);
	}

	static final protected String[] split(final String v, final String sep, final boolean sort) {
		final StringTokenizer st = new StringTokenizer(v, sep, false);
		final String ret[] = new String[st.countTokens()];
		int i = 0;

		while (st.hasMoreTokens()) {
			ret[i++] = st.nextToken();
		}
		if (sort) {
			qsort(ret);
		}
		return ret;
	}

	private static void swap(final String array[], final int i, final int j) {
		String tmp = array[i];

		tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;
	}

	private AgletsPermissionBase() {
		super(null);
	}

	public AgletsPermissionBase(final String name) {
		super(name);
	}
}
