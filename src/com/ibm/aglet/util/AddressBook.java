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

	private final Button _add = new Button("Add to AddressBook");
	private final Button _delete = new Button("Delete");
	private Button _target = null;

	private final GridBagLayout _layout = new GridBagLayout();

	private String _title = "AddressBook";
	private Rectangle _title_bounds;

	int pad = 0;
	int ascent = 0;

	MouseListener mlistener = new MouseAdapter() {
		@Override
		public void mouseEntered(final MouseEvent e) {
			if (AddressBook.this.getParent().isVisible() == false) {
				AddressBook.this.setVisible(false);
				_target = null;
			} else {
				AddressBook.this.adjust();
			}
		}
	};

	WindowListener wlistener = new WindowAdapter() {
		@Override
		public void windowClosing(final WindowEvent ev) {
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
	public AddressBook(final Frame parent, final AddressChooser chooser) {
		super(parent);
		_chooser = chooser;

		setLayout(_layout);
		final GridBagConstraints cns = new GridBagConstraints();

		cns.fill = GridBagConstraints.NONE;
		cns.gridwidth = 1;
		cns.weighty = 0.0;
		cns.weightx = 0.0;
		cns.anchor = GridBagConstraints.WEST;
		addCmp(_add, cns);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.anchor = GridBagConstraints.EAST;
		addCmp(_delete, cns);
		_delete.setEnabled(_list.getSelectedItem() != null);

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.weightx = 1.0;
		cns.weighty = 1.0;
		cns.fill = GridBagConstraints.BOTH;

		addCmp(_list, cns);

		_list.addActionListener(this);
		_list.addItemListener(this);

		_add.setActionCommand("add");
		_add.addActionListener(this);

		_delete.setActionCommand("delete");
		_delete.addActionListener(this);

		addWindowListener(wlistener);
		addMouseListener(mlistener);
		addFocusListener(this);
	}

	@Override
	public void actionPerformed(final ActionEvent ev) {
		if ("add".equals(ev.getActionCommand())) {
			if (_chooser != null) {
				final String newitem = _chooser.getAddress();
				final int count = _list.getItemCount();

				for (int i = 0; i < count; i++) {
					if (newitem.equals(_list.getItem(i))) {
						return;
					}
				}
				_list.add(newitem);
				updateAddressBook();
			}
		} else if ("delete".equals(ev.getActionCommand())) {
			final int x = _list.getSelectedIndex();

			if (x >= 0) {
				_list.remove(x);
			}
			updateAddressBook();
		}
		if (_list == ev.getSource()) {
			setVisible(false);
			_chooser.addressSelected(_list.getSelectedItem());
		}
	}

	protected void addCmp(final Component c, final GridBagConstraints cns) {
		_layout.setConstraints(c, cns);
		this.add(c);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		if (_title != null) {
			final FontMetrics fm = getFontMetrics(getFont());

			pad = fm.getHeight();
			ascent = fm.getAscent() + fm.getLeading();
			_title_bounds = new Rectangle(pad * 2, 0, fm.stringWidth(_title), fm.getHeight());
		}
		reLayout();
	}

	public void adjust() {
		if (_target == null) {
			return;
		}
		final Point loc = _target.getLocationOnScreen();

		loc.y += _target.getSize().height;
		this.setLocation(loc.x, loc.y);
	}

	@Override
	public void focusGained(final FocusEvent ev) {
		if (isVisible()) {
			adjust();
			toFront();
		}
	}

	@Override
	public void focusLost(final FocusEvent ev) {
	}

	@Override
	public void itemStateChanged(final ItemEvent ev) {
		final String item = _list.getSelectedItem();

		if (item != null) {
			_delete.setEnabled(true);
			_chooser.setAddress(item);
		} else {
			_delete.setEnabled(false);
			_chooser.setAddress("");
		}
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		g.setColor(getBackground());
		final Dimension size = this.getSize();

		g.fillRect(0, 0, size.width, size.height);

		g.draw3DRect(3, 3, size.width - 6, size.height - 6, false);
		g.draw3DRect(4, 4, size.width - 8, size.height - 8, true);
		if (_title != null) {
			g.fillRect(_title_bounds.x, _title_bounds.y, _title_bounds.width, _title_bounds.height);
			g.setColor(Color.black);
			g.drawString(_title, pad * 2, ascent);
		}
	}

	public void popup(final Button c) {
		_target = c;
		_target.setLabel("Close");

		adjust();

		if (_list.getItemCount() > 0) {
			_list.removeAll();
		}

		final Resource res = Resource.getResourceFor("aglets");
		final String address[] = res.getStringArray("aglets.addressbook", " ");

		for (final String addres : address) {
			_list.add(addres);
		}

		this.show();
		toFront();
	}

	private void reLayout() {
		final Insets insets = new Insets(pad * 2, pad, pad, pad);
		final GridBagLayout newlayout = new GridBagLayout();

		GridBagConstraints cns = null;

		cns = _layout.getConstraints(_add);
		cns.insets = insets;
		newlayout.setConstraints(_add, cns);

		cns = _layout.getConstraints(_delete);
		cns.insets = insets;
		newlayout.setConstraints(_delete, cns);

		cns = _layout.getConstraints(_list);
		cns.insets = new Insets(0, pad, pad, pad);
		newlayout.setConstraints(_list, cns);

		setLayout(newlayout);
	}

	@Override
	public void setBounds(final int x, final int y, final int width, final int height) {
		super.setBounds(x, y, width, height);
		this.repaint();
	}

	public void setTitle(final String title) {
		_title = title;
		if (_title != null) {
			final FontMetrics fm = getFontMetrics(getFont());

			pad = fm.getHeight();
			ascent = fm.getAscent() + fm.getLeading();
		}
		reLayout();
	}

	@Override
	public void setVisible(final boolean v) {
		if (_target != null) {
			if (v) {
				_target.setLabel("Close");
			} else {
				_target.setLabel("AddressBook");
			}
		}
		super.setVisible(v);
	}

	private void updateAddressBook() {
		final Resource res = Resource.getResourceFor("aglets");

		String all = "";
		final int count = _list.getItemCount();

		for (int i = 0; i < count; i++) {
			all += _list.getItem(i) + " ";
		}

		res.setResource("aglets.addressbook", all);
	}
}
