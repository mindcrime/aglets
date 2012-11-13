package net.sourceforge.aglets.itinerary;

import java.util.Iterator;

/**
 * This interface defines the generic type for an itinerary. An itinerary is a
 * collection of destination for one or more agents. The itinerary provides
 * facilities to move the agent along its trip and to get/set the
 * next/previous/current location.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 13-set-2005
 */
public interface Itinerary {

	/**
	 * Returns information about the current location of the agent. This method
	 * does not provide the next destination, but the destination the agent
	 * should be depending on this itinerary.
	 * 
	 * @return the current location of the agent, as URI string as stored in
	 *         this itinerary.
	 */
	public String getCurrentLocation();

	/**
	 * Returns the number of destination contained in this itinerary.
	 * 
	 * @return the size of this itinerary, that means the total number of
	 *         destination contained in this itinerary.
	 */
	public int getDestinationCount();

	/**
	 * Gets the list of destinations.
	 * 
	 * @return the iterator with the list of destinations stored, at the moment,
	 *         in the itinerary.
	 */
	public Iterator getDestinations();

	/**
	 * Returns the first address of the collection contained in this itinerary.
	 * 
	 * @return the first destination of this itinerary.
	 */
	public String getFirstDestination();

	/**
	 * Returns the last destination address contained in this itinerary.
	 * 
	 * @return the last destination string of this itinerary.
	 */
	public String getLastDestination();

	/**
	 * Provides information about the number of destinations not visited yet.
	 * 
	 * @return the number of destinations not yet visited during this itinerary.
	 */
	public int getRemainingDestinationCount();

	/**
	 * The main and most important method of the itinerary. It force the agent
	 * to move to the next destination of this itinerary. If there is not the
	 * next destination in this itinerary, nothing happens (but the itinerary is
	 * supposed to provide the same information about the current location).
	 * 
	 */
	public void goToNextDestination();

	/**
	 * Once this method is called, the itinerary will no more be modifiable,
	 * that means there will be no way to add/remove a destination from this
	 * itinerary.
	 */
	public void setImmutable();

	/**
	 * This method allows an agent to skip the next destination of this
	 * itinerary, that means the next destination becomes the one following the
	 * current next one. It is like the <i>internal destination index</i> is
	 * incremented of one location.
	 * 
	 */
	public void skipNext();

}
