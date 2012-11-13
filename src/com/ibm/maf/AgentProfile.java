package com.ibm.maf;

/*
 * @(#)AgentProfile.java
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

/*
 * File: ./CfMAF/AgentProfile.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class AgentProfile implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1256734124644017705L;
	// instance variables
	public short language_id;
	public short agent_system_type;
	public String agent_system_description;
	public short major_version;
	public short minor_version;
	public short serialization;
	public Object[] properties; // any

	// constructors
	public AgentProfile() {
	}

	public AgentProfile(final short __language_id, final short __agent_system_type,
	                    final String __agent_system_description, final short __major_version,
	                    final short __minor_version, final short __serialization, final Object[] __properties) {
		language_id = __language_id;
		agent_system_type = __agent_system_type;
		agent_system_description = __agent_system_description;
		major_version = __major_version;
		minor_version = __minor_version;
		serialization = __serialization;
		properties = __properties;
	}
}
