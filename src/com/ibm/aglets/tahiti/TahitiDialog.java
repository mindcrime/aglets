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
import javax.swing.*;
import com.ibm.aglet.*;
import com.ibm.aglets.*;
import com.ibm.aglets.tahiti.utils.*;

/**
 * The Tahiti base dialog window, used for messages and other UI windows.
 * Ported from AWT to Swing by Luca Ferrari.
 */
public class TahitiDialog extends JDialog {
    
    // load resources
    protected static ResourceBundle bundle = null;
	static {
		bundle = ResourceBundle.getBundle("tahiti");
	}
    
	static final public String lineSeparator = "\n";

	static protected class MessagePanel extends JPanel {
		private boolean raised;
		private int alignment = JLabel.LEFT;

		
		/*
		 * Constructs a message panel with the message.
		 * @param message
		 * @param alignment
		 * @param raised
		 */
		public MessagePanel(String message) {
			this(message, JLabel.LEFT);
		}
		
		public MessagePanel(String[] messages,int alignment, boolean raised){
		    super();
		    JPanel p2 = new JPanel();
		    for(int i=0; messages!=null && i<messages.length; i++){
		        p2.add(new JLabel(messages[i], alignment));
		    }
		    this.add(new JScrollPane(p2));
		}

		public MessagePanel(String message, int alignment) {
			this.alignment = alignment;
			
			JLabel msg = new JLabel(message,alignment);
			this.add(new JScrollPane(msg));
			
		}
	}



	private JPanel buttonPanel = new JPanel();
	private boolean shown = false;
	private JButton _closeButton = null;

	protected TahitiDialog(JFrame f) {
		this(f, "", false);
	}
	protected TahitiDialog(JFrame f, String title, boolean modal) {
		super(f, title, modal);
		this.getContentPane().setLayout(new BorderLayout());
		setTitle(title);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.getContentPane().add("South", buttonPanel);

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
	public JButton addButton(String name) {
		JButton b = new JButton(name);

		buttonPanel.add(b);
		return b;
	}
	protected JButton addButton(String name, ActionListener listener) {
		JButton b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(listener);
		return b;
	}
	
	/**
	 * Creates and adds a JButton to the south button panel.
	 * @param label the label of the button
	 * @param actionCommand the action command of the button (if null => the label)
	 * @param icon the icon of the button
	 * @param listener the actionlistener for the button
	 */
	protected void addJButton(String label, String actionCommand, Icon icon, ActionListener listener){
	    if(label==null){ return; }
	    if(actionCommand == null){ actionCommand = label; }
	    
	    JButton button = new JButton(label,icon);
	    button.setActionCommand(actionCommand);
	    button.addActionListener(listener);
	    this.buttonPanel.add(button);
	}
	
	/**
	 * A method to get the agent name as a string starting from its proxy. The method can show
	 * a dialog window to report errors.
	 * @param proxy the agent proxy
	 * @return a special string if there's an error, or the agent name
	 */
	protected String getAgletName(AgletProxy proxy){
	    if(proxy==null){
	        JOptionPane.showMessageDialog(this,bundle.getString("dialog.tahitidialog.error.proxynull"),bundle.getString("dialog.tahitidialog.error.title"),JOptionPane.ERROR_MESSAGE, IconRepository.getIcon("proxy"));
	        return bundle.getString("dialog.tahitidialog.error.proxy2name");
	    }
	    
	    // try to get the aglet name from the proxy
	    String agletName = "Invalid Aglet";
		try {
			agletName = (proxy == null ? "No Aglet" : proxy.getAgletClassName());
		} catch (InvalidAgletException ex) {
		    JOptionPane.showMessageDialog(this,bundle.getString("dialog.tahitidialog.error.proxyexception"),bundle.getString("dialog.tahitidialog.error.title"),JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("proxy"));
		    return bundle.getString("dialog.tahitidialog.error.proxy2name");
		}
		
		return agletName;
	}
	
	
	protected JButton addButton(String name, ActionListener actionListener, 
							   KeyListener keyListener) {
		JButton b = addButton(name);

		b.setActionCommand(name);
		b.addActionListener(actionListener);
		b.addKeyListener(keyListener);
		return b;
	}
	
	protected JButton addCloseButton(String name) {
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
		return _closeButton;
	}
	/*
	 * Alert Dialog
	 */
	public static TahitiDialog alert(JFrame f, String msg) {
		TahitiDialog dialog = null;

		try {
			final JFrame fFrame = f;
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

					d.getContentPane().add("Center", 
						  new MessagePanel(fMsg, JLabel.LEFT));
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
	    
        for(int i=0;a!=null && i<a.length;i++){
            TahitiDialog.alert(new JFrame(), a[0]).popupAtCenterOfScreen();
        }
        
        if(a==null){
            TahitiDialog.alert(new JFrame(), "Tahiti dialog test!").popupAtCenterOfScreen();
        }
	}
	/*
	 * 
	 */
	public static TahitiDialog message(JFrame f, String title, String msg) {
		TahitiDialog dialog = null;

		try {
			final JFrame fFrame = f;
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

					d.getContentPane().add("North", new Label(fTitle));
					d.getContentPane().add("Center", 
						  new MessagePanel(fMsg, JLabel.LEFT));
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
