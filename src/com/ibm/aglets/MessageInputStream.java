package com.ibm.aglets;

/*
 * @(#)MessageInputStream.java
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import org.aglets.log.AgletsLogger;

/**
 * 
 */

final class MessageInputStream extends ObjectInputStream {
    private static AgletsLogger logger = AgletsLogger.getLogger(MessageInputStream.class.getName());
    private ResourceManager rm = null;

    /**
     * Create a new instance of this class.
     * 
     * @param in
     *            an input stream containing objests and class data.
     * @exception IOException
     *                if can not read data from the input stream.
     * @exception StreamCorruptedException
     *                if data in the input stream is invalid.
     */
    public MessageInputStream(InputStream in, ResourceManager rm)
	    throws IOException {
	super(in);
	this.rm = rm;
    }

    /**
     * @param classname
     *            class name.
     * @return the resolved class.
     * @exception IOException
     *                if can not read data from the input stream.
     * @exception ClassNotFoundException
     *                if can not resolve the class.
     */
    @Override
    public Class resolveClass(ObjectStreamClass osc)
						    throws IOException,
						    ClassNotFoundException {

	Class cls = (this.rm == null) ? Class.forName(osc.getName())
		: this.rm.loadClass(osc.getName());

	ClassLoader loader = cls.getClassLoader();

	if ((loader == null) || !(loader instanceof ResourceManager)
		|| this.rm.contains(cls)) {
	    return cls;
	}

	throw new AgletsSecurityException();
    }

    static Object toObject(ResourceManager rm, byte[] b)
							throws OptionalDataException,
							ClassNotFoundException,
							IOException {
	ByteArrayInputStream in = new ByteArrayInputStream(b);
	MessageInputStream ois = new MessageInputStream(in, rm);

	return ois.readObject();
    }
}
