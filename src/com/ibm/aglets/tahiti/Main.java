package com.ibm.aglets.tahiti;

/*
 * @(#)Main.java
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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.StringTokenizer;
import java.util.Vector;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglets.MAFAgentSystem_AgletsImpl;
import com.ibm.awb.misc.Opt;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.MAFAgentSystem;

public class Main {

    private static final String VIEWER_TAHITI = "com.ibm.aglets.tahiti.Tahiti";
    private static final String VIEWER_COMMANDLINE = "com.ibm.aglets.tahiti.CommandLine";
    private static final String DEFAULT_VIEWER = VIEWER_TAHITI;
    private static final String DELIM = ", \t\n";

    private static String[] startupAgletList = null;
    private static boolean reactivation = true;

    static private Opt option_defs[] = {
	Opt.Entry("-help", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) {
		Opt.message();
		System.exit(2);
		return true;
	    }
	}, "    -help                print this message"),
	Opt.Entry("-verbose", "verbose", "true", "    -verbose             turn on verbose mode"),
	Opt.Entry("-debug", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) {
		com.ibm.awb.misc.Debug.debug(true);
		return true;
	    }
	}, null),
	Opt.Entry("-err", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) throws Exception {
		System.setErr(new PrintStream(new FileOutputStream(a)));
		return true;
	    }
	}, null),
	Opt.Entry("-out", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) throws Exception {
		System.setOut(new PrintStream(new FileOutputStream(a)));
		return true;
	    }
	}, null),
	Opt.Entry("-cleanstart", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) throws Exception {
		reactivation = false;
		return true;
	    }
	}, "    -cleanstart          do not re-activate aglets, remove it"),
	Opt.Entry("-startup", new Opt.Proc() {
	    @Override
	    public boolean exec(String a) throws Exception {
		StringTokenizer st = new StringTokenizer(a, DELIM);
		Vector v = new Vector();

		while (st.hasMoreTokens()) {
		    v.addElement(st.nextToken());
		}
		startupAgletList = new String[v.size()];
		v.copyInto(startupAgletList);
		return true;
	    }
	}, "    -startup <url,..>    create initial aglets"),
	Opt.Entry("-startup_file", new Opt.Proc() {
	    @Override
	    public boolean exec(String file) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		Vector v = new Vector();

		while ((line = in.readLine()) != null) {
		    if (!line.startsWith("#")) {
			v.addElement(line);
		    }
		}
		startupAgletList = new String[v.size()];
		v.copyInto(startupAgletList);
		return true;
	    }
	}, "    -startup_file <file> create initial aglets"),
	Opt.Entry("-viewer", "aglets.viewer", "    -viewer <class>      set the viewer class"),
	Opt.Entry("-commandline", "aglets.viewer", VIEWER_COMMANDLINE, "    -commandline         use command line interface."),
	Opt.Entry("-noui", "aglets.viewer", "", "    -noui                no GUI/CUI."),
	Opt.Entry("-protocol", "maf.protocol", null), };

    static private ContextListener getViewer() {
	return getViewer(getViewerClassName());
    }

    static private ContextListener getViewer(String viewer) {
	if ((viewer != null) && (viewer.length() > 0)) {
	    Class viewClass = null;

	    try {
		viewClass = Class.forName(viewer);
	    } catch (ClassNotFoundException ex) {
		System.err.println("[Viewer " + viewer + " not found.]");
		return null;
	    }
	    if (ContextListener.class.isAssignableFrom(viewClass) == false) {
		System.err.println("[Viewer " + viewer
			+ " is not subclass of ContextListener interface.");
		return null;
	    }
	    try {
		return (ContextListener) viewClass.newInstance();
	    } catch (IllegalAccessException excpt) {
		return null;
	    } catch (InstantiationException excpt) {
		return null;
	    }
	}
	return null;
    }

    static private String getViewerClassName() {
	Resource res = Resource.getResourceFor("aglets");
	String viewer;

	if (res != null) {
	    viewer = res.getString("aglets.viewer", DEFAULT_VIEWER);
	} else {
	    viewer = (String) AccessController.doPrivileged(new PrivilegedAction() {
		@Override
		public Object run() {
		    return System.getProperty("aglets.viewer", DEFAULT_VIEWER);
		}
	    });
	}
	return viewer;
    }

    static public Certificate login(AgletRuntime runtime) {
	UserManager userManager = null;

	// decide UI
	String viewerClassName = getViewerClassName();

	if ((viewerClassName != null) & viewerClassName.equals(VIEWER_TAHITI)) {
	    userManager = Tahiti.getUserManager();
	} else {
	    userManager = CommandLine.getUserManager();
	}

	// user authentication on UI
	String username = runtime.getOwnerName();

	if (username == null) {
	    username = UserManager.getDefaultUsername();
	}
	if (username == null) {
	    System.err.println("No username.");
	    return null;
	}

	/*
	 * ----------------------- while (!UserManager.isUserRegistered()) { //
	 * user registration is needed
	 * System.out.println("No user is registered. Register yourself.");
	 * userManager.registration(); } -------------------------
	 */

	// try to login with no password
	Certificate cert = runtime.authenticateOwner(username, "");

	if (cert == null) {

	    // login failed. try to login with password
	    if (userManager != null) {
		cert = userManager.login();
		username = userManager.getUsername();
	    }
	}
	if (cert == null) {
	    System.err.println("Authentication of user '" + username
		    + "' is failed.");
	}
	return cert;
    }

    static public void main(String args[]) throws Exception {
	Opt.setopt(option_defs);
	AgletRuntime runtime = AgletRuntime.init(args);

	// User authentication
	if (login(runtime) == null) {
	    return;
	}
	MAFAgentSystem maf_system = new MAFAgentSystem_AgletsImpl(runtime);
	String protocol = (String) AccessController.doPrivileged(new PrivilegedAction() {
	    @Override
	    public Object run() {
		return System.getProperty("maf.protocol", "atp");
	    }
	});

	MAFAgentSystem.initMAFAgentSystem(maf_system, protocol);
	Tahiti.init();
	Tahiti.initializeGUI();
	Tahiti.installFactories();

	//
	// Creates a named context. To dispatch to this context, you have to
	// specify the destination, for example,
	// "atp://aglets.trl.ibm.com:4434/test"
	//
	AgletContext cxt = runtime.createAgletContext("");
	ContextListener viewer = getViewer();

	if (viewer != null) {
	    cxt.addContextListener(viewer);
	}
	Tahiti.installSecurity();
	MAFAgentSystem.startMAFAgentSystem(maf_system, protocol);
	cxt.start(reactivation);
	startupAglets(cxt);
    }

    /*
     * Launch the startup aglets
     */
    static protected void startupAglets(AgletContext context) {
	String[] startup_aglets;
	boolean startup = false;

	if (startupAgletList != null) {

	    // handle "-startup" command line option which overrides
	    // "tahiti.properties"
	    startup = true;
	    startup_aglets = startupAgletList;
	} else {

	    // handle startup entry in "tahiti.properties"
	    Resource tahiti_res = Resource.getResourceFor("tahiti");

	    startup = tahiti_res.getBoolean("tahiti.startup", false);
	    startup_aglets = tahiti_res.getStringArray("tahiti.startupAglets", DELIM);
	}
	if (startup) {
	    for (String startup_aglet : startup_aglets) {
		String initparam = null;
		URL codebase = null;
		String name = startup_aglet;

		try {
		    int del = name.lastIndexOf('#');

		    if (del > 0) {
			initparam = name.substring(del + 1);
			name = name.substring(0, del);
		    }
		    del = name.lastIndexOf('/');
		    if (del > 0) {
			codebase = new URL(name.substring(0, del));
			name = name.substring(del + 1);
		    }
		} catch (java.net.MalformedURLException ex) {
		    ex.printStackTrace();
		}
		try {
		    context.createAglet(codebase, name, initparam);
		} catch (Exception e) {
		    System.err.println("Failed to create the \"Startup\" Aglet:"
			    + e.getMessage());
		    System.err.println("[" + codebase + "] [" + name + "]");
		    e.printStackTrace();
		}
	    }
	}
    }
}
