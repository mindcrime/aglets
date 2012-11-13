package com.ibm.maf;

/*
 * @(#)AgentSystemInfo.java
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
 * File: ./CfMAF/AgentSystemInfo.java
 * From: maf.idl
 * Date: Fri Aug 29 15:13:36 1997
 * By: idltojava JavaIDL Thu Feb 27 11:22:49 1997
 */

public final class AgentSystemInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2485496872165622357L;
	// instance variables
	public Name agent_system_name;
	public short agent_system_type;
	public LanguageMap[] language_maps;
	public String agent_system_description;
	public short major_version;
	public short minor_version;
	public Object[] properties; // any

	// constructors
	public AgentSystemInfo() {
	}

	public AgentSystemInfo(final Name __agent_system_name, final short __agent_system_type,
	                       final LanguageMap[] __language_maps, final String __agent_system_description,
	                       final short __major_version, final short __minor_version, final Object[] __properties) {

		agent_system_name = __agent_system_name;
		agent_system_type = __agent_system_type;
		language_maps = __language_maps;
		agent_system_description = __agent_system_description;
		major_version = __major_version;
		minor_version = __minor_version;
		properties = __properties;
	}
}
