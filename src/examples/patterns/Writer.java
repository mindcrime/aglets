package examples.patterns;

/*
 * @(#)Writer.java
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
import com.ibm.aglet.util.*;
import com.ibm.agletx.patterns.Slave;
import java.net.*;
import java.io.IOException;
import java.util.Vector;

/**
 * Class Writer is used to display a message on a remote aglet server.
 * Given an URL (Agler Resource Locator) it will dispatch a slave (the
 * WriterSlave class) to popup a window with the message on the remote
 * terminal. Finally, the slave returns to its origin host.
 * 
 * @see WriterSlave
 * @version     1.00    96/12/28
 * @author      Yariv Aridor
 */

public class Writer extends SampleAglet {

	// -- callback function for the "go" bottom.
	// 
	protected void createSlave(Vector destinations, Object obj) {
		Arguments args = new Arguments();
		String username = "unknown";

		args.setArg("msg", (String)obj);
		args.setArg("user", username);
		try {
			Slave.create(null, "examples.patterns.WriterSlave", 
						 getAgletContext(), this, destinations, args);
		} catch (IOException ae) {
			inError(ae.getMessage());
		} catch (AgletException ae) {
			inError(ae.getMessage());
		} 
	}
	public void createWindow() {
		try {
			_msw = new WriterWindow(this);
			updateWindow();
		} catch (Exception e) {
			inError(e.getMessage());
		} 
	}
	// -- Handler for messages
	// 
	public boolean handleMessage(Message msg) {
		try {
			if (msg.sameKind("result")) {
				String arg = null;

				if ((arg = (String)(msg.getArg())) != null) {
					try {
						setTheMessage(arg);
						setTheMessage("Finished");
					} catch (Exception e) {

						// not yet implemented
					} 
				} else {
					setTheMessage("Finished, but no argument!");
				}
			} else {
				super.handleMessage(msg);
			}
		} catch (Exception e) {

			// -- not yet handled
		} 
		return false;
	}
}
