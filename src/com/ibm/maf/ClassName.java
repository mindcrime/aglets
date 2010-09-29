package com.ibm.maf;

/*
 * @(#)ClassName.java
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
 * File: ./CfMAF/ClassName.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class ClassName implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1921090469040763716L;
    // instance variables
    public String name;
    public byte[] descriminator;

    // constructors
    public ClassName() {
    }

    public ClassName(String __name, byte[] __descriminator) {
	this.name = __name;
	this.descriminator = __descriminator;
    }
}
