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
	this._oout = new ObjectOutputStream(this._baos);
    }

    /*
     * 
     */
    byte[] getBytes() {
	return this._baos.toByteArray();
    }

    /*
     * 
     */
    ClassName[] getClassNames() {
	return this._table;
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
	this._table = aos.getClassNames(ref.resourceManager);
	this._oout.writeObject(ref.resourceManager.getArchive(this._table));
	this._oout.flush();

	tmp.writeTo(this._baos);
    }

    void writeInfo(LocalAgletRef ref) throws IOException {

	//
	// AgletInfo
	//
	this._oout.writeObject(ref.info);

	//
	// Protections
	//
	this._oout.writeObject(ref.protections);

	//
	// secure/unsecure
	//
	this._oout.writeBoolean(ref.getSecurity());
    }
}
