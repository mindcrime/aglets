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

    /**
     * 
     */
    private static final long serialVersionUID = -5878175992615314666L;
    // instance variables
    public byte[] authority;
    public byte[] identity;
    public short agent_system_type;

    // constructors
    public Name() {
    }

    public Name(byte[] __authority, byte[] __identity, short __agent_system_type) {
	this.authority = __authority;
	this.identity = __identity;
	this.agent_system_type = __agent_system_type;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Name)) {
	    return false;
	}
	Name n = (Name) o;

	if ((this.authority.length != n.authority.length)
		|| (this.identity.length != n.identity.length)
		|| (this.agent_system_type != n.agent_system_type)) {
	    return false;
	}
	for (int i = 0; i < this.authority.length; i++) {
	    if (this.authority[i] != n.authority[i]) {
		return false;
	    }
	}
	for (int i = 0; i < this.identity.length; i++) {
	    if (this.identity[i] != n.identity[i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Returns an integer suitable for hash table indexing.
     * 
     * @return hash table indexing integer.
     */
    @Override
    public int hashCode() {
	int h = 0;

	for (byte element : this.authority) {
	    h += (h * 37) + element;
	}
	for (byte element : this.identity) {
	    h += (h * 37) + element;
	}
	h += (h * 37) + this.agent_system_type;
	return h;
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();

	for (byte b : this.identity) {
	    buf.append(Character.forDigit((b >>> 4) & 0xF, 16));
	    buf.append(Character.forDigit(b & 0xF, 16));
	}
	return buf.toString();
    }
}
