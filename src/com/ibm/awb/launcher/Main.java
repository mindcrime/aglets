package com.ibm.awb.launcher;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sourceforge.aglets.log.AgletsLogger;
import net.sourceforge.aglets.util.AgletsTranslator;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglets.MAFAgentSystem_AgletsImpl;
import com.ibm.aglets.tahiti.CommandLine;
import com.ibm.aglets.tahiti.Tahiti;
import com.ibm.aglets.tahiti.TahitiDaemon;
import com.ibm.aglets.tahiti.UserManager;
import com.ibm.awb.misc.FileUtils;
import com.ibm.awb.misc.LogStream;
import com.ibm.awb.misc.Resource;
import com.ibm.maf.MAFAgentSystem;

/**
 * Aglets server bootstrap.
 * 
 * @author Hideki Tai
 * @version $Revision: 1.10 $ $Date: 2009/07/28 07:04:54 $ $Author: cat4hire $
 */
public class Main {
	private final static String VIEWER_TAHITI = "com.ibm.aglets.tahiti.Tahiti";
	private final static String VIEWER_COMMANDLINE = "com.ibm.aglets.tahiti.CommandLine";
	private final static String VIEWER_TAHITI_DAEMON = "com.ibm.aglets.tahiti.TahitiDaemon";
	private final static String DEFAULT_VIEWER = VIEWER_TAHITI;
	private final static String DELIM = ", \t\n";

	private static String _viewer_class_name = DEFAULT_VIEWER;

	private static boolean _reactivation = true;
	private static int _port_num = -1;
	private static int _control_port_num = -1;
	private static boolean _verbose = false;
	private static boolean _nogui = false;
	private static boolean _daemon = false;
	private static boolean _nosound = false;

	private static String FS;
	private static String PS;
	private static AgletsLogger logger = AgletsLogger.getLogger(Main.class.getName());

	/**
	 * Bootstraps aglets server. (AgletRuntime, MAFAgentSystem, and Tahiti)
	 * 
	 * @exception Exception
	 *                Description of Exception
	 */
	private static void bootstrap() throws Exception {

		// Initialize logging system.
		final String initializerName = System.getProperty("aglets.logger.class", "com.ibm.awb.launcher.Main");
		Class.forName(initializerName);
		logger.info("Logging system initialized!");

		// Initializes AWT and Audio classes.
		if (!(_nogui || _daemon)) {
			_viewer_class_name = VIEWER_TAHITI;
			if (_verbose) {
				System.err.print("[Loading AWT classes ... ");
			}
			loadAWTClasses();
			if (_verbose) {
				System.err.println("done.]");
			}
			if (!_nosound) {
				if (_verbose) {
					System.err.print("[Loading Audio classes ... ");
				}
				loadAudioClasses();
				if (_verbose) {
					System.err.println("done.]");
				}
			}
		}
		if (_nogui) {
			_viewer_class_name = VIEWER_COMMANDLINE;
		}
		if (_daemon) {
			_viewer_class_name = VIEWER_TAHITI_DAEMON;
		}

		// Initializes the Aglet runtime.
		final AgletRuntime runtime = AgletRuntime.init(null);

		// Authenticate the owner of the runtime.
		// At first, tries to get account information from the server property.
		final String owner = System.getProperty("aglets.owner.name");
		boolean authenticated = false;

		if (owner != null) {
			final String passwd = System.getProperty("aglets.owner.password");

			authenticated = (runtime.authenticateOwner(owner, passwd) != null);
			if (authenticated) {
				if (_verbose) {
					System.err.println("[Succeed in owner authentication: \""
							+ owner + "\"]");
				}
			} else {
				if (_verbose) {
					System.err.println("[Failed to authenticate the owner: \""
							+ owner + "\"]");
				}
			}
		}
		if (!authenticated) {

			// Asks for the user to login via user interface.
			if (login(runtime) == null) {
				return;
			}
		}

		// Creates MAFAgentSystem and initialize it.
		final MAFAgentSystem maf_system = new MAFAgentSystem_AgletsImpl(runtime);
		final String protocol = System.getProperties().getProperty("maf.protocol");

		logger.debug("Initializing handler: " + protocol);
		MAFAgentSystem.initMAFAgentSystem(maf_system, protocol);

		// Initializes Tahiti(part of the agent system)
		Tahiti.init();
		Tahiti.installFactories();

		// Creates the default context. To dispatch to this context, you have to
		// specify the destination, for example,
		// "atp://aglets.trl.ibm.com:4434/"
		final AgletContext cxt = runtime.createAgletContext("");

		// Attatches a viewer to the default context.
		final ContextListener viewer = getViewer();

		if (viewer != null) {
			cxt.addContextListener(viewer);
		}

		// Installs the Aglets security manager.
		Tahiti.installSecurity();

		// Starts the MAFAgentSystem.
		MAFAgentSystem.startMAFAgentSystem(maf_system, protocol);

		// Starts the default context.
		cxt.start(_reactivation);

		// Creates initial aglets which are specified by the server properties.
		startupAglets(cxt);
		if (_verbose) {
			System.err.println("Aglets server started");
		}
	}

