package com.ibm.awb.misc;

import java.util.Hashtable;
import java.util.Enumeration;
import sun.io.CharToByteConverter;
import sun.io.ByteToCharConverter;

/*
 * @(#)Encoding.java
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

/**
 * The <tt>Encoding</tt> class
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:40 $
 * @author      ONO Kouichi
 */

public class Encoding {
	static private Hashtable encodingTable = new Hashtable();

	static {

		// MIME Official encoding schema

		encodingTable.put("ISO8859_1", "iso-8859-1");
		encodingTable.put("ISO8859_2", "iso-8859-2");
		encodingTable.put("ISO8859_3", "iso-8859-3");
		encodingTable.put("ISO8859_4", "iso-8859-4");
		encodingTable.put("ISO8859_5", "iso-8859-5");
		encodingTable.put("ISO8859_6", "iso-8859-6");
		encodingTable.put("ISO8859_7", "iso-8859-7");
		encodingTable.put("ISO8859_8", "iso-8859-8");
		encodingTable.put("ISO8859_9", "iso-8859-9");

		encodingTable.put("ISO2022JP", "iso-2022-jp");
		encodingTable.put("ISO2022CN", "iso-2022-cn");
		encodingTable.put("ISO2022KR", "iso-2022-kr");

		// MIME Unofficial encoding schema

		encodingTable.put("EUC_JP", "euc-jp");
		encodingTable.put("EUC_KR", "euc-kr");
		encodingTable.put("EUC_CN", "euc-cn");
		encodingTable.put("EUC_TW", "euc-tw");

		encodingTable.put("SJIS", "Shift_JIS");
	} 

	private String _encoding = null;
	private String _charset = null;

	/**
	 * Java Encoding name into HTML charset name.
	 * @param javaEncoding Java Encoding name
	 */
	public Encoding(String javaEncoding) {
		_encoding = javaEncoding;
		_charset = javaEncodingToHTMLCharset(_encoding);
	}
	public static Encoding getDefault() {
		return new Encoding(CharToByteConverter.getDefault()
			.getCharacterEncoding());
	}
	/**
	 * get HTML charset name.
	 * @return HTML charset name
	 */
	public String getHTMLCharset() {
		return _charset;
	}
	/**
	 * get Java Encoding name.
	 * @return Java Encoding name
	 */
	public String getJavaEncoding() {
		return _encoding;
	}
	/**
	 * Converts HTML charset name into Java Encoding name.
	 * @param charset HTML charset name
	 * @return Java Encoding name
	 */
	public static String htmlCharsetToJavaEncoding(String charset) {
		Enumeration keys = encodingTable.keys();

		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String cs = (String)encodingTable.get(key);

			if (cs != null && cs.equals(charset)) {
				return key;
			} 
		} 
		return null;
	}
	/**
	 * Converts Java Encoding name into HTML charset name.
	 * @param javaEncoding Java Encoding name
	 * @return HTML charset name
	 */
	public static String javaEncodingToHTMLCharset(String javaEncoding) {
		return (String)encodingTable.get(javaEncoding);
	}
}
