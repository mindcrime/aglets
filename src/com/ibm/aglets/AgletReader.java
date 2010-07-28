package com.ibm.aglets;

/*
 * @(#)AgletReader.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.security.Protections;
import com.ibm.awb.misc.Archive;

/**
 * <tt> AgletReader </tt>
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @author ONO Kouichi
 */
final class AgletReader {
    private InputStream _is = null;
    private ObjectInputStream _ois = null;

    /*
	 * 
	 */
    AgletReader(byte[] agent) throws IOException {
	this._is = new ByteArrayInputStream(agent);
	this._ois = new ObjectInputStream(this._is);
    }

    void readAglet(LocalAgletRef ref)
				     throws IOException,
				     ClassNotFoundException {
	ref.resourceManager.setResourceManagerContext();

	Archive a = (Archive) this._ois.readObject();

	ref.resourceManager.importArchive(a);

	AgletInputStream ais = new AgletInputStream(this._is, ref.resourceManager);

	try {

	    //
	    // MessageManager
	    //
	    ref.setMessageManager((MessageManagerImpl) ais.readObject());

	    //
	    // Aglet
	    //
	    ref.setAglet((Aglet) ais.readObject());

	} finally {
	    ref.resourceManager.unsetResourceManagerContext();
	}
    }

    void readInfo(LocalAgletRef ref) throws IOException, ClassNotFoundException {
	ref.info = (AgletInfo) this._ois.readObject();

	// # //
	// # // Allowance
	// # //
	// # ref.allowance = (Allowance)_ois.readObject();

	//
	// Protections
	//
	ref.protections = (Protections) this._ois.readObject();

	//
	// secure/unsecure
	//
	ref.setSecurity(this._ois.readBoolean());
    }
}
