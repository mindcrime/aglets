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

import com.ibm.aglet.*;
import com.ibm.aglet.util.*;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.TextArea;
import java.awt.Color;
import java.awt.Insets;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;
import java.io.IOException;
/**
 * Class WriterWindow represents the interaction window with the
 * Writer.
 * 
 * @see Writer
 * @version  1.01  96/12/28
 * @author   Yariv Aridor
 */

public class WriterWindow extends SampleWindow {
	private static final String TITLE = "Writer";

	// -- Main panel
	private TextField _messageText = new TextField(FIELD);

	// --  Constructs the dialog window.
	// 
	public WriterWindow(Writer aglet) throws AgletException {
		super(aglet);
		makeMainPanel();
		displayFrame(this);
	}
	// --  The call back methods
	protected void go() {
		String text = _messageText.getText().trim();
		boolean ok = true;
		String adr = _addressChooser.getAddress();
		Vector destination = null;

		if (!adr.equals("")) {
			URL dest = null;

			try {
				dest = new URL(adr);
				destination = new Vector();
				destination.addElement(dest);
			} catch (MalformedURLException e) {
				_malFormedURLWindow.popup(this);
				ok = false;
			} catch (IOException ae) {
				ok = false;
			} 
			if (ok) {
				((SampleAglet)_aglet).go(destination, text);
			} 
		} else {
			_malFormedURLWindow.popup(this);
		}
	}
	private void makeMainPanel() throws AgletException {
		Component comp;

		// button
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = makeMainButtonPanel();
		layout.setConstraints(comp, constraints);
		add(comp);

		// title
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = new Label(TITLE);
		comp.setFont(new Font(getFont().getName(), Font.BOLD, 
							  getFont().getSize() + 1));

		layout.setConstraints(comp, constraints);
		add(comp);

		// information settings
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 10, 0);
		constraints.weightx = 1.0;
		comp = _addressChooser;
		layout.setConstraints(comp, constraints);
		add(comp);

		// message text panel
		addLabeledComponent("Message", _messageText);

		// area for error messages
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		initMessagePanel();
		comp = _msgLine;
		layout.setConstraints(comp, constraints);
		add(comp);
	}
	// --  Event handler methods
	protected boolean popUpHandleButton(Button button) {
		if (button == _malFormedURLWindow.getButton(PopUpMessageWindow.OKAY) 
				&& "Okay".equals(button.getLabel())) {
			_malFormedURLWindow.setVisible(false);
			return true;
		} 
		return false;		// -- should not reach here.
	}
}
