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
import com.ibm.aglet.system.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.*;
import com.ibm.agletx.patterns.Meeting;
import java.util.Vector;
import java.util.Enumeration;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

class AgletTask extends MeetingTask {

	public AgletTask(Meeting meeting) {
		super(meeting);
	}
	public void execute(SeqItinerary itin, Enumeration agletIDs) {
		String list = "";
		String id = "";

		for (; agletIDs.hasMoreElements(); ) {
			list += (AgletID)(agletIDs.nextElement()) + " ";
		} 
		try {
			id = itin.getOwnerAglet().getAgletID().toString();
		} catch (Exception ex) {

			// 
		} 
		System.out.println(">>VisitingAglet: [" + id + "] I met with [" 
						   + list + "]");
	}
}
