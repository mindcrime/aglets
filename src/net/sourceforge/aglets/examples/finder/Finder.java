package net.sourceforge.aglets.examples.finder;

/*
 * @(#)Finder.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.util.Hashtable;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

public class Finder extends Aglet {

    // add this.
    Hashtable _database = new Hashtable();

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("Lookup")) {
	    msg.sendReply(this._database.get(msg.getArg()));
	} else if (msg.sameKind("Register")) {
	    System.out.println("Registering .. " + msg.getArg("NAME"));
	    System.out.println(msg.getArg("PROXY"));
	    this._database.put(msg.getArg("NAME"), msg.getArg("PROXY"));

	    // same
	} else if (msg.sameKind("Unregister")) {
	    this._database.remove(msg.getArg("NAME"));
	} else {
	    return false;
	}
	return true;
    }

    @Override
    public void onCreation(Object init) {

	// register it as an default finder..
	AgletProxy proxy = this.getAgletContext().getAgletProxy(this.getAgletID());

	this.getAgletContext().setProperty("finder", proxy);
    }
}
