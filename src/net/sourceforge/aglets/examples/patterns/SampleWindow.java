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

	// -- Appends text to TextArea
	//
	public static void appendTextArea(final TextArea ta, final String str) {
		ta.append(str + "\n");
	}

	public static void clearTextArea(final TextArea ta) {
		ta.setText("");
	}
	// -- Static general GUI methods
	public static void displayFrame(final Frame f) {
		f.pack();
		f.setSize(f.getPreferredSize());
		f.setVisible(true);
	}

	// -- Creates a single <label> <text field> panel.
	//
	public static Panel makeSingleField(final String firstLabel, final Component firstField) {
		final Panel p = new Panel();

		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(new Label(firstLabel));
		p.add(firstField);

		return p;
	}

	// -- Writes text to TextArea
	//
	public static void setTextArea(final TextArea ta, final String str) {
		ta.setText(str);
	}
	// -- Set window characteristics (font, background color etc)
	// -- inherited from a context of a specific aglet.
	//
	public static void setWindowProperties(final Frame f, final Aglet aglet)
	throws AgletException {
	}
	// -- popup window for malformed Urls error message.
	protected PopUpMessageWindow _malFormedURLWindow = null;

	// -- the layout of the window
	protected GridBagLayout layout;

	protected GridBagConstraints constraints = new GridBagConstraints();
	// -- a pointer to an aglet
	protected Aglet _aglet = null;

	// -- Message Panel
	//

	// -- default panels
	protected AddressChooser _addressChooser = new AddressChooser();

	protected TextArea _result = new TextArea();
	protected TextArea _msgLine = new TextArea();

	// -- although not shared by all samples, it is included
	// -- for reasons of easy maintenance
	protected TextField _filepath = new TextField(FIELD);

	// -- default labels
	protected static final String URLLabel = "Which URL";

	// -- Result panel
	//

	protected static final String hotlistLabel = "URL hotlist";

	// -- A default message
	private final String _initMessage = "Mobile Agent is Ready\n";

	// -- Button panel
	//
	protected Button _go = new Button("Go");

	protected Button _quit = new Button("Quit");

	// -- A constructor method.
	//
	public SampleWindow(final Aglet aglet) throws AgletException {
		super("Sample Aglets");
		_aglet = aglet;
		setLayout(layout = new GridBagLayout());
		setWindowProperties(this, _aglet);
		_malFormedURLWindow = new PopUpMessageWindow(this, "URL format error", MAL_FORMED_URL_MSG);
	}

	// -- Add <label> and <component> pair.
	//
	protected void addLabeledComponent(final String label, final Component component) {
		Component comp;

		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 1.0;
		comp = new Label(label);
		layout.setConstraints(comp, constraints);
		this.add(comp);

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		layout.setConstraints(component, constraints);
		this.add(component);
	}

	// -- Appends string to the result panel
	//
	public void appendResult(final String s) {
		appendTextArea(_result, s);
	}

	// -- Clears the message panel.
	//
	public void clearMessage() {
		clearTextArea(_msgLine);
	}

	// -- Clears result panel
	//
	public void clearResult() {
		clearTextArea(_result);
	}

	// -- A dispose method
	//
	@Override
	public void dispose() {
		super.dispose();
	}

	public String getFilename() {
		return _filepath.getText();
	}

	public String getPosition() {
		return _addressChooser.getAddress();
	}

	public int getResultSize() {
		return _result.getText().length();
	}

	// -- Event Handling
	//

	protected void go() {
	}

	// -- Handles button events.
	//
	protected boolean handleButton(final Button button) {
		if (button == _go) {
			go();
		} else if (button == _quit) {
			quit();
		} else {
			return popUpHandleButton(button);
		}
		return true;
	}

	// -- Handles text field events.
	//
	boolean handleChoice(final Choice hotlist) {
		return false;
	}

	// -- The event handler.
	//
	@Override
	public boolean handleEvent(final Event event) {
		if (event.id == Event.ACTION_EVENT) {
			if (event.target instanceof Button) {
				return handleButton((Button) event.target);
			} else if (event.target instanceof Choice) {
				return handleChoice((Choice) event.target);
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

	// -- Inits message panel
	//
	public void initMessagePanel() {
		_msgLine.setText(_initMessage);
		_msgLine.setEditable(false);
	}

	// -- Inits result panel
	//
	public void initResultPanel() {
		_result.setEditable(false);
	}

	protected Panel makeMainButtonPanel() {
		final Panel p = new Panel();

		p.setLayout(new FlowLayout(FlowLayout.RIGHT));
		p.add(_go);
		p.add(_quit);
		return p;
	}

	protected boolean popUpHandleButton(final Button button) {
		return false;
	}

	protected void quit() {
		dispose();
		try {
			_aglet.getAgletContext().getAgletProxy(_aglet.getAgletID()).dispose();
		} catch (final AgletException ae) {
		}
	}

	// -- Shared variables

	public void setFilename(final String filename) {
		_filepath.setText(filename);
	}

	// -- Writes a string to the message panel
	//
	public void setMessage(final String message) {
		appendTextArea(_msgLine, message);
	}

	// "setLocation" is in java.awt.Component of JDK1.1.
	// So rename it into setPosition.
	public void setPosition(final String position) {
		_addressChooser.setAddress(position);
	}

	// -- Writes a string to the result panel
	//
	public void setResult(final String str) {
		setTextArea(_result, str);
	}
}
