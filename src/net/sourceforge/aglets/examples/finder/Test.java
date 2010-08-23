package net.sourceforge.aglets.examples.finder;

/*
 * @(#)Test.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

public class Test extends Aglet {
    AgletProxy _finder;
    Message lookup = new Message("Lookup", "Traveller");

    @Override
    public boolean handleMessage(Message msg) {
	try {
	    AgletProxy proxy = (AgletProxy) this._finder.sendMessage(this.lookup);

	    System.out.println(proxy.getAgletInfo());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return true;
    }

    @Override
    public void onCreation(Object init) {
	this._finder = (AgletProxy) this.getAgletContext().getProperty("finder");

	try {
	    AgletProxy proxy = (AgletProxy) this._finder.sendMessage(this.lookup);

	    System.out.println(proxy.getAgletInfo());
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
