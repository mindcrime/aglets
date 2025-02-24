package net.sourceforge.aglets.examples.watcher;

/*
 * @(#)ProxyWatcher.java
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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.ibm.aglet.message.Message;
import com.ibm.aglet.util.AddressChooser;

class WatcherFrame extends Frame implements WindowListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1240859890295707297L;
	ProxyWatcher aglet;
	TextArea text = new TextArea(10, 10);
	AddressChooser address = new AddressChooser(15);
	Button go = new Button("Go!");
	Button start = new Button("Start");
	Button stop = new Button("Stop");
	Button sleep = new Button("Sleep");
	Button move = new Button("Move");
	Button terminate = new Button("Terminate");

	WatcherFrame(final ProxyWatcher a) {
		aglet = a;
		setLayout(new BorderLayout());
		this.add("North", address);
		this.add("Center", text);
		final Panel p = new Panel();

		p.setLayout(new FlowLayout());

		addWindowListener(this);
		go.addActionListener(this);
		start.addActionListener(this);
		stop.addActionListener(this);
		sleep.addActionListener(this);
		move.addActionListener(this);
		terminate.addActionListener(this);

		p.add(go);
		p.add(start);
		p.add(stop);
		p.add(sleep);
		p.add(move);
		p.add(terminate);
		this.add("South", p);
	}

	/**
	 * Handles the action event
	 * 
	 * @param ae
	 *            the event to be handled
	 */
	@Override
	public void actionPerformed(final ActionEvent ae) {
		if ("Go!".equals(ae.getActionCommand())) {
			aglet.go(address.getAddress());
		} else if ("Start".equals(ae.getActionCommand())) {
			aglet.sendMessage(new Message("start"));
		} else if ("Stop".equals(ae.getActionCommand())) {
			aglet.sendMessage(new Message("stop"));
		} else if ("Sleep".equals(ae.getActionCommand())) {
			aglet.sendMessage(new Message("sleep"));
		} else if ("Move".equals(ae.getActionCommand())) {
			aglet.move(address.getAddress());
		} else if ("Terminate".equals(ae.getActionCommand())) {
			aglet.terminate();
		}
	}

	void update(final String s) {
		text.setText(s);
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
