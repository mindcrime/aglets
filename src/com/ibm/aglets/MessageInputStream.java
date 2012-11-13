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

import net.sourceforge.aglets.log.AgletsLogger;

/**
 * 
 */

final class MessageInputStream extends ObjectInputStream {
	private static AgletsLogger logger = AgletsLogger.getLogger(MessageInputStream.class.getName());
	static Object toObject(final ResourceManager rm, final byte[] b)
	throws OptionalDataException,
	ClassNotFoundException,
	IOException {
		final ByteArrayInputStream in = new ByteArrayInputStream(b);
		final MessageInputStream ois = new MessageInputStream(in, rm);

		return ois.readObject();
	}

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
	public MessageInputStream(final InputStream in, final ResourceManager rm)
	throws IOException {
		super(in);
		this.rm = rm;
	}

	/**
	 * @param osc
	 *            object stream.
	 * @return the resolved class.
	 * @exception IOException
	 *                if can not read data from the input stream.
	 * @exception ClassNotFoundException
	 *                if can not resolve the class.
	 */
	@Override
	public Class resolveClass(final ObjectStreamClass osc)
	throws IOException,
	ClassNotFoundException {

		final Class cls = (rm == null) ? Class.forName(osc.getName())
				: rm.loadClass(osc.getName());

		final ClassLoader loader = cls.getClassLoader();

		if ((loader == null) || !(loader instanceof ResourceManager)
				|| rm.contains(cls)) {
			return cls;
		}

		throw new AgletsSecurityException();
	}
}
