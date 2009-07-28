package examples.patterns;

/*
 * @(#)PopUpMessageWindow.java
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

import java.awt.Frame;

/**
 * Class PopUpMessageWindow is a popup window for error messages.
 * 
 * @version     1.00    96/07/22
 * @author      Yariv Aridor
 */

class PopUpMessageWindow extends MessageDialog {

	PopUpMessageWindow(Frame parent, String title, String message) {
		super(parent, title, message);
		setButtons(MessageDialog.OKAY);
	}
}
