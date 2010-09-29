package net.sourceforge.aglets.examples.itinerary;

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

import java.util.Enumeration;

import com.ibm.agletx.patterns.Meeting;
import com.ibm.agletx.util.MeetingTask;
import com.ibm.agletx.util.SeqItinerary;

class AgletTask extends MeetingTask {

    /**
     * 
     */
    private static final long serialVersionUID = -4205233067065946508L;

    public AgletTask(Meeting meeting) {
	super(meeting);
    }

    @Override
    public void execute(SeqItinerary itin, Enumeration agletIDs) {
	String list = "";
	String id = "";

	for (; agletIDs.hasMoreElements();) {
	    list += (agletIDs.nextElement()) + " ";
	}
	try {
	    id = itin.getOwnerAglet().getAgletID().toString();
	} catch (Exception ex) {

	    //
	}
	System.out.println(">>VisitingAglet: [" + id + "] I met with [" + list
		+ "]");
    }
}
