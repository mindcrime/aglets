package examples.patterns;

/*
 * @(#)WriterSlave.java
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
import com.ibm.agletx.patterns.*;
import com.ibm.aglet.util.*;

import java.util.Vector;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Properties;

class WriterSlaveWindow extends Frame {
	private static final int SIZE = 40;					// -- size of message field
	private static final String TITLE = "Writer";		// -- title of message window
	private static final String THANKS = "THANKS!!";	// -- thank message text
	private GridBagLayout layout;
	private TextField _messageText = new TextField(SIZE);
	private Aglet _aglet = null;

	protected Button _thanks = new Button("Thank");
	protected Button _quit = new Button("Quit");

	// --  Constructs the dialog window.
	// 
	public WriterSlaveWindow(Aglet ag, String text, 
							 String from) throws AgletException {
		super(TITLE);
		_aglet = ag;
		setLayout(layout = new GridBagLayout());
		SampleWindow.setWindowProperties(this, _aglet);
		makeMainPanel(text, from);
		SampleWindow.displayFrame(this);
		writeMessage(text);
	}
	public void dispose() {
		super.dispose();
	}
	protected boolean handleButton(Button button) {
		if (button == _thanks) {
			thanks();
		} else if (button == _quit) {
			quit();
		} 
		return true;
	}
	// -- Event Handler...
	public boolean handleEvent(Event event) {
		if (event.id == Event.ACTION_EVENT) {
			if (event.target instanceof Button) {
				return handleButton((Button)event.target);
			} 
		} else if (event.id == Event.WINDOW_ICONIFY) {
			setVisible(false);
			return true;
		} else if (event.id == Event.WINDOW_DESTROY) {
			quit();
			return true;
		} 
		return super.handleEvent(event);
	}
	protected Panel makeButtonPanel() {
		Panel p = new Panel();

		p.setLayout(new FlowLayout(FlowLayout.RIGHT));
		p.add(_thanks);
		p.add(_quit);
		return p;
	}
	private void makeMainPanel(String text, 
							   String from) throws AgletException {
		Component comp;
		GridBagConstraints constraints = new GridBagConstraints();

		// button
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = makeButtonPanel();
		layout.setConstraints(comp, constraints);
		add(comp);

		// title
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = new Label("MESSAGE FROM: " + from);
		layout.setConstraints(comp, constraints);
		add(comp);

		// message text panel
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;

		// _messageText.setEditable(false);
		comp = _messageText;
		layout.setConstraints(comp, constraints);
		add(comp);
	}
	void quit() {
		dispose();
		((WriterSlave)_aglet).wakeup();
	}
	void thanks() {
		((WriterSlave)_aglet).setResult(THANKS);
		quit();
	}
	public void update(Graphics g) {
		paint(g);
	}
	private void writeMessage(String text) {
		FontMetrics fm = getFontMetrics(getFont());
		int l = _messageText.getSize().width;
		int l3 = fm.charWidth(' ');

		try {
			for (int i = 0; i < l; i++) {
				char msg[] = new char[(l - i) / l3];

				for (int j = 0; j < msg.length; j++) {
					msg[j] = ' ';
				} 
				String str = new String(msg);

				_messageText.setText(str + text);
			} 
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
