package examples.protection;

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

    private ProtectionAglet _masterAglet;
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
	    this.agletID = info.getAgletID();
	    this.className = info.getAgletClassName();
	    this.owner = info.getAuthorityName();
	    this.birthDay = info.getCreationTime();
	}

	@Override
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    sb.append((new Date(this.birthDay)).toString());
	    sb.append(": ");
	    sb.append(this.agletID.toString());
	    sb.append(": ");
	    sb.append(this.className);
	    sb.append(": ");
	    sb.append(this.owner);
	    return sb.toString();
	}
    }

    GetTargetAgletDialog(JFrame owner, ProtectionAglet masterAglet) {
	super(owner, "Select the target aglet", true);
	this._masterAglet = masterAglet;
	this.getContentPane().add(this.createMainPanel());
    }

    JPanel createMainPanel() {
	JPanel main = new JPanel();
	main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

	// Title label
	main.add(new JLabel("Select the target aglet"));

	JPanel listPanel = new JPanel(new BorderLayout());
	this._list = new JList();
	this.updateAgletList();
	listPanel.add(this._list, BorderLayout.CENTER);

	JPanel p1 = new JPanel(new BorderLayout());
	p1.add(this._updateButton, BorderLayout.EAST);
	this._updateButton.addActionListener(this);
	listPanel.add(p1, BorderLayout.SOUTH);
	main.add(listPanel);

	JPanel p2 = new JPanel(new BorderLayout());
	p2.add(this._selectButton, BorderLayout.WEST);
	p2.add(this._cancelButton, BorderLayout.EAST);
	this._selectButton.addActionListener(this);
	this._cancelButton.addActionListener(this);
	main.add(p2);

	return main;
    }

    private void updateAgletList() {
	Vector v = new Vector();
	try {
	    Enumeration enumer = this._masterAglet.getAgletContext().getAgletProxies();
	    while (enumer.hasMoreElements()) {
		AgletProxy proxy = (AgletProxy) enumer.nextElement();
		if (proxy.isValid()) {
		    ListEntry entry = new ListEntry(proxy.getAgletInfo());
		    v.addElement(entry);
		}
	    }
	} catch (Exception ex) {
	} finally {
	    this._list.setListData(v);
	}
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	Object evtSrc = ev.getSource();
	if (evtSrc == this._updateButton) {
	    this.updateAgletList();
	} else if (evtSrc == this._selectButton) {
	    ListEntry selected = (ListEntry) this._list.getSelectedValue();
	    if (selected == null) {
		return;
	    }
	    try {
		AgletProxy target = this._masterAglet.getAgletContext().getAgletProxy(selected.agletID);
		this._masterAglet.setTarget(target);
		this.dispose();
	    } catch (Exception ex) {
	    }
	} else if (evtSrc == this._cancelButton) {
	    this.dispose();
	}
    }
}
