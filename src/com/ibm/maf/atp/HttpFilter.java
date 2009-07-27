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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Hashtable;

/**
 * @version     1.01	$Date: 2009/07/27 10:31:41 $
 * @author	Gaku Yamamoto
 * @author	Mitsuru Oshima
 */
final class HttpFilter {

	static void readHttpHeaders(InputStream in, 
								Hashtable headers) throws IOException {

		// -in.mark(8192);
		// -BufferedReader r = new BufferedReader(new InputStreamReader(in));
		// -String line = r.readLine();
		// -in.reset();
		DataInputStream r = new DataInputStream(in);
		String line = r.readLine();

		// BufferedReader r = new BufferedReader(new InputStreamReader(in));
		// String line = r.readLine();
		// 
		int index = line.indexOf(' ');
		String method = line.substring(0, index);
		int index2 = line.indexOf(' ', index + 1);
		String uri = line.substring(index + 1, index2);
		String protocol = line.substring(index2 + 1);

		// 
		headers.put("requestline", line);
		headers.put("method", method);
		headers.put("requesturi", uri);
		headers.put("protocol", protocol);

		// 
		while (true) {
			String field = r.readLine();

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
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			} 
		} 
	}
}
