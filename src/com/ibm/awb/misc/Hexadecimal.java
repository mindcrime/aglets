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
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */

public class Hexadecimal {
	private String _hex = null;
	private int _num = 0;

	/**
	 * Constructs a hexadecimal number with a byte.
	 * @param num a byte
	 */
	public Hexadecimal(byte num) {
		_hex = valueOf(num);
		_num = (int)num;
	}
	/**
	 * Constructs a hexadecimal number with a integer.
	 * @param num a integer
	 */
	public Hexadecimal(int num) {
		_hex = valueOf(num);
		_num = (int)num;
	}
	/**
	 * Constructs a hexadecimal number with a short integer.
	 * @param num a short integer
	 */
	public Hexadecimal(short num) {
		_hex = valueOf(num);
		_num = (int)num;
	}
	/**
	 * Gets a byte value.
	 * @return a byte of the hexadecimal number
	 */
	public byte byteValue() throws NumberFormatException {
		if (_num > 255 || _num < 0) {
			throw new NumberFormatException("Out of range for byte.");
		} 
		return (byte)_num;
	}
	// -   /**
	// -    * Constructs a hexadecimal number with a long integer.
	// -    * @param num a long integer
	// -    */
	// -   public Hexadecimal(long num) {
	// -     _hex = valueOf(num);
	// -     _num = (int)num;
	// -   }

