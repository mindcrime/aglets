package com.ibm.aglets.security;

/*
 * @(#)PolicyImpl.java
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilePermission;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.URIPattern;

/**
 * The <tt>PolicyImpl</tt> class is an implementation of Policy abstract class
 * for Aglets.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class PolicyImpl extends Policy {
    private static final String PROPERTY_JAVA_CLASS_PATH = "java.class.path";
    private static final String PROPERTY_AGLETS_CLASS_PATH = "aglets.class.path";
    private static final String PROPERTY_USER_DIRECTORY = "user.dir";
    private static final String PROPERTY_AGLETS_HOME = "aglets.home";
    private static final String PROPERTY_JAVA_HOME = "java.home";

    private static final String JAVA_CLASS_PATH = getSystemProperty(PROPERTY_JAVA_CLASS_PATH, "");
    private static final String AGLETS_CLASS_PATH = getSystemProperty(PROPERTY_AGLETS_CLASS_PATH, "");
    private static final String USER_DIRECTORY = getSystemProperty(PROPERTY_USER_DIRECTORY);
    private static final String AGLETS_HOME = getSystemProperty(PROPERTY_AGLETS_HOME);
    private static final String JAVA_HOME = getSystemProperty(PROPERTY_JAVA_HOME);

    private static final String SEP = File.separator;
    private static final char SEPCHAR = File.separatorChar;

    private Permissions _systemPermissions = null;
    private Permissions _appPermissions = null;
    private Permissions _untrustedPermissions = new Permissions();

    private PolicyDB _policyDB = null;

    private static final String PATH_SEPARATORS = " " + File.pathSeparator;
    private static final char CHAR_DOT = '.';
    private static final String STRING_DOT = String.valueOf(CHAR_DOT);
    private static final String CURRENT_DIRECTORY = STRING_DOT;

    private static final String defaultAgletsPolicy[] = {
	    "//",
	    "// Aglets Security Policy File",
	    "//",
	    "// This file should be placed on",
	    "//      {user.home}/.aglets/security/aglets.policy",
	    "//",
	    "// ------------------------------------------------------------",
	    "// If you wish to specify a backslash character ('\\'),",
	    "// in the policy file (e.g. \"C:\\tmp\\ASDK\")",
	    "// use double backslashes \"\\\\\".",
	    "// For example,",
	    "//   permission java.io.FilePermission \"C:\\\\tmp\\\\ASDK\", \"read\";",
	    "// But you can substitute a slash character ('/') for backslash like this:",
	    "//   permission java.io.FilePermission \"C:/tmp/ASDK\", \"read\";",
	    "// ------------------------------------------------------------",
	    "//",
	    "//",
	    "// ------------------------------------------------------------",
	    "// sample",
	    "// ------------------------------------------------------------",
	    "//",
	    "grant",
	    " // codeBase \"atp://host.foo.bar:4434/-\"",
	    " // codeBase \"atp://*.ibm.com:>=1024/\"",
	    " // codeBase \"atp://*.ibm.com:2000-3000/\"",
	    " // codeBase \"*://*:*/\"",
	    " codeBase \"atp://*:*/\"",
	    " // , signedBy \"onono,moshima\" /* code is signed by onono and moshima */",
	    " // , ownedBy \"kosaka,mima\" /* the aglet is created by kosaka or mima */",
	    "{",
	    "  // aglet protections",
	    "  protection com.ibm.aglet.security.AgletProtection",
	    "    \"*\", \"dispatch,dispose,deactivate,activate,clone,retract\";",
	    "",
	    "  // message protections",
	    "  protection com.ibm.aglet.security.MessageProtection",
	    "    \"*\", \"*\";",
	    "",

	    // # "  // allowance",
	    // #
	    // "  // any aglet has an allowance to make clones of itself any times,",
	    // # "  // to move any hops,",
	    // # "  // and to live till it is disposed explicitly.",
	    // # "  allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "    \"*\", \"cloning=infinite, hops=infinite, lifetime=unlimited\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live for 3600000 miliseconds (=1hour).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // # "  //  \"*\", \"cloning=3, hops=5, lifetime=+3600000\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live by 1998.12.31 12:00:00.000 (JST).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "  //  \"*\", \"cloning=3, hops=5, lifetime=1998.12.31-12:00:00.000 (JST)\";",
	    // # "",
	    "  // aglet",
	    "  permission com.ibm.aglets.security.AgletPermission",
	    "    \"*\", \"dispatch,dispose,deactivate,activate,clone,retract\";",
	    "",
	    "  // messages",
	    "  permission com.ibm.aglets.security.MessagePermission",
	    "    \"*\", \"*\";",
	    "",
	    "  // aglet context",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"*\", \"multicast,subscribe\";",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"*\", \"create,receive,retract\";",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"property.*\", \"read,write\";",
	    "",
	    "  // runtime",
	    "  permission java.lang.RuntimePermission",
	    "    \"createClassLoader\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.java.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.java.*\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglets.util.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglets.util.*\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglets.AgletProxyImpl\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglets.AgletProxyImpl\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglet.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglet.*\";",
	    "  permission java.lang.RuntimePermission",
	    "    \"loadLibrary.JdbcOdbc\";         // for JDBC/ODBC",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.sun.jdbc.odbc\"; // for JDBC/ODBC",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.sun.jdbc.odbc\"; // for JDBC/ODBC",
	    "",
	    "  // window",

	    // JDK 1.2beta3
	    // - "  permission java.awt.AWTPermission \"topLevelWindow\";",
	    // JDK 1.2beta4
	    "  permission java.awt.AWTPermission \"showWindowWithoutWarningBanner\";",
	    "",
	    "  // property",
	    "  permission java.util.PropertyPermission \"awt.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"hotjava.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"apple.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"file.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"line.separator\", \"read\";",
	    "  permission java.util.PropertyPermission \"path.separator\", \"read\";",
	    "  permission java.util.PropertyPermission \"http.maxConnections\", \"read\";",
	    "  permission java.util.PropertyPermission \"user.timezone\", \"read\";",
	    "  permission java.util.PropertyPermission \"socksProxyHost\", \"read\";",
	    "  permission java.util.PropertyPermission \"socksProxyPort\", \"read\";",
	    "  // for JDBC/ODBC",
	    "  permission java.util.PropertyPermission \"browser\", \"read\";",
	    "  // for RMI",
	    "  permission java.util.PropertyPermission \"java.rmi.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"sun.rmi.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"http.proxyHost\", \"read\";",
	    "  permission java.util.PropertyPermission \"proxyHost\", \"read\";",
	    "  // for examples.patterns.Finger, examples.patterns.Writer",
	    "  permission java.util.PropertyPermission \"user.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"os.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"java.*\", \"read\";",
	    "",
	    "  // socket",
	    "  permission java.net.SocketPermission \"localhost:*\", \"listen,resolve\";",
	    "  permission java.net.SocketPermission \"codebase:*\", \"connect\";",
	    "",
	    "  // file",
	    "  permission java.io.FilePermission \"codebase\", \"read\";",
	    "};",
	    "",
	    "grant",
	    " codeBase \"http://*:*/\"",
	    "{",
	    "  // aglet protections",
	    "  protection com.ibm.aglet.security.AgletProtection",
	    "    \"*\", \"dispatch,dispose,deactivate,activate,clone,retract\";",
	    "",
	    "  // message protections",
	    "  protection com.ibm.aglet.security.MessageProtection",
	    "    \"*\", \"*\";",
	    "",

	    // # "  // allowance",
	    // #
	    // "  // any aglet has an allowance to make clones of itself any times,",
	    // # "  // to move any hops,",
	    // # "  // and to live till it is disposed explicitly.",
	    // # "  allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "    \"*\", \"cloning=infinite, hops=infinite, lifetime=unlimited\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live for 3600000 miliseconds (=1hour).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // # "  //  \"*\", \"cloning=3, hops=5, lifetime=+3600000\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live by 1998.12.31 12:00:00.000 (JST).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "  //  \"*\", \"cloning=3, hops=5, lifetime=1998.12.31-12:00:00.000 (JST)\";",
	    // # "",
	    "  // aglet",
	    "  permission com.ibm.aglets.security.AgletPermission",
	    "    \"*\", \"dispatch,dispose,deactivate,activate,clone,retract\";",
	    "",
	    "  // messages",
	    "  permission com.ibm.aglets.security.MessagePermission",
	    "    \"*\", \"*\";",
	    "",
	    "  // aglet context",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"*\", \"multicast,subscribe\";",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"*\", \"create,receive,retract\";",
	    "  permission com.ibm.aglets.security.ContextPermission",
	    "    \"property.*\", \"read,write\";",
	    "",
	    "  // runtime",
	    "  permission java.lang.RuntimePermission",
	    "    \"createClassLoader\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.java.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.java.*\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglets.util.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglets.util.*\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglets.AgletProxyImpl\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglets.AgletProxyImpl\";",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.com.ibm.aglet.*\";",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.com.ibm.aglet.*\";",
	    "  permission java.lang.RuntimePermission",
	    "    \"loadLibrary.JdbcOdbc\";         // for JDBC/ODBC",
	    "  permission java.lang.RuntimePermission",

	    // JDK 1.2beta3
	    // - "    \"package.access.sun.jdbc.odbc\"; // for JDBC/ODBC",
	    // JDK 1.2beta4
	    "    \"accessClassInPackage.sun.jdbc.odbc\"; // for JDBC/ODBC",
	    "",
	    "  // window",

	    // JDK 1.2beta3
	    // - "  permission java.awt.AWTPermission \"topLevelWindow\";",
	    // JDK 1.2beta4
	    "  permission java.awt.AWTPermission \"showWindowWithoutWarningBanner\";",
	    "",
	    "  // property",
	    "  permission java.util.PropertyPermission \"awt.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"hotjava.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"apple.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"file.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"line.separator\", \"read\";",
	    "  permission java.util.PropertyPermission \"path.separator\", \"read\";",
	    "  permission java.util.PropertyPermission \"http.maxConnections\", \"read\";",
	    "  permission java.util.PropertyPermission \"user.timezone\", \"read\";",
	    "  permission java.util.PropertyPermission \"socksProxyHost\", \"read\";",
	    "  permission java.util.PropertyPermission \"socksProxyPort\", \"read\";",
	    "  // for JDBC/ODBC",
	    "  permission java.util.PropertyPermission \"browser\", \"read\";",
	    "  // for RMI",
	    "  permission java.util.PropertyPermission \"java.rmi.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"sun.rmi.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"http.proxyHost\", \"read\";",
	    "  permission java.util.PropertyPermission \"proxyHost\", \"read\";",
	    "  // for examples.patterns.Finger, examples.patterns.Writer",
	    "  permission java.util.PropertyPermission \"user.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"os.*\", \"read\";",
	    "  permission java.util.PropertyPermission \"java.*\", \"read\";",
	    "",
	    "  // socket",
	    "  permission java.net.SocketPermission \"localhost:*\", \"listen,resolve\";",
	    "  permission java.net.SocketPermission \"codebase:*\", \"connect\";",
	    "",
	    "  // file",
	    "  permission java.io.FilePermission \"codebase\", \"read\";",
	    "};",
	    "",
	    "grant",
	    " codeBase \"file:///-/\"",
	    "{",
	    "  // aglet protections",
	    "  protection com.ibm.aglet.security.AgletProtection",
	    "    \"*\", \"dispatch,dispose,deactivate,activate,clone,retract\";",
	    "",
	    "  // message protections",
	    "  protection com.ibm.aglet.security.MessageProtection",
	    "    \"*\", \"*\";",
	    "",

	    // # "  // allowance",
	    // #
	    // "  // any aglet has an allowance to make clones of itself any times,",
	    // # "  // to move any hops,",
	    // # "  // and to live till it is disposed explicitly.",
	    // # "  allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "    \"*\", \"cloning=infinite, hops=infinite, lifetime=unlimited\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live for 3600000 miliseconds (=1hour).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // # "  //  \"*\", \"cloning=3, hops=5, lifetime=+3600000\";",
	    // #
	    // "  // any aglet has an allowance to make 3 clones of itself, to move 5 hops,",
	    // # "  // and to live by 1998.12.31 12:00:00.000 (JST).",
	    // # "  // allowance com.ibm.aglet.security.ActivityPermission",
	    // #
	    // "  //  \"*\", \"cloning=3, hops=5, lifetime=1998.12.31-12:00:00.000 (JST)\";",
	    // # "",
	    "  // can do anything",
	    "  permission java.security.AllPermission \"*\", \"*\";", "};" };

    public PolicyImpl() {
	super();

	// System.out.println("###########PolicyImpl was created.");
	this.refresh();
    }

    private void addAppClassPath() {
	this.addClassPath(JAVA_CLASS_PATH);
	this.addClassPath(AGLETS_CLASS_PATH);
    }

    private void addAppPermission(Permission p) {
	if (this._appPermissions == null) {
	    this._appPermissions = new Permissions();
	}
	this._appPermissions.add(p);
    }

    private void addClassPath(String[] class_path_list) {
	if (class_path_list == null) {
	    return;
	}
	for (String element : class_path_list) {
	    String class_path = URIPattern.canonicalFilename(element);

	    if (new File(class_path).isDirectory()) {
		if (class_path.charAt(class_path.length() - 1) != SEPCHAR) {
		    class_path += SEP + "-";
		} else {
		    class_path += "-";
		}
	    }

	    // System.out.println("add class path="+class_path);
	    this.addAppPermission(new FilePermission(class_path, "read"));
	}
    }

    private void addClassPath(String class_path_list) {
	if (class_path_list == null) {
	    return;
	}

	// + addClassPath(FileUtils.strToPathList(class_path_list));
	this.addClassPath(strToPathList(class_path_list));
    }

    private void addClassPath(Enumeration class_path_list) {
	if (class_path_list == null) {
	    return;
	}
	while (class_path_list.hasMoreElements()) {
	    String path = (String) class_path_list.nextElement();

	    if (CURRENT_DIRECTORY.equals(path)) {
		path = USER_DIRECTORY;
	    }
	    String class_path = URIPattern.canonicalFilename(path);

	    if (new File(class_path).isDirectory()) {
		if (class_path.charAt(class_path.length() - 1) != SEPCHAR) {
		    class_path += SEP + "-";
		} else {
		    class_path += "-";
		}
	    }

	    // System.out.println("add class path="+class_path);
	    this.addAppPermission(new FilePermission(class_path, "read"));
	}
    }

    private void addSystemPermission(Permission p) {
	if (this._systemPermissions == null) {
	    this._systemPermissions = new Permissions();
	}
	this._systemPermissions.add(p);
    }

    private void checkAgletsPolicyFile() {
	this.checkAgletsPolicyFile(PolicyFileReader.getUserPolicyFilename());
    }

    private void checkAgletsPolicyFile(String filename) {

	// System.out.println("==========checkAgletsPolicyFile: " + filename);
	// Create aglets policy file
	File file = new File(filename);

	if ((file == null) || !file.exists()) {
	    System.out.println("Aglets Policy File does not exist.");
	    this.makeDefaultAgletsPolicyFile(filename);
	    System.out.println("Aglets Policy File is created.");
	}

	// Create sample policy file
	int idx = filename.lastIndexOf(SEP);
	String dirname = "";

	if (idx >= 0) {
	    dirname = filename.substring(0, idx);
	}
	String samplePolicyFilename = dirname + SEP + "sample.policy";
	File samplePolicyFile = new File(samplePolicyFilename);

	if ((samplePolicyFile == null) || !samplePolicyFile.exists()) {
	    this.makeDefaultAgletsPolicyFile(samplePolicyFilename);
	}
    }

    public PermissionCollection getPermissions(CodeSource cs) {
	if (this._policyDB == null) {
	    return this._untrustedPermissions;
	}
	return this._policyDB.getPermissions(cs);
    }

    static final String getSystemProperty(String key) {
	String value = null;

	try {
	    final String fkey = key;

	    value = (String) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return System.getProperty(fkey);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return value;
    }

    static final String getSystemProperty(String key, String defValue) {
	String value = null;

	try {
	    final String fkey = key;
	    final String defval = defValue;

	    value = (String) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return System.getProperty(fkey, defval);
		}
	    });
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return value;
    }

    private void initAppPermissions() {
	this._appPermissions = new Permissions();

	// Classes in Application Domain can do anything
	this.addAppPermission(new AllPermission());
    }

    private void initSystemPermissions() {
	this._systemPermissions = new Permissions();

	// Classes in System Domain can do anything
	this.addSystemPermission(new AllPermission());
    }

    private void makeAgletsPolicyFile(FileWriter writer, String[] lines)
									throws IOException {
	BufferedWriter buff = new BufferedWriter(writer);
	int i;

	for (i = 0; i < lines.length; i++) {
	    this.writeLine(buff, lines[i]);
	}
	buff.close();
    }

    private void makeDefaultAgletsPolicyFile(FileWriter writer)
							       throws IOException {
	this.makeAgletsPolicyFile(writer, defaultAgletsPolicy);
    }

    private void makeDefaultAgletsPolicyFile(String filename) {
	try {
	    if (FileUtils.ensureDirectory(filename) == false) {
		System.out.println("Aglets Policy File initialization failed.");
		return;
	    }
	    this.makeDefaultAgletsPolicyFile(new FileWriter(filename));
	} catch (IOException excpt) {
	    excpt.printStackTrace();
	}
    }

    public void refresh() {
	this.initSystemPermissions();
	this.initAppPermissions();
	this.checkAgletsPolicyFile();
	this._policyDB = PolicyFileReader.getAllPolicyDB();
    }

    public void setPublicRoot(String path) {
	this._policyDB.setPublicRoot(path);
    }

    public void setSystemCodeBase(String codebase) {
	this._policyDB.setSystemCodeBase(codebase);
    }

    public void setSystemCodeBase(URL codebase) {
	this._policyDB.setSystemCodeBase(codebase);
    }

    private static final String[] strToPathList(String path_list) {
	if (path_list == null) {
	    return null;
	}
	StringTokenizer st = new StringTokenizer(path_list, PATH_SEPARATORS);
	Vector paths = new Vector();

	while (st.hasMoreTokens()) {
	    String path = st.nextToken();

	    if (CURRENT_DIRECTORY.equals(path)) {
		path = USER_DIRECTORY;
	    }
	    if ((path != null) && !path.equals("")) {
		paths.addElement(path);
	    }
	}
	final int num = paths.size();
	String[] list = new String[num];
	int j = 0;

	for (int i = 0; i < num; i++) {
	    Object obj = paths.elementAt(i);

	    if (obj instanceof String) {
		String path = (String) obj;

		list[j] = path;
		j++;
	    }
	}
	return list;
    }

    private void writeLine(BufferedWriter buff, String line) throws IOException {
	if (buff == null) {
	    throw new IOException("no BufferedWriter");
	}
	buff.write(line);
	buff.newLine();
    }
}
