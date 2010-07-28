package com.ibm.atp.auth;

/*
 * @(#)AuthenticationManager.java
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

import java.util.Hashtable;

/**
 * The <tt>AuthenticationManager</tt> class is the manager class of
 * authentication.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class AuthenticationManager {
    /**
     * Hashtable of server identifiers and domain names
     */
    private static Hashtable _table = new Hashtable();

    /**
     * Un-register all entries.
     */
    public final static synchronized void clear() {
	_table.clear();
    }

    /**
     * Gets doman name of registered server identifier.
     * 
     * @param id
     *            server identifier to be registered as authenticated
     * @return domain name the server belongs
     */
    public final static synchronized String getDomainName(ServerIdentifier id) {
	Object obj = _table.get(id);

	if (obj == null) {
	    return null;
	}
	if (!(obj instanceof String)) {
	    return null;
	}
	return (String) obj;
    }

    /**
     * Register a server identifier with a domain name.
     * 
     * @param id
     *            server identifier to be registered as authenticated
     * @param domain
     *            domain name the server belongs
     */
    public final static synchronized void register(
						   ServerIdentifier id,
						   String domain) {
	_table.put(id, domain);
    }

    /**
     * Un-register a server identifier.
     * 
     * @param id
     *            server identifier to be registered as authenticated
     */
    public final static synchronized void unregister(ServerIdentifier id) {
	_table.remove(id);
    }
}
