package com.ibm.aglets.tahiti;

/*
 * @(#)PersistenceFactory.java
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

import com.ibm.aglets.Persistence;
import com.ibm.aglets.AgletRuntime;
import com.ibm.awb.misc.Resource;
import java.io.File;
import java.net.URL;

class PersistenceFactory implements com.ibm.aglets.PersistenceFactory {

	static String spool_dir = null;
	static {
		Resource r = Resource.getResourceFor("aglets");

		spool_dir = r.getString("aglets.spool", ".");
	} 

	private String _dir;

	PersistenceFactory() {
		String server = AgletRuntime.getAgletRuntime().getServerAddress();

		try {
			URL u = new URL(server);

			_dir = spool_dir + File.separator + u.getHost() + '@' 
				   + u.getPort() + File.separator;
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public Persistence createPersistenceFor(com.ibm.aglet.AgletContext cxt) {

		// 
		// Create a default persistence if doesn't exist.
		// 
		// spool_dir += File.separator + server.getHost() + '@' +
		// server.getPort();
		try {
			return new SimplePersistence(_dir + cxt.getName());
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
			return null;
		} 
	}
}
