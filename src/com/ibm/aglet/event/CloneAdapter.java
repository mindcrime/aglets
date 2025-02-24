package com.ibm.aglet.event;

/*
 * @(#)CloneAdapter.java
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
 * The adapter which receives clone events. This class is provided as
 * convenience for easily creating listerns by extending this class and
 * overriding only the methods of interest.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Misturu Oshima
 */
public class CloneAdapter implements CloneListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8036790083460884469L;

	/**
	 * Invoked after an aglet was cloned. This is delivered to the cloned aglet.
	 */
	@Override
	public void onClone(final CloneEvent event) {
	}

	/**
	 * Invoked after an aglet was cloned. This is delivered to the original
	 * aglet.
	 */
	@Override
	public void onCloned(final CloneEvent event) {
	}

	/**
	 * Invoked when an aglet is attempted to clone
	 */
	@Override
	public void onCloning(final CloneEvent event) {
	}
}
