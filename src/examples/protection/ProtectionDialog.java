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

import com.ibm.aglet.AgletProxy;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;

/**
 * <tt>ProtectionDialog</tt> is a dialog for selecting actions
 * to be protected or not.
 * 
 * @version 2.00
 * @author ONO, Kouichi
 * @author TAI, Hideki
 */
class ProtectionDialog extends JFrame implements ItemListener, ActionListener {
	static final String ACTION_DISPOSE = "dispose";
	static final String ACTION_CLONE = "clone";
	static final String ACTION_DISPATCH = "dispatch";
	static final String ACTION_RETRACT = "retract";
	static final String ACTION_DEACTIVATE = "deactivate";
	static final String[] ACTIONS = {
		ACTION_DISPOSE,
		ACTION_CLONE,
		ACTION_DISPATCH,
		ACTION_RETRACT,
		ACTION_DEACTIVATE
	};

	private ProtectionAglet _aglet;

	private JCheckBox[] _protectionCheckBox;
	private JButton _closeButton = new JButton("Close");
	private JButton _createButton = new JButton("Create Target");
	private JButton _selectButton = new JButton("Select Target");

	private JComboBox _cmdChoice;
	private CardLayout _cmdParamsLayout;
	private JPanel _cmdParamsPanel;
	private JButton _doButton = new JButton("Do");

	private JTextField _destination = new JTextField(15);
	private JTextField _duration = new JTextField("0", 5);

	ProtectionDialog(ProtectionAglet aglet, String label) {
		super(label);
		_aglet = aglet;
		getContentPane().add(createMainPanel());
		setAgletProtection();
	}

	private JPanel createMainPanel() {
		// The main panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// In the first row, locate the check boxes for the protections.
		JPanel pBox = new JPanel();
		pBox.setLayout(new BoxLayout(pBox, BoxLayout.X_AXIS));
		panel.add(pBox);
		{
			_protectionCheckBox = new JCheckBox[ACTIONS.length];
			for (int i = 0; i < ACTIONS.length; i++) {
				_protectionCheckBox[i] = new JCheckBox(ACTIONS[i], true);
				_protectionCheckBox[i].addItemListener(this);
				pBox.add(_protectionCheckBox[i]);
			}
		}

		// In the second row, locate the command panel.
		panel.add(createCommandPanel());

		// In the third row, locate the close button and the create target button.
		JPanel pButton = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(pButton);
		_closeButton.addActionListener(this);
		pButton.add(_closeButton);
		_createButton.addActionListener(this);
		pButton.add(_createButton);
		_selectButton.addActionListener(this);
		pButton.add(_selectButton);

		return panel;
	}

	private JPanel createCommandPanel() {
		JPanel cmdPanel = new JPanel();
		_cmdChoice = new JComboBox();
		cmdPanel.add(_cmdChoice);
		_cmdParamsLayout = new CardLayout();
		_cmdParamsPanel = new JPanel(_cmdParamsLayout);
		cmdPanel.add(_cmdParamsPanel);
		cmdPanel.add(_doButton);

		for (int i = 0; i < ACTIONS.length; i++) {
			String command = ACTIONS[i];
			_cmdChoice.addItem(command);
			JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT));
			_cmdParamsPanel.add(card, command);
			if (ACTION_DISPATCH.equals(command)) {
				card.add(new JLabel("Destination"));
				card.add(_destination);
			} else if (ACTION_DEACTIVATE.equals(command)) {
				card.add(new JLabel("Duration"));
				card.add(_duration);
				card.add(new JLabel("[ms]"));
			}
		}

		_cmdChoice.addItemListener(this);
		_doButton.addActionListener(this);
		return cmdPanel;
	}

	public void itemStateChanged(ItemEvent ev) {
		Object evtSrc = ev.getItemSelectable();
		if (evtSrc == _cmdChoice) {
			String command = (String)ev.getItem();
			if (command == null) {
				return;
			}
			_cmdParamsLayout.show(_cmdParamsPanel, command);
		}
	}

	public void actionPerformed(ActionEvent ev) {
		Object evtSrc = ev.getSource();
		if (evtSrc == _closeButton) {
			dispose();
		} else if (evtSrc == _createButton) {
			_aglet.createTarget();
		} else if (evtSrc == _selectButton) {
			GetTargetAgletDialog dialog = new GetTargetAgletDialog(this, _aglet);
			dialog.pack();
			dialog.show();
		} else if (evtSrc == _doButton) {
			setAgletProtection();
			String command = (String)_cmdChoice.getSelectedItem();
			if (ACTION_DISPOSE.equals(command)) {
				_aglet.disposeTarget();
				System.out.println("-----DISPOSE");
			} else if (ACTION_CLONE.equals(command)) {
				_aglet.cloneTarget();
				System.out.println("-----CLONE");
			} else if (ACTION_DISPATCH.equals(command)) {
				String destination = _destination.getText();
				_aglet.dispatchTarget(destination);
				System.out.println("-----DISPATCH: " + destination);
			} else if (ACTION_RETRACT.equals(command)) {
				_aglet.retractTarget();
				System.out.println("-----RETRACT");
			} else if (ACTION_DEACTIVATE.equals(command)) {
				long duration = 0;
				try {
					duration = Long.parseLong(_duration.getText());
				} catch (NumberFormatException ex) {
					System.err.println(ex.toString());
					return;
				} 
				_aglet.deactivateTarget(duration);
				System.out.println("-----DEACTIVATE: " + duration);
			}
		}
	}

	private String getProtections() {
		StringBuffer protections = null;
		for (int i = 0; i < _protectionCheckBox.length; i++) {
			if (_protectionCheckBox[i].isSelected() == true) {
				if (protections == null) {
					protections = new StringBuffer();
				} else {
					protections.append(",");
				}
				protections.append(ACTIONS[i]);
				System.out.println("****SELECTED: " + ACTIONS[i]);
			} else {
				System.out.println("****DE-SELECTED: " + ACTIONS[i]);
			}
		} 

		if (protections == null) {
			return null;
		} else {
			return protections.toString();
		}
	}

	private void setAgletProtection() {
		String protections = getProtections();
		_aglet.setAgletProtectionActions(null, protections);
	}

	public static void main(String[] args) throws Exception {
		ProtectionDialog w = new ProtectionDialog(null, "Protection Dialog");
		w.pack();
		w.show();
	}
}
