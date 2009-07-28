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

import java.awt.*;
import java.awt.event.*;
import com.ibm.aglet.util.*;

/**
 * @version     1.01    99/01/14
 * @author      Mitsuru Oshima
 * @author      Yoshiaki Mima
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
		setLayout(new BorderLayout(5, 5));
		dest = new AddressChooser();

		add("North", dest);
		add("Center", text);
		add("South", input);

		text.setEditable(false);
		input.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			} 
		});
	}
	public TalkWindow(TalkSlave slave) {
		super("Talk");
		this.slave = slave;
		setLayout(new BorderLayout());

		add("Center", text);
		add("South", input);

		text.setEditable(false);
		input.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == input) {
			String t = input.getText();

			appendText(t);
			if (master != null) {
				if (!address.equals(dest.getAddress())) {
					master.dispatchSlave(address = dest.getAddress());
				} 
				master.sendText(t);
			} else if (slave != null) {
				slave.sendText(t);
			} 
			input.setText("");
		} 
	}
	public void appendText(String str) {
		text.append(str + "\r\n");
	}
}
