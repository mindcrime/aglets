/*
 * Created on 4-giu-2005
 *
 */
package net.sourceforge.aglets.rolex.descriptors;

/**
 * The descriptor of a whole role.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1
 */
public interface RoleDescriptor extends GenericDescriptor {

	/**
	 * Returns the available operations.
	 * 
	 * @return the operations that belongs to this role
	 */
	public OperationDescriptor[] getOperations();
}
