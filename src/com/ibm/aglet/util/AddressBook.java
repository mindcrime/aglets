package com.ibm.aglet.util;

/*
 * @(#)AddressBook.java
 * 
 * (c) Copyright IBM Corp. 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.List;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.ibm.awb.misc.Resource;

public class AddressBook extends Window implements ActionListener,
ItemListener, FocusListener {

    /**
     * 
     */
    private static final long serialVersionUID = 6730962348239624441L;
    transient List _list = new List(10, false);
    transient AddressChooser _chooser = null;

    private Button _add = new Button("Add to AddressBook");
    private Button _delete = new Button("Delete");
    private Button _target = null;

    private GridBagLayout _layout = new GridBagLayout();

    private String _title = "AddressBook";
    private Rectangle _title_bounds;

    int pad = 0;
    int ascent = 0;

    MouseListener mlistener = new MouseAdapter() {
	@Override
	public void mouseEntered(MouseEvent e) {
	    if (AddressBook.this.getParent().isVisible() == false) {
		AddressBook.this.setVisible(false);
		AddressBook.this._target = null;
	    } else {
		AddressBook.this.adjust();
	    }
	}
    };

    WindowListener wlistener = new WindowAdapter() {
	@Override
	public void windowClosing(WindowEvent ev) {
	    AddressBook.this.setVisible(false);
	}
    };

    /*
     * static public void main(String str[]) { Frame f = new Frame();
     * 
     * AddressBook c = new AddressBook("Address Book");
     * c.setBackground(Color.lightGray); c.list.addItem("itemA");
     * c.list.addItem("itemB"); c.list.addItem("itemC");
     * c.list.addItem("atp://moshima.trl.ibm.com/");
     * 
     * f.setLayout(new GridLayout(1,1)); f.add(c); f.pack(); f.show(); }
     */
    public AddressBook(Frame parent, AddressChooser chooser) {
	super(parent);
	this._chooser = chooser;

	this.setLayout(this._layout);
	GridBagConstraints cns = new GridBagConstraints();

	cns.fill = GridBagConstraints.NONE;
	cns.gridwidth = 1;
	cns.weighty = 0.0;
	cns.weightx = 0.0;
	cns.anchor = GridBagConstraints.WEST;
	this.addCmp(this._add, cns);

	cns.gridwidth = GridBagConstraints.REMAINDER;
	cns.anchor = GridBagConstraints.EAST;
	this.addCmp(this._delete, cns);
	this._delete.setEnabled(this._list.getSelectedItem() != null);

	cns.gridwidth = GridBagConstraints.REMAINDER;
	cns.weightx = 1.0;
	cns.weighty = 1.0;
	cns.fill = GridBagConstraints.BOTH;

	this.addCmp(this._list, cns);

	this._list.addActionListener(this);
	this._list.addItemListener(this);

	this._add.setActionCommand("add");
	this._add.addActionListener(this);

	this._delete.setActionCommand("delete");
	this._delete.addActionListener(this);

	this.addWindowListener(this.wlistener);
	this.addMouseListener(this.mlistener);
	this.addFocusListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
	if ("add".equals(ev.getActionCommand())) {
	    if (this._chooser != null) {
		String newitem = this._chooser.getAddress();
		int count = this._list.getItemCount();

		for (int i = 0; i < count; i++) {
		    if (newitem.equals(this._list.getItem(i))) {
			return;
		    }
		}
		this._list.add(newitem);
		this.updateAddressBook();
	    }
	} else if ("delete".equals(ev.getActionCommand())) {
	    int x = this._list.getSelectedIndex();

	    if (x >= 0) {
		this._list.remove(x);
	    }
	    this.updateAddressBook();
	}
	if (this._list == ev.getSource()) {
	    this.setVisible(false);
	    this._chooser.addressSelected(this._list.getSelectedItem());
	}
    }

    protected void addCmp(Component c, GridBagConstraints cns) {
	this._layout.setConstraints(c, cns);
	this.add(c);
    }

    @Override
    public void addNotify() {
	super.addNotify();
	if (this._title != null) {
	    FontMetrics fm = this.getFontMetrics(this.getFont());

	    this.pad = fm.getHeight();
	    this.ascent = fm.getAscent() + fm.getLeading();
	    this._title_bounds = new Rectangle(this.pad * 2, 0, fm.stringWidth(this._title), fm.getHeight());
	}
	this.reLayout();
    }

    public void adjust() {
	if (this._target == null) {
	    return;
	}
	Point loc = this._target.getLocationOnScreen();

	loc.y += this._target.getSize().height;
	this.setLocation(loc.x, loc.y);
    }

    @Override
    public void focusGained(FocusEvent ev) {
	if (this.isVisible()) {
	    this.adjust();
	    this.toFront();
	}
    }

    @Override
    public void focusLost(FocusEvent ev) {
    }

    @Override
    public void itemStateChanged(ItemEvent ev) {
	String item = this._list.getSelectedItem();

	if (item != null) {
	    this._delete.setEnabled(true);
	    this._chooser.setAddress(item);
	} else {
	    this._delete.setEnabled(false);
	    this._chooser.setAddress("");
	}
    }

    @Override
    public void paint(Graphics g) {
	super.paint(g);
	g.setColor(this.getBackground());
	Dimension size = this.getSize();

	g.fillRect(0, 0, size.width, size.height);

	g.draw3DRect(3, 3, size.width - 6, size.height - 6, false);
	g.draw3DRect(4, 4, size.width - 8, size.height - 8, true);
	if (this._title != null) {
	    g.fillRect(this._title_bounds.x, this._title_bounds.y, this._title_bounds.width, this._title_bounds.height);
	    g.setColor(Color.black);
	    g.drawString(this._title, this.pad * 2, this.ascent);
	}
    }

    public void popup(Button c) {
	this._target = c;
	this._target.setLabel("Close");

	this.adjust();

	if (this._list.getItemCount() > 0) {
	    this._list.removeAll();
	}

	Resource res = Resource.getResourceFor("aglets");
	String address[] = res.getStringArray("aglets.addressbook", " ");

	for (String addres : address) {
	    this._list.add(addres);
	}

	this.show();
	this.toFront();
    }

    private void reLayout() {
	Insets insets = new Insets(this.pad * 2, this.pad, this.pad, this.pad);
	GridBagLayout newlayout = new GridBagLayout();

	GridBagConstraints cns = null;

	cns = this._layout.getConstraints(this._add);
	cns.insets = insets;
	newlayout.setConstraints(this._add, cns);

	cns = this._layout.getConstraints(this._delete);
	cns.insets = insets;
	newlayout.setConstraints(this._delete, cns);

	cns = this._layout.getConstraints(this._list);
	cns.insets = new Insets(0, this.pad, this.pad, this.pad);
	newlayout.setConstraints(this._list, cns);

	this.setLayout(newlayout);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
	super.setBounds(x, y, width, height);
	this.repaint();
    }

    public void setTitle(String title) {
	this._title = title;
	if (this._title != null) {
	    FontMetrics fm = this.getFontMetrics(this.getFont());

	    this.pad = fm.getHeight();
	    this.ascent = fm.getAscent() + fm.getLeading();
	}
	this.reLayout();
    }

    @Override
    public void setVisible(boolean v) {
	if (this._target != null) {
	    if (v) {
		this._target.setLabel("Close");
	    } else {
		this._target.setLabel("AddressBook");
	    }
	}
	super.setVisible(v);
    }

    private void updateAddressBook() {
	Resource res = Resource.getResourceFor("aglets");

	String all = "";
	int count = this._list.getItemCount();

	for (int i = 0; i < count; i++) {
	    all += this._list.getItem(i) + " ";
	}

	res.setResource("aglets.addressbook", all);
    }
}
