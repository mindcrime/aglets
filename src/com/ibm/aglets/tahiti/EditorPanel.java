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

import java.awt.Panel;
import java.util.Vector;

class EditorPanel extends Panel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2028821710329549232L;
	private static final char CHAR_COMMA = ',';
	private static final char CHAR_SEPARATOR = CHAR_COMMA;
	private static final char CHAR_STRING_QUOTE = '"';

	protected static final String getArg(final Vector args, final int idx) {
		final int num = args.size();

		if (idx >= num) {
			return null;
		}
		final Object obj = args.elementAt(idx);

		if (obj == null) {
			return null;
		}
		if (obj instanceof String) {
			return (String) obj;
		} else {
			return null;
		}
	}

	private static String quote(final String str) {
		return quote(str, CHAR_STRING_QUOTE);
	}

	private static final String quote(final String str, final char c) {
		if (str == null) {
			return null;
		}

		final String q = String.valueOf(c);

		return q + str.trim() + q;
	}

	protected static String toText(final Vector args) {
		if (args == null) {
			return "";
		}
		final int num = args.size();

		if (num == 0) {
			return "";
		}
		String text = null;
		int idx = 0;

		while (idx < num) {
			String arg = getArg(args, idx);

			if (arg == null) {
				arg = "";
			}
			arg = quote(arg);
			if (text == null) {
				text = arg;
			} else {
				text = text + CHAR_SEPARATOR + arg;
			}
			idx++;
		}
		return text;
	}

	protected static final Vector toVector(final String text) {
		return toVector(text, CHAR_STRING_QUOTE, CHAR_SEPARATOR);
	}

	protected static final Vector toVector(
	                                       final String text,
	                                       final char cQuote,
	                                       final char cSeparator) {
		final Vector args = new Vector();
		final String str = text.trim();
		int idx = 0;
		final int len = str.length();
		boolean inQuotedString = false;
		int fromIndex = -1;
		int toIndex = -1;

		while (idx < len) {
			final char c = str.charAt(idx);

			if (c == cQuote) {
				if (inQuotedString) {
					inQuotedString = false;
					toIndex = idx;
				} else {
					inQuotedString = true;
					fromIndex = idx;
				}
			} else if (c == cSeparator) {
				if (!inQuotedString) {
					if ((fromIndex >= 0) && (toIndex >= fromIndex)) {
						final String arg = str.substring(fromIndex + 1, toIndex);

						args.addElement(arg);
					}
				}
			}
			idx++;
		}
		if (!inQuotedString) {
			if ((fromIndex >= 0) && (toIndex >= fromIndex)) {
				final String arg = str.substring(fromIndex + 1, toIndex);

				args.addElement(arg);
			}
		}
		return args;
	}

	private Vector _args = null;

	protected final String getArg(final int idx) {
		return getArg(_args, idx);
	}

	protected void parseText(final String text) {
		this.parseText(text, CHAR_STRING_QUOTE, CHAR_SEPARATOR);
	}

	protected final void parseText(final String text, final char cQuote, final char cSeparator) {
		_args = toVector(text, cQuote, cSeparator);
	}

	protected final String toText() {
		return toText(_args);
	}
}
