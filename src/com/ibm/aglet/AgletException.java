package com.ibm.aglet;

/*
 * @(#)AgletException.java
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

import java.io.*;

/**
 * Signals that an aglet exception has occurred.
 * 
 * @version     1.10   $Date: 2009/07/27 10:31:41 $
 * @author      Danny B. Lange
 */
public class AgletException extends Exception implements Serializable {
	private boolean _original = true;
	private String _stackTrace = null;

	/**
	 * Constructs an AgletException with no detail message.
	 * A detail message is a string that describes this particular exception.
	 */
	public AgletException() {
		super();
	}
	/**
	 * Constructs an AgletException with the specified detail message.
	 * A detail message is a string that describes this particular exception.
	 * @param s the detail message.
	 */
	public AgletException(String s) {
		super(s);
	}
	public void printStackTrace() {
		if (_original) {
			super.printStackTrace();
		} else {
			System.err.println(this);
			System.err.println(_stackTrace);
		} 
	}
	private void readObject(ObjectInputStream s) 
			throws IOException, ClassNotFoundException {
		_original = false;
		_stackTrace = (String)s.readObject();
	}
	/**
	 * 
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		if (_original) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			printStackTrace(new PrintWriter(new OutputStreamWriter(out)));
			s.writeObject(new String(out.toByteArray()));
		} 
	}
}
