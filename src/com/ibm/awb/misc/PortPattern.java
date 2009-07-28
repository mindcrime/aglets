package com.ibm.awb.misc;

/*
 * @(#)PortPattern.java
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

/**
 * The <tt>PortPattern</tt> class represents a range of ports
 * permitted to access.
 * 
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */
public class PortPattern {
	private static final int DEFAULT_PORT_NO = -1;
	private static final String DEFAULT_PORT = 
		String.valueOf(DEFAULT_PORT_NO);
	private static final String GREATER_THAN_OR_EQUAL = ">=";
	private static final String LESS_THAN_OR_EQUAL = "<=";
	private static final String GREATER_THAN = ">";
	private static final String LESS_THAN = "<";
	private static final String BETWEEN_PORTS = "-";
	private static final String ANYPORT = "*";

	private static final int TYPE_NOTYPE = 0;
	private static final int TYPE_DEFAULT_PORT = 1;
	private static final int TYPE_A_PORT = 2;
	private static final int TYPE_ANYPORT = 3;
	private static final int TYPE_GREATER_THAN_OR_EQUAL = 4;
	private static final int TYPE_GREATER_THAN = 5;
	private static final int TYPE_LESS_THAN_OR_EQUAL = 6;
	private static final int TYPE_LESS_THAN = 7;
	private static final int TYPE_BETWEEN = 8;

	private static final int NO_PORT = -1;

	private String _pattern = null;
	private int _type = TYPE_NOTYPE;
	private int _portFrom = NO_PORT;
	private int _portTo = NO_PORT;

