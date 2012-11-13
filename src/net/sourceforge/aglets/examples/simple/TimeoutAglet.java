package net.sourceforge.aglets.examples.simple;

/*
 * @(#)TimeoutAglet.java
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

/**
 * TimeoutAglet
 * 
 * After 60 seconds, this aglet will disappeare. For the loadtesting of
 * AgletServer.
 * 
 * @version 1.00
 * @author Yoshiaki Mima
 */
public class TimeoutAglet extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7015232163677957516L;

	@Override
	public void run() {
		try {
			for (int i = 60; i > 0; i--) {
				Thread.sleep(1000); // 1 second
				setText(i + " more seconds.");
			}
			dispose();
		} catch (final Exception e) {
			System.out.println(e);
		}
	}
}
