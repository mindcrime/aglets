package com.ibm.awb.misc;

import java.util.StringTokenizer;

/*
 * @(#)Hexadecimal.java
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
 * The <tt>Hexadecimal</tt> class
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */

public class Hexadecimal {
	public static void main(final String[] args) {
		final StringBuffer buff = new StringBuffer();

		for (final String arg : args) {
			buff.append(arg);
		}
		try {
			final byte[] seq = parseSeq(buff.toString());

			for (final byte element : seq) {
				System.out.print(element + " ");
			}
			System.out.println("");
		} catch (final NumberFormatException excpt) {
			System.err.println(excpt.toString());
		}
	}
	/**
	 * Converts a pair of characters as an octet in hexadecimal notation into
	 * integer.
	 * 
	 * @param c0
	 *            higher character of given octet in hexadecimal notation
	 * @param c1
	 *            lower character of given octet in hexadecimal notation
	 * @return a integer value of the octet
	 */
	public static int octetValue(final char c0, final char c1) throws NumberFormatException {
		final int n0 = Character.digit(c0, 16);

		if (n0 < 0) {
			throw new NumberFormatException(c0
					+ " is not a hexadecimal character.");
		}
		final int n1 = Character.digit(c1, 16);

		if (n1 < 0) {
			throw new NumberFormatException(c1
					+ " is not a hexadecimal character.");
		}
		return (n0 << 4) + n1;
	}

	/**
	 * Converts a string in hexadecimal notation into byte.
	 * 
	 * @param hex
	 *            string in hexadecimal notation
	 * @return a byte (1bytes)
	 */
	public static byte parseByte(final String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		}
		if (hex.equals("")) {
			return 0;
		}
		final Integer num = Integer.decode("0x" + hex);
		final int n = num.intValue();