	/**
	 * Gets the Viewer attribute of the Main class
	 * 
	 * @return The Viewer value
	 */
	private static ContextListener getViewer() {
		return getViewer(_viewer_class_name);
	}

	/**
	 * Gets the Viewer attribute of the Main class
	 * 
	 * @param viewer
	 *            Description of Parameter
	 * @return The Viewer value
	 */
	private static ContextListener getViewer(final String viewer) {
		if ((viewer != null) && (viewer.length() > 0)) {
			Class viewClass = null;

			try {
				viewClass = Class.forName(viewer);
			} catch (final ClassNotFoundException ex) {
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
			} catch (final IllegalAccessException excpt) {
				return null;
			} catch (final InstantiationException excpt) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Description of the Method
	 */
	private static void loadAudioClasses() {
		try {
			Class.forName("sun.audio.AudioPlayer");
		} catch (final Throwable ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Description of the Method
	 */
	private static void loadAWTClasses() {
		try {

			// Disabled for test on VAJava2(HT)
			// Class.forName("sun.awt.image.JPEGImageDecoder");
			final java.awt.Frame f = new java.awt.Frame();

			f.addNotify();
			f.dispose();

			// Class.forName("sun.awt.PlatformFont"); // for 1.1
			// Class.forName("sun.awt.ScreenUpdater");
		} catch (final Throwable ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param runtime
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 */
	private static Certificate login(final AgletRuntime runtime) {
		UserManager userManager = null;

		// decide UI
		if (VIEWER_TAHITI.equals(_viewer_class_name)) {
			userManager = Tahiti.getUserManager();
		} else if (VIEWER_COMMANDLINE.equals(_viewer_class_name)) {
			userManager = CommandLine.getUserManager();
		} else if (VIEWER_TAHITI_DAEMON.equals(_viewer_class_name)) {
			userManager = TahitiDaemon.getUserManager();
		} // else? throw exception, or default to what?

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
		 * -------------- while (!UserManager.isUserRegistered()) { user
		 * registration is needed
		 * System.err.println("No user is registered. Register yourself.");
		 * userManager.registration(); } ---------------
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

	/**
	 * Bootstrap aglets server. This main method takes at most one parameter
	 * which specifies a name of a bootstrarp property file. The default file
	 * name is "./boot.props"
	 * 
	 * @param args
	 *            The command line arguments
	 * @exception IOException
	 *                Description of Exception
	 */
	public static void main(final String[] args) throws IOException {

		// System.setOut(new LogStream("OUT", System.out));
		// System.setErr(new LogStream("ERR", System.err));

		// Get system properties
		final Properties system_props = System.getProperties();

		FS = system_props.getProperty("file.separator");
		PS = system_props.getProperty("path.separator");
		setDefaultProperties();
		parseArgs(args);
		resolveProperties();
		if (_verbose) {
			system_props.put("verbose", "true");
		} else {
			system_props.put("verbose", "false");
		}

		if (_port_num > 0) {
			system_props.put("maf.port", Integer.toString(_port_num));
		}
		if (_control_port_num > 0) {
			system_props.put("maf.controlport", Integer.toString(_control_port_num));
		}

		try {
			bootstrap();
		} catch (final Exception e) {
			System.err.println("Exception while performing the platform bootstrap, unable to start!");
			e.printStackTrace();
			System.exit(1);
		}

	}

	/**
	 * Description of the Method
	 * 
	 * @param args
	 *            Description of Parameter
	 */
	private static void parseArgs(final String[] args) {
		if (args.length <= 0) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-help")) {
			} else if (args[i].equalsIgnoreCase("-verbose")) {
				_verbose = true;
			} else if (args[i].equalsIgnoreCase("-nogui")) {
				_nogui = true;
			} else if (args[i].equalsIgnoreCase("-daemon")) {
				_daemon = true;
			} else if (args[i].equalsIgnoreCase("-nosound")) {
				_nosound = true;
			} else if (args[i].equalsIgnoreCase("-cleanstart")) {
				_reactivation = false;
			} else if (args[i].equalsIgnoreCase("-f")) {
				if (i + 1 >= args.length) {
					usage();
				}
				i++;
				try {
					readProperties(args[i]);
				} catch (final IOException ex) {
					System.err.println("Server property file was not found: "
							+ args[i]);
					usage();
				}
			} else if (args[i].equalsIgnoreCase("-port")) {
				if (i + 1 >= args.length) {
					usage();
				}
				i++;
				try {
					_port_num = Integer.parseInt(args[i]);
				} catch (final NumberFormatException ex) {
					ex.printStackTrace();
				}
			} else if (args[i].equalsIgnoreCase("-controlport")) {
				if (i + 1 >= args.length) {
					usage();
				}
				i++;
				try {
					_control_port_num = Integer.parseInt(args[i]);
				} catch (final NumberFormatException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Concatenates path strings(util method).
	 * 
	 * @param p1
	 *            Preceding path string.
	 * @param p2
	 *            Posterior path string.
	 * @return A concatenated path string.
	 */
	private static String pathConcat(String p1, String p2) {
		p2 = p2.replace('/', FS.charAt(0));
		if (!p1.endsWith(FS)) {
			p1 = p1 + FS;
		}
		if (p2.startsWith(FS)) {
			p2 = p2.substring(0, p2.length() - 1);
		}
		return (p1 + p2);
	}

	/**
	 * Reads property from specified file(util method).
	 * 
	 * @param file
	 *            name of the property file.
	 * @exception IOException
	 *                if it failed to read the property file.
	 */
	private static void readProperties(final String file) throws IOException {
		if (_verbose) {
			System.err.println("[Reading property file: " + file + "]");
		}

		final InputStream is = new FileInputStream(file);

		final Properties system_props = System.getProperties();
		final Properties props = new Properties();

		props.load(is);

		// Copy properties into the system property
		for (final Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			final String key = (String) e.nextElement();
			final String val = (String) props.get(key);

			if (("aglets.viewer".equals(key))
					|| ((val != null) && (val.length() > 0))) {
				if (val == null) {
					system_props.remove(key);
				} else {
					system_props.put(key, val);
				}
			}
		}
	}

	/**
	 * Description of the Method
	 */
	private static void resolveProperties() {
		final Properties props = System.getProperties();

		// Get mandatory properties
		String aglets_home = props.getProperty("install.root");

		// install.root will be given the script produced
		// by InstallShield JavaEdition.
		if (aglets_home == null) {
			aglets_home = props.getProperty("aglets.home", null);
			if ((aglets_home == null) || (aglets_home.length() == 0)) {
				System.err.println("Please specify aglets.home property");
				System.exit(1);
			}
		}

		String user_home = props.getProperty("user.home", null);

		if ((user_home == null) || (user_home.length() == 0)) {
			user_home = FileUtils.getUserHome();
		}
		if ((user_home == null) || (user_home.length() == 0)) {
			System.err.println("Please specify user.home property");
			System.exit(1);
		}

		String p;

		// java.policy
		p = props.getProperty("java.policy", null);
		if (p == null) {
			props.put("java.policy", pathConcat(user_home, ".aglets/security/aglets.policy"));
		}

		// aglets.class.path
		p = props.getProperty("aglets.class.path", props.getProperty("java.class.path"));
		final String agletClasspath = pathConcat(aglets_home, "public");
		if ((agletClasspath != null) && (agletClasspath.length() > 0))
			p += File.pathSeparator + agletClasspath;

		props.put("aglets.class.path", p);
		logger.info("Classpath is specified as "
				+ props.get("aglets.class.path"));
		logger.info("Real classpath = " + props.getProperty("java.class.path"));
		logger.info( String.format( "AGLETS_HOME is %s", AgletsTranslator.getAgletsHome() ));

		// aglets.public.root
		p = props.getProperty("aglets.public.root", null);
		if (p == null) {
			props.put("aglets.public.root", pathConcat(aglets_home, "public"));
		}

		// aglets.viewer
		_viewer_class_name = props.getProperty("aglets.viewer", null);

		// aglets.logfile
		p = props.getProperty("aglets.logfile", null);
		if (p != null) {
			try {
				final LogStream log = new LogStream("LOG", new FileOutputStream(p));

				// OutputStream log = new java.io.FileOutputStream(p);
				final OutputStream tee = new com.ibm.awb.misc.TeeOutputStream(System.out, log);
				final PrintStream ps = new java.io.PrintStream(tee);

				System.setOut(ps);
				System.setErr(ps);
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		// aglets.cleanstart
		p = props.getProperty("aglets.cleanstart", "false");
		if ("true".equalsIgnoreCase(p)) {
			_reactivation = false;
		}
	}

	/**
	 * Sets the DefaultProperties attribute of the Main class
	 */
	private static void setDefaultProperties() {
		final Properties props = System.getProperties();

		props.remove("java.policy");// Should be set after
		props.remove("aglets.class.path");// Should be set after
		props.remove("aglets.public.root");// Should be set after
		props.put("verbose", "false");
		props.put("maf.protocol", "atp");
		props.put("maf.port", "4434");
		props.put("maf.controlport", "5545");
		props.remove("maf.finder.host");
		props.put("maf.finder.port", "4435");
		props.put("maf.finder.name", "MAFFinder");
		props.put("aglets.secure", "true");
		props.put("aglets.viewer", DEFAULT_VIEWER);
		props.put("aglets.cleanstart", "false");
		props.remove("aglets.startup");
		props.put("aglets.owner.name", props.getProperty("user.name"));
		props.remove("aglets.logfile");
		props.put("atp.resolve", "false");
		props.put("atp.useip", "false");
		props.put("atp.offline", "false");
		props.put("atp.authentication", "false");
		props.put("atp.secureseed", "true");
	}

	/**
	 * Creates initial aglets.
	 * 
	 * @param context
	 *            Description of Parameter
	 */
	protected static void startupAglets(final AgletContext context) {
		String[] startup_aglets = null;
		boolean startup = false;

		final String p = System.getProperties().getProperty("aglets.startup", null);

		if (p != null) {
			final StringTokenizer st = new StringTokenizer(p, DELIM);
			final Vector v = new Vector();

			while (st.hasMoreTokens()) {
				v.addElement(st.nextToken());
			}
			startup_aglets = new String[v.size()];
			v.copyInto(startup_aglets);
			startup = true;
		}

		if (startup_aglets == null) {

			// handle startup entry in "tahiti.properties"
			final Resource tahiti_res = Resource.getResourceFor("tahiti");

			startup = tahiti_res.getBoolean("tahiti.startup", false);
			startup_aglets = tahiti_res.getStringArray("tahiti.startupAglets", DELIM);
		}

		if (startup && (startup_aglets != null)) {
			for (final String startup_aglet : startup_aglets) {
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
				} catch (final java.net.MalformedURLException ex) {
					ex.printStackTrace();
				}
				try {
					context.createAglet(codebase, name, initparam);
				} catch (final Exception e) {
					System.err.println("Failed to create the \"Startup\" Aglet:"
							+ e.getMessage());
					System.err.println("[" + codebase + "] [" + name + "]");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Description of the Method
	 */
	private static void usage() {
		System.err.println("\nAglet Server(com.ibm.awb.launcher.Main) usage:");
		System.err.println("options:");
		System.err.println("    -f <file.props>  server property file");
		System.err.println("    -port <num>      port number (default 4434)");
		System.err.println("    -controlport <num>      port number (default 4444)");
		System.err.println("    -verbose         verbose output");
		System.err.println("    -nogui           omit AWT initialization");
		System.err.println("    -daemon          run as a daemon");
		System.err.println("    -nosound         omit Sound initialization");
		System.err.println("    -cleanstart      kill deactivated aglets");
		System.err.println("    -help            print this message");
		System.err.println("note: '-f' option can be specified more than once,");
		System.err.println("        properties in the following file will override");
		System.exit(1);
	}

}
