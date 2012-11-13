package net.sourceforge.aglets.itinerary.helpers;

import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.aglets.itinerary.Itinerary;
import net.sourceforge.aglets.itinerary.SimpleItinerary;

import com.ibm.aglet.Aglet;

/**
 * This class represents the base class for more complex itinerary. It provides
 * the storing mechanism for locations and provides method implementations.
 * Extends this class and override apt methods to produce the itinerary logic
 * you want. Please note that this class does not provide synchronized methods,
 * since a single itinerary object is not supposed to be shared between several
 * concurrent entities. If you need to share the itinerary you should provide
 * synchronization by your own or use the syncrhonized itinerary facilities. <BR>
 * <B>Attention:</b> this itinerary does not allow duplicated destination, that
 * means if you try to insert more than once the same destination, only the
 * first insert will be successful.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 13-set-2005
 */
public class BaseItinerary implements SimpleItinerary {

	/**
	 * The agent owning this itinerary. An agent is owning this itinerary if it
	 * will use the itinerary. The itinerary will act on the owning agent.
	 */
	protected Aglet agletOwner = null;

	/**
	 * A vector used to store information about each destination of this
	 * itinerary.
	 */
	protected Vector destinations = null;

	/**
	 * This flag indicates if the itinerary is mutable or not, that means if
	 * add/delete operation will affect this itinerary or not.
	 */
	private boolean mutable = true;

	/**
	 * This counter tracks the current location in the itinerary list.
	 */
	private int currentIndex = 0;

	/**
	 * Overriden constructor, initializes the itinerary with the owning aglet.
	 * 
	 * @param owner
	 *            the aglet that owns (i.e., will use) this itinerary.
	 * @throws IllegalArgumentException
	 *             if the owner is null.
	 */
	public BaseItinerary(final Aglet owner) throws IllegalArgumentException {
		this(owner, 10);
	}

	/**
	 * Default constructor for this itinerary. It requires the agent that is
	 * owning this itinerary in order to know which agent needs to be moved once
	 * the itinerary methods are called.
	 * 
	 * @param owner
	 *            the agent owner of this itinerary, that is the agent is
	 *            creating and using this itinerary.
	 * @param baseSize
	 *            the initial dimension of the itinerary list, used to create
	 *            the vector with the appropriate size. If an invalid size is
	 *            provided (i.e., baseSize <= 0), the standard value of 10 is
	 *            adopted.
	 * @throws IllegalArgumentException
	 *             if the owner is null.
	 */
	public BaseItinerary(final Aglet owner, final int baseSize)
	throws IllegalArgumentException {
		super();

		// check if the agent is valid
		if (owner == null) {
			throw new IllegalArgumentException("Cannot create an itinerary with a null aglet-owner");
		}

		// store the agent
		agletOwner = owner;

		// create the vector
		if (baseSize > 0)
			destinations = new Vector(baseSize);
		else
			destinations = new Vector(10);
	}

	/**
	 * Constructs an itinerary starting from another itinerary.
	 * 
	 * @param oldItinerary
	 *            the itinerary to use as base for this one.
	 * @param owner
	 *            the agent that must own this itinerary.
	 */
	public BaseItinerary(final Itinerary oldItinerary, final Aglet owner) {
		// initialize this itinerary
		this(owner);

		// get all the destinations of the other itinerary
		final Iterator toCopy = oldItinerary.getDestinations();

		// copy each destination from the argument itinerary to this one.
		while ((toCopy != null) && toCopy.hasNext()) {
			this.addNextDestination((String) toCopy.next());
		}
	}

