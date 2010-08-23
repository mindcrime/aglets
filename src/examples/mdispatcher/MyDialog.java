package examples.mdispatcher;

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

    /*
     * The aglet a user interacts with.
     */
    private HelloAglet aglet = null;

    /*
     * UI Components
     */
    private AddressChooser dest = new AddressChooser();
    private TextField msg = new TextField(15);
    private Button go = new Button("GO!");
    private Button close = new Button("CLOSE");

    /*
     * Constructs the dialog window
     * 
     * @param aglet The aglet the user interacts with.
     */
    MyDialog(HelloAglet aglet) {
	this.aglet = aglet;
	this.layoutComponents();

	this.addWindowListener(this);
	this.dest.addActionListener(this);
	this.msg.addActionListener(this);
	this.go.addActionListener(this);
	this.close.addActionListener(this);
    }

    /**
     * Handles the action event
     * 
     * @param ae
     *            the event to be handled
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
	if ("GO!".equals(ae.getActionCommand())) {
	    this.aglet.message = this.msg.getText();
	    this.aglet.goDestination(this.dest.getAddress());
	} else if ("CLOSE".equals(ae.getActionCommand())) {
	    this.setVisible(false);
	}
    }

    /*
     * Layouts all components
     */
    private void layoutComponents() {
	this.msg.setText(this.aglet.message);

	// Layouts components
	GridBagLayout grid = new GridBagLayout();
	GridBagConstraints cns = new GridBagConstraints();

	this.setLayout(grid);

	cns.weightx = 0.5;
	cns.ipadx = cns.ipady = 5;
	cns.fill = GridBagConstraints.HORIZONTAL;
	cns.insets = new Insets(5, 5, 5, 5);

	cns.weightx = 1.0;
	cns.gridwidth = GridBagConstraints.REMAINDER;
	grid.setConstraints(this.dest, cns);
	this.add(this.dest);

	cns.gridwidth = GridBagConstraints.REMAINDER;
	cns.fill = GridBagConstraints.BOTH;
	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.gridheight = 2;
	grid.setConstraints(this.msg, cns);
	this.add(this.msg);

	cns.weighty = 0.0;
	cns.fill = GridBagConstraints.NONE;
	cns.gridheight = 1;

	Panel p = new Panel();

	grid.setConstraints(p, cns);
	this.add(p);
	p.setLayout(new FlowLayout());
	p.add(this.go);
	p.add(this.close);
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
     * @param ae
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
