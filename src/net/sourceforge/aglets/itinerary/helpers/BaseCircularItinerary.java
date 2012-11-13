package net.sourceforge.aglets.itinerary.helpers;

import java.io.Serializable;
import java.net.URL;

import net.sourceforge.aglets.itinerary.CircularItinerary;

import com.ibm.aglet.Aglet;

/**
 * An itinerary that loops over destinations, thus the agent always has a next
 * destination.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net 1-ott-2005
 * @version 1.0
 */
public class BaseCircularItinerary extends BaseItinerary implements
CircularItinerary, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8761678263123539307L;
	/**
	 * The number of loops of this itinerary.
	 */
	private int loopCount = 0;

	/**
	 * Constructs an itinerary for the specified agent.
	 * 
	 * @param owner
	 *            the owner aglet.
	 */
	public BaseCircularItinerary(final Aglet owner) {
		super(owner);
	}

	/**
	 * Provides the number of loops the agent has already done using this
	 * itinerary.
	 * 
	 * @return the number of loops.
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
