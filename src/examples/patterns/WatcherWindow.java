package examples.patterns;

/*
 * @(#)WatcherWindow.java
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.net.MalformedURLException;
import java.net.URL;

import com.ibm.aglet.AgletException;

/**
 * Class WatcherWindow represents the main window for user interaction with the
 * Watcher.
 * 
 * @see Watcher
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yoshiaki Mima
 * @author Yariv Aridor
 */

public class WatcherWindow extends SampleWindow {
    private static final String TITLE = "Watcher";
    private static final String NO_FILENAME_MSG = "Please input a valid file name.";
    private PopUpMessageWindow _noFileNameWindow = null;

    // -- Main panel

    private Choice _chkInterval = new Choice();
    private Choice _duration = new Choice();
    private Choice _stay = new Choice();

    // Constructs the FileWatch dialog window.
    //
    public WatcherWindow(Watcher aglet) throws AgletException {
	super(aglet);
	this._noFileNameWindow = new PopUpMessageWindow(this, "NO FILE NAME", NO_FILENAME_MSG);
	this.makeMainPanel();
	displayFrame(this);
    }

    // -- The call back methods

    @Override
    protected void go() {
	String url = this._addressChooser.getAddress();
	String path = this._filepath.getText().trim();
	String selection;
	double interval = 1.0 / 360;
	double duration = 0;
	boolean stay = false;

	// set interval
	selection = this._chkInterval.getSelectedItem();
	if (selection.equals("10 seconds")) {
	    interval = 1.0 / (3600 / 10);
	} else if (selection.equals("30 seconds")) {
	    interval = 1.0 / (3600 / 30);
	} else if (selection.equals("1 minute")) {
	    interval = 1.0 / (3600 / 60);
	} else if (selection.equals("1 hour")) {
	    interval = 1.0;
	} else if (selection.equals("half a day")) {
	    interval = 12.0;
	} else if (selection.equals("1 day")) {
	    interval = 24.0;
	}

	// set ingterval
	selection = this._duration.getSelectedItem();
	if (selection.equals("1 minute")) {
	    duration = 1 / 60.0;
	} else if (selection.equals("2 minutes")) {
	    duration = 2 / 60.0;
	} else if (selection.equals("5 minutes")) {
	    duration = 5 / 60.0;
	} else if (selection.equals("10 minutes")) {
	    duration = 10 / 60.0;
	} else if (selection.equals("1 hour")) {
	    duration = 1.0;
	} else if (selection.equals("2 hours")) {
	    duration = 2.0;
	} else if (selection.equals("6 hours")) {
	    duration = 6.0;
	} else if (selection.equals("24 hours")) {
	    duration = 24.0;
	}

	if (this._stay.getSelectedItem().equals("Yes")) {
	    stay = true;
	}

	if (url.equals("")) {
	    this._malFormedURLWindow.popup(this);
	} else if (path.equals("")) {
	    this._noFileNameWindow.popup(this);
	} else {
	    try {
		((Watcher) this._aglet).go(new URL(url), interval, duration, stay, path);
	    } catch (MalformedURLException e) {
		this._malFormedURLWindow.popup(this);
	    }
	}
    }

    private void makeMainPanel() throws AgletException {
	Component comp;

	this._result.setEditable(false);

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
	// addLabeledComponent(URLLabel, _URLString);
	// initURLFields(getHotlist(_aglet)); // hotlist & URL field
	// addLabeledComponent(hotlistLabel, _hotlist);

	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.HORIZONTAL;
	this.constraints.insets = new Insets(0, 0, 10, 0);
	this.constraints.weightx = 1.0;
	comp = this._addressChooser;
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// file name panel
	this.addLabeledComponent("File Name (Full Path):", this._filepath);

	// setting parameters for the Notifier
	this.constraints.anchor = GridBagConstraints.WEST;
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	comp = this.makeParamPanel();
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// panel for showing the result
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	comp = this._result;
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);

	// area for error messages
	this.constraints.gridwidth = GridBagConstraints.REMAINDER;
	this.constraints.fill = GridBagConstraints.BOTH;
	this.initMessagePanel();
	comp = this._msgLine;
	this.layout.setConstraints(comp, this.constraints);
	this.add(comp);
    }

    // -- Parameters Panel
    //
    private Panel makeParamPanel() {
	Panel p = new Panel();

	p.setLayout(new FlowLayout(FlowLayout.LEFT));

	this._chkInterval.addItem("10 seconds");
	this._chkInterval.addItem("30 seconds");
	this._chkInterval.addItem("1 minute");
	this._chkInterval.addItem("1 hour");
	this._chkInterval.addItem("half a day");
	this._chkInterval.addItem("1 day");
	p.add(makeSingleField("Check Interval(sec)", this._chkInterval));

	this._duration.addItem("1 minute");
	this._duration.addItem("2 minutes");
	this._duration.addItem("5 minutes");
	this._duration.addItem("10 minutes");
	this._duration.addItem("1 hour");
	this._duration.addItem("2 hours");
	this._duration.addItem("6 hours");
	this._duration.addItem("24 hours");
	p.add(makeSingleField("Duration", this._duration));

	this._stay.addItem("Yes");
	this._stay.addItem("No");
	p.add(makeSingleField("Keep Notifing?", this._stay));

	return p;
    }

    // -- Event handler methods
    @Override
    protected boolean popUpHandleButton(Button button) {
	if ((button == this._noFileNameWindow.getButton(GeneralDialog.OKAY))
		&& "Okay".equals(button.getLabel())) {
	    this._noFileNameWindow.setVisible(false);
	    return true;
	}

	if ((button == this._malFormedURLWindow.getButton(GeneralDialog.OKAY))
		&& "Okay".equals(button.getLabel())) {
	    this._malFormedURLWindow.setVisible(false);
	    return true;
	}

	return false; // -- should not reach here.
    }
}
