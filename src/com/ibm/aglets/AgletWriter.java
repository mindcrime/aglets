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

import com.ibm.maf.*;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.event.CloneListener;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.event.PersistencyListener;

// import com.ibm.awb.misc.DigestTable;
import com.ibm.awb.misc.Archive;
import com.ibm.maf.ClassName;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * <tt> AgletWriter </tt>
 * 
 * Format
 * AGLET_MAGIC
 * STREAM_VERSION
 * IDENTITY
 * OTHER INFO
 * 
 * CLASS TABLE
 * MESSAGE MANAGER
 * AGLET
 * BYTECODE
 * 
 * @version     1.20	$Date: 2009/07/28 07:04:53 $
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
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
	void writeAglet(LocalAgletRef ref) throws IOException {

		ByteArrayOutputStream tmp = new ByteArrayOutputStream();
		AgletOutputStream aos = new AgletOutputStream(tmp);

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
	void writeInfo(LocalAgletRef ref) throws IOException {

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
