package com.ibm.aglets.tahiti;

/*
 * @(#)TahitiDialog.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class TahitiDialog extends Dialog {
	static final public String lineSeparator = "\n";

	static protected class MessagePanel extends Panel {
		private GridBagConstraints cns = new GridBagConstraints();
		private GridBagLayout grid = new GridBagLayout();
		private boolean raised;
		private int alignment = Label.LEFT;

		/**
		 * Line separator constants
		 */

		/*
		 * Constructs a message panel with the message.
		 * @param message
		 * @param alignment
		 * @param raised
		 */
		public MessagePanel(String message, int alignment, boolean raised) {
			this(split(message), alignment, raised);
		}

		public MessagePanel(String messages[], int alignment, 
							boolean raised) {
			this.alignment = alignment;
			this.raised = raised;
			cns.gridwidth = GridBagConstraints.REMAINDER;
			cns.fill = GridBagConstraints.BOTH;
			cns.weightx = 1.0;
			cns.weighty = 1.0;
			cns.insets = new Insets(3, 3, 3, 3);
			setLayout(grid);
			for (int i = 0; i < messages.length; i++) {
				Label l = new Label(messages[i], alignment);

				grid.setConstraints(l, cns);
				add(l);
			} 
		}

		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(getBackground());
			Dimension d = getSize();

			g.fillRect(0, 0, d.width, d.height);
			g.draw3DRect(1, 1, d.width - 2, d.height - 2, raised);
		} 
	}

	private static ResourceBundle bundle = null;
	static {
		try {
			bundle = 
				(ResourceBundle)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return ResourceBundle.getBundle("tahiti");
				} 
			});
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} 
	} 

	private Panel buttonPanel = new Panel();
	private boolean shown = false;
	private Button _closeButton = null;

	protected TahitiDialog(Frame f) {
		this(f, "", false);
	}
	protected TahitiDialog(Frame f, String title, boolean modal) {
		super(f, title, modal);
		setLayout(new BorderLayout());
		setTitle(title);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		add("South", buttonPanel);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (TahitiDialog.this.windowClosing(e)) {
					dispose();
				} else {
					setVisible(false);
				} 
			} 
		});

	}
	public Button addButton(String name) {
		Button b = new Button(name);

		buttonPanel.add(b);
		return b;
	}
	protected Button addButton(String name, ActionListener listener) {
		Button b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(listener);
		return b;
	}
	protected Button addButton(String name, ActionListener actionListener, 
							   KeyListener keyListener) {
		Button b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(actionListener);
		b.addKeyListener(keyListener);
		return b;
	}
	protected void addCloseButton(String name) {
		class AlertCloseListener extends ActionAndKeyListener {
			Dialog target;

			AlertCloseListener(Dialog d) {
				target = d;
			}
			protected void doAction() {
				target.dispose();
				closeButtonPressed();
			} 
		}
		if (name == null) {
			name = bundle.getString("dialog.close");
		} 
		ActionAndKeyListener listener = new AlertCloseListener(this);

		_closeButton = addButton(name, listener, listener);
	}
	/*
	 * Alert Dialog
	 */
	public static TahitiDialog alert(Frame f, String msg) {
		TahitiDialog dialog = null;

		try {
			final Frame fFrame = f;
			final String fMsg = msg;
			final ResourceBundle fBundle = bundle;

			dialog = 
				(TahitiDialog)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					TahitiDialog d = 
						new TahitiDialog(fFrame, 
										 fBundle.getString("title.alert"), 
										 true);

					d.add("Center", 
						  new MessagePanel(fMsg, Label.LEFT, false));
					d.addCloseButton(null);
					return d;
				} 
			});
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} 
		return dialog;
	}
	public void beep() {
		getToolkit().beep();
	}
	protected void closeButtonPressed() {}
	public MainWindow getMainWindow() {
		return (MainWindow)getParent();
	}
	static public void main(String a[]) {
		TahitiDialog.alert(new Frame(), a[0]).popupAtCenterOfScreen();
	}
	/*
	 * 
	 */
	public static TahitiDialog message(Frame f, String title, String msg) {
		TahitiDialog dialog = null;

		try {
			final Frame fFrame = f;
			final String fTitle = title;
			final String fMsg = msg;
			final ResourceBundle fBundle = bundle;

			dialog = 
				(TahitiDialog)AccessController
					.doPrivileged(new PrivilegedAction() {
				public Object run() {
					TahitiDialog d = 
						new TahitiDialog(fFrame, 
										 fBundle.getString("title.message"), 
										 true);

					d.add("North", new Label(fTitle));
					d.add("Center", 
						  new MessagePanel(fMsg, Label.LEFT, false));
					d.addCloseButton(null);
					return d;
				} 
			});
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} 
		return dialog;
	}
	public void popup() {
		if (shown == false) {
			pack();
			shown = true;
		} 
		show();
	}
	public void popupAtCenterOfParent() {
		if (shown == false) {
			pack();
			shown = true;
		} 
		Dimension d = getToolkit().getScreenSize();
		Frame parent = (Frame)getParent();

		Point ploc = parent.getLocationOnScreen();
		Dimension psize = parent.getSize();

		Dimension size = getSize();

		int x = (psize.width - size.width) / 2 + ploc.x;
		int y = (psize.height - size.height) / 2 + ploc.y;

		if (x < 0) {
			x = 0;
		} 
		if (x > d.width - size.width) {
			x = d.width - size.width;

		} 
		if (y < 0) {
			y = 0;
		} 
		if (y > d.height - size.height) {
			y = d.height - size.height;

		} 
		setLocation(x, y);

		show();
	}
	public void popupAtCenterOfScreen() {
		if (shown == false) {
			pack();
			shown = true;
		} 
		Dimension d = getToolkit().getScreenSize();

		setLocation((d.width - getSize().width) / 2, 
					(d.height - getSize().height) / 2);
		show();
	}
	/*
	 * 
	 * public static TahitiDialog message(Frame f, String title, String msg, String detail) {
	 * TahitiDialog dialog = null;
	 * try {
	 * AccessController.beginPrivileged();
	 * dialog = new TahitiDialog(f, "Message", true);
	 * dialog.add("North", new Label(title));
	 * dialog.add("Center", new MessagePanel(msg, Label.LEFT, false));
	 * dialog.addCloseButton(null);
	 * } finally {
	 * AccessController.endPrivileged();
	 * }
	 * return dialog;
	 * }
	 */

	public static String[] split(String str) {
		String msg[] = new String[50];
		int pos, i, size = lineSeparator.length();

		for (i = 0; (pos = str.indexOf(lineSeparator)) >= 0 && i < 49; i++) {
			msg[i] = str.substring(0, pos);
			str = str.substring(pos + size);
		} 
		msg[i++] = str;

		String ret[] = new String[i];

		System.arraycopy(msg, 0, ret, 0, i);
		return ret;
	}
	protected boolean windowClosing(WindowEvent e) {
		return true;
	}
}
