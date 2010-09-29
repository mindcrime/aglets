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

    public AgentSystemInfo(Name __agent_system_name, short __agent_system_type,
                           LanguageMap[] __language_maps, String __agent_system_description,
                           short __major_version, short __minor_version, Object[] __properties) {

	this.agent_system_name = __agent_system_name;
	this.agent_system_type = __agent_system_type;
	this.language_maps = __language_maps;
	this.agent_system_description = __agent_system_description;
	this.major_version = __major_version;
	this.minor_version = __minor_version;
	this.properties = __properties;
    }
}
