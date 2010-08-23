package net.sourceforge.aglets.itinerary;

/**
 * A simple itinerary type. It extends the itinerary adding facilities to
 * insert/remove destinations in the itinerary.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 14/9/2005
 */
public interface SimpleItinerary extends Itinerary {
    /**
     * Add another destination to the collection of destinations of this
     * itinerary.
     * 
     * @param destinationURI
     *            the string that represents the destination to add. It should
     *            be something like an URL, but using a string avoids dealing
     *            with exceptions.
     * @return true if the destination has been added to the itinerary, false
     *         otherwise.
     */
    public boolean addNextDestination(String destinationURI);

    /**
     * Add another destination to the collection of destinations of this
     * itinerary, specifying the position of the destination in the list of
     * destinations.
     * 
     * @param destinationURI
     *            the destination to be added
     * @param index
     *            the position to insert the destination
     * @return true if the destination is added, false otherwise.
     */
    public boolean addNextDestination(String destinationURI, int index);

    /**
     * Adds another destination to the collection of destination of this
     * itinerary. The added destination is inserted <i>just after</i> the
     * specified previous destination.
     * 
     * @param destinationURI
     *            the destination to add.
     * @param previousDestinationURI
     *            the destination that must be previous the one added.
     * @return true if the destination is added, false otherwise.
     */
    public boolean addNextDestination(
                                      String destinationURI,
                                      String previousDestinationURI);

    /**
     * Adds the specified destination to the destination list <b>only if it is
     * not already contained</b> in the itinerary (i.e., only if there is not a
     * destination equal to this one).
     * 
     * @param destinationURI
     *            the destination to add.
     * @return true if the destination has been added, false otherwise.
     */
    public boolean addNextDestinationIfNotDuplicated(String destinationURI);

    /**
     * Removes a specific destination address from the collection of
     * destinations of this itinerary.
     * 
     * @param destinationURI
     *            the destination address to remove.
     * @return true if the destination has been removed, false otherwise.
     */
    public boolean removeDestination(String destinationURI);

    /**
     * Removes the destination of order <index>.
     * 
     * @param index
     *            the number of the destination to be removed.
     * @return true if the destination is removed, false otherwise.
     */
    public boolean removeDestinationAt(int index);
}
