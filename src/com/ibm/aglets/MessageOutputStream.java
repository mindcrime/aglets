package com.ibm.aglets;

/*
 * @(#)MessageOutputStream.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @see MessageInputStream
 */
final class MessageOutputStream extends ObjectOutputStream {

    private ResourceManager rm = null;

    /**
     * Create a new instance of this class with version given.
     * 
     * @param out
     *            an output stream where data are written into.
     * @exception IOException
     *                if can not write into the output stream.
     */
    MessageOutputStream(OutputStream out, ResourceManager rm)
	    throws IOException {
	super(out);
	this.rm = rm;
    }

    /**
     * Write the class data into the output stream. Class data of all super
     * classes of the class will be written together.
     * 
     * @param cl
     *            class.
     * @exception IOException
     *                if can not write into the output stream.
     */
    @Override
    synchronized public void annotateClass(Class cls) throws IOException {
	ClassLoader loader = cls.getClassLoader();

	if ((loader != null) && (loader instanceof ResourceManager)
		&& ((this.rm == null) || (this.rm.contains(cls) == false))) {
	    throw new AgletsSecurityException(cls.getName());
	}
    }

    static byte[] toByteArray(ResourceManager rm, Object obj)
							     throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	MessageOutputStream mos = new MessageOutputStream(out, rm);

	mos.writeObject(obj);
	return out.toByteArray();
    }
}
