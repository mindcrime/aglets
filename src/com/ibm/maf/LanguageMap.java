package com.ibm.maf;

/*
 * @(#)LanguageMap.java
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
 * File: ./CfMAF/LanguageMap.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class LanguageMap implements java.io.Serializable {

	// instance variables
	public short language_id;
	public short[] serializations;

	// constructors
	public LanguageMap() {}
	public LanguageMap(short __language_id, short[] __serializations) {
		language_id = __language_id;
		serializations = __serializations;
	}
}
