package com.ibm.awb.misc;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;

/*
 * @(#)FileUtils.java
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
public class FileUtils {
	/**
	 * Utilities
	 */
	private static String USER_DIR = null;
	private static String USER_HOME = null;
	static {
		USER_DIR = System.getProperty("user.dir");
		USER_HOME = System.getProperty("user.home");
		if ((USER_HOME == null) || USER_HOME.equals("")) {
			USER_HOME = "/";
		}
		try {
			final File file = new File(USER_HOME);

			USER_HOME = file.getCanonicalPath();
		} catch (final IOException excpt) {
		}
	}
	private static final String PATH_SEPARATORS = " " + File.pathSeparator;

	static String absolute(final File file) {
		if (file.isAbsolute()) {
			return file.getPath();
		} else {
			return USER_DIR + File.separator + file.getPath();
		}
	}

	/*
	 * Checks if the current execution is allowed to access the path/file.
	 */
	public static boolean checkFile(final String file, final String[] checkList) {
		String abs = absolute(new File(file));

		abs = compact(abs.replace(File.separatorChar, '/') + "/");
		if (checkList != null) {
			for (final String element : checkList) {
				if (localizedCheck(abs, element)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String compact(String name) {
		String drive = "";

		int drive_index = name.indexOf(":");

		if (drive_index < 0) {
			drive_index = name.indexOf("|");
		}
		if (drive_index > 0) {
			drive = name.substring(0, drive_index + 1);
			name = name.substring(drive_index + 1);
		}
		new StringBuffer(name);

		final StringBuffer buffer = new StringBuffer();

		trim(name, buffer);

		if ((name.charAt(0) != '/') && (buffer.charAt(0) == '/')) {
			name = buffer.toString().substring(1);
		} else {
			name = buffer.toString();
		}
		return drive + name;
	}

	static private boolean ensureDir(final String dir) {
		final File f = new File(dir);

		if (f.exists() == false) {

			// what should i do?
			final String parent = f.getParent();

			if ((parent == null) || (ensureDir(parent) == true)) {
				return makeDir(dir);
			}
		}
		return true;
	}

	/*
	 * Checks whether the directory already exists.
	 */
	static public boolean ensureDirectory(final String filename) {
		final File file = new File(filename);

		return FileUtils.ensureDir(file.getParent());
	}

	static public boolean ensureFile(final String filename) {
		final File f = new File(filename);

		if (f.exists() == true) {
			if ((f.isFile() == false) || (f.isDirectory() == true)) {
				System.out.println('[' + filename + " is not a normal file]");
				return false;
			}
			if (f.canRead() == false) {
				System.out.println('[' + filename + " is not readable]");
				return false;
			}
			if (f.canWrite() == false) {
				System.out.println('['
						+ filename
						+ " is not writable. Any modification will not be saved.]");
			}
		} else {

			// can write?
			System.out.println('[' + filename
					+ " is not found. Creating new file.]");
			try {
				final java.io.OutputStream out = new java.io.FileOutputStream(filename);

				out.close();
			} catch (final java.io.IOException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets cache directory
	 */
	public final static String getCacheDirectory() {
		return getWorkDirectory() + File.separator + "cache";
	}

	/**
	 * Gets log directory
	 */
	public final static String getLogDirectory() {
		return getWorkDirectory() + File.separator + "logs";
	}

	/**
	 * Gets directory of property files for a user
	 */
	private final static String getPropertyDirectoryForUser(final String username) {
		return getWorkDirectoryForUser(username);
	}

	/**
	 * Gets property filename for a user
	 */
	public final static String getPropertyFilenameForUser(
	                                                      final String username,
	                                                      final String propname) {
		return getPropertyDirectoryForUser(username) + File.separator
		+ propname + ".properties";
	}

	/**
	 * Gets directory for security files
	 */
	public final static String getSecurityDirectory() {
		return getWorkDirectory() + File.separator + "security";
	}

	/**
	 * Gets spool directory
	 */
	public final static String getSpoolDirectory() {
		return getWorkDirectory() + File.separator + "spool";
	}

	/**
	 * Gets user directory
	 */
	public final static String getUserDirectory() {
		return USER_DIR;
	}

	/**
	 * Gets user home
	 */
	public final static String getUserHome() {
		return USER_HOME;
	}

	/**
	 * Gets work directory
	 */
	public final static String getWorkDirectory() {

		// - Resource aglets_res = Resource.getResourceFor("aglets");
		final String default_path = USER_HOME + File.separator + ".aglets";
		final String work_dir = System.getProperty("aglets.work", default_path);

		// - if(aglets_res==null) {
		return work_dir;

		// - }
		// - return aglets_res.getString("aglets.work", work_dir);
	}

	/**
	 * Gets Work directory for a user
	 */
	public final static String getWorkDirectoryForUser(final String username) {
		return getWorkDirectoryForUsers() + File.separator + username;
	}

	/**
	 * Gets Work directory for users
	 */
	private final static String getWorkDirectoryForUsers() {
		return getWorkDirectory() + File.separator + "users";
	}

	/*
	 * The character '/' is used as an universal file separator. The problem
	 * with case sensitivity must be taken into account.
	 */
	public static String[] localize(final String s[]) {
		if (s == null) {
			return new String[0];
		}
		final String acls[] = new String[s.length];

		for (int i = 0; i < s.length; i++) {
			String path = absolute(new File(s[i]));

			// '/' is the universal path separator for path checking
			path = compact(path.replace(File.separatorChar, '/'));

			// path authentification will go here ?

			acls[i] = localize(path);
		}
		return acls;
	}

	/*
	 * File system dependent convertion
	 */
	public static String localize(final String filename) {

		//
		// ad hoc.... just for DOS/WINDOWS (case insensitive)
		//
		if (File.separatorChar == '\\') {
			String path = filename;

			if ("/".equals(path)) {
				final String current = USER_DIR;
				final String drive = current.substring(0, current.indexOf(':') + 1);

				path = drive + "/";
			}
			return path.toUpperCase();
		}
		return filename;
	}

	/*
	 * Filesystem dependent comparison. sigh...;-<
	 */
	public static boolean localizedCheck(final String abs, final String path) {
		switch (File.separatorChar) {
			case '\\': // DOS,WINDOWS,OS/2
				return (abs.toUpperCase().startsWith(path) ||

						//
						// the file is like /e:/test/memoryBar..
						//
						((abs.charAt(0) == '/') && (abs.length() > 2)
								&& (abs.charAt(2) == ':') && abs.toUpperCase().startsWith('/' + path)));
			case '/': // UNIX , Mac MRJ.
				return abs.startsWith(path);
			case ':': // none..
				return abs.startsWith(path);
			default: // something else?
		}
		return false;
	}

	static private boolean makeDir(final String dir) {
		return new File(dir).mkdir();

		/*
		 * System.out.println("\nDirecotry " + dir +
		 * " does not exist. \nCreate? y/n[y]"); java.io.DataInput di = new
		 * java.io.DataInputStream(System.in); String input = "y"; try { input =
		 * di.readLine(); } catch (java.io.IOException ex) { input = "n"; } if
		 * (input.length()==0 || "y".equalsIgnoreCase(input)) { return new
		 * File(dir).mkdir(); } return false;
		 */
	}

	public static final Enumeration strToPathList(final String path_list) {
		return strToPathList(path_list, PATH_SEPARATORS);
	}

	public static final Enumeration strToPathList(
	                                              final String path_list,
	                                              final String separators) {
		if (path_list == null) {
			return null;
		}
		final StringTokenizer st = new StringTokenizer(path_list, separators);
		final StringList list = new StringList();

		while (st.hasMoreTokens()) {
			final String path = st.nextToken();

			if ((path != null) && !path.equals("")) {
				list.addString(path);
			}
		}
		return list;
	}

	public static void trim(final String name, final StringBuffer buff) {
		final int next_trim = name.indexOf("/..");

		if (next_trim < 0) {
			buff.append(name);
			return;
		}
		int next = name.indexOf("/");
		final int orig = 0;
		int copy = 0;

		while (next_trim > next) {
			copy = next;
			next = name.indexOf("/", next + 1);
		}
		buff.append(name.substring(orig, copy));
		trim(name.substring(next_trim + 3), buff);
	}
}
