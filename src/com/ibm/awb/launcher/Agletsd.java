package com.ibm.awb.launcher;

/*
 * @(#)Agletsd.java
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

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ibm.awb.misc.FileUtils;

public class Agletsd extends Thread {
	static final String SCRIPT_NAME = "agletsrv.ini";
	static String FS;
	static String PS;
	static String ROOT;
	static String HOME;
	static String JAVA_HOME;
	private final InputStream in;

	public static Frame console = null;

	public static void main(final String[] args) throws IOException {

		// Get system properties
		final Properties system_props = System.getProperties();

		FS = system_props.getProperty("file.separator");
		PS = system_props.getProperty("path.separator");
		ROOT = system_props.getProperty("install.root");
		HOME = FileUtils.getUserHome();
		JAVA_HOME = system_props.getProperty("java.home");

		boolean win32 = false;
		final File f = new File(ROOT + FS + SCRIPT_NAME);

		if (f.exists()) {
			win32 = true;
		}

		final String aglets_home = ROOT;
		final String aglets_class_path = aglets_home + FS + "public";
		final String aglets_export_path = aglets_home + FS + "public";
		final String program_name = "agletsd";

		// set properties if necessary
		// if (system_props.get("aglets.home") == null) {
		system_props.put("aglets.home", aglets_home);

		// }
		// if (system_props.get("aglets.export.path") == null) {
		system_props.put("aglets.export.path", aglets_export_path);

		// }
		// if (system_props.get("aglets.class.path") == null) {
		system_props.put("aglets.class.path", aglets_class_path);

		// }
		system_props.put("program-name", program_name);

		final File policy_file = new File(HOME + FS + ".aglets" + FS + "security"
				+ FS + "aglets.policy");

		// if (policy_file.exists() && policy_file.canRead()) {
		system_props.put("java.policy", policy_file.getAbsolutePath());

		// }

		boolean openconsole = win32;

		for (final String arg : args) {
			if ("-noconsole".equalsIgnoreCase(arg)
					|| "-nogui".equalsIgnoreCase(arg)
					|| "-daemon".equalsIgnoreCase(arg)
					|| "-commandline".equalsIgnoreCase(arg)) {
				openconsole = false;
			}
		}

		if (openconsole == true) {
			console = new Console();
			console.pack();
			console.show();
		}

		try {
			com.ibm.aglets.tahiti.Main.main(args);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	protected Agletsd(final InputStream is) {
		in = is;
	}

	@Override
	public void run() {
		int c;

		try {
			while ((c = in.read()) >= 0) {
				System.out.write(c);
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
}
