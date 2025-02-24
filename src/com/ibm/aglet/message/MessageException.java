package com.ibm.aglet.message;

/*
 * @(#)MessageException.java
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.ibm.aglet.AgletException;

/**
 * Signals that the exception occured while processsing the message.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */

public class MessageException extends AgletException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3923549098763231035L;
	private Throwable _exception;

	/*
	 * Constructs a MessageException
	 */
	public MessageException(final Throwable ex) {
		_exception = ex;
	}

	/*
	 * Constructs a MessageException with the detailed message.
	 * 
	 * @param s the detailed message
	 */
	public MessageException(final Throwable ex, final String s) {
		super(s);
		_exception = ex;
	}

	/**
	 * Gets the message occured
	 */
	public Throwable getException() {
		return _exception;
	}

	private void readObject(final ObjectInputStream s)
	throws IOException,
	ClassNotFoundException {
		final Object ex = s.readObject();

		if (ex instanceof String) {
			try {
				_exception = (Throwable) Class.forName((String) ex).newInstance();
			} catch (final Exception ee) {
				ee.printStackTrace();
			}
		} else if (ex instanceof Throwable) {
			_exception = (Throwable) ex;
		} else {
			_exception = null;
		}
	}

	@Override
	public String toString() {
		return super.toString() + " (" + _exception + ")";
	}

	/*
	 * 
	 */
	private void writeObject(final ObjectOutputStream s) throws IOException {
		if (_exception instanceof Serializable) {
			s.writeObject(_exception);
		} else if (_exception != null) {
			s.writeObject(_exception.getClass().getName());
		} else {
			s.writeObject(null);
		}
	}
}
