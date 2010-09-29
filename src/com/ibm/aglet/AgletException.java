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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Signals that an aglet exception has occurred.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Danny B. Lange
 */
public class AgletException extends Exception implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -5852186361181050874L;
    private boolean _original = true;
    private String _stackTrace = null;

    /**
     * Constructs an AgletException with no detail message. A detail message is
     * a string that describes this particular exception.
     */
    public AgletException() {
	super();
    }

    /**
     * Creates an exception with another exception as cause.
     * 
     * @param initialException
     *            the cause of this exception
     */
    public AgletException(Exception initialException) {
	super(initialException);
    }

    /**
     * Constructs an AgletException with the specified detail message. A detail
     * message is a string that describes this particular exception.
     * 
     * @param s
     *            the detail message.
     */
    public AgletException(String s) {
	super(s);
    }

    @Override
    public void printStackTrace() {
	if (this._original) {
	    super.printStackTrace();
	} else {
	    System.err.println(this);
	    System.err.println(this._stackTrace);
	}
    }

    private void readObject(ObjectInputStream s)
    throws IOException,
    ClassNotFoundException {
	this._original = false;
	this._stackTrace = (String) s.readObject();
    }

    /**
     * 
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
	if (this._original) {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();

	    this.printStackTrace(new PrintWriter(new OutputStreamWriter(out)));
	    s.writeObject(new String(out.toByteArray()));
	}
    }
}
