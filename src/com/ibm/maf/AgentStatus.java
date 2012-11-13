package com.ibm.maf;

/*
 * @(#)AgentStatus.java
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

/*
 * File: ./CfMAF/AgentStatus.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class AgentStatus implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8832582183048115230L;
	public static final int _Running = 0, _Suspended = 1, _Terminated = 2;
	public static final AgentStatus Running = new AgentStatus(_Running);
	public static final AgentStatus Suspended = new AgentStatus(_Suspended);
	public static final AgentStatus Terminated = new AgentStatus(_Terminated);
	public static final AgentStatus from_int(final int i)
	throws IllegalArgumentException {
		switch (i) {
			case _Running:
				return Running;
			case _Suspended:
				return Suspended;
			case _Terminated:
				return Terminated;
			default:
				throw new IllegalArgumentException();
		}
	}

	private final int _value;

	private AgentStatus(final int _value) {
		this._value = _value;
	}

	public int value() {
		return _value;
	}
}
