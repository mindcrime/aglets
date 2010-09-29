package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)SampleWindow.java
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
import java.awt.Choice;
import java.awt.Component;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.util.AddressChooser;

/**
 * Class DemoWindow abstracts an interactive window for demos. IT SHOULD NOT BE
 * CONSIDERED an ultimate base class for every future demo. It is currently
 * defined ONLY for reasons of reusability of code shared by current demos:
 * FINGER, WATCHER, SEARCHER, etc.
 * 
 * @version 1.00 96/12/28
 * @author Yariv Aridor
 */

public abstract class SampleWindow extends Frame {

    /**
     * 
     */
    private static final long serialVersionUID = -4437953414140111820L;

    // -- size of text field
    static final int FIELD = 40;

    // -- error msg for malformed URLs.
    static final String MAL_FORMED_URL_MSG = "Invalid destination address.\n"
	+ "Please type the correct destination to go.\n"
	+ "Example: {atp|http}://java.trl.ibm.com.";

    // -- error msg for missing file name.
    static final String NO_FILENAME_MSG = "Please insert a valid file name.";

    // -- popup window for malformed Urls error message.
    protected PopUpMessageWindow _malFormedURLWindow = null;

    // -- the layout of the window
    protected GridBagLayout layout;
    protected GridBagConstraints constraints = new GridBagConstraints();

    // -- a pointer to an aglet
    protected Aglet _aglet = null;

    // -- default panels
    protected AddressChooser _addressChooser = new AddressChooser();
    protected TextArea _result = new TextArea();
    protected TextArea _msgLine = new TextArea();

    // -- although not shared by all samples, it is included
    // -- for reasons of easy maintenance
    protected TextField _filepath = new TextField(FIELD);

    // -- default labels
    protected static final String URLLabel = "Which URL";
    protected static final String hotlistLabel = "URL hotlist";

    // -- Message Panel
    //

    // -- A default message
    private final String _initMessage = "Mobile Agent is Ready\n";

    // -- Button panel
    //
    protected Button _go = new Button("Go");
    protected Button _quit = new Button("Quit");

    // -- A constructor method.
    //
    public SampleWindow(Aglet aglet) throws AgletException {
	super("Sample Aglets");
	this._aglet = aglet;
	this.setLayout(this.layout = new GridBagLayout());
	setWindowProperties(this, this._aglet);
	this._malFormedURLWindow = new PopUpMessageWindow(this, "URL format error", MAL_FORMED_URL_MSG);
    }

    // -- Add <label> and <component> pair.
    //
    protected void addLabeledComponent(String label, Component component) {
	Component comp;

	this.constraints.gridwidth = 1;
	this.constraints.fill = GridBagConstraints.NONE;
	this.constraints.weightx = 1.0;
	comp = new Label(label);
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.weightx = 1.0;
	this.constraints.fill = GridBagConstraints.BOTH;
	this.layout.setConstraints(component, this.constraints);
	this.add(component);
    }

    // -- Result panel
    //

    // -- Appends string to the result panel
    //
    public void appendResult(String s) {
	appendTextArea(this._result, s);
    }

    // -- Appends text to TextArea
    //
    public static void appendTextArea(TextArea ta, String str) {
	ta.append(str + "\n");
    }

    // -- Clears the message panel.
    //
    public void clearMessage() {
	clearTextArea(this._msgLine);
    }

    // -- Clears result panel
    //
    public void clearResult() {
	clearTextArea(this._result);
    }

    public static void clearTextArea(TextArea ta) {
	ta.setText("");
    }

    // -- Static general GUI methods
    public static void displayFrame(Frame f) {
	f.pack();
	f.setSize(f.getPreferredSize());
	f.setVisible(true);
    }

    // -- A dispose method
    //
    @Override
    public void dispose() {
	super.dispose();
    }

    public String getFilename() {
	return this._filepath.getText();
    }

    public String getPosition() {
	return this._addressChooser.getAddress();
    }

    public int getResultSize() {
	return this._result.getText().length();
    }

    protected void go() {
    }

    // -- Handles button events.
    //
    protected boolean handleButton(Button button) {
	if (button == this._go) {
	    this.go();
	} else if (button == this._quit) {
	    this.quit();
	} else {
	    return this.popUpHandleButton(button);
	}
	return true;
    }

    // -- Handles text field events.
    //
    boolean handleChoice(Choice hotlist) {
	return false;
    }

    // -- Event Handling
    //

    // -- The event handler.
    //
    @Override
    public boolean handleEvent(Event event) {
	if (event.id == Event.ACTION_EVENT) {
	    if (event.target instanceof Button) {
		return this.handleButton((Button) event.target);
	    } else if (event.target instanceof Choice) {
		return this.handleChoice((Choice) event.target);
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

    // -- Inits message panel
    //
    public void initMessagePanel() {
	this._msgLine.setText(this._initMessage);
	this._msgLine.setEditable(false);
    }

    // -- Inits result panel
    //
    public void initResultPanel() {
	this._result.setEditable(false);
    }

    protected Panel makeMainButtonPanel() {
	Panel p = new Panel();

	p.setLayout(new FlowLayout(FlowLayout.RIGHT));
	p.add(this._go);
	p.add(this._quit);
	return p;
    }

    // -- Creates a single <label> <text field> panel.
    //
    public static Panel makeSingleField(String firstLabel, Component firstField) {
	Panel p = new Panel();

	p.setLayout(new FlowLayout(FlowLayout.LEFT));
	p.add(new Label(firstLabel));
	p.add(firstField);

	return p;
    }

    protected boolean popUpHandleButton(Button button) {
	return false;
    }

    protected void quit() {
	this.dispose();
	try {
	    this._aglet.getAgletContext().getAgletProxy(this._aglet.getAgletID()).dispose();
	} catch (AgletException ae) {
	}
    }

    public void setFilename(String filename) {
	this._filepath.setText(filename);
    }

    // -- Writes a string to the message panel
    //
    public void setMessage(String message) {
	appendTextArea(this._msgLine, message);
    }

    // -- Shared variables

    // "setLocation" is in java.awt.Component of JDK1.1.
    // So rename it into setPosition.
    public void setPosition(String position) {
	this._addressChooser.setAddress(position);
    }

    // -- Writes a string to the result panel
    //
    public void setResult(String str) {
	setTextArea(this._result, str);
    }

    // -- Writes text to TextArea
    //
    public static void setTextArea(TextArea ta, String str) {
	ta.setText(str);
    }

    // -- Set window characteristics (font, background color etc)
    // -- inherited from a context of a specific aglet.
    //
    public static void setWindowProperties(Frame f, Aglet aglet)
    throws AgletException {
    }
}
