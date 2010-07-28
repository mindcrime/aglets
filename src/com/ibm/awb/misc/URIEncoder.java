package com.ibm.awb.misc;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/*
 * @(#)URIEncoder.java
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
 * The class substitutes <code>java.net.URLEncoder</code>.
 * 
 * @see java.net.URLEncoder
 */
public class URIEncoder {
    private static final char CHAR_PERCENT = '%';
    private static final char CHAR_SPACE = ' ';
    private static final char CHAR_PLUS = '+';
    private static final char CHAR_MINUS = '-';
    private static final char CHAR_UNDERSCORE = '_';
    private static final char CHAR_DOT = '.';
    private static final char CHAR_ASTERISK = '*';
    private static final char CHAR_A_LOWERCASE = 'a';
    private static final char CHAR_A_UPPERCASE = 'A';
    private static final char CHAR_Z_LOWERCASE = 'z';
    private static final char CHAR_Z_UPPERCASE = 'Z';
    private static final char CHAR_NUMBER_0 = '0';
    private static final char CHAR_NUMBER_9 = '9';

    /*
     * The list of characters that are not encoded have been determined by
     * referencing O'Reilly's "HTML: The Definitive Guide" (page 164).
     */

    private static final int NUM_CHAR = 256;
    private static BitSet dontNeedEncoding;
    static {
	dontNeedEncoding = new BitSet(NUM_CHAR);
	int i;

	for (i = CHAR_A_LOWERCASE; i <= CHAR_Z_LOWERCASE; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = CHAR_A_UPPERCASE; i <= CHAR_Z_UPPERCASE; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = CHAR_NUMBER_0; i <= CHAR_NUMBER_9; i++) {
	    dontNeedEncoding.set(i);
	}
	dontNeedEncoding.set(CHAR_SPACE);

	/* encoding a space to a + is done in the encode() method */
	dontNeedEncoding.set(CHAR_MINUS);
	dontNeedEncoding.set(CHAR_UNDERSCORE);
	dontNeedEncoding.set(CHAR_DOT);
	dontNeedEncoding.set(CHAR_ASTERISK);
    }

    /**
     * You can't call the constructor.
     */
    private URIEncoder() {
    }

    /**
     * Decode encoded URI string into String under aglet's encoding
     * 
     * @param str
     *            encoded URI string
     * @return decoded string under specified encoding
     * @see java.net.URLEncoder
     */
    public static String decode(String str) {
	if (str == null) {
	    return null;
	}
	String encoding = Encoding.getDefault().getJavaEncoding();

	if (encoding == null) {
	    return null;
	}
	String s = null;

	try {
	    s = decode(str, encoding);
	} catch (UnsupportedEncodingException excpt) {
	    System.err.println(excpt.toString());
	    return null;
	}
	return s;
    }

    /**
     * Decode encoded URI string into String under specified encoding
     * 
     * @param str
     *            encoded URI string
     * @param encoding
     *            character encoding name
     * @return decoded string under specified encoding
     * @see java.net.URLEncoder
     */
    public static String decode(String str, String encoding)
							    throws UnsupportedEncodingException {
	if (str == null) {
	    return null;
	}
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	int length = str.length();
	int index = 0;

	while (index < length) {
	    char c = str.charAt(index);

	    index++;
	    if (c == CHAR_PLUS) {
		baos.write((byte) CHAR_SPACE);
	    } else if ((c == CHAR_PERCENT) && (index < length)) {
		char c1 = str.charAt(index);

		index++;
		if (index >= length) {
		    baos.write((byte) c);
		    baos.write((byte) c1);
		} else if (c1 == CHAR_PERCENT) {
		    baos.write((byte) c1);
		} else {
		    char c2 = str.charAt(index);

		    index++;
		    try {
			baos.write((byte) Hexadecimal.octetValue(c1, c2));
		    } catch (NumberFormatException excpt) {
			throw new IllegalArgumentException(excpt.toString());
		    }
		}
	    } else {
		baos.write((byte) c);
	    }
	}
	if ((encoding == null) || encoding.equals("")) {
	    return baos.toString();
	} else {
	    return baos.toString(encoding);
	}
    }

    /**
     * Translates a byte sequence into <code>x-www-form-urlencoded</code>
     * format.
     * 
     * @param A
     *            byte sequence to be translated.
     * @return the translated <code>String</code>.
     */
    public static String encode(byte[] sequence) {
	if ((sequence == null) || (sequence.length == 0)) {
	    return null;
	}
	StringBuffer buf = new StringBuffer();

	for (byte b : sequence) {
	    int c = b;

	    if ((0 <= c) && (c < NUM_CHAR) && dontNeedEncoding.get(c)) {
		if (c == CHAR_SPACE) {
		    c = CHAR_PLUS;
		}
		buf.append((char) c);
	    } else {
		buf.append(CHAR_PERCENT);
		buf.append(Hexadecimal.valueOf(b).toUpperCase());
	    }
	}
	return buf.toString();
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     * 
     * @param str
     *            <code>String</code> to be translated.
     * @return the translated <code>String</code>.
     */
    public static String encode(String str) {
	if (str == null) {
	    return null;
	}
	String encoding = Encoding.getDefault().getJavaEncoding();

	if (encoding == null) {
	    return null;
	}
	String s = null;

	try {
	    s = encode(str, encoding);
	} catch (UnsupportedEncodingException excpt) {
	    System.err.println(excpt.toString());
	    return null;
	}
	return s;
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     * 
     * @param str
     *            <code>String</code> to be translated.
     * @param encoding
     *            character encoding name
     * @exception UnsupportedEncodingException
     * @return the translated <code>String</code>.
     */
    public static String encode(String str, String encoding)
							    throws UnsupportedEncodingException {
	if (str == null) {
	    return null;
	}
	return encode(str.getBytes(encoding));
    }

    public static void main(String[] args) throws Exception {
	for (String str : args) {
	    System.out.println("encode(" + str + ")=" + encode(str));
	    System.out.println("decode(" + str + ")=" + decode(str));
	}
    }
}
