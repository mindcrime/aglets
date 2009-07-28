package examples.openurl;

/*
 * @(#)OpenURL.java
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
import com.ibm.aglet.event.*;
import com.ibm.aglet.util.*;
import com.ibm.agletx.util.*;

import java.lang.InterruptedException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.*;
import java.awt.*;

import java.util.Enumeration;

/*
 * MyDialog class is the window to be opened when the dialog required.
 * This is NOT a subclass of Dialog.
 */
class MyDialog extends Frame {

	/*
	 * The aglet a user interacts with.
	 */
	private OpenURL aglet = null;

	/*
	 * UI Components
	 */
	private AddressChooser dest = new AddressChooser();
	private TextField msg = new TextField(15);
	private Button go = new Button("GO!");
	private Button open = new Button("Open!");
	private Button close = new Button("CLOSE");

	/*
	 * Constructs the dialog window
	 * @param aglet The aglet the user interacts with.
	 */
	MyDialog(OpenURL aglet) {
		this.aglet = aglet;
		layoutComponents();
	}
	/**
	 * Handles the action
	 * @param ev the event to be handled
	 * @param arg the extra argument
	 */
	public boolean action(Event ev, Object obj) {
		if (ev.target == open) {
			try {
				aglet.getAgletContext().showDocument(new URL(msg.getText()));
			} catch (IOException ex) {
				ex.printStackTrace();
			} 
		} 
		if (ev.target == go) {
			aglet.url = msg.getText();
			aglet.goDestination(dest.getAddress());
		} else if (ev.target == close) {
			setVisible(false);
		} else {
			return false;
		} 
		return true;
	}
	/**
	 * Handles the events
	 * @param ev the event to be handled
	 */
	public boolean handleEvent(Event ev) {
		if (ev.id == Event.WINDOW_DESTROY) {
			setVisible(false);
			return true;
		} 
		return super.handleEvent(ev);
	}
	/*
	 * Layouts all components
	 */
	private void layoutComponents() {
		msg.setText(aglet.url);

		// Layouts components
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints cns = new GridBagConstraints();

		setLayout(grid);

		cns.weightx = 0.5;
		cns.ipadx = cns.ipady = 5;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.insets = new Insets(5, 5, 5, 5);

		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(dest, cns);
		add(dest);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.gridheight = 2;
		grid.setConstraints(msg, cns);
		add(msg);

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.NONE;
		cns.gridheight = 1;

		Panel p = new Panel();

		grid.setConstraints(p, cns);
		add(p);
		p.setLayout(new FlowLayout());
		p.add(go);
		p.add(open);
		p.add(close);
	}
}
