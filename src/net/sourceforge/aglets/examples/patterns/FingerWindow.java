package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)FingerWindow.java
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
import java.net.MalformedURLException;
import java.net.URL;

import com.ibm.aglet.AgletException;

/**
 * Class FingerWindow represents the main window for user interaction
 * 
 * @see Watcher
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yoshiaki Mima
 * @author Yariv Aridor
 */

class FingerWindow extends SampleWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8507387907570451861L;
	private static final String TITLE = "Finger";

	// -- Constructor
	//
	FingerWindow(final Finger aglet) throws AgletException {
		super(aglet);
		makeMainPanel();
		displayFrame(this);
	}

	// -- The call back methods
	//
	@Override
	protected void go() {
		boolean ok = true;
		final String adr = _addressChooser.getAddress();

		if (!adr.equals("")) {
			URL dest = null;

			try {
				dest = new URL(adr);
			} catch (final MalformedURLException e) {
				_malFormedURLWindow.popup(this);
				ok = false;
			}
			if (ok) {
				((Finger) _aglet).go(dest);
			}
		} else {
			_malFormedURLWindow.popup(this);
		}
	}

	// -- Main panel
	//
	private void makeMainPanel() throws AgletException {
		Component comp;

		// button
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = makeMainButtonPanel();
		layout.setConstraints(comp, constraints);
		this.add(comp);

		// title
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		comp = new Label(TITLE);
		comp.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize() + 1));

		layout.setConstraints(comp, constraints);
		this.add(comp);

		// information settings
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 10, 0);
		constraints.weightx = 1.0;
		comp = _addressChooser;
		layout.setConstraints(comp, constraints);
		this.add(comp);

		// panel for showing the result
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weighty = 1.0;
		comp = _result;
		layout.setConstraints(comp, constraints);
		this.add(comp);

		// area for error messages
		constraints.gridwidth = GridBagConstraints.REMAINDER;
		constraints.fill = GridBagConstraints.BOTH;
		initMessagePanel();
		constraints.weighty = 1.0;
		comp = _msgLine;
		layout.setConstraints(comp, constraints);
		this.add(comp);
	}

	// -- Event handler method
	//
	@Override
	protected boolean popUpHandleButton(final Button button) {
		if ((button == _malFormedURLWindow.getButton(GeneralDialog.OKAY))
				&& "Okay".equals(button.getLabel())) {
			_malFormedURLWindow.setVisible(false);
			return true;
		}
		return false; // -- should not reach here.
	}
}
