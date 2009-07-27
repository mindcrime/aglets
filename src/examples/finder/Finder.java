package examples.finder;

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

import com.ibm.aglet.*;
import com.ibm.aglet.message.Message;

import java.util.Hashtable;

public class Finder extends Aglet {

	// add this.
	Hashtable _database = new Hashtable();

	public boolean handleMessage(Message msg) {
		if (msg.sameKind("Lookup")) {
			msg.sendReply(_database.get(msg.getArg()));
		} else if (msg.sameKind("Register")) {
			System.out.println("Registering .. " + msg.getArg("NAME"));
			System.out.println(msg.getArg("PROXY"));
			_database.put(msg.getArg("NAME"), msg.getArg("PROXY"));

			// same
		} else if (msg.sameKind("Unregister")) {
			_database.remove(msg.getArg("NAME"));
		} else {
			return false;
		}
		return true;
	}
	public void onCreation(Object init) {

		// register it as an default finder..
		AgletProxy proxy = getAgletContext().getAgletProxy(getAgletID());

		getAgletContext().setProperty("finder", proxy);
	}
}
