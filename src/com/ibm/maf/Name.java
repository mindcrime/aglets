package com.ibm.maf;

/*
 * @(#)Name.java
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
 * File: ./CfMAF/Name.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class Name implements java.io.Serializable {

	// instance variables
	public byte[] authority;
	public byte[] identity;
	public short agent_system_type;

	// constructors
	public Name() {}
	public Name(byte[] __authority, byte[] __identity, 
				short __agent_system_type) {
		authority = __authority;
		identity = __identity;
		agent_system_type = __agent_system_type;
	}
	public boolean equals(Object o) {
		if (!(o instanceof Name)) {
			return false;
		} 
		Name n = (Name)o;

		if (authority.length != n.authority.length 
				|| identity.length != n.identity.length 
				|| agent_system_type != n.agent_system_type) {
			return false;
		} 
		for (int i = 0; i < authority.length; i++) {
			if (authority[i] != n.authority[i]) {
				return false;
			} 
		} 
		for (int i = 0; i < identity.length; i++) {
			if (identity[i] != n.identity[i]) {
				return false;
			} 
		} 
		return true;
	}
	/**
	 * Returns an integer suitable for hash table indexing.
	 * @return hash table indexing integer.
	 */
	public int hashCode() {
		int h = 0;

		for (int i = 0; i < authority.length; i++) {
			h += (h * 37) + (int)authority[i];
		} 
		for (int i = 0; i < identity.length; i++) {
			h += (h * 37) + (int)identity[i];
		} 
		h += (h * 37) + (int)agent_system_type;
		return h;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < identity.length; i++) {
			byte b = identity[i];

			buf.append(Character.forDigit((b >>> 4) & 0xF, 16));
			buf.append(Character.forDigit(b & 0xF, 16));
		} 
		return buf.toString();
	}
}
