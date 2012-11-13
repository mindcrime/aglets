package com.ibm.aglets.tahiti;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.ImagePanel;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

public class TahitiToolBar extends JToolBar implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6327657485302108603L;

	private final String baseKey = this.getClass().getName();

	/**
	 * A button to reduce/enlarge the window size.
	 */
	protected JButton shrink = null;

	/**
	 * A button for creating a new aglet.
	 */
	protected JButton create = null;

	/**
	 * A button to clone an existing aglet,
	 */
	protected JButton clone = null;

	/**
	 * A button to dispose an aglet.
	 */
	protected JButton dispose = null;

	/**
	 * Buttons for dispatching/retracting an aglet
	 */
	protected JButton dispatch = null;
	protected JButton retract = null;

	/**
	 * Activatation and deactivation buttons.
	 */
	protected JButton activate = null;
	protected JButton deactivate = null;

	/**
	 * A button for sending a dialog message.
	 */
	protected JButton dialog = null;

	/**
	 * A button for getting info about the agent.
	 */
	protected JButton info = null;

	/**
	 * Let's an agent to sleep.
	 */
	protected JButton sleep = null;

	/**
	 * A list of conditional buttons, that can be enabled or disbaled depending
	 * on some other events in the GUI.
	 */
	private LinkedList<JButton> conditionalButtons = null;

	/**
	 * Creates the toolbar, placing buttons and labels accordingly to the
	 * resource bundle.
	 * 
	 * @param listener
	 *            the action listener to associate to each button of the toolbar
	 */
	public TahitiToolBar(final ActionListener listener) {
		super("Tahiti ToolBar", SwingConstants.HORIZONTAL);
		conditionalButtons = new LinkedList<JButton>();

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// creates all the buttons
		shrink = JComponentBuilder.createJButton(baseKey + ".shrink", GUICommandStrings.REDUCE_COMMAND, this);
		if (listener != null)
			shrink.addActionListener(listener);

		this.add(shrink);
		this.addSeparator();

		create = JComponentBuilder.createJButton(baseKey + ".create", GUICommandStrings.CREATE_AGLET_COMMAND, listener);
		this.add(create);

		clone = JComponentBuilder.createJButton(baseKey + ".clone", GUICommandStrings.CLONE_AGLET_COMMAND, listener);
		this.add(clone);
		conditionalButtons.add(clone);

		dispose = JComponentBuilder.createJButton(baseKey
				+ ".dispose", GUICommandStrings.DISPOSE_AGLET_COMMAND, listener);

		this.add(dispose);
		conditionalButtons.add(dispose);

		this.addSeparator();

		dispatch = JComponentBuilder.createJButton(baseKey
				+ ".dispatch", GUICommandStrings.DISPATCH_AGLET_COMMAND, listener);

		this.add(dispatch);
		conditionalButtons.add(dispatch);

		retract = JComponentBuilder.createJButton(baseKey
				+ ".retract", GUICommandStrings.RETRACT_AGLET_COMMAND, listener);
		this.add(retract);

		this.addSeparator();

		activate = JComponentBuilder.createJButton(baseKey
				+ ".activate", GUICommandStrings.ACTIVATE_AGLET_COMMAND, listener);
		this.add(activate);
		conditionalButtons.add(activate);

		deactivate = JComponentBuilder.createJButton(baseKey
				+ ".deactivate", GUICommandStrings.DEACTIVATE_AGLET_COMMAND, listener);
		this.add(deactivate);
		conditionalButtons.add(deactivate);

		sleep = JComponentBuilder.createJButton(baseKey + ".sleep", GUICommandStrings.SLEEP_AGLET_COMMAND, listener);
		this.add(sleep);
		conditionalButtons.add(sleep);

		this.addSeparator();

		dialog = JComponentBuilder.createJButton(baseKey + ".dialog", GUICommandStrings.MESSAGE_AGLET_COMMAND, listener);
		this.add(dialog);
		conditionalButtons.add(dialog);

		info = JComponentBuilder.createJButton(baseKey + ".info", GUICommandStrings.INFO_AGLET_COMMAND, listener);
		this.add(info);
		conditionalButtons.add(info);
		this.addSeparator();

		this.add(Box.createHorizontalGlue()); // fill the space
		final ImagePanel logo = JComponentBuilder.createSmallLogoPanel();
		if (logo != null)
			this.add(logo);

		// disable conditiona buttons
		enableConditionalButtons(false);
	}

	/**
	 * Manage events for the shrink button
	 * 
	 * @param event
	 *            the event to manage
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();

		if ((command != null)
				&& command.equals(GUICommandStrings.REDUCE_COMMAND)) {
			final String key = baseKey + ".shrink2";
			final String text = JComponentBuilder.getTranslator().translate(key);
			shrink.setText(text);
			shrink.setIcon(JComponentBuilder.getIcon(key));
			shrink.setToolTipText(JComponentBuilder.getTooltipText(key));
			shrink.setActionCommand(GUICommandStrings.ENLARGE_COMMAND);

		} else if ((command != null)
				&& command.equals(GUICommandStrings.ENLARGE_COMMAND)) {
			final String key = baseKey + ".shrink";
			final String text = JComponentBuilder.getTranslator().translate(key);
			shrink.setText(text);
			shrink.setIcon(JComponentBuilder.getIcon(key));
			shrink.setToolTipText(JComponentBuilder.getTooltipText(key));
			shrink.setActionCommand(GUICommandStrings.REDUCE_COMMAND);
		}
	}

	/**
	 * Enables only some buttons that depends on the selection of an aglet.
	 * 
	 * @param enabled
	 *            the enabling flag
	 */
	public final void enableConditionalButtons(final boolean enabled) {
		if ((conditionalButtons == null)
				|| conditionalButtons.isEmpty())
			return;

		final Iterator iter = conditionalButtons.iterator();
		while ((iter != null) && iter.hasNext()) {
			final JButton button = (JButton) iter.next();
			button.setEnabled(enabled);

		}
	}
}
