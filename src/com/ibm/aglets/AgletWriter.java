package com.ibm.aglets;

/*
 * @(#)AgletWriter.java
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

import com.ibm.maf.ClassName;

/**
 * <tt> AgletWriter </tt>
 * 
 * Format AGLET_MAGIC STREAM_VERSION IDENTITY OTHER INFO
 * 
 * CLASS TABLE MESSAGE MANAGER AGLET BYTECODE
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
final class AgletWriter {
	ByteArrayOutputStream _baos = new ByteArrayOutputStream();
	ObjectOutputStream _oout = null;
	ClassName[] _table = null;

	AgletWriter() throws IOException {
		_oout = new ObjectOutputStream(_baos);
	}

	/*
	 * 
	 */
	byte[] getBytes() {
		return _baos.toByteArray();
	}

	/*
	 * 
	 */
	ClassName[] getClassNames() {
		return _table;
	}

	void writeAglet(final LocalAgletRef ref) throws IOException {

		final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		final AgletOutputStream aos = new AgletOutputStream(tmp);

		//
		// Message Manager
		//
		aos.writeObject(ref.messageManager);

		//
		// Aglet
		//
		aos.writeObject(ref.aglet);
		aos.flush();

		//
		// writes the table first
		//
		_table = aos.getClassNames(ref.resourceManager);
		_oout.writeObject(ref.resourceManager.getArchive(_table));
		_oout.flush();

		tmp.writeTo(_baos);
	}

	void writeInfo(final LocalAgletRef ref) throws IOException {

		//
		// AgletInfo
		//
		_oout.writeObject(ref.info);

		//
		// Protections
		//
		_oout.writeObject(ref.protections);

		//
		// secure/unsecure
		//
		_oout.writeBoolean(ref.getSecurity());
	}
}
