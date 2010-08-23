/*
 * Created on 4-giu-2005
 *
 */
package net.sourceforge.aglets.rolex.descriptors;

/**
 * This interfaces provides a description for events used in role operations.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1
 */
public interface EventDescriptor extends GenericDescriptor {

    /**
     * Indicates if this event is sent as a reply or as a first event.
     * 
     * @return True if this event is a reply to some query.
     */
    public boolean isReply();

    /**
     * Returns the array of possible replies to this event.
     * 
     * @return the reply events
     */
    public EventDescriptor[] getPossibleReplies();

}
