package examples.itinerary;

/*
 * @(#)VisitingAglet.java
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
import com.ibm.aglet.system.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.*;
import com.ibm.agletx.patterns.Meeting;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

/**
 * <tt>VisitingAglet</tt> illustrates how to use MeetingsItinerary.
 * Remote stationary aglets are created afterwhich a mobile aglet
 * meet with them to dispose them.
 * 
 * @version     1.00	$Date: 2009/07/28 07:04:53 $
 * @author	Yariv Aridor
 */
public class VisitingAglet extends Aglet {

	MeetingsItinerary itinerary;
	Vector addresses = new Vector();

	public void createStationaryAglets(MeetingsItinerary itin) 
			throws Exception {
		String addr = null;

		for (int i = 0; i < addresses.size(); i++) {
			try {
				Meeting m = 
					new Meeting(addr = (String)addresses.elementAt(i));

				// temoporary since can not create a remote aglet with
				// a user defined initial object
				AgletProxy p = getAgletContext().createAglet(null, 
						"examples.itinerary.StationaryAglet", m);

				p.dispatch(new URL(addr));
				itin.addMeetingTask(new AgletTask(m));
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			Frame f = new VisitingFrame(this);

			f.pack();
			f.setVisible(true);
			return true;
		} 
		return false;
	}
	public void onCreation(Object ini) {
		itinerary = new MeetingsItinerary(this);
	}
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
