package examples.protection;

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

import java.awt.Panel;
import java.awt.Label;
import java.awt.TextField;
import java.awt.Choice;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <tt>ActionPanel</tt> is a panel to execute action to an aglet.
 * 
 * @version     1.00    $Date: 2009/07/28 07:04:53 $
 * @author      ONO Kouichi
 */
class ActionPanel extends Panel implements ItemListener, ActionListener {
	private ProtectionAglet _aglet = null;
	private Choice _command = new Choice();
	private CardLayout _layout = new CardLayout();
	private Panel _field = new Panel();
	private TextField _destination = new TextField(20);
	private TextField _duration = new TextField("0", 5);
	private Button _doButton = new Button("Do");
	private int _action = 0;

	ActionPanel(ProtectionAglet aglet) {
		_aglet = aglet;
		_field.setLayout(_layout);
		for (int i = 0; i < ProtectionDialog.ACTIONS.length; i++) {
			String label = ProtectionDialog.ACTIONS[i];

			_command.addItem(label);
			Panel panel = new Panel();

			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			_field.add(label, panel);
			if ( ProtectionDialog.ACTIONS[i].equals( ProtectionDialog.ACTION_DISPATCH) ) {
				panel.add(new Label("Destination"));
				panel.add(_destination);
			} else if ( ProtectionDialog.ACTIONS[i].equals( ProtectionDialog.ACTION_DEACTIVATE) ) {
				panel.add(new Label("Duration"));
				panel.add(_duration);
				panel.add(new Label("[ms]"));
			} 
		} 
		_command.addItemListener(this);
		add(_command);
		add(_field);
		_doButton.addActionListener(this);
		add(_doButton);
	}
	public void actionPerformed(ActionEvent ev) {
		if (ev.getSource() == _doButton) {
		    
		    if( ProtectionDialog.ACTIONS[ this._action ].equals( ProtectionDialog.ACTION_DISPOSE) ){
				_aglet.disposeTarget();
		    } else if ( ProtectionDialog.ACTIONS[ this._action ].equals( ProtectionDialog.ACTION_CLONE) ){
				_aglet.cloneTarget();
		    } else if ( ProtectionDialog.ACTIONS[ this._action ].equals( ProtectionDialog.ACTION_DISPATCH) ){
			
				String destination = _destination.getText();

				_aglet.dispatchTarget(destination);
		    }else if ( ProtectionDialog.ACTIONS[ this._action ].equals( ProtectionDialog.ACTION_RETRACT) ){
			
				_aglet.retractTarget();
		    } else if ( ProtectionDialog.ACTIONS[ this._action ].equals( ProtectionDialog.ACTION_DEACTIVATE) ){
			
				long duration = 0;

				try {
					duration = Long.parseLong(_duration.getText());
				} catch (NumberFormatException ex) {
					System.err.println(ex.toString());
					return;
				} 
				_aglet.deactivateTarget(duration);
				
		    }
		} 
	}
	public void itemStateChanged(ItemEvent ev) {
		String action = selectedAction(ev);

		if (action == null) {
			return;
		} 
		selectAction(action);
		_layout.show(_field, action);
	}
	private void selectAction(String action) {
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
	private String selectedAction(ItemEvent ev) {
		Object[] items = ev.getItemSelectable().getSelectedObjects();

		if (items == null) {
			return null;
		} 
		return (String)items[0];
	}
}
