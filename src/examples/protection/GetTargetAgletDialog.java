package examples.protection;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletID;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;

class GetTargetAgletDialog extends JDialog implements ActionListener {

	private ProtectionAglet _masterAglet;
	private AgletProxy _selectedAglet = null;
	
	private JButton _updateButton = new JButton("Update");
	private JButton _selectButton = new JButton("Select");
	private JButton _cancelButton = new JButton("Cancel");
	private JList _list;

	static class ListEntry {
		AgletID agletID;
		String className;
		String owner;
		long birthDay;

		ListEntry(AgletInfo info) {
			agletID = info.getAgletID();
			className = info.getAgletClassName();
			owner = info.getAuthorityName();
			birthDay = info.getCreationTime();
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append((new Date(birthDay)).toString());
			sb.append(": ");
			sb.append(agletID.toString());
			sb.append(": ");
			sb.append(className);
			sb.append(": ");
			sb.append(owner);
			return sb.toString();
		}
	}

	GetTargetAgletDialog(JFrame owner, ProtectionAglet masterAglet) {
		super(owner, "Select the target aglet", true);
		_masterAglet = masterAglet;
		getContentPane().add(createMainPanel());
	}

	JPanel createMainPanel() {
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		// Title label
		main.add(new JLabel("Select the target aglet"));

		JPanel listPanel = new JPanel(new BorderLayout());
		_list = new JList();
		updateAgletList();
		listPanel.add(_list, BorderLayout.CENTER);

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(_updateButton, BorderLayout.EAST);
		_updateButton.addActionListener(this);
		listPanel.add(p1, BorderLayout.SOUTH);
		main.add(listPanel);

		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(_selectButton, BorderLayout.WEST);
		p2.add(_cancelButton, BorderLayout.EAST);
		_selectButton.addActionListener(this);
		_cancelButton.addActionListener(this);
		main.add(p2);

		return main;
	}

	private void updateAgletList() {
		Vector v = new Vector();
		try {
			Enumeration enumer = _masterAglet.getAgletContext().getAgletProxies();
			while (enumer.hasMoreElements()) {
				AgletProxy proxy = (AgletProxy)enumer.nextElement();
				if (proxy.isValid()) {
					ListEntry entry = new ListEntry(proxy.getAgletInfo());
					v.addElement(entry);
				}
			}
		} catch (Exception ex) {
		} finally {
			_list.setListData(v);
		}
	}

	public void actionPerformed(ActionEvent ev) {
		Object evtSrc = ev.getSource();
		if (evtSrc == _updateButton) {
			updateAgletList();
		} else if (evtSrc == _selectButton) {
			ListEntry selected = (ListEntry)_list.getSelectedValue();
			if (selected == null) {
				return;
			}
			try {
				AgletProxy target =
					_masterAglet.getAgletContext().getAgletProxy(selected.agletID);
				_masterAglet.setTarget(target);
				this.dispose();
			} catch (Exception ex) {
			}
		} else if (evtSrc == _cancelButton) {
			this.dispose();
		}
	}
}
