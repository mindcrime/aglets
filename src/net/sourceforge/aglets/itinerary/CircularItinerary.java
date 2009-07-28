package net.sourceforge.aglets.itinerary;

/**
 * This interface defines a circular itinerary. A circular itinerary is an itinerary that loops over the destinations,
 * thus once the agent has reached the last destination, it restarts its trip from the first one.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0
 * 1/10/2005
 *
 */
public interface CircularItinerary extends Itinerary {

	/**
	 * Provides information about the number of loops the agent has already completed over this itinerary.
	 * @return the number of loop (0 if the agent has not yet completed the first trip).
	 */
	public int getLoopCount();
}
