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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;

// now this is reusable!
public class Register extends MobilityAdapter {
	AgletProxy _finder;
	Message _msg = new Message("Register");

	Register(Aglet a, AgletProxy finder, String name) {
		a.addMobilityListener(this);
		_msg.setArg("NAME", name);
		_finder = finder;
		AgletProxy proxy = a.getAgletContext().getAgletProxy(a.getAgletID());

		register(proxy);
	}
	public void onArrival(MobilityEvent me) {
		System.out.println(me.getAgletProxy());
		register(me.getAgletProxy());
	}
	public void register(AgletProxy proxy) {
		_msg.setArg("PROXY", proxy);
		try {
			_finder.sendOnewayMessage(_msg);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public void unregister() {
		Message unreg = new Message("Unregister");

		unreg.setArg("NAME", _msg.getArg("NAME"));
		try {
			_finder.sendOnewayMessage(unreg);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
