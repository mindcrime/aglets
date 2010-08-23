package examples.watcher;

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
    ProxyWatcher aglet;
    TextArea text = new TextArea(10, 10);
    AddressChooser address = new AddressChooser(15);
    Button go = new Button("Go!");
    Button start = new Button("Start");
    Button stop = new Button("Stop");
    Button sleep = new Button("Sleep");
    Button move = new Button("Move");
    Button terminate = new Button("Terminate");

    WatcherFrame(ProxyWatcher a) {
	this.aglet = a;
	this.setLayout(new BorderLayout());
	this.add("North", this.address);
	this.add("Center", this.text);
	Panel p = new Panel();

	p.setLayout(new FlowLayout());

	this.addWindowListener(this);
	this.go.addActionListener(this);
	this.start.addActionListener(this);
	this.stop.addActionListener(this);
	this.sleep.addActionListener(this);
	this.move.addActionListener(this);
	this.terminate.addActionListener(this);

	p.add(this.go);
	p.add(this.start);
	p.add(this.stop);
	p.add(this.sleep);
	p.add(this.move);
	p.add(this.terminate);
	this.add("South", p);
    }

    /**
     * Handles the action event
     * 
     * @param ae
     *            the event to be handled
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
	if ("Go!".equals(ae.getActionCommand())) {
	    this.aglet.go(this.address.getAddress());
	} else if ("Start".equals(ae.getActionCommand())) {
	    this.aglet.sendMessage(new Message("start"));
	} else if ("Stop".equals(ae.getActionCommand())) {
	    this.aglet.sendMessage(new Message("stop"));
	} else if ("Sleep".equals(ae.getActionCommand())) {
	    this.aglet.sendMessage(new Message("sleep"));
	} else if ("Move".equals(ae.getActionCommand())) {
	    this.aglet.move(this.address.getAddress());
	} else if ("Terminate".equals(ae.getActionCommand())) {
	    this.aglet.terminate();
	}
    }

    void update(String s) {
	this.text.setText(s);
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    /**
     * Handles the window event
     * 
     * @param we
     *            the event to be handled
     */

    @Override
    public void windowClosing(WindowEvent we) {
	this.dispose();
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }
}
