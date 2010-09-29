package net.sourceforge.aglets.examples.protection;

/*
 * @(#)ProtectionDialog.java
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
import java.awt.CardLayout;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * <tt>ActionPanel</tt> is a panel to execute action to an aglet.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author ONO Kouichi
 */
class ActionPanel extends Panel implements ItemListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -2589131708155099309L;
    private ProtectionAglet _aglet = null;
    private Choice _command = new Choice();
    private CardLayout _layout = new CardLayout();
    private Panel _field = new Panel();
    private TextField _destination = new TextField(20);
    private TextField _duration = new TextField("0", 5);
    private Button _doButton = new Button("Do");
    private int _action = 0;

    ActionPanel(ProtectionAglet aglet) {
	this._aglet = aglet;
	this._field.setLayout(this._layout);
	for (String label : ProtectionDialog.ACTIONS) {
	    this._command.addItem(label);
	    Panel panel = new Panel();

	    panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    this._field.add(label, panel);
	    if (label.equals(ProtectionDialog.ACTION_DISPATCH)) {
		panel.add(new Label("Destination"));
		panel.add(this._destination);
	    } else if (label.equals(ProtectionDialog.ACTION_DEACTIVATE)) {
		panel.add(new Label("Duration"));
		panel.add(this._duration);
		panel.add(new Label("[ms]"));
	    }
	}
	this._command.addItemListener(this);
	this.add(this._command);
	this.add(this._field);
	this._doButton.addActionListener(this);
	this.add(this._doButton);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	if (ev.getSource() == this._doButton) {

	    if (ProtectionDialog.ACTIONS[this._action].equals(ProtectionDialog.ACTION_DISPOSE)) {
		this._aglet.disposeTarget();
	    } else if (ProtectionDialog.ACTIONS[this._action].equals(ProtectionDialog.ACTION_CLONE)) {
		this._aglet.cloneTarget();
	    } else if (ProtectionDialog.ACTIONS[this._action].equals(ProtectionDialog.ACTION_DISPATCH)) {

		String destination = this._destination.getText();

		this._aglet.dispatchTarget(destination);
	    } else if (ProtectionDialog.ACTIONS[this._action].equals(ProtectionDialog.ACTION_RETRACT)) {

		this._aglet.retractTarget();
	    } else if (ProtectionDialog.ACTIONS[this._action].equals(ProtectionDialog.ACTION_DEACTIVATE)) {

		long duration = 0;

		try {
		    duration = Long.parseLong(this._duration.getText());
		} catch (NumberFormatException ex) {
		    System.err.println(ex.toString());
		    return;
		}
		this._aglet.deactivateTarget(duration);

	    }
	}
    }

    @Override
    public void itemStateChanged(ItemEvent ev) {
	String action = this.selectedAction(ev);

	if (action == null) {
	    return;
	}
	this.selectAction(action);
	this._layout.show(this._field, action);
    }

    private void selectAction(String action) {
	if (action == null) {
	    return;
	}
	for (int i = 0; i < ProtectionDialog.ACTIONS.length; i++) {
	    if (action.equals(ProtectionDialog.ACTIONS[i])) {
		this._action = i;
		return;
	    }
	}
	return;
    }

    private String selectedAction(ItemEvent ev) {
	Object[] items = ev.getItemSelectable().getSelectedObjects();

	if (items == null) {
	    return null;
	}
	return (String) items[0];
    }
}
