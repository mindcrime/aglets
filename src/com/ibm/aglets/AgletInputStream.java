package com.ibm.aglets;

/*
 * @(#)AgletInputStream.java
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

/**
 * An instance of this class reads objects from an input stream which contains
 * class data with objects. The input stream contains objects, class data of
 * these objects and class data of all super classes of these classes. Data in
 * the input stream must be written by an instance of the AgletOutputStream.
 * <p>
 * 
 * This aglet input stream looks into the class loader cache of the ResourceManager
 * and gets a class loader corresponding to the URL of an origin of the received
 * class. If the class loader is not found in the cache, this stream will
 * creates a new class loader and put it into the cache. After getting the class
 * loader, this stream gets a class of the received object from the class cache
 * of the class loader. If the class is not found in the class cache, the class
 * loader will create the class.
 * 
 * @see AgletOutputStream
 * @see ResourceManager
 * @version 1.00 96/06/28
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */

final class AgletInputStream extends ObjectInputStream {

	private ResourceManager rm = null;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param in
	 *            an input stream containing objects and class data.
	 * @exception IOException
	 *                if can not read data from the input stream.
	 * @exception StreamCorruptedException
	 *                if data in the input stream is invalid.
	 */
	public AgletInputStream(final InputStream in, final ResourceManager rm)
	throws IOException {
		super(in);
		this.rm = rm;
	}

	/*
	 * Verify and check whether the stream contains the aglet
	 */
	@Override
	protected void readStreamHeader()
	throws IOException,
	StreamCorruptedException {
		final int incoming_magic = readInt();

		if (incoming_magic != AgletRuntime.AGLET_MAGIC) {
			throw new StreamCorruptedException("InputStream does not contain an aglet");
		}

		final byte stream_version = readByte();

		if (stream_version != AgletRuntime.AGLET_STREAM_VERSION) {
			throw new StreamCorruptedException("Stream Version Mismatch: "
					+ stream_version);
		}
	}

	/**
	 * Resolve a class specified by classinfo. This method reads class data from
	 * the input stream and resolves the class by using a class loader
	 * corresponding to the origin of the class. If the class is common class,
	 * the class will be resolved by super.resolveClass. This method reads class
	 * data of all super classes of the class in the input stream and put them
	 * into the class data cache of the class loader. These super classes will
	 * be resolved on demand.
	 * 
	 * @param classinfo
	 *            stream containing class data.
	 * @return the resolved class.
	 * @exception IOException
	 *                if can not read data from the input stream.
	 * @exception ClassNotFoundException
	 *                if can not resolve the class.
	 */
	@Override
	public Class resolveClass(final ObjectStreamClass classinfo)
	throws IOException,
	ClassNotFoundException {
		return rm.loadClass(classinfo.getName());
	}
}
