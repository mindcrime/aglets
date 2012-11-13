package net.sourceforge.aglets.itinerary.helpers;

import java.net.URL;

import net.sourceforge.aglets.itinerary.CircularItinerary;

import com.ibm.aglet.Aglet;

/**
 * This class represents a task itinerary that loops over destination, thus the
 * agent has always a next destination.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net 1/10/2005
 * @version 1.0
 * 
 */
public class BaseCircularTaskItinerary extends BaseTaskItinerary implements
CircularItinerary {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6610194904023599370L;
	/**
	 * Traces the number of loops the agent owner of this itinerary has already
	 * done.
	 */
	private int loopCount = 0;

	/**
	 * Constructs a circular itinerary for the agent owner specified as
	 * parameter.
	 * 
	 * @param owner
	 *            the agent owning this itinerary.
	 */
	public BaseCircularTaskItinerary(final Aglet owner) {
		super(owner);
	}

	/**
	 * Provides information about the loops performed by the agent owner of this
	 * itinerary.
	 */
	@Override
	public int getLoopCount() {
		return loopCount;
	}

	/**
	 * Forces the migration to the next destination. If the migration is not
	 * possible, the stacktrace and the exception are printed, but the agent is
	 * not required to handle the exception.
	 */
	@Override
	public void goToNextDestination() {

		// has the agent already visited all the destinations?
		if (getCurrentIndex() >= destinations.size()) {
			setCurrentIndex(0);
			loopCount++;
		}

		// get the next destination
		final String nextDest = (String) destinations.get(getCurrentIndex());

		// now try to move the agent
		try {
			agletOwner.dispatch(new URL(nextDest));
		} catch (final Exception e) {
			System.err.println("[BaseItinerary.goToNextDestination] - Exception while migrating to "
					+ nextDest);
			e.printStackTrace();
		} finally {
			setCurrentIndex(getCurrentIndex() + 1);
		}
	}
}
