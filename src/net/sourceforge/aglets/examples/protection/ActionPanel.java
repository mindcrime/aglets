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
	private final Choice _command = new Choice();
	private final CardLayout _layout = new CardLayout();
	private final Panel _field = new Panel();
	private final TextField _destination = new TextField(20);
	private final TextField _duration = new TextField("0", 5);
	private final Button _doButton = new Button("Do");
	private int _action = 0;

	ActionPanel(final ProtectionAglet aglet) {
		_aglet = aglet;
		_field.setLayout(_layout);
		for (final String label : ProtectionDialog.ACTIONS) {
			_command.addItem(label);
			final Panel panel = new Panel();

			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			_field.add(label, panel);
			if (label.equals(ProtectionDialog.ACTION_DISPATCH)) {
				panel.add(new Label("Destination"));
				panel.add(_destination);
			} else if (label.equals(ProtectionDialog.ACTION_DEACTIVATE)) {
				panel.add(new Label("Duration"));
				panel.add(_duration);
				panel.add(new Label("[ms]"));
			}
		}
		_command.addItemListener(this);
		this.add(_command);
		this.add(_field);
		_doButton.addActionListener(this);
		this.add(_doButton);
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		if (ev.getSource() == _doButton) {

			if (ProtectionDialog.ACTIONS[_action].equals(ProtectionDialog.ACTION_DISPOSE)) {
				_aglet.disposeTarget();
			} else if (ProtectionDialog.ACTIONS[_action].equals(ProtectionDialog.ACTION_CLONE)) {
				_aglet.cloneTarget();
			} else if (ProtectionDialog.ACTIONS[_action].equals(ProtectionDialog.ACTION_DISPATCH)) {

				final String destination = _destination.getText();

				_aglet.dispatchTarget(destination);
			} else if (ProtectionDialog.ACTIONS[_action].equals(ProtectionDialog.ACTION_RETRACT)) {

				_aglet.retractTarget();
			} else if (ProtectionDialog.ACTIONS[_action].equals(ProtectionDialog.ACTION_DEACTIVATE)) {

				long duration = 0;

				try {
					duration = Long.parseLong(_duration.getText());
				} catch (final NumberFormatException ex) {
					System.err.println(ex.toString());
					return;
				}
				_aglet.deactivateTarget(duration);

			}
		}
	}

	@Override
	public void itemStateChanged(final ItemEvent ev) {
		final String action = selectedAction(ev);

		if (action == null) {
			return;
		}
		selectAction(action);
		_layout.show(_field, action);
	}

	private void selectAction(final String action) {
		if (action == null) {
			return;
		}
		for (int i = 0; i < ProtectionDialog.ACTIONS.length; i++) {
			if (action.equals(ProtectionDialog.ACTIONS[i])) {
				_action = i;
				return;
			}
		}
		return;
	}

	private String selectedAction(final ItemEvent ev) {
		final Object[] items = ev.getItemSelectable().getSelectedObjects();

		if (items == null) {
			return null;
		}
		return (String) items[0];
	}
}
