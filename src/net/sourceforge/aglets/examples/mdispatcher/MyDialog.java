package net.sourceforge.aglets.examples.mdispatcher;

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

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.ibm.aglet.util.AddressChooser;

/*
 * MyDialog class is the window to be opened when the dialog required.
 * This is NOT a subclass of Dialog.
 */
class MyDialog extends Frame implements ActionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1875990539177538749L;

	/*
	 * The aglet a user interacts with.
	 */
	private HelloAglet aglet = null;

	/*
	 * UI Components
	 */
	private final AddressChooser dest = new AddressChooser();
	private final TextField msg = new TextField(15);
	private final Button go = new Button("GO!");
	private final Button close = new Button("CLOSE");

	/*
	 * Constructs the dialog window
	 * 
	 * @param aglet The aglet the user interacts with.
	 */
	MyDialog(final HelloAglet aglet) {
		this.aglet = aglet;
		layoutComponents();

		addWindowListener(this);
		dest.addActionListener(this);
		msg.addActionListener(this);
		go.addActionListener(this);
		close.addActionListener(this);
	}

	/**
	 * Handles the action event
	 * 
	 * @param ae
	 *            the event to be handled
	 */
	@Override
	public void actionPerformed(final ActionEvent ae) {
		if ("GO!".equals(ae.getActionCommand())) {
			aglet.message = msg.getText();
			aglet.goDestination(dest.getAddress());
		} else if ("CLOSE".equals(ae.getActionCommand())) {
			setVisible(false);
		}
	}

	/*
	 * Layouts all components
	 */
	private void layoutComponents() {
		msg.setText(aglet.message);

		// Layouts components
		final GridBagLayout grid = new GridBagLayout();
		final GridBagConstraints cns = new GridBagConstraints();

		setLayout(grid);

		cns.weightx = 0.5;
		cns.ipadx = cns.ipady = 5;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.insets = new Insets(5, 5, 5, 5);

		cns.weightx = 1.0;
		cns.gridwidth = GridBagConstraints.REMAINDER;
		grid.setConstraints(dest, cns);
		this.add(dest);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.BOTH;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.gridheight = 2;
		grid.setConstraints(msg, cns);
		this.add(msg);

		cns.weighty = 0.0;
		cns.fill = GridBagConstraints.NONE;
		cns.gridheight = 1;

		final Panel p = new Panel();

		grid.setConstraints(p, cns);
		this.add(p);
		p.setLayout(new FlowLayout());
		p.add(go);
		p.add(close);
	}

	@Override
	public void windowActivated(final WindowEvent we) {
	}

	@Override
	public void windowClosed(final WindowEvent we) {
	}

	/**
	 * Handles the window event
	 * 
	 * @param we
	 *            the event to be handled
	 */
	@Override
	public void windowClosing(final WindowEvent we) {
		dispose();
	}

	@Override
	public void windowDeactivated(final WindowEvent we) {
	}

	@Override
	public void windowDeiconified(final WindowEvent we) {
	}

	@Override
	public void windowIconified(final WindowEvent we) {
	}

	@Override
	public void windowOpened(final WindowEvent we) {
	}
}
