package com.ibm.maf.rmi;

/*
 * @(#)LocationInfo.java
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
import com.ibm.maf.AgentProfile;
import com.ibm.maf.AgentSystemInfo;

public class LocationInfo implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 8597414854651255669L;
    private String _location;
    private Object _info;

    public LocationInfo(String location, Object info) {
	this._location = location;
	this._info = info;
    }

    public AgentProfile getAgentProfile() {
	return (AgentProfile) this._info;
    }

    public AgentSystemInfo getAgentSystemInfo() {
	return (AgentSystemInfo) this._info;
    }

    public Object getInfo() {
	return this._info;
    }

    public String getLocation() {
	return this._location;
    }

    public boolean matchAgentProfile(AgentProfile p) {
	if (p == null) {
	    return true;
	}
	AgentProfile my = this.getAgentProfile();

	if ((p.language_id != 0) && (p.language_id != my.language_id)) {
	    return false;
	}
	if ((p.agent_system_type != 0)
		&& (p.agent_system_type != my.agent_system_type)) {
	    return false;
	}
	if ((p.agent_system_description != null)
		&& !(my.agent_system_description.equals(p.agent_system_description))) {
	    return false;
	}
	if ((p.major_version != 0) && (p.major_version != my.major_version)) {
	    return false;
	}
	if ((p.minor_version != 0) && (p.minor_version != my.minor_version)) {
	    return false;
	}
	if ((p.serialization != 0) && (p.serialization != my.serialization)) {
	    return false;
	}
	if (p.properties != null) {

	    // Not implemented yet.
	}
	return true;
    }

    public boolean matchAgentSystemInfo(AgentSystemInfo p) {
	if (p == null) {
	    return true;
	}
	AgentSystemInfo my = this.getAgentSystemInfo();

	if ((p.agent_system_name != null)
		&& !(p.agent_system_name.equals(my.agent_system_name))) {
	    return false;
	}
	if ((p.agent_system_type != 0)
		&& (p.agent_system_type != my.agent_system_type)) {
	    return false;
	}
	if (p.language_maps != null) {

	    // Not implemented yet
	}
	if ((p.agent_system_description != null)
		&& !(p.agent_system_description.equals(my.agent_system_description))) {
	    return false;
	}
	if ((p.major_version != 0) && (p.major_version != my.major_version)) {
	    return false;
	}
	if ((p.minor_version != 0) && (p.minor_version != my.minor_version)) {
	    return false;
	}
	if (p.properties != null) {

	    // Not implemented yet.
	}
	return true;
    }

    @Override
    public String toString() {
	return this._location;
    }
}
