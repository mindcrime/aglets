package examples.patterns;

/*
 * @(#)GeneralDialog.java
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
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Dimension;

/**
 * The <tt>GeneralDialog</tt> class is a common and generic dialog to display
 * the messages.
 * 
 * @version     1.00    96/07/16
 * @author      M.Oshima
 */

abstract public class GeneralDialog extends Dialog {

	/*
	 * Button (and Event?) constants
	 */
	public static final int OKAY = 0x1;
	public static final int CANCEL = 0x1 << 2;
	public static final int APPLY = 0x1 << 3;
	public static final int HELP = 0x1 << 4;

	/*
	 * Location Constants
	 */
	public static final int ALWAYS_CENTER = 1;
	public static final int CENTER_ONLY_ONCE = 2;
	public static final int FREE = 3;

	/*
	 * Component that gets invoked when a button is pressed.
	 */
	private Component callback_component = null;

	/*
	 * Post the event. The posted event will be sent to
	 * the callback component if any.
	 * @param ev the event
	 */
	private Event newEvent = null;

	/*
	 * Optional argument
	 * @see java.awt.Event#arg
	 */
	private Object object = null;

	/*
	 * Buttons
	 */
	private Button okay = new Button("Okay");
	private Button cancel = new Button("Cancel");
	private Button apply = new Button("Apply");
	private Button help = new Button("Help");
	private Panel button_panel = new Panel();

	/*
	 * Bitfield that specifies the kinds of button.
	 */
	private int button_bits = OKAY | CANCEL | HELP;

	private boolean shown = false;
	Object lock = new Object();

