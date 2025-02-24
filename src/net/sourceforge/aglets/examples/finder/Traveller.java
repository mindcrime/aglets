package net.sourceforge.aglets.examples.finder;

/*
 * @(#)Traveller.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;

public class Traveller extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7660259734565493722L;
	Register register = null;

	@Override
	public void onCreation(final Object o) {

		// get the default finder..
		final AgletProxy finder = (AgletProxy) getAgletContext().getProperty("finder");

		register = new Register(this, finder, "Traveller");
	}

	@Override
	public void onDisposing() {
		register.unregister();
	}
}
