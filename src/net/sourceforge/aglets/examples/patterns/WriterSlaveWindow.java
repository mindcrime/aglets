package net.sourceforge.aglets.examples.patterns;

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

import java.awt.Button;
import java.awt.Component;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;

class WriterSlaveWindow extends Frame {
    private static final int SIZE = 40; // -- size of message field
    private static final String TITLE = "Writer"; // -- title of message window
    private static final String THANKS = "THANKS!!"; // -- thank message text
    private GridBagLayout layout;
    private TextField _messageText = new TextField(SIZE);
    private Aglet _aglet = null;

    protected Button _thanks = new Button("Thank");
    protected Button _quit = new Button("Quit");

    // -- Constructs the dialog window.
    //
    public WriterSlaveWindow(Aglet ag, String text, String from)
    throws AgletException {
	super(TITLE);
	this._aglet = ag;
	this.setLayout(this.layout = new GridBagLayout());
	SampleWindow.setWindowProperties(this, this._aglet);
	this.makeMainPanel(text, from);
	SampleWindow.displayFrame(this);
	this.writeMessage(text);
    }

    @Override
    public void dispose() {
	super.dispose();
    }

    protected boolean handleButton(Button button) {
	if (button == this._thanks) {
	    this.thanks();
	} else if (button == this._quit) {
	    this.quit();
	}
	return true;
    }

    // -- Event Handler...
    @Override
    public boolean handleEvent(Event event) {
	if (event.id == Event.ACTION_EVENT) {
	    if (event.target instanceof Button) {
		return this.handleButton((Button) event.target);
	    }
	} else if (event.id == Event.WINDOW_ICONIFY) {
	    this.setVisible(false);
	    return true;
	} else if (event.id == Event.WINDOW_DESTROY) {
	    this.quit();
	    return true;
	}
	return super.handleEvent(event);
    }

    protected Panel makeButtonPanel() {
	Panel p = new Panel();

	p.setLayout(new FlowLayout(FlowLayout.RIGHT));
	p.add(this._thanks);
	p.add(this._quit);
	return p;
    }

    private void makeMainPanel(String text, String from) throws AgletException {
	Component comp;
	GridBagConstraints constraints = new GridBagConstraints();

	// button
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	comp = this.makeButtonPanel();
	this.layout.setConstraints(comp, constraints);
	this.add(comp);

	// title
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	comp = new Label("MESSAGE FROM: " + from);
	this.layout.setConstraints(comp, constraints);
	this.add(comp);

	// message text panel
	constraints.gridwidth = GridBagConstraints.REMAINDER;
	constraints.fill = GridBagConstraints.BOTH;

	// _messageText.setEditable(false);
	comp = this._messageText;
	this.layout.setConstraints(comp, constraints);
	this.add(comp);
    }

    void quit() {
	this.dispose();
	((WriterSlave) this._aglet).wakeup();
    }

    void thanks() {
	((WriterSlave) this._aglet).setResult(THANKS);
	this.quit();
    }

    @Override
    public void update(Graphics g) {
	this.paint(g);
    }

    private void writeMessage(String text) {
	FontMetrics fm = this.getFontMetrics(this.getFont());
	int l = this._messageText.getSize().width;
	int l3 = fm.charWidth(' ');

	try {
	    for (int i = 0; i < l; i++) {
		char msg[] = new char[(l - i) / l3];

		for (int j = 0; j < msg.length; j++) {
		    msg[j] = ' ';
		}
		String str = new String(msg);

		this._messageText.setText(str + text);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
