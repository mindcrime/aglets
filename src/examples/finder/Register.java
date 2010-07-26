package examples.finder;

/*
 * @(#)Register.java
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
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.message.Message;

// now this is reusable!
public class Register extends MobilityAdapter {
    AgletProxy _finder;
    Message _msg = new Message("Register");

    Register(Aglet a, AgletProxy finder, String name) {
	a.addMobilityListener(this);
	this._msg.setArg("NAME", name);
	this._finder = finder;
	AgletProxy proxy = a.getAgletContext().getAgletProxy(a.getAgletID());

	this.register(proxy);
    }

    @Override
    public void onArrival(MobilityEvent me) {
	System.out.println(me.getAgletProxy());
	this.register(me.getAgletProxy());
    }

    public void register(AgletProxy proxy) {
	this._msg.setArg("PROXY", proxy);
	try {
	    this._finder.sendOnewayMessage(this._msg);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void unregister() {
	Message unreg = new Message("Unregister");

	unreg.setArg("NAME", this._msg.getArg("NAME"));
	try {
	    this._finder.sendOnewayMessage(unreg);
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
