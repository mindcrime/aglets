package com.ibm.aglet;

/*
 * @(#)InvalidAgletException.java
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
 * Signals that the aglet proxy is not valid any longer.
 * 
 * @version 1.20 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class InvalidAgletException extends AgletException {

    /**
     * 
     */
    private static final long serialVersionUID = 382151911909514979L;

    /*
     * Constructs a InvalidAgletException.
     * 
     * @param s the detailed message
     */
    public InvalidAgletException() {
	super();
    }

    /*
     * Constructs a InvalidAgletException with the specified detailed message.
     * 
     * @param s the detailed message
     */
    public InvalidAgletException(String s) {
	super(s);
    }

    public InvalidAgletException(Exception cause) {
	super(cause);
    }
}
