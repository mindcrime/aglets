package com.ibm.aglets.security;

/*
 * @(#)Randoms.java
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

import java.security.SecureRandom;
import java.util.Hashtable;

/**
 * The <tt>Randoms</tt> class is a set of secure random number generators.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
public class Randoms extends Object {
    /**
     * The hash table for secure random number generator.
     */
    private static Hashtable _secureTable = new Hashtable();
    private static Hashtable _pseudoTable = new Hashtable();

    /**
     * Use a secure random seed or an unsecure random seed
     */
    private static boolean _useSecureRandomSeed = true;

    /*
     * public static byte[] getPseudoSeed(int numOfBytes) { final Calendar cal =
     * Calendar.getInstance(); final Date time = cal.getTime(); final long mills
     * = time.getTime(); final String str = Long.toString(mills); final byte[]
     * seq = str.getBytes(); byte[] seed = new byte[numOfBytes]; int i; for(i=0;
     * i<numOfBytes && i<seq.length; i++) { seed[i] = seq[i]; } return seed; }
     */
    /**
     * Returns the given number of pseudo seed bytes, computed with Calendar's
     * milliseconds value.
     * 
     * @return the given number of pseudo seed bytes
     */
    public static byte[] getPseudoSeed(int length) {
	byte b[] = new byte[length];
	for (int i = 0; i < length; i++) {
	    b[i] = (byte) (System.currentTimeMillis() * 3);

	    long h = (new Object().hashCode() + System.currentTimeMillis()) & 0xFF;

	    try {
		Thread.currentThread();
		Thread.sleep(h);
	    } catch (InterruptedException ex) {
	    }
	}
	return b;
    }

    /**
     * Generates a random number for given length by the secure random number
     * generator.
     * 
     * @param length
     *            length of byte sequence to be generated
     * @param seq
     *            a byte sequence to which random number is copied
     */
    public static synchronized void getRandom(int length, byte[] seq) {
	SecureRandom random = getRandomGenerator(length);

	if (random != null) {
	    random.nextBytes(seq);
	}
    }

    /**
     * Gets a secure random number generator for given length with seed. If the
     * secure random number generator is not stored in the hash table, creates
     * it and stores it into the table.
     * 
     * @param length
     *            length of byte sequence to be generated
     * @return secure random number generator for given length
     */
    public static synchronized SecureRandom getRandomGenerator(int length) {
	return getRandomGenerator(length, _useSecureRandomSeed);
    }

    /**
     * Gets a secure random number generator for given length with seed. If the
     * secure random number generator is not stored in the hash table, creates
     * it and stores it into the table.
     * 
     * @param length
     *            length of byte sequence to be generated
     * @param useSecureRandomSeed
     *            use a secure random seed if true, or use an unsecure random
     *            seed otherwise.
     * @return secure random number generator for given length
     */
    public static synchronized SecureRandom getRandomGenerator(int length,
	    boolean useSecureRandomSeed) {
	final Integer len = new Integer(length);
	SecureRandom random = null;

	Hashtable table = null;

	if (useSecureRandomSeed) {

	    // Use a secure random seed
	    table = _secureTable;
	} else {

	    // Use an unsecure random seed
	    table = _pseudoTable;
	}

	Object obj = table.get(len);

	if ((obj != null) && (obj instanceof SecureRandom)) {
	    random = (SecureRandom) obj;
	} else {
	    random = new SecureRandom(getSeed(length, useSecureRandomSeed));
	    table.put(len, random);
	}

	return random;
    }

    private static byte[] getSeed(int length, boolean secure) {
	if (!secure) {
	    System.out.print("(Using Pseudo Seed)");
	    return getPseudoSeed(length);
	} else {
	    // SecureRandom.getSeed(length);
	    try {
		System.out.print("(Using Secure Seed - SHA1PRNG)");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		return sr.generateSeed(length);
	    } catch (java.security.NoSuchAlgorithmException nsae) {
		System.out.print("[FAILED, use pseudo seed]");
		return getPseudoSeed(length);
	    }
	}
    }

    /**
     * Returns the use of secure/unsecure random seed.
     * 
     * @return use a secure random seed if true, or use an unsecure random seed
     *         otherwise.
     */
    public static boolean getUseSecureRandomSeed() {
	return _useSecureRandomSeed;
    }

    /**
     * Sets the use of secure/unsecure random seed.
     * 
     * @param useSecureRandomSeed
     *            use a secure random seed if true, or use an unsecure random
     *            seed otherwise.
     */
    public static void setUseSecureRandomSeed(boolean useSecureRandomSeed) {
	_useSecureRandomSeed = useSecureRandomSeed;
    }
}
