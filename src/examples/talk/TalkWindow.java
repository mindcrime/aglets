package examples.talk;

/*
 * @(#)TalkWindow.java
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
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.ibm.aglet.util.AddressChooser;

/**
 * @version 1.01 99/01/14
 * @author Mitsuru Oshima
 * @author Yoshiaki Mima
 */
public class TalkWindow extends Frame implements ActionListener {
    TextArea text = new TextArea();
    TextField input = new TextField();
    AddressChooser dest = null;

    String address = "";
    TalkMaster master = null;
    TalkSlave slave = null;

    public TalkWindow(TalkMaster master) {
	super("Talk");
	this.master = master;
	this.setLayout(new BorderLayout(5, 5));
	this.dest = new AddressChooser();

	this.add("North", this.dest);
	this.add("Center", this.text);
	this.add("South", this.input);

	this.text.setEditable(false);
	this.input.addActionListener(this);

	this.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		TalkWindow.this.setVisible(false);
	    }
	});
    }

    public TalkWindow(TalkSlave slave) {
	super("Talk");
	this.slave = slave;
	this.setLayout(new BorderLayout());

	this.add("Center", this.text);
	this.add("South", this.input);

	this.text.setEditable(false);
	this.input.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();

	if (source == this.input) {
	    String t = this.input.getText();

	    this.appendText(t);
	    if (this.master != null) {
		if (!this.address.equals(this.dest.getAddress())) {
		    this.master.dispatchSlave(this.address = this.dest.getAddress());
		}
		this.master.sendText(t);
	    } else if (this.slave != null) {
		this.slave.sendText(t);
	    }
	    this.input.setText("");
	}
    }

    public void appendText(String str) {
	this.text.append(str + "\r\n");
    }
}
