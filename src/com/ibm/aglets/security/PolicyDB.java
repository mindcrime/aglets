package com.ibm.aglets.security;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.URIPattern;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;

/*
 *  @(#)PolicyDB.java
 *
 *  IBM Confidential-Restricted
 *
 *  OCO Source Materials
 *
 *  03L7246 (c) Copyright IBM Corp. 1996, 1998
 *
 *  The source code for this program is not published or otherwise
 *  divested of its trade secrets, irrespective of what has been
 *  deposited with the U.S. Copyright Office.
 */
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import java.util.Vector;

/**
 *  The <tt>PolicyDB</tt> class accesses Java policy database file and returns
 *  permissions for specified code source.
 *
 *@author     ONO Kouichi
 *@version    1.00 $Date: 2002/02/20 22:17:18 $
 */
public class PolicyDB {

    private final static String CODEBASE = "codebase";
    private final static String PROTOCOL_FILE = "file";
    private final static String WILDCARD_ANYDIR = "-";
    private final static String PORT_LEADER = ":";
    private URL _system_codebase = null;
    private String _public_root = null;
    private Vector _grants = new Vector();


    /**
     *  Constructor for the PolicyDB object
     */
    public PolicyDB() { }


    /**
     *  Gets the hostPart attribute of the PolicyDB class
     *
     *@param  name  Description of Parameter
     *@return       The hostPart value
     */
    private final static String getHostPart(String name) {
        String hostpart = name;
        final int i = name.indexOf(PORT_LEADER);

        if (i >= 0) {
            hostpart = name.substring(0, i);
        }
        return hostpart;
    }


    /**
     *  Gets the portPart attribute of the PolicyDB class
     *
     *@param  name  Description of Parameter
     *@return       The portPart value
     */
    private final static String getPortPart(String name) {
        String portpart = null;
        final int i = name.indexOf(PORT_LEADER);

        if (i >= 0) {
            portpart = name.substring(i + 1);
        }
        return portpart;
    }


    /**
     *  Gets the grants attribute of the PolicyDB object
     *
     *@return    The grants value
     */
    public Enumeration getGrants() {
        return _grants.elements();
    }


    /**
     *  Gets permissions for indicated code source.
     *
     *@param  cs  code source
     *@return     permissions for indicated code source. null when the indicated
     *      code source has no permissions.
     */
    public Permissions getPermissions(CodeSource cs) {
        return getPermissions(cs, true);
    }


    /**
     *  Gets permissions for indicated code source. When conversion of code
     *  source is required, converts "codebase" into current code source.
     *
     *@param  cs       code source
     *@param  convert  conversion of code source
     *@return          permissions for indicated code source. null when the
     *      indicated code source has no permissions.
     */
    public Permissions getPermissions(CodeSource cs, boolean convert) {
        Permissions permissions = null;
        final int num = _grants.size();

        for (int i = 0; i < num; i++) {
            PolicyGrant grant = (PolicyGrant) _grants.elementAt(i);
            Permissions perms = grant.getPermissions(cs);

            if (perms != null) {

                if (permissions == null) {
                    permissions = new Permissions();
                }
                Enumeration ps = perms.elements();

                while (ps.hasMoreElements()) {
                    Permission perm = (Permission) ps.nextElement();

                    // These conversion should be placed on PolicyImpl class.
                    // Because no converted permissions are needed for editing.
                    // So the flag to request conversion is added as the last argument.
                    if (convert) {
                        if (perm instanceof FilePermission) {

                            // special name "codebase" for Aglets of FilePermission
                            // The name "codebase" means a filesystem of current code source
                            final FilePermission filep = (FilePermission) perm;

                            if (CODEBASE.equalsIgnoreCase(filep.getName())) {
                                final URL cb = cs.getLocation();

                                if (cb != null) {
                                    Permission p =
                                            (Permission) AccessController.doPrivileged(
                                        new PrivilegedAction() {
                                            public Object run() {
                                                if (PROTOCOL_FILE.equalsIgnoreCase(cb.getProtocol())) {
                                                    String file =
                                                            URIPattern.canonicalFilename(cb.getFile());

                                                    file += File.separator
                                                             + WILDCARD_ANYDIR;
                                                    return new FilePermission(file, filep.getActions());
                                                } else if (cb.equals(_system_codebase)) {
                                                    String file = _public_root;

                                                    if (file.charAt(file.length() - 1)
                                                             != File.separatorChar) {
                                                        file += File.separator;
                                                    }
                                                    file += WILDCARD_ANYDIR;
                                                    return new FilePermission(file, filep.getActions());
                                                }
                                                return null;
                                            }
                                        });

                                    if (p != null) {
                                        perm = p;
                                    }
                                }
                            }
                        } else if (perm instanceof SocketPermission) {

                            // special name "codebase" for Aglets of SocketPermission
                            // The name "codebase" means a host of current code source
                            SocketPermission socketp = (SocketPermission) perm;
                            String name = socketp.getName();
                            String hostpart = getHostPart(name);
                            String portpart = getPortPart(name);

                            if (CODEBASE.equalsIgnoreCase(hostpart)) {
                                URL cb = cs.getLocation();

                                if (cb != null) {
                                    hostpart = cb.getHost();
                                }
                                String n = null;

                                if (portpart != null) {
                                    n = hostpart + PORT_LEADER + portpart;
                                } else {
                                    n = hostpart;
                                }
                                perm =
                                        new SocketPermission(n,
                                        socketp.getActions());
                            }
                        }
                    }
                    permissions.add(perm);
                }
            }
        }
        return permissions;
    }


    /**
     *  Adds a feature to the Grant attribute of the PolicyDB object
     *
     *@param  grant  The feature to be added to the Grant attribute
     */
    public void addGrant(PolicyGrant grant) {
        _grants.addElement(grant);
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Returned Value
     */
    public String toString() {
        int num = _grants.size();
        String str = "";
        int i;

        for (i = 0; i < num; i++) {
            Object obj = _grants.elementAt(i);

            if (obj != null) {
                str += obj.toString();
            }
        }
        return str;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Returned Value
     */
    public Vector toVector() {
        final int num = _grants.size();
        Vector lines = new Vector();
        int idx = 0;

        for (idx = 0; idx < num; idx++) {
            Object obj = _grants.elementAt(idx);

            if (obj != null && obj instanceof PolicyGrant) {
                PolicyGrant grant = (PolicyGrant) obj;
                Vector grantLines = grant.toVector();
                final int n = grantLines.size();
                int i = 0;

                for (i = 0; i < n; i++) {
                    lines.addElement(grantLines.elementAt(i));
                }
            }
        }
        return lines;
    }


    /**
     *  Sets the publicRoot attribute of the PolicyDB object
     *
     *@param  path  The new publicRoot value
     */
    void setPublicRoot(String path) {
        _public_root = path;
    }


    /**
     *  Sets the systemCodeBase attribute of the PolicyDB object
     *
     *@param  codebase  The new systemCodeBase value
     */
    void setSystemCodeBase(String codebase) {
        try {
            setSystemCodeBase(new URL(codebase));
        } catch (MalformedURLException excpt) {
        }
    }


    /**
     *  Sets the systemCodeBase attribute of the PolicyDB object
     *
     *@param  codebase  The new systemCodeBase value
     */
    void setSystemCodeBase(URL codebase) {
        _system_codebase = codebase;
    }
}

