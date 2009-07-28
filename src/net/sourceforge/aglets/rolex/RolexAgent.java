package net.sourceforge.aglets.rolex;
import net.sourceforge.aglets.rolex.descriptors.OperationDescriptor;

/*
 * Created on 4-giu-2005
 *
*/

/**
 * This interface specifies the capabilities that an agent who wants to
 * exploit the RoleX service must provide. In other words, an agent to be free to
 * assume/release and use a role (in the RoleX meaning), must implement this
 * interface.
 *
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1
 */
public interface RolexAgent {

	/**
	 * Performs a single operation starting from its descriptor.
	 * @return the return value of the operation execution.
	 * @throws RolexException if the operation cannot be executed or if the operation
	 * execution raises an exception
	 */
	public Object act(OperationDescriptor operation) throws RolexException;


        public String getRoleClass(String intf);

        /**
         * Add interface-class couple to records of agent
         * @param intf String interface's name of role
         * @param roleclass String class's name of role
         * @return boolean return if the operation has been termined succesfully
         */
        public boolean storeRoleInformation(String intf, String roleclass);

        /**
         * Remove interface-class couple to records of agent
         * It's not necessary to pass the class name because the interface is the key of hashtable
         * @param intf String interface's name of role
         * @param roleclass String class's name of role
         * @return boolean return if the operation has been termined succesfully
         */

        public boolean removeRoleInformation(String intf);

}
