package com.ibm.aglets.security;

/*
 * @(#)PolicyFileWriter.java
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * The <tt>PolicyFileWriter</tt> class writes a PolicyDB object into Java policy
 * database file.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class PolicyFileWriter {
	private static final String BACKUP_FILENAME_SUFFIX = ".bak";

	public static final String getBackupFilename(final String filename) {
		return filename + BACKUP_FILENAME_SUFFIX;
	}

	// for test
	static public void main(final String arg[]) {
		PolicyDB db = null;

		if (arg.length == 0) {
			return;
		} else {
			db = PolicyFileReader.getAllPolicyDB();
			try {
				PolicyFileWriter.writePolicyDB(arg[0], db);
			} catch (final IOException excpt) {
				System.out.println(excpt.toString());
			}
		}
	}

	public synchronized static void writePolicyDB(final String filename, final PolicyDB db)
	throws IOException {
		if (filename == null) {
			throw new IOException("Policy filename is null.");
		}
		if (db == null) {
			throw new IOException("Policy DB is null.");
		}

		final FileWriter fwriter = new FileWriter(filename);

		if (fwriter == null) {
			throw new IOException("File Writer for '" + filename + "' is null.");
		}

		final BufferedWriter bwriter = new BufferedWriter(fwriter);

		if (bwriter == null) {
			throw new IOException("Buffered Writer for '" + filename
					+ "' is null.");
		}

		final Vector lines = db.toVector();
		final int num = lines.size();
		int idx = 0;

		for (idx = 0; idx < num; idx++) {
			final String line = (String) lines.elementAt(idx);

			bwriter.write(line);
			bwriter.newLine();
		}

		bwriter.close();
	}
}