	/**
	 * Gets a string in hexadecimal notation.
	 * @return string in hexadecimal notation of the number
	 */
	public String hexadecimalValue() {
		return _hex;
	}
	/**
	 * Gets a integer value.
	 * @return a integer of the hexadecimal number
	 */
	public int intValue() throws NumberFormatException {
		if (_num > 4294967295L || _num < 0) {
			throw new NumberFormatException("Out of range for integer.");
		} 
		return (int)_num;
	}
	public static void main(String[] args) {
		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < args.length; i++) {
			buff.append(args[i]);
		} 
		try {
			byte[] seq = parseSeq(buff.toString());

			for (int i = 0; i < seq.length; i++) {
				System.out.print(seq[i] + " ");
			} 
			System.out.println("");
		} catch (NumberFormatException excpt) {
			System.err.println(excpt.toString());
		} 
	}
	// -   /**
	// -    * Converts a string in hexadecimal notation into long integer.
	// -    * @param hex string in hexadecimal notation
	// -    * @return a long integer (8bytes)
	// -    */
	// -   public static long parseLong(String hex) throws NumberFormatException {
	// -     if(hex==null) {
	// -       throw new IllegalArgumentException("Null string in hexadecimal notation.");
	// -     }
	// -     if(hex.equals("")) {
	// -       return 0;
	// -     }
	// -
	// -     return Integer.decode("0x"+hex).longValue();
	// -   }

	/**
	 * Converts a pair of characters as an octet in hexadecimal notation into integer.
	 * @param c0 higher character of given octet in hexadecimal notation
	 * @param c1 lower character of given octet in hexadecimal notation
	 * @return a integer value of the octet
	 */
	public static int octetValue(char c0, 
								 char c1) throws NumberFormatException {
		int n0 = Character.digit(c0, 16);

		if (n0 < 0) {
			throw new NumberFormatException(c0 
											+ " is not a hexadecimal character.");
		} 
		int n1 = Character.digit(c1, 16);

		if (n1 < 0) {
			throw new NumberFormatException(c1 
											+ " is not a hexadecimal character.");
		} 
		return (n0 << 4) + n1;
	}
	/**
	 * Converts a string in hexadecimal notation into byte.
	 * @param hex string in hexadecimal notation
	 * @return a byte (1bytes)
	 */
	public static byte parseByte(String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		} 
		if (hex.equals("")) {
			return 0;
		} 
		Integer num = Integer.decode("0x" + hex);
		int n = num.intValue();

		if (n > 255 || n < 0) {
			throw new NumberFormatException("Out of range for byte.");
		} 
		return num.byteValue();
	}
	/**
	 * Converts a string in hexadecimal notation into integer.
	 * @param hex string in hexadecimal notation
	 * @return a integer (4bytes)
	 */
	public static int parseInt(String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		} 
		if (hex.equals("")) {
			return 0;
		} 
		Integer num = Integer.decode("0x" + hex);
		long n = num.longValue();

		if (n > 4294967295L || n < 0L) {
			throw new NumberFormatException("Out of range for integer.");
		} 
		return num.intValue();
	}
	/**
	 * Converts a string in hexadecimal notation into byte sequence.
	 * @param str a string in hexadecimal notation
	 * @return byte sequence
	 */
	public static byte[] parseSeq(String str) throws NumberFormatException {
		if (str == null || str.equals("")) {
			return null;
		} 
		int len = str.length();

		if (len % 2 != 0) {
			throw new NumberFormatException("Illegal length of string in hexadecimal notation.");
		} 
		int numOfOctets = len / 2;
		byte[] seq = new byte[numOfOctets];

		for (int i = 0; i < numOfOctets; i++) {
			String hex = str.substring(i * 2, i * 2 + 2);

			seq[i] = parseByte(hex);
		} 
		return seq;
	}
	/**
	 * Converts a string in hexadecimal notation into byte sequence.
	 * @param str a string in hexadecimal notation
	 * @param delimiters a set of delimiters
	 * @return byte sequence
	 */
	public static byte[] parseSeq(String str, String delimiters) 
			throws NumberFormatException {
		if (str == null || str.equals("")) {
			return null;
		} 
		if (delimiters == null || delimiters.equals("")) {
			return parseSeq(str);
		} 
		StringTokenizer tokenizer = new StringTokenizer(str, delimiters);
		int numOfOctets = tokenizer.countTokens();
		byte[] seq = new byte[numOfOctets];
		int i = 0;

		while (tokenizer.hasMoreTokens() && i < numOfOctets) {
			seq[i] = Hexadecimal.parseByte(tokenizer.nextToken());
			i++;
		} 
		return seq;
	}
	/**
	 * Converts a string in hexadecimal notation into short integer.
	 * @param hex string in hexadecimal notation
	 * @return a short integer (2bytes)
	 */
	public static short parseShort(String hex) throws NumberFormatException {
		if (hex == null) {
			throw new IllegalArgumentException("Null string in hexadecimal notation.");
		} 
		if (hex.equals("")) {
			return 0;
		} 
		Integer num = Integer.decode("0x" + hex);
		int n = num.intValue();

		if (n > 65535 || n < 0) {
			throw new NumberFormatException("Out of range for short integer.");
		} 
		return num.shortValue();
	}
	/**
	 * Gets a short integer value.
	 * @return a short integer of the hexadecimal number
	 */
	public short shortValue() throws NumberFormatException {
		if (_num > 65535 || _num < 0) {
			throw new NumberFormatException("Out of range for short integer.");
		} 
		return (short)_num;
	}
	/**
	 * Converts a byte sequence into its hexadecimal notation.
	 * @param seq a byte sequence
	 * @return hexadecimal notation of the byte sequence
	 */
	public static String valueOf(byte[] seq) {
		if (seq == null) {
			return null;
		} 
		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < seq.length; i++) {
			buff.append(valueOf(seq[i], true));
		} 
		return buff.toString();
	}
	/**
	 * Converts a byte sequence into its hexadecimal notation.
	 * @param seq a byte sequence
	 * @param separator separator between bytes
	 * @return hexadecimal notation of the byte sequence
	 */
	public static String valueOf(byte[] seq, char separator) {
		if (seq == null) {
			return null;
		} 
		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < seq.length; i++) {
			if (i > 0) {
				buff.append(separator);
			} 
			buff.append(valueOf(seq[i], true));
		} 
		return buff.toString();
	}
	/**
	 * Converts a byte into its hexadecimal notation.
	 * @param num a byte (1bytes)
	 * @return hexadecimal notation of the byte
	 */
	public static String valueOf(byte num) {
		return valueOf(num, true);
	}
	/**
	 * Converts a byte into its hexadecimal notation.
	 * @param num a byte (1bytes)
	 * @param padding fit the length to 2 by filling with '0' when padding is true
	 * @return hexadecimal notation of the byte
	 */
	public static String valueOf(byte num, boolean padding) {
		String hex = Integer.toHexString((int)num);

		if (padding) {
			hex = "00" + hex;
			int len = hex.length();

			hex = hex.substring(len - 2, len);
		} 
		return hex;
	}
	/**
	 * Converts a integer into its hexadecimal notation.
	 * @param num a integer (4bytes)
	 * @return hexadecimal notation of the integer
	 */
	public static String valueOf(int num) {
		return valueOf(num, true);
	}
	/**
	 * Converts a integer into its hexadecimal notation.
	 * @param num a integer (4bytes)
	 * @param padding fit the length to 8 by filling with '0' when padding is true
	 * @return hexadecimal notation of the integer
	 */
	public static String valueOf(int num, boolean padding) {
		String hex = Integer.toHexString(num);

		if (padding) {
			hex = "00000000" + hex;
			int len = hex.length();

			hex = hex.substring(len - 8, len);
		} 
		return hex;
	}
	/**
	 * Converts a long integer into its hexadecimal notation.
	 * @param num a long integer (8bytes)
	 * @return hexadecimal notation of the long integer
	 */
	public static String valueOf(long num) {
		return valueOf(num, true);
	}
	/**
	 * Converts a long integer into its hexadecimal notation.
	 * @param num a long integer (8bytes)
	 * @param padding fit the length to 16 by filling with '0' when padding is true
	 * @return hexadecimal notation of the long integer
	 */
	public static String valueOf(long num, boolean padding) {
		String hex = Long.toHexString(num);

		if (padding) {
			hex = "0000000000000000" + hex;
			int len = hex.length();

			hex = hex.substring(len - 16, len);
		} 
		return hex;
	}
	/**
	 * Converts a short integer into its hexadecimal notation.
	 * @param num a short integer (2bytes)
	 * @return hexadecimal notation of the short integer
	 */
	public static String valueOf(short num) {
		return valueOf(num, true);
	}
	/**
	 * Converts a short integer into its hexadecimal notation.
	 * @param num a short integer (2bytes)
	 * @param padding fit the length to 8 by filling with '0' when padding is true
	 * @return hexadecimal notation of the short integer
	 */
	public static String valueOf(short num, boolean padding) {
		String hex = Integer.toHexString((int)num);

		if (padding) {
			hex = "0000" + hex;
			int len = hex.length();

			hex = hex.substring(len - 4, len);
		} 
		return hex;
	}
}
