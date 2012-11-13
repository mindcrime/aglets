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

interface Contents {
	public void addElements(String[] elems);

	public void addElements(String[] elems, int idx);

	public void moveToLast(int idx);

	public void moveToTop(int idx);

	public void removeElements(int idx);

	// public void removeAllElements();
	public void replaceElements(String[] elems, int idx);
}
