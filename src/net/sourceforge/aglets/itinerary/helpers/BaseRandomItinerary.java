package net.sourceforge.aglets.itinerary.helpers;

import java.util.Random;

import com.ibm.aglet.Aglet;

/**
 * This class represents a random itinerary, that is an itinerary which chose
 * randomly the next destination.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net 6-ott-2005
 * @version 1.0
 */
public class BaseRandomItinerary extends BaseItinerary {

    /**
     * Initializes the itinerary.
     * 
     * @param owner
     *            the agent owner of this itinerary.
     */
    public BaseRandomItinerary(Aglet owner) {
	super(owner);
    }

    /**
     * Selects the next destination and goes to it. It is ensured that the next
     * destination will be different from the current one, but not it will be
     * different from the previous one!
     */
    @Override
    public void goToNextDestination() {
	// chose randomly the next destination
	int randomIndex = 0;
	Random random = new Random();

	// search for a valid destination idnex
	do {
	    randomIndex = random.nextInt();
	} while ((randomIndex < 0) || (randomIndex > this.destinations.size())
		|| (this.destinations.get(randomIndex) == null)
		|| (this.getCurrentIndex() == randomIndex));

	this.setCurrentIndex(randomIndex);
	super.goToNextDestination();

    }

}
