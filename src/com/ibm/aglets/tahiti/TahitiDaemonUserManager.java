package com.ibm.aglets.tahiti;

/*
 * $Id: TahitiDaemonUserManager.java,v 1.4 2009/07/28 07:04:52 cat4hire Exp $
 *
 * @(#)TahitiDaemonUserManager.java
 *
 */

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.cert.Certificate;

import com.ibm.aglet.system.AgletRuntime;

/**
 * Provides user authentication for the Tahiti Daemon.
 * 
 * @author Larry Spector
 * @version $Revision: 1.4 $ $Date: 2009/07/28 07:04:52 $ $Author: cat4hire $
 */
public final class TahitiDaemonUserManager extends UserManager {

	private static boolean _verbose = false;

	/**
	 * Constructor for the TahitiDaemonUserManager object
	 * 
	 * @since 1.0
	 */
	public TahitiDaemonUserManager() {
		_verbose = Boolean.getBoolean(System.getProperties().getProperty("verbose"));
	}

	/**
	 * Reads input line by line from standand in.
	 * 
	 * @param title
	 *            Description of Parameter
	 * @param defval
	 *            Description of Parameter
	 * @param enforce
	 *            Description of Parameter
	 * @return User's keyboard input.
	 * @since 1.0
	 */
	private String input(final String title, final String defval, final boolean enforce) {
		String line = null;
		final LineNumberReader r = new LineNumberReader(new InputStreamReader(System.in));

		while (true) {
			if (_verbose) {
				System.out.print(title
						+ ((defval == null) || (defval.length() == 0) ? ":"
								: "[" + defval + "]:"));
				System.out.flush();
			}
			try {
				line = r.readLine();
			} catch (final IOException ex) {
			}
			if (line == null) {
				System.exit(1);
			}
			if (line.trim().length() != 0) {
				return line.trim();
			} else if ((defval != null) && (defval.length() != 0)) {
				return defval;
			} else if (enforce == false) {
				return null;
			}
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param title
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since 1.0
	 */
	private String inputUsername(final String title) {
		return this.inputUsername(title, getDefaultUsername());
	}

	/**
	 * Description of the Method
	 * 
	 * @param title
	 *            Description of Parameter
	 * @param defaultUsername
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @since 1.0
	 */
	private String inputUsername(final String title, final String defaultUsername) {
		return input(title, defaultUsername, true);
	}

	/**
	 * Verify the user and return their certificate.
	 * 
	 * @return The user's certificate
	 * @since 1.0
	 */
	@Override
	public Certificate login() {
		final AgletRuntime runtime = AgletRuntime.getAgletRuntime();

		if (runtime == null) {
			return null;
		}
		Certificate cert = null;
		String username = null;

		while (cert == null) {
			while (username == null) {
				username = this.inputUsername("login");
			}
			String password = input("password", "", false);

			if (password == null) {
				password = "";
			}
			cert = runtime.authenticateOwner(username, password);
			if (cert == null) {
				if (_verbose) {
					System.out.println("Password is incorrect.");
				}
				username = null;
			}
		}
		setUsername(username);
		setCertificate(cert);
		return cert;
	}
}
