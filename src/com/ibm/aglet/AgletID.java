package com.ibm.aglet;

/*
 * @(#)AgletID.java
 * 
 * (c) Copyright IBM Corp. 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

/**
 * The <tt>AgletID</tt> class represents the unique identifier
 * given the aglet.
 * 
 * @version     1.10	$Date: 2009/07/27 10:31:41 $
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */

public /* final */ class AgletID implements java.io.Serializable {

	static final long serialVersionUID = -2404000023094224993L;

	/**
	 * byte array containing id information used in the system.
	 */
	private byte[] id = null;

	private AgletID() {}
	/**
	 * Constructs an aglet identifier with given byte array.
	 */
	public AgletID(byte[] b) {
		id = new byte[b.length];
		System.arraycopy(b, 0, id, 0, b.length);
	}
	/**
	 * Constructs an aglet identifier with a given string.
	 */
	public AgletID(String rep) {
		int len = rep.length();
		byte[] b = new byte[len / 2];

		for (int i = 0, j = 0; j < len; i++, j++) {
			b[i] = (byte)(Character.digit(rep.charAt(j++), 16) << 4);
			b[i] += (byte)Character.digit(rep.charAt(j), 16);
		} 
		id = b;
	}
	/**
	 * Compares two aglet identifiers.
	 * @param obj the Aglet to be compared with.
	 * @return true if and only if the two Aglets are identical.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AgletID) {
			byte b[] = ((AgletID)obj).id;

			if (id.length == b.length) {
				for (int i = 0; i < id.length; i++) {
					if (id[i] != b[i]) {
						return false;
					} 
				} 
				return true;
			} 
		} 
		return false;
	}
	/**
	 * Returns an integer suitable for hash table indexing.
	 * @return hash table indexing integer.
	 */
	public int hashCode() {
		int h = 0;

		for (int i = 0; i < id.length; i++) {
			h += (h * 37) + (int)id[i];
		} 
		return h;
	}
	/**
	 * Returns byte array representation of the id. The copy of array
	 * is returned so that it cannot be altered.
	 */
	public byte[] toByteArray() {
		byte[] b = new byte[id.length];

		System.arraycopy(id, 0, b, 0, id.length);
		return b;
	}
	/**
	 * Returns a human readable form of the aglet identifier.
	 * @return the Aglet identity in text form.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < id.length; i++) {
			byte b = id[i];

			buf.append(Character.forDigit((b >>> 4) & 0xF, 16));
			buf.append(Character.forDigit(b & 0xF, 16));
		} 
		return buf.toString();
	}
}
