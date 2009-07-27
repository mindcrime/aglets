package com.ibm.aglet.util;

/*
 * @(#)Arguments.java
 * 
 * (c) Copyright IBM Corp. 1996
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

import java.util.Hashtable;

/**
 * The <tt>Argument</tt> class is a object that holds various kinds of
 * objects as arguments. This exists because the Hashtable does not
 * accept "null" as arguments.
 * 
 * @version     1.20    $Date: 2009/07/27 10:31:42 $
 * @author	Mitsuru Oshima
 */
final public class Arguments extends Hashtable {

	static final long serialVersionUID = -2495749088367637553L;

	/**
	 * In order to store null value in the Hashtable,
	 * NULL is used as a magic object to specify the null value.
	 */
	private final static String NULL = "null";

	/**
	 * Constructs a empty arguments object.
	 */
	public Arguments() {}
	public Object clone() {
		return super.clone();
	}
	/**
	 * Get the value associated with the name.
	 * @return the value associated with the given name
	 */
	public Object getArg(String name) {
		Object o = get(name);

		return o == NULL ? null : o;
	}
	/**
	 * Set a byte value with an associated name.
	 * @param name a name of this argument.
	 * @param value a byte value of this argument.
	 */
	public Object setArg(String name, byte value) {
		return super.put(name, new Byte(value));
	}
	/**
	 * Sets a character value with an associated name.
	 * @param name a name of this argument.
	 * @param value a character value of this argument.
	 */
	public Object setArg(String name, char value) {
		return super.put(name, new Character(value));
	}
	/**
	 * Set a double value with an associated name.
	 * @param name a name of this argument.
	 * @param d a double value of this argument.
	 */
	public Object setArg(String name, double value) {
		return super.put(name, new Double(value));
	}
	/**
	 * Set a float value with an associated name.
	 * @param name a name of this argument.
	 * @param value a float value of this argument.
	 */
	public Object setArg(String name, float value) {
		return super.put(name, new Float(value));
	}
	/**
	 * Set a int value with an associated name.
	 * @param name a name of this argument.
	 * @param value an integer value of this argument.
	 */
	public Object setArg(String name, int value) {
		return super.put(name, new Integer(value));
	}
	/**
	 * Sets a long value with an associated name.
	 * @param name a name of this argument.
	 * @param value a long value of this argument.
	 */
	public Object setArg(String name, long value) {
		return super.put(name, new Long(value));
	}
	/**
	 * Set a value with an associated name.
	 * @param name a name of this argument.
	 * @param value a value of this argument.
	 */
	public Object setArg(String name, Object value) {
		return value == null ? super.put(name, NULL) : super.put(name, value);
	}
	/**
	 * Set a short value with an associated name.
	 * @param name a name of this argument.
	 * @param value a short value of this argument.
	 */
	public Object setArg(String name, short value) {
		return super.put(name, new Short(value));
	}
	/**
	 * Set a boolean value with an associated name.
	 * @param name a name of this argument.
	 * @param value a boolean value of this argument.
	 */
	public Object setArg(String name, boolean value) {
		return super.put(name, new Boolean(value));
	}
}
