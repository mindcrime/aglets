package com.ibm.aglet.system;

/*
 * @(#)Aglets.java
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

import java.io.IOException;
import java.net.URL;

import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;

/**
 * Aglets class defines a set of convenient functions for a client which has no
 * AgletContext and daemon to receive incoming aglets.
 * 
 * <pre>
 * static public void main(String args[]) {
 *     String contextAddress = &quot;atp://server.com:4434&quot;;
 * 
 *     // create from server's local path
 *     AgletProxy p1 = Aglets.createAglet(contextAddress, null, &quot;test.Aglet&quot;, null);
 *     AgletID id = p1.getAgletID();
 * 
 *     // this returns a proxy equivalent to p1.
 *     AgletProxy p2 = Aglets.getAgletProxy(contextAddress, id);
 * 
 *     p2.sendMessage(new Message(&quot;startTrip&quot;));
 * }
 * </pre>
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 */
abstract public class Aglets {

    static {
	try {
	    AgletRuntime.init(null);
	} catch (Throwable t) {

	    // ignore
	}
    }

    /**
     * Creates an aglet
     * 
     */
    static public AgletProxy createAglet(
					 String contextAddress,
					 URL codebase,
					 String classname,
					 Object init) throws IOException {
	return AgletRuntime.getAgletRuntime().createAglet(contextAddress, codebase, classname, init);
    }

    /**
     * Gets an enumeration of aglet proxies of all aglets residing in the
     * context specified by contextAddress.
     * 
     * @param contextAddress
     *            specify context URL with a string.
     */
    static public AgletProxy[] getAgletProxies(String contextAddress)
								     throws IOException {
	return AgletRuntime.getAgletRuntime().getAgletProxies(contextAddress);
    }

    /**
     * Obtains a proxy reference to the remote aglet.
     */
    static public AgletProxy getAgletProxy(String contextAddress, AgletID id)
									     throws IOException {
	return AgletRuntime.getAgletRuntime().getAgletProxy(contextAddress, id);
    }
}