	/**
	 * Adds a new destination at the end of the destination list of this
	 * itinerary. The destination is not added in the case it is null or a void
	 * string.
	 * 
	 * @param destinationURI
	 *            the destination to add.
	 * @return true if the destination is added, false otherwise.
	 */
	@Override
	public boolean addNextDestination(final String destinationURI) {
		// check param
		if ((destinationURI == null) || (destinationURI.equals("") == true)) {
			// adding this destination does not make sense!
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// is the destination already contained in this itinerary?
		if (destinations.contains(destinationURI))
			return false;

		// now add the destination
		destinations.add(destinationURI);
		return true;
	}

	/**
	 * Adds the specified destination at the specified position in the
	 * destination list. <B>ATTENTION: since this itinerary implementation is
	 * based on a Vector, inserting the destination at the specified position
	 * could raise an ArrayIndexOutOfBound exception, since the position could
	 * exceed the vector size.</b> To avoid this, there is a check on the
	 * position to avoid inserting in position less than zero or greater than
	 * the size of vector.
	 * 
	 * @param destinationURI
	 *            the destination to add.
	 * @param index
	 *            the index (position) where the destinationURI should be
	 *            inserted.
	 * @return true if the destination is added, false otherwise
	 */
	@Override
	public boolean addNextDestination(final String destinationURI, final int index) {
		// check param
		if ((destinationURI == null) || (destinationURI.equals("") == true)) {
			// adding this destination does not make sense!
			return false;
		}

		if ((index < 0) || (index > destinations.size())) {
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// does this itinerary already contain the destination?
		if (destinations.contains(destinationURI))
			return false;

		// now add the destination
		destinations.add(index, destinationURI);
		return true;
	}

	/**
	 * Adds a destination just after the previous one as specified. If the
	 * previous destination is not contained in the itinerary, it is first added
	 * as the last one, and then the following destination is added.
	 * 
	 * @param destinationURI
	 *            the destination to add. #param previousDestinationURI the
	 *            destination that must be visited before the one is going to be
	 *            added.
	 * @return true if the destination is inserted, false otherwise
	 */
	@Override
	public boolean addNextDestination(
	                                  final String destinationURI,
	                                  final String previousDestinationURI) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (previousDestinationURI == null)
				|| previousDestinationURI.equals("")) {
			// invalid destinations!
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// don't add the destination if it is already contained
		if (destinations.contains(destinationURI))
			return false;

		// now search for the previousDestinationURI
		final int previousDestIndex = destinations.indexOf(previousDestinationURI);

		// if the index is -1 the previous destination is not contained
		if (previousDestIndex < 0) {
			destinations.add(previousDestinationURI);
			destinations.add(destinationURI);
			return true;
		}

		// if here the previous destination has been added, thus I can insert
		// the
		// destination at the index+1 position
		if (previousDestIndex < destinations.size())
			destinations.add(previousDestIndex + 1, destinationURI);
		else
			destinations.add(destinationURI);

		return true;
	}

	/**
	 * Adds a destination only if it is not already contained in this itinerary.
	 * 
	 * @param destinationURI
	 *            the destination to add
	 * @return true if the destination has been added, false otherwise
	 */
	@Override
	public boolean addNextDestinationIfNotDuplicated(final String destinationURI) {
		// check params
		if ((destinationURI == null) || destinationURI.equals("")) {
			// adding this destination does not make sense!
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// check if the destination is already contained
		if (destinations.contains(destinationURI) == false) {
			destinations.add(destinationURI);
			return true;
		} else
			return false;
	}

	/**
	 * Gets the index of the destination list.
	 * 
	 * @return the index.
	 */
	protected int getCurrentIndex() {
		return currentIndex;
	}

	/**
	 * Returns the current location of the itinerary, that means the location
	 * the agent should be depending on the use of the itinerary.
	 * 
	 * @return the destination the agent is at.
	 */
	@Override
	public String getCurrentLocation() {
		return (String) destinations.get(currentIndex);
	}

	/**
	 * Provides information about the size of the itinerary.
	 * 
	 * @return the number of destinations stored in this itinerary.
	 */
	@Override
	public int getDestinationCount() {
		return destinations.size();
	}

	/**
	 * Gets the list of destinations.
	 * 
	 * @return the iterator with the list of destinations stored, at the moment,
	 *         in the itinerary.
	 */
	@Override
	public Iterator getDestinations() {
		return destinations.iterator();
	}

	/**
	 * Gets the first destination of the itinerary.
	 * 
	 * @return first destination stored in this itinerary.
	 */
	@Override
	public String getFirstDestination() {
		return (String) destinations.firstElement();
	}

	/**
	 * Gets the last destination of this itinerary.
	 * 
	 * @return the last destination stored in the itinerary.
	 */
	@Override
	public String getLastDestination() {
		return (String) destinations.lastElement();
	}

	/**
	 * Provides information about the number of destinations not visited yet.
	 * 
	 * @return the number of destinations not yet visited during this itinerary.
	 */
	@Override
	public int getRemainingDestinationCount() {
		return destinations.size() - currentIndex;
	}

	/**
	 * Forces the migration to the next destination. If the migration is not
	 * possible, the stacktrace and the exception are printed, but the agent is
	 * not required to handle the exception. <B>Please note that once the agent
	 * has visited all the destinations, the execution of this method will have
	 * no effect.</B>
	 */
	@Override
	public void goToNextDestination() {

		// has the agent already visited all the destinations?
		if (currentIndex >= destinations.size()) {
			return;
		}

		// get the next destination
		final String nextDest = (String) destinations.get(currentIndex);

		// now try to move the agent
		try {
			agletOwner.dispatch(new URL(nextDest));
		} catch (final Exception e) {
			System.err.println("[BaseItinerary.goToNextDestination] - Exception while migrating to "
					+ nextDest);
			e.printStackTrace();
		} finally {
			currentIndex++;
		}
	}

	/**
	 * A method to test the state of this itinerary.
	 * 
	 * @return true if the itinerary is mutable (add/delete operations are
	 *         allowed), false otherwise.
	 */
	protected final boolean isMmutable() {
		return mutable;
	}

	/**
	 * Removes the specified destination from the itinerary.
	 * 
	 * @return true if the destination has been removed, false otherwise.
	 */
	@Override
	public boolean removeDestination(final String destinationURI) {
		// cehck params
		if ((destinationURI == null) || destinationURI.equals("")
				|| (destinations.contains(destinationURI) == false)) {
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// remove the destination
		if (destinations.contains(destinationURI)) {
			destinations.remove(destinationURI);
			return true;
		} else
			return false;
	}

	/**
	 * Removes the destination at the specified index. The index must be valid,
	 * that is greater than 0 and less than the max size of the destination
	 * lists.
	 * 
	 * @param index
	 *            index of the destination to be removed.
	 * @return true if the destination has been removed, false otherwise.
	 */
	@Override
	public boolean removeDestinationAt(final int index) {
		// check params
		if ((index < 0) || (index > destinations.size())) {
			return false;
		}

		// is the itinerary immutable?
		if (isMmutable() == false)
			return false;

		// remove the destination
		if (destinations.elementAt(index) != null) {
			destinations.remove(index);
			return true;
		} else
			return false;
	}

	/**
	 * Sets the index in the destination list.
	 * 
	 * @param value
	 *            the new value of the index.
	 */
	protected void setCurrentIndex(final int value) {
		currentIndex = value;
	}

	/**
	 * This method sets the itinerary as frozen, that means no more add/delete
	 * operation can be performed on this itinerary (if you want to modify the
	 * itinerary you have to create a new one). This method is useful when
	 * there's the risk that the itinerary is changed multiple times (e.g., due
	 * to a message handling).
	 */
	@Override
	public final void setImmutable() {
		mutable = false;
	}

	/**
	 * Skips the next destination on this itinerary.
	 */
	@Override
	public void skipNext() {
		currentIndex++;
	}

	/**
	 * Print the whole list and other information about this itinerary.
	 * 
	 * @return the string that describes this itinerary.
	 */
	@Override
	public String toString() {
		// build a string with all the destinations
		final StringBuffer head = new StringBuffer();
		head.append(this.getClass().getName() + " owned by " + agletOwner);
		head.append(" - " + destinations.size() + " destinations - ");
		head.append("" + getRemainingDestinationCount()
				+ " destinations remaining.");

		final StringBuffer ret = new StringBuffer();
		ret.append(head);
		ret.append("\n");

		for (int i = 0; i < destinations.size(); i++) {
			ret.append(i + ") " + destinations.get(i) + "\n");
		}

		return ret.toString();
	}

}
