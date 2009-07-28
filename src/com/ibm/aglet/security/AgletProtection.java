package com.ibm.aglet.security;

/*
 * @(#)AgletProtection.java
 * 
 * (c) Copyright IBM Corp. 1997
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

import java.security.Permission;

import com.ibm.aglets.security.PlainAgletPermission;

/**
 * The <tt>AgletProtection</tt> class represents the protection of
 * system messages for aglet.
 * 
 * @version     1.00	$Date: 2009/07/28 07:04:53 $
 * @author	ONO Kouichi
 */

public final class AgletProtection extends PlainAgletPermission 
	implements Protection {
	public AgletProtection(String name, String actions) {
		super(name, actions);
	}
	public boolean implies(Permission p) {
		if (!(p instanceof AgletProtection)) {
			return false;
		} 
		return super.implies(p);
	}
}
