package com.ibm.maf.atp;

/*
 * @(#)HttpFilter.java
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

// import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * @version 1.01 $Date: 2009/07/28 07:04:53 $
 * @author Gaku Yamamoto
 * @author Mitsuru Oshima
 */
final class HttpFilter {

	static void readHttpHeaders(final InputStream in, final Hashtable headers)
	throws IOException {

		// -in.mark(8192);
		// -BufferedReader r = new BufferedReader(new InputStreamReader(in));
		// -String line = r.readLine();
		// -in.reset();
		final DataInputStream r = new DataInputStream(in);
		final String line = r.readLine();

		// BufferedReader r = new BufferedReader(new InputStreamReader(in));
		// String line = r.readLine();
		//
		int index = line.indexOf(' ');
		final String method = line.substring(0, index);
		final int index2 = line.indexOf(' ', index + 1);
		final String uri = line.substring(index + 1, index2);
		final String protocol = line.substring(index2 + 1);

		//
		headers.put("requestline", line);
		headers.put("method", method);
		headers.put("requesturi", uri);
		headers.put("protocol", protocol);

		//
		while (true) {
			final String field = r.readLine();

			try {
				if (field.length() == 0) {
					break;
				}
				index = field.indexOf(':');
				String key = field.substring(0, index);
				String value = field.substring(index + 1);

				key = key.toLowerCase().trim();
				value = value.trim();
				headers.put(key, value);
			} catch (final Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}
}
