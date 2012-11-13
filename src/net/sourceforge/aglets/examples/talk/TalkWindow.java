package net.sourceforge.aglets.examples.talk;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = -4541762637219919068L;
	TextArea text = new TextArea();
	TextField input = new TextField();
	AddressChooser dest = null;

	String address = "";
	TalkMaster master = null;
	TalkSlave slave = null;

	public TalkWindow(final TalkMaster master) {
		super("Talk");
		this.master = master;
		setLayout(new BorderLayout(5, 5));
		dest = new AddressChooser();

		this.add("North", dest);
		this.add("Center", text);
		this.add("South", input);

		text.setEditable(false);
		input.addActionListener(this);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				TalkWindow.this.setVisible(false);
			}
		});
	}

	public TalkWindow(final TalkSlave slave) {
		super("Talk");
		this.slave = slave;
		setLayout(new BorderLayout());

		this.add("Center", text);
		this.add("South", input);

		text.setEditable(false);
		input.addActionListener(this);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final Object source = e.getSource();

		if (source == input) {
			final String t = input.getText();

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

	public void appendText(final String str) {
		text.append(str + "\r\n");
	}
}
