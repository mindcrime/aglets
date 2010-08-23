/*
 * Created on 4-giu-2005
 *
 */
package net.sourceforge.aglets.rolex.descriptors;

/**
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1
 */
public interface OperationDescriptor extends GenericDescriptor {

    /**
     * Indicates the class type of the return type for this operation. It should
     * be null if the isVoid() method returns true
     * 
     * @return the return type
     */
    public Class getReturnType();

    /**
     * Indicates if the operation has a return type.
     * 
     * @return true if the operation has not a return type.
     */
    public boolean isVoid();

    /**
     * Indicates which events will be sent due to the execution of this
     * operation.
     * 
     * @return the events that will be sent.
     */
    public EventDescriptor[] getOutgoingEvents();

    /**
     * Indicates which events could be received due to the execution of this
     * operation.
     * 
     * @return the events that could be received
     */
    public EventDescriptor[] getIncomingEvents();
}
