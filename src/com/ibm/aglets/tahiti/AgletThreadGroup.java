package com.ibm.aglets.tahiti;

/*
 * @(#)AgletThreadGroup.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

/**
 * An AgletThreadGroup contains are the running threads of a specific aglet.
 * 
 * @version 1.10 $Date: 2009/07/28 07:04:53 $
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */
final class AgletThreadGroup extends ThreadGroup {

	protected ResourceManagerImpl _rm;

	//
	private int maxThreadNumber = 10;

	// Constructs an aglet thread group for the specified aglet.
	//
	public AgletThreadGroup(final ThreadGroup parent, final ResourceManagerImpl rm) {
		super(parent, "AgletThreadGroup:" + rm.getName());
		_rm = rm;
		setMaxPriority(5);
	}

	public int getMaxThreadNumber() {
		checkAccess();
		return maxThreadNumber;
	}

	public void invalidate() {
		_rm = null;
	}

	/*
	 * 
	 */
	public void setMaxThreadNumber(final int i) {
		checkAccess();
		maxThreadNumber = i;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "[name=" + getName()
		+ ",maxpri=" + getMaxPriority() + "]";
	}
}