	public PortPattern() {
		this(null);
	}
	public PortPattern(int port) {
		_pattern = String.valueOf(port);
		if (port == DEFAULT_PORT_NO) {
			_type = TYPE_DEFAULT_PORT;
		} else {
			_type = TYPE_A_PORT;
			_portFrom = port;
			if (!isValid(_portFrom)) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(_pattern 
								   + ". Port number should be positive.");
			} 
		} 
	}
	public PortPattern(String pattern) {
		if (pattern == null) {
			_type = TYPE_DEFAULT_PORT;
			return;
		} 

		_pattern = pattern.trim();

		final int ind = _pattern.indexOf(BETWEEN_PORTS);
		String ptFrom = null;
		String ptTo = null;

		if (ind > 0) {
			ptFrom = _pattern.substring(0, ind);
			ptTo = _pattern.substring(ind + 1);
		} 

		if (_pattern.equals(DEFAULT_PORT)) {
			_type = TYPE_DEFAULT_PORT;
		} else if (_pattern.equals(ANYPORT)) {
			_type = TYPE_ANYPORT;
		} else if (_pattern.startsWith(GREATER_THAN_OR_EQUAL)) {
			_type = TYPE_GREATER_THAN_OR_EQUAL;
		} else if (_pattern.startsWith(LESS_THAN_OR_EQUAL)) {
			_type = TYPE_LESS_THAN_OR_EQUAL;
		} else if (_pattern.startsWith(GREATER_THAN)) {
			_type = TYPE_GREATER_THAN;
		} else if (_pattern.startsWith(LESS_THAN)) {
			_type = TYPE_LESS_THAN;
		} else {
			if (ind > 0) {
				_type = TYPE_BETWEEN;
			} else {
				_type = TYPE_A_PORT;
			} 
		} 

		String pt = null;

		switch (_type) {
		case TYPE_NOTYPE:
			break;
		case TYPE_DEFAULT_PORT:
			break;
		case TYPE_A_PORT:
			try {
				_portFrom = Integer.parseInt(_pattern);
				_portTo = NO_PORT;
			} catch (NumberFormatException excpt) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(_pattern);
			} 
			if (!isValid(_portFrom)) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(_pattern 
								   + ". Port number should be positive.");
			} 
			break;
		case TYPE_ANYPORT:
			break;
		case TYPE_GREATER_THAN_OR_EQUAL:
		case TYPE_GREATER_THAN:
		case TYPE_LESS_THAN_OR_EQUAL:
		case TYPE_LESS_THAN:
			switch (_type) {
			case TYPE_GREATER_THAN_OR_EQUAL:
			case TYPE_LESS_THAN_OR_EQUAL:
				pt = _pattern.substring(2).trim();
				break;
			case TYPE_GREATER_THAN:
			case TYPE_LESS_THAN:
				pt = _pattern.substring(1).trim();
				break;
			}
			try {
				_portFrom = Integer.parseInt(pt);
				_portTo = NO_PORT;
			} catch (NumberFormatException excpt) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				_portTo = NO_PORT;
			} 
			if (!isValid(_portFrom)) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(pt + ". Port number should be positive.");
			} 
			break;
		case TYPE_BETWEEN:
			if (ptFrom == null || ptFrom.equals("") || ptTo == null 
					|| ptTo.equals("")) {
				throw getException(_pattern);
			} 
			try {
				_portFrom = Integer.parseInt(ptFrom);
			} catch (NumberFormatException excpt) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(ptFrom);
			} 
			try {
				_portTo = Integer.parseInt(ptTo);
			} catch (NumberFormatException excpt) {
				_type = TYPE_NOTYPE;
				_portTo = NO_PORT;
				throw getException(ptTo);
			} 
			if (!isValid(_portFrom)) {
				_type = TYPE_NOTYPE;
				_portFrom = NO_PORT;
				throw getException(ptFrom 
								   + ". Port number should be positive.");
			} 
			if (!isValid(_portTo)) {
				_type = TYPE_NOTYPE;
				_portTo = NO_PORT;
				throw getException(ptTo 
								   + ". Port number should be positive.");
			} 
			break;
		}
	}
	public boolean equals(PortPattern ppat) {
		if (ppat == null) {
			return false;
		} 
		if (ppat._type != _type) {
			return false;
		} 
		boolean eq = false;

		switch (_type) {
		case TYPE_NOTYPE:
			eq = false;
			break;
		case TYPE_DEFAULT_PORT:
			eq = true;
			break;
		case TYPE_A_PORT:
			eq = ppat._portFrom == _portFrom;
			break;
		case TYPE_ANYPORT:
			eq = true;
			break;
		case TYPE_GREATER_THAN_OR_EQUAL:
			eq = ppat._portFrom == _portFrom;
			break;
		case TYPE_GREATER_THAN:
			eq = ppat._portFrom == _portFrom;
			break;
		case TYPE_LESS_THAN_OR_EQUAL:
			eq = ppat._portFrom == _portFrom;
			break;
		case TYPE_LESS_THAN:
			eq = ppat._portFrom == _portFrom;
			break;
		case TYPE_BETWEEN:
			eq = ppat._portFrom == _portFrom && ppat._portTo == _portTo;
			break;
		}
		return eq;
	}
	public boolean equals(Object obj) {
		if (obj instanceof PortPattern) {
			PortPattern ppat = (PortPattern)obj;

			return equals(ppat);
		} 
		return false;
	}
	private static IllegalArgumentException getException(String msg) {
		return new IllegalArgumentException("Illegal port number : " + msg);
	}
	public int getFromPort() {
		return _portFrom;
	}
	public int getToPort() {
		return _portTo;
	}
	public boolean isMatch(int port) {
		boolean matched = false;

		switch (_type) {
		case TYPE_NOTYPE:
			matched = false;
			break;
		case TYPE_DEFAULT_PORT:
			matched = port == DEFAULT_PORT_NO;
			break;
		case TYPE_A_PORT:
			matched = _portFrom == port;
			break;
		case TYPE_ANYPORT:
			matched = true;
			break;
		case TYPE_GREATER_THAN_OR_EQUAL:
			matched = port >= _portFrom;
			break;
		case TYPE_GREATER_THAN:
			matched = port > _portFrom;
			break;
		case TYPE_LESS_THAN_OR_EQUAL:
			matched = port <= _portFrom;
			break;
		case TYPE_LESS_THAN:
			matched = port < _portFrom;
			break;
		case TYPE_BETWEEN:
			matched = _portFrom <= port && port <= _portTo;
			break;
		}
		return matched;
	}
	public boolean isSinglePort() {
		if (_type == TYPE_DEFAULT_PORT || _type == TYPE_A_PORT) {
			return true;
		} 
		return false;
	}
	private static boolean isValid(int port) {
		return port >= 0;
	}
	// for test
	static public void main(String arg[]) {
		PortPattern ppat = null;

		if (arg.length == 0) {
			ppat = new PortPattern();
		} else {
			ppat = new PortPattern(arg[0]);
		} 
		if (ppat != null) {
			System.out.print(ppat.toString());
		} 
	}
	public String toString() {
		String str = null;

		switch (_type) {
		case TYPE_NOTYPE:
			break;
		case TYPE_DEFAULT_PORT:
			str = DEFAULT_PORT;
			break;
		case TYPE_A_PORT:
			str = String.valueOf(_portFrom);
			break;
		case TYPE_ANYPORT:
			str = ANYPORT;
			break;
		case TYPE_GREATER_THAN_OR_EQUAL:
			str = GREATER_THAN_OR_EQUAL + String.valueOf(_portFrom);
			break;
		case TYPE_GREATER_THAN:
			str = GREATER_THAN + String.valueOf(_portFrom);
			break;
		case TYPE_LESS_THAN_OR_EQUAL:
			str = LESS_THAN_OR_EQUAL + String.valueOf(_portFrom);
			break;
		case TYPE_LESS_THAN:
			str = LESS_THAN_OR_EQUAL + String.valueOf(_portFrom);
			break;
		case TYPE_BETWEEN:
			str = String.valueOf(_portFrom) + BETWEEN_PORTS 
				  + String.valueOf(_portTo);
			break;
		}
		return str;
	}
}
