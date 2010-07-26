package examples.patterns;

/*
 * @(#)WriterWindow.java
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.TextField;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import com.ibm.aglet.AgletException;

/**
 * Class WriterWindow represents the interaction window with the Writer.
 * 
 * @see Writer
 * @version 1.01 96/12/28
 * @author Yariv Aridor
 */

public class WriterWindow extends SampleWindow {
    private static final String TITLE = "Writer";

    // -- Main panel
    private TextField _messageText = new TextField(FIELD);

    // -- Constructs the dialog window.
    //
    public WriterWindow(Writer aglet) throws AgletException {
	super(aglet);
	this.makeMainPanel();
	displayFrame(this);
    }

    // -- The call back methods
    @Override
    protected void go() {
	String text = this._messageText.getText().trim();
	boolean ok = true;
	String adr = this._addressChooser.getAddress();
	Vector destination = null;

	if (!adr.equals("")) {
	    URL dest = null;

	    try {
		dest = new URL(adr);
		destination = new Vector();
		destination.addElement(dest);
	    } catch (MalformedURLException e) {
		this._malFormedURLWindow.popup(this);
		ok = false;
	    } catch (IOException ae) {
		ok = false;
	    }
	    if (ok) {
		((SampleAglet) this._aglet).go(destination, text);
	    }
	} else {
	    this._malFormedURLWindow.popup(this);
	}
    }

    private void makeMainPanel() throws AgletException {
	Component comp;

	// button
	this.constraints.anchor = GridBagConstraints.CENTER;
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	this.constraints.weightx = 1.0;
	comp = this.makeMainButtonPanel();
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// title
	this.constraints.anchor = GridBagConstraints.WEST;
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	this.constraints.weightx = 1.0;
	comp = new Label(TITLE);
	comp.setFont(new Font(this.getFont().getName(), Font.BOLD, this.getFont().getSize() + 1));

	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// information settings
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.HORIZONTAL;
	this.constraints.insets = new Insets(0, 0, 10, 0);
	this.constraints.weightx = 1.0;
	comp = this._addressChooser;
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// message text panel
	this.addLabeledComponent("Message", this._messageText);

	// area for error messages
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	this.initMessagePanel();
	comp = this._msgLine;
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);
    }

    // -- Event handler methods
    @Override
    protected boolean popUpHandleButton(Button button) {
	if ((button == this._malFormedURLWindow.getButton(GeneralDialog.OKAY))
		&& "Okay".equals(button.getLabel())) {
	    this._malFormedURLWindow.setVisible(false);
	    return true;
	}
	return false; // -- should not reach here.
    }
}
