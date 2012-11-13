package com.ibm.aglet;

/*
 * @(#)AgletNotFoundException.java
 * 
 * (c) Copyright IBM Corp. 1996, 1997, 1998
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
 * Signals that the aglet could not be found.
 * 
 * @version 1.00 96/08/23
 * @author Mitsuru Oshima
 */

public class AgletNotFoundException extends AgletException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6140535636613461078L;

	/*
	 * Constructs a AgletNotFoundException
	 */
	public AgletNotFoundException() {
	}

	/*
	 * Constructs a AgletNotFoundException with the specified detailed message.
	 * 
	 * @param s the detailed message
	 */
	public AgletNotFoundException(final String s) {
		super(s);
	}
}