	/*
	 * Constructs a general dialog
	 * @param parent the parent window
	 * @param callback_component the AWT component which will receive the
	 * callback event.
	 * @param title the title string
	 * @param object optional argument to the event
	 * @param modal the boolean to specify modality
	 */
	public GeneralDialog(Frame parent, Component callback_component, 
						 String title, Object object, boolean modal) {
		super(parent, title, modal);
		this.callback_component = callback_component;
		this.object = object;

		/*
		 * Tentative code to avoid layouting bugs.
		 */

		// if (parent != null && parent.getPeer() == null) {
		if (parent != null && parent.isDisplayable() == false) {
			parent.addNotify();
		} 

		// if (getPeer() == null) {
		if (isDisplayable() == false) {
			addNotify();
		} 
	}
	/**
	 * Adds extra button you like.
	 * 
	 * @param b the button to get added.
	 */
	public void addButton(Button b) {
		button_panel.add(b);
	}
	/*
	 * Adds component in accordance with the parameter.
	 * @param c a component to add.
	 * @param grid a layoutobject. Normally the object given in makePanel
	 * method is used.
	 * @param cns a constraint object.
	 */
	protected void addCmp(Component c, GridBagLayout grid, 
						  GridBagConstraints cns) {
		grid.setConstraints(c, cns);
		add(c);
	}
	/**
	 * Rings a bell. This is a tentative method and this should
	 * move to more common class for aglets.
	 */
	public void beep() {
		System.out.println("\007");
		System.out.flush();
	}
	/**
	 * Obtains dialog button
	 */
	public Button getButton(int b) {
		switch (b) {
		case OKAY:
			return okay;
		case CANCEL:
			return cancel;
		case HELP:
			return help;
		case APPLY:
			return apply;
		}
		return null;
	}
	/*
	 * get Title label
	 */
	public Label getTitleLabel() {
		Font win_font = new Font(getFont().getName(), Font.BOLD, 
								 getFont().getSize() + 4);

		Label l = new Label(getTitle(), Label.CENTER);

		l.setFont(win_font);
		return l;
	}
	/**
	 * Handles the events.
	 * 
	 * @param event
	 */
	public boolean handleEvent(Event event) {

		if (event.id == Event.ACTION_EVENT) {
			boolean b = super.handleEvent(event);

			if (b == false && my_action(event)) {
				return true;
			} 

			// If the event was handled correctly within this
			// object, don't forward the event!
			if (callback_component != null && callback_component != this) {

				event.arg = object;
				if (callback_component.handleEvent(event)) {
					return true;
				} 
			} 

			// If this object or callback_component posted
			// the new event, delegate it to the callback
			// component!
			if (newEvent != null && callback_component != this) {
				b = callback_component.handleEvent(newEvent);
			} 
			newEvent = null;
			return b;
		} else if (event.id == Event.WINDOW_DESTROY) {
			setVisible(false);
		} else {
			return super.handleEvent(event);
		} 
		return true;
	}
	/*
	 * layouts all components
	 */
	protected final void layoutComponents() {
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints cns = new GridBagConstraints();

		cns.gridwidth = GridBagConstraints.REMAINDER;
		cns.fill = GridBagConstraints.HORIZONTAL;
		cns.weightx = 1.0;
		cns.weighty = 0.0;

		setLayout(grid);

		Label l = getTitleLabel();

		l.setAlignment(Label.CENTER);
		addCmp(l, grid, cns);

		makePanel(grid);

		button_panel.setLayout(new java.awt.FlowLayout());
		button_panel.doLayout();
		addCmp(button_panel, grid, cns);
	}
	/*
	 * This is a template method to be implemented by a subclass.
	 * Subclass have to implement this method to define its own
	 * window.
	 * @param grid the layout object used to layout components in
	 * this class.
	 */
	abstract protected void makePanel(GridBagLayout grid);
	/*
	 * 
	 */
	final private boolean my_action(Event ev) {
		if (ev.target == okay) {
			return onOkay();
		} else if (ev.target == cancel) {
			return onCancel();
		} else if (ev.target == help) {
			return onHelp();
		} else if (ev.target == apply) {
			return onApply();
		} 
		return false;
	}
	protected boolean onApply() {
		return false;
	}
	protected boolean onCancel() {
		return false;
	}
	protected boolean onHelp() {
		return false;
	}
	/*
	 * 
	 */
	protected boolean onOkay() {
		return false;
	}
	public void pack() {
		if ((button_bits & OKAY) != 0x0) {
			button_panel.add(okay);
		} 
		if ((button_bits & CANCEL) != 0x0) {
			button_panel.add(cancel);
		} 
		if ((button_bits & APPLY) != 0x0) {
			button_panel.add(apply);
		} 
		if ((button_bits & HELP) != 0x0) {
			button_panel.add(help);
		} 
		button_panel.doLayout();
		super.pack();
	}
	/**
	 * Pops up the dialog window according to the location given as a parameter
	 * 
	 * @param location
	 */
	public void popup(int location) {
		if (shown == false) {
			pack();
			if (location == CENTER_ONLY_ONCE) {
				location = ALWAYS_CENTER;
			} 
		} 
		switch (location) {
		case ALWAYS_CENTER:
			Dimension d = getToolkit().getScreenSize();

			setLocation((d.width - getSize().width) / 2, 
						(d.height - getSize().height) / 2);
			break;
		case CENTER_ONLY_ONCE:
		case FREE:
			break;
		}
		show();
	}
	/**
	 * Pops up the dialog window so that it get located at the center
	 * of the frame. If frame is null, parent frame will be used.
	 * 
	 * @param frame
	 */
	public void popup(Frame frame) {
		if (frame == null) {
			frame = (Frame)getParent();		// not so good...
		} 
		if (shown == false) {
			pack();
		} 
		Rectangle b = frame.getBounds();

		setLocation(b.x + (b.width - getSize().width) / 2, 
					b.y + (b.height - getSize().height) / 2);
		show();
	}
	protected void postCallbackEvent(Event ev) {
		newEvent = ev;
	}
	/**
	 * Specifies which buttons should be appeared on the bottom of
	 * window.
	 * 
	 * @param b logical OR value of constants,
	 * OKAY, CANCEL, HELP, APPLY.
	 */
	public void setButtons(int b) {
		button_bits = b;
		if (isVisible()) {
			button_panel.removeAll();
			pack();
		} 
	}
	protected void setCallbackComponent(Component c) {
		callback_component = c;
	}
	/**
	 * Waits until the dialog window is actually shown.
	 */
	public void waitForDisplay() throws InterruptedException {

		// It's seems there is no way to write thread safe code
		// which runs on both JDK1.0.2 and 1.1...
		Thread t = Thread.currentThread();

		while (isVisible() == false) {
			t.yield();
			try {
				t.sleep(100);
			} catch (Exception ex) {}
		} 
		t.yield();
	}
	/**
	 * Waits until the dialog window is disposed.
	 */
	public void waitForDisposal() throws InterruptedException {

		// It's seems there is no way to write thread safe code
		// which runs on both JDK1.0.2 and 1.1...
		Thread t = Thread.currentThread();

		while (isVisible()) {
			t.yield();
			try {
				t.sleep(100);
			} catch (Exception ex) {}
		} 
	}
}
