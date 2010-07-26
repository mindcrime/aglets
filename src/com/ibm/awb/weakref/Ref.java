package com.ibm.awb.weakref;

/*
 * @(#)Ref.java
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Ref {

    public Ref getRef(VirtualRef vref);

    public String getRefClassName();

    public void referenced();

    public void setRef(VirtualRef vref, ObjectInputStream s)
	    throws IOException, ClassNotFoundException;

    public void unreferenced();

    public void writeInfo(ObjectOutputStream s) throws IOException;
}
