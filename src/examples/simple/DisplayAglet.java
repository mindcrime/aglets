package examples.simple;

/*
 * @(#)DisplayAglet.java
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

import com.ibm.aglet.*;

/**
 * DisplayAglet
 * 
 * Displays text in the message area on its aglet viewer.
 * 
 * @version     1.00
 * @author	Yoshiaki Mima
 */
public class DisplayAglet extends Aglet {

	public void run() {
		setText("Hello, world! I am " + getClass().getName() + ".");
	}
}
