package net.sourceforge.aglets.rolex;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Sandro Cremonini
 * @version 1.0
 */
public interface RolexLoader {

    public RolexAgent addRole(RolexAgent agent, Role role)
							  throws RolexException;

    public RolexAgent removeRole(RolexAgent agent, Role role)
							     throws RolexException;

    public RolexAgent addRole(RolexAgent agent, Class role)
							   throws RolexException;

    public RolexAgent removeRole(RolexAgent agent, Class role)
							      throws RolexException;

}
