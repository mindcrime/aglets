package examples.itinerary;

/*
 * @(#)MessengerAglet.java
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
import com.ibm.aglet.system.*;
import com.ibm.agletx.util.MessengerItinerary;
import com.ibm.aglet.util.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;

/**
 * <tt>MessengerAglet</tt> illustrates how to use MessengerItinerary.
 * Remote stationary aglets are created afterwhich a messenger aglet
 * visits each one of them to send an asynchronous  local message.
 * 
 * @version     1.00	$Date: 2001/07/28 06:34:17 $
 * @author	Yariv Aridor
 */
public class MessengerAglet extends Aglet {

	MessengerItinerary itinerary;
	Vector addresses = new Vector();

	public void createStationaryAglets(MessengerItinerary itin) 
			throws Exception {
		String addr = null;

		for (int i = 0; i < addresses.size(); i++) {
			try {
				AgletProxy p = Aglets.createAglet(addr = (String)addresses.elementAt(i), 
												  null, 
												  "examples.itinerary.StationaryAglet", 
												  null);

				itin.addAglet(addr, p.getAgletID());
			} catch (Exception ex) {}
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			Frame f = new MessengerFrame(this);

			f.pack();
			f.setVisible(true);
			return true;
		} 
		return false;
	}
	public void onCreation(Object ini) {}
	public void start() {
		try {
			itinerary.clear();
			createStationaryAglets(itinerary);
			itinerary.startTrip();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