		if ((n > 255) || (n < 0)) {
			throw new NumberFormatException("Out of range for byte.");
		}
		return num.byteValue();
	}

	/**
	 * Converts a string in hexadecimal notation into integer.
	 * 
	 * @param hex
	 *            string in hexadecimal notation
	 * @return a integer (4bytes)
	 */
	public static int parseInt(final String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		}
		if (hex.equals("")) {
			return 0;
		}
		final Integer num = Integer.decode("0x" + hex);
		final long n = num.longValue();

		if ((n > 4294967295L) || (n < 0L)) {
			throw new NumberFormatException("Out of range for integer.");
		}
		return num.intValue();
	}

	/**
	 * Converts a string in hexadecimal notation into byte sequence.
	 * 
	 * @param str
	 *            a string in hexadecimal notation
	 * @return byte sequence
	 */
	public static byte[] parseSeq(final String str) throws NumberFormatException {
		if ((str == null) || str.equals("")) {
			return null;
		}
		final int len = str.length();

		if (len % 2 != 0) {
			throw new NumberFormatException("Illegal length of string in hexadecimal notation.");
		}
		final int numOfOctets = len / 2;
		final byte[] seq = new byte[numOfOctets];

		for (int i = 0; i < numOfOctets; i++) {
			final String hex = str.substring(i * 2, i * 2 + 2);

			seq[i] = parseByte(hex);
		}
		return seq;
	}

	/**
	 * Converts a string in hexadecimal notation into byte sequence.
	 * 
	 * @param str
	 *            a string in hexadecimal notation
	 * @param delimiters
	 *            a set of delimiters
	 * @return byte sequence
	 */
	public static byte[] parseSeq(final String str, final String delimiters)
	throws NumberFormatException {
		if ((str == null) || str.equals("")) {
			return null;
		}
		if ((delimiters == null) || delimiters.equals("")) {
			return parseSeq(str);
		}
		final StringTokenizer tokenizer = new StringTokenizer(str, delimiters);
		final int numOfOctets = tokenizer.countTokens();
		final byte[] seq = new byte[numOfOctets];
		int i = 0;

		while (tokenizer.hasMoreTokens() && (i < numOfOctets)) {
			seq[i] = Hexadecimal.parseByte(tokenizer.nextToken());
			i++;
		}
		return seq;
	}

	// - /**
	// - * Constructs a hexadecimal number with a long integer.
	// - * @param num a long integer
	// - */
	// - public Hexadecimal(long num) {
	// - _hex = valueOf(num);
	// - _num = (int)num;
	// - }

	/**
	 * Converts a string in hexadecimal notation into short integer.
	 * 
	 * @param hex
	 *            string in hexadecimal notation
	 * @return a short integer (2bytes)
	 */
	public static short parseShort(final String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		}
		if (hex.equals("")) {
			return 0;
		}
		final Integer num = Integer.decode("0x" + hex);
		final int n = num.intValue();

		if ((n > 65535) || (n < 0)) {
			throw new NumberFormatException("Out of range for short integer.");
		}
		return num.shortValue();
	}

	/**
	 * Converts a byte into its hexadecimal notation.
	 * 
	 * @param num
	 *            a byte (1bytes)
	 * @return hexadecimal notation of the byte
	 */
	public static String valueOf(final byte num) {
		return valueOf(num, true);
	}

	/**
	 * Converts a byte into its hexadecimal notation.
	 * 
	 * @param num
	 *            a byte (1bytes)
	 * @param padding
	 *            fit the length to 2 by filling with '0' when padding is true
	 * @return hexadecimal notation of the byte
	 */
	public static String valueOf(final byte num, final boolean padding) {
		String hex = Integer.toHexString(num);

		if (padding) {
			hex = "00" + hex;
			final int len = hex.length();

			hex = hex.substring(len - 2, len);
		}
		return hex;
	}

	// - /**
	// - * Converts a string in hexadecimal notation into long integer.
	// - * @param hex string in hexadecimal notation
	// - * @return a long integer (8bytes)
	// - */
	// - public static long parseLong(String hex) throws NumberFormatException {
	// - if(hex==null) {
	// - throw new
	// IllegalArgumentException("Null string in hexadecimal notation.");
	// - }
	// - if(hex.equals("")) {
	// - return 0;
	// - }
	// -
	// - return Integer.decode("0x"+hex).longValue();
	// - }

	/**
	 * Converts a byte sequence into its hexadecimal notation.
	 * 
	 * @param seq
	 *            a byte sequence
	 * @return hexadecimal notation of the byte sequence
	 */
	public static String valueOf(final byte[] seq) {
		if (seq == null) {
			return null;
		}
		final StringBuffer buff = new StringBuffer();

		for (final byte element : seq) {
			buff.append(valueOf(element, true));
		}
		return buff.toString();
	}

	/**
	 * Converts a byte sequence into its hexadecimal notation.
	 * 
	 * @param seq
	 *            a byte sequence
	 * @param separator
	 *            separator between bytes
	 * @return hexadecimal notation of the byte sequence
	 */
	public static String valueOf(final byte[] seq, final char separator) {
		if (seq == null) {
			return null;
		}
		final StringBuffer buff = new StringBuffer();

		for (int i = 0; i < seq.length; i++) {
			if (i > 0) {
				buff.append(separator);
			}
			buff.append(valueOf(seq[i], true));
		}
		return buff.toString();
	}

	/**
	 * Converts a integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a integer (4bytes)
	 * @return hexadecimal notation of the integer
	 */
	public static String valueOf(final int num) {
		return valueOf(num, true);
	}

	/**
	 * Converts a integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a integer (4bytes)
	 * @param padding
	 *            fit the length to 8 by filling with '0' when padding is true
	 * @return hexadecimal notation of the integer
	 */
	public static String valueOf(final int num, final boolean padding) {
		String hex = Integer.toHexString(num);

		if (padding) {
			hex = "00000000" + hex;
			final int len = hex.length();

			hex = hex.substring(len - 8, len);
		}
		return hex;
	}

	/**
	 * Converts a long integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a long integer (8bytes)
	 * @return hexadecimal notation of the long integer
	 */
	public static String valueOf(final long num) {
		return valueOf(num, true);
	}

	/**
	 * Converts a long integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a long integer (8bytes)
	 * @param padding
	 *            fit the length to 16 by filling with '0' when padding is true
	 * @return hexadecimal notation of the long integer
	 */
	public static String valueOf(final long num, final boolean padding) {
		String hex = Long.toHexString(num);

		if (padding) {
			hex = "0000000000000000" + hex;
			final int len = hex.length();

			hex = hex.substring(len - 16, len);
		}
		return hex;
	}

	/**
	 * Converts a short integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a short integer (2bytes)
	 * @return hexadecimal notation of the short integer
	 */
	public static String valueOf(final short num) {
		return valueOf(num, true);
	}

	/**
	 * Converts a short integer into its hexadecimal notation.
	 * 
	 * @param num
	 *            a short integer (2bytes)
	 * @param padding
	 *            fit the length to 8 by filling with '0' when padding is true
	 * @return hexadecimal notation of the short integer
	 */
	public static String valueOf(final short num, final boolean padding) {
		String hex = Integer.toHexString(num);

		if (padding) {
			hex = "0000" + hex;
			final int len = hex.length();

			hex = hex.substring(len - 4, len);
		}
		return hex;
	}

	private String _hex = null;

	private int _num = 0;

	/**
	 * Constructs a hexadecimal number with a byte.
	 * 
	 * @param num
	 *            a byte
	 */
	public Hexadecimal(final byte num) {
		_hex = valueOf(num);
		_num = num;
	}

	/**
	 * Constructs a hexadecimal number with a integer.
	 * 
	 * @param num
	 *            a integer
	 */
	public Hexadecimal(final int num) {
		_hex = valueOf(num);
		_num = num;
	}

	/**
	 * Constructs a hexadecimal number with a short integer.
	 * 
	 * @param num
	 *            a short integer
	 */
	public Hexadecimal(final short num) {
		_hex = valueOf(num);
		_num = num;
	}

	/**
	 * Gets a byte value.
	 * 
	 * @return a byte of the hexadecimal number
	 */
	public byte byteValue() throws NumberFormatException {
		if ((_num > 255) || (_num < 0)) {
			throw new NumberFormatException("Out of range for byte.");
		}
		return (byte) _num;
	}

	/**
	 * Gets a string in hexadecimal notation.
	 * 
	 * @return string in hexadecimal notation of the number
	 */
	public String hexadecimalValue() {
		return _hex;
	}

	/**
	 * Gets a integer value.
	 * 
	 * @return a integer of the hexadecimal number
	 */
	public int intValue() throws NumberFormatException {
		if ((_num > 4294967295L) || (_num < 0)) {
			throw new NumberFormatException("Out of range for integer.");
		}
		return _num;
	}

	/**
	 * Gets a short integer value.
	 * 
	 * @return a short integer of the hexadecimal number
	 */
	public short shortValue() throws NumberFormatException {
		if ((_num > 65535) || (_num < 0)) {
			throw new NumberFormatException("Out of range for short integer.");
		}
		return (short) _num;
	}
}
