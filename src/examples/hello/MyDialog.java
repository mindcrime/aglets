package examples.hello;

/*
 * @(#)HelloAglet.java
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

import com.ibm.agletx.util.SimpleItinerary;

import java.lang.InterruptedException;
import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

/*
 * MyDialog class is the window to be opened when the dialog required.
 * This is NOT a subclass of Dialog.
 */
class MyDialog extends Frame implements WindowListener, ActionListener {

	/*
	 * The aglet a user interacts with.
	 */
	private HelloAglet aglet = null;

	/*
	 * UI Components
	 */
	private AddressChooser dest = new AddressChooser();
	private TextField msg = new TextField(18);
	private Button go = new Button("GO!");
	private Button send = new Button("Send CLONE!");
	private Button close = new Button("CLOSE");

	/*
	 * Constructs the dialog window
	 * @param aglet The aglet the user interacts with.
	 */
	MyDialog(HelloAglet aglet) {
		this.aglet = aglet;
		layoutComponents();

		addWindowListener(this);
		go.addActionListener(this);
		send.addActionListener(this);
		close.addActionListener(this);
	}
	/**
	 * Handles the action event
	 * @param ae the event to be handled
	 */
	public void actionPerformed(ActionEvent ae) {

		// execute "GO!" command
		if ("GO!".equals(ae.getActionCommand())) {
			aglet.setMessage(msg.getText());
			try {
				AgletProxy p = aglet.getProxy();

				p.sendOnewayMessage(new Message("startTrip", 
												dest.getAddress()));
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} 

		// execute "Send CLONE!" command
		else if ("Send CLONE!".equals(ae.getActionCommand())) {
			aglet.message = msg.getText();
			try {
				AgletProxy p = (AgletProxy)aglet.clone();

				p.sendOnewayMessage(new Message("startTrip", 
												dest.getAddress()));
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} 

		// execute "CLOSE" command
		else if ("CLOSE".equals(ae.getActionCommand())) {
			setVisible(false);
		} 
	}
	/*
	 * Layouts all components
	 */
	private void layoutComponents() {
		msg.setText(aglet.message);

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
		p.add(send);
		p.add(close);
	}
	public void windowActivated(WindowEvent we) {}
	public void windowClosed(WindowEvent we) {}
	/**
	 * Handles the window event
	 * @param we the event to be handled
	 */

	public void windowClosing(WindowEvent we) {
		dispose();
	}
	public void windowDeactivated(WindowEvent we) {}
	public void windowDeiconified(WindowEvent we) {}
	public void windowIconified(WindowEvent we) {}
	public void windowOpened(WindowEvent we) {}
}
