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

import com.ibm.maf.*;

// # import com.ibm.aglets.security.Allowance;
import com.ibm.aglet.security.Protections;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.event.CloneListener;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.event.PersistencyListener;

// import com.ibm.aglets.security.Authenticator;
import com.ibm.awb.misc.Archive;

import java.security.Identity;
import java.net.URL;
import java.io.*;

import java.util.Hashtable;

/**
 * <tt> AgletReader </tt>
 * 
 * @version     1.10	$Date: 2009/07/27 10:31:41 $
 * @author	Mitsuru Oshima
 * @author	ONO Kouichi
 */
final class AgletReader {
	private InputStream _is = null;
	private ObjectInputStream _ois = null;

	/*
	 * 
	 */
	AgletReader(byte[] agent) throws IOException {
		_is = new ByteArrayInputStream(agent);
		_ois = new ObjectInputStream(_is);
	}
	void readAglet(LocalAgletRef ref) 
			throws IOException, ClassNotFoundException {
		ref.resourceManager.setResourceManagerContext();

		Archive a = (Archive)_ois.readObject();

		ref.resourceManager.importArchive(a);

		AgletInputStream ais = new AgletInputStream(_is, ref.resourceManager);

		try {

			// 
			// MessageManager
			// 
			ref.setMessageManager((MessageManagerImpl)ais.readObject());

			// 
			// Aglet
			// 
			ref.setAglet((Aglet)ais.readObject());

		} 
		finally {
			ref.resourceManager.unsetResourceManagerContext();
		} 
	}
	void readInfo(LocalAgletRef ref) 
			throws IOException, ClassNotFoundException {
		ref.info = (AgletInfo)_ois.readObject();

		// # 	//
		// # 	// Allowance
		// # 	//
		// # 	ref.allowance = (Allowance)_ois.readObject();

		// 
		// Protections
		// 
		ref.protections = (Protections)_ois.readObject();

		// 
		// secure/unsecure
		// 
		ref.setSecurity(_ois.readBoolean());
	}
}
