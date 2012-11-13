package com.ibm.aglets;

import java.security.cert.Certificate;

import com.ibm.aglet.AgletInfo;
import com.ibm.maf.AgentProfile;
import com.ibm.maf.MAFUtil;
import com.ibm.maf.Name;

/*
 * @(#)MAF.java
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

// package
final class MAF {
	static Certificate unknown_user_cert = AgletRuntime.getAnonymousUserCertificate();

	static AgentProfile toAgentProfile(final AgletInfo info) {
		final AgentProfile p = new AgentProfile(MAFUtil.toLanguageID("Java"), MAFUtil.toAgentSystemType("Aglets"), "Aglets 2.5 alpha", info.getAPIMajorVersion(), info.getAPIMinorVersion(), (short) 1, /* serialization */
				null);

		// Object[] __properties);
		return p;
	}

	static Name toAgentSystemName(
	                              final MAFAgentSystem_AgletsImpl sys,
	                              final Certificate owner) {
		final java.util.Random r = new java.util.Random();
		final byte[] ident_bytes = new byte[8];

		r.nextBytes(ident_bytes);
		try {
			byte[] ownerEncoded = null;

			if (owner != null) {
				ownerEncoded = owner.getEncoded();
			}
			return new Name(ownerEncoded, ident_bytes, MAFUtil.AGENT_SYSTEM_TYPE_AGLETS);
		} catch (final java.security.cert.CertificateEncodingException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	static AgletInfo toAgletInfo(final AgentProfile profile) {
		return null;
	}

	public void print(final Name name) {
		System.out.println("type = " + name.agent_system_type);
		System.out.println("auth = " + new String(name.authority));
	}
}
