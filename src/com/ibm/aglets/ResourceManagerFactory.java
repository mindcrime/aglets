package com.ibm.aglets;

/*
 * @(#)ResourceManagerFactory.java
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

import java.net.URL;
import java.security.cert.Certificate;
import com.ibm.maf.ClassName;

// import com.ibm.awb.misc.DigestTable;

public interface ResourceManagerFactory {

	void clearCache();
	void clearCache(URL codebase, Certificate owner);
	ResourceManager createResourceManager(URL codebase, Certificate owner, 
										  ClassName[] table);
	ResourceManager getCurrentResourceManager();
	URL lookupCodeBaseFor(String name);
}
