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

import java.awt.Frame;
import java.net.URL;
import java.util.Vector;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.agletx.patterns.Meeting;
import com.ibm.agletx.util.MeetingsItinerary;

/**
 * <tt>VisitingAglet</tt> illustrates how to use MeetingsItinerary. Remote
 * stationary aglets are created afterwhich a mobile aglet meet with them to
 * dispose them.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Yariv Aridor
 */
public class VisitingAglet extends Aglet {

    MeetingsItinerary itinerary;
    Vector addresses = new Vector();

    public void createStationaryAglets(MeetingsItinerary itin) throws Exception {
	String addr = null;

	for (int i = 0; i < this.addresses.size(); i++) {
	    try {
		Meeting m = new Meeting(addr = (String) this.addresses.elementAt(i));

		// temoporary since can not create a remote aglet with
		// a user defined initial object
		AgletProxy p = this.getAgletContext().createAglet(null, "examples.itinerary.StationaryAglet", m);

		p.dispatch(new URL(addr));
		itin.addMeetingTask(new AgletTask(m));
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("dialog")) {
	    Frame f = new VisitingFrame(this);

	    f.pack();
	    f.setVisible(true);
	    return true;
	}
	return false;
    }

    @Override
    public void onCreation(Object ini) {
	this.itinerary = new MeetingsItinerary(this);
    }

    public void start() {
	try {
	    this.itinerary.clear();
	    this.createStationaryAglets(this.itinerary);
	    this.itinerary.startTrip();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
