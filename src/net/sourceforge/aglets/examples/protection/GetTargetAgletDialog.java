package net.sourceforge.aglets.examples.protection;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.AgletProxy;

class GetTargetAgletDialog extends JDialog implements ActionListener {

	static class ListEntry {
		AgletID agletID;
		String className;
		String owner;
		long birthDay;

		ListEntry(final AgletInfo info) {
			agletID = info.getAgletID();
			className = info.getAgletClassName();
			owner = info.getAuthorityName();
			birthDay = info.getCreationTime();
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer();
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 8216898100559700859L;
	private final ProtectionAglet _masterAglet;
	private final JButton _updateButton = new JButton("Update");
	private final JButton _selectButton = new JButton("Select");
	private final JButton _cancelButton = new JButton("Cancel");

	private JList _list;

	GetTargetAgletDialog(final JFrame owner, final ProtectionAglet masterAglet) {
		super(owner, "Select the target aglet", true);
		_masterAglet = masterAglet;
		getContentPane().add(createMainPanel());
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		final Object evtSrc = ev.getSource();
		if (evtSrc == _updateButton) {
			updateAgletList();
		} else if (evtSrc == _selectButton) {
			final ListEntry selected = (ListEntry) _list.getSelectedValue();
			if (selected == null) {
				return;
			}
			try {
				final AgletProxy target = _masterAglet.getAgletContext().getAgletProxy(selected.agletID);
				_masterAglet.setTarget(target);
				dispose();
			} catch (final Exception ex) {
			}
		} else if (evtSrc == _cancelButton) {
			dispose();
		}
	}

	JPanel createMainPanel() {
		final JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

		// Title label
		main.add(new JLabel("Select the target aglet"));

		final JPanel listPanel = new JPanel(new BorderLayout());
		_list = new JList();
		updateAgletList();
		listPanel.add(_list, BorderLayout.CENTER);

		final JPanel p1 = new JPanel(new BorderLayout());
		p1.add(_updateButton, BorderLayout.EAST);
		_updateButton.addActionListener(this);
		listPanel.add(p1, BorderLayout.SOUTH);
		main.add(listPanel);

		final JPanel p2 = new JPanel(new BorderLayout());
		p2.add(_selectButton, BorderLayout.WEST);
		p2.add(_cancelButton, BorderLayout.EAST);
		_selectButton.addActionListener(this);
		_cancelButton.addActionListener(this);
		main.add(p2);

		return main;
	}

	private void updateAgletList() {
		final Vector v = new Vector();
		try {
			final Enumeration enumer = _masterAglet.getAgletContext().getAgletProxies();
			while (enumer.hasMoreElements()) {
				final AgletProxy proxy = (AgletProxy) enumer.nextElement();
				if (proxy.isValid()) {
					final ListEntry entry = new ListEntry(proxy.getAgletInfo());
					v.addElement(entry);
				}
			}
		} catch (final Exception ex) {
		} finally {
			_list.setListData(v);
		}
	}
}
