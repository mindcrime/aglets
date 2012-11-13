/*
 * Created on 4-giu-2005
 *
 */
package net.sourceforge.aglets.rolex.descriptors;

/**
 * This interface provides generic services for all descriptors used in RoleX.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 */
public interface GenericDescriptor {

	/**
	 * The aim of the class related to this descriptor.
	 * 
	 * @return the aim as string
	 */
	public String getAim();

	/**
	 * The keywords related to this descriptor.
	 * 
	 * @return the keywords array
	 */
	public String[] getKeywords();

	/**
	 * The symbolic name of the descriptor.
	 * 
	 * @return the name of the descriptor
	 */
	public String getName();
}
