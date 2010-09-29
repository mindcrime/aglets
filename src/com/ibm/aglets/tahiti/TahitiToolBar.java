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

    private String baseKey = this.getClass().getName();

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
    public TahitiToolBar(ActionListener listener) {
	super("Tahiti ToolBar", SwingConstants.HORIZONTAL);
	this.conditionalButtons = new LinkedList<JButton>();

	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

	// creates all the buttons
	this.shrink = JComponentBuilder.createJButton(this.baseKey + ".shrink", GUICommandStrings.REDUCE_COMMAND, this);
	if (listener != null)
	    this.shrink.addActionListener(listener);

	this.add(this.shrink);
	this.addSeparator();

	this.create = JComponentBuilder.createJButton(this.baseKey + ".create", GUICommandStrings.CREATE_AGLET_COMMAND, listener);
	this.add(this.create);

	this.clone = JComponentBuilder.createJButton(this.baseKey + ".clone", GUICommandStrings.CLONE_AGLET_COMMAND, listener);
	this.add(this.clone);
	this.conditionalButtons.add(this.clone);

	this.dispose = JComponentBuilder.createJButton(this.baseKey
		+ ".dispose", GUICommandStrings.DISPOSE_AGLET_COMMAND, listener);

	this.add(this.dispose);
	this.conditionalButtons.add(this.dispose);

	this.addSeparator();

	this.dispatch = JComponentBuilder.createJButton(this.baseKey
		+ ".dispatch", GUICommandStrings.DISPATCH_AGLET_COMMAND, listener);

	this.add(this.dispatch);
	this.conditionalButtons.add(this.dispatch);

	this.retract = JComponentBuilder.createJButton(this.baseKey
		+ ".retract", GUICommandStrings.RETRACT_AGLET_COMMAND, listener);
	this.add(this.retract);

	this.addSeparator();

	this.activate = JComponentBuilder.createJButton(this.baseKey
		+ ".activate", GUICommandStrings.ACTIVATE_AGLET_COMMAND, listener);
	this.add(this.activate);
	this.conditionalButtons.add(this.activate);

	this.deactivate = JComponentBuilder.createJButton(this.baseKey
		+ ".deactivate", GUICommandStrings.DEACTIVATE_AGLET_COMMAND, listener);
	this.add(this.deactivate);
	this.conditionalButtons.add(this.deactivate);

	this.sleep = JComponentBuilder.createJButton(this.baseKey + ".sleep", GUICommandStrings.SLEEP_AGLET_COMMAND, listener);
	this.add(this.sleep);
	this.conditionalButtons.add(this.sleep);

	this.addSeparator();

	this.dialog = JComponentBuilder.createJButton(this.baseKey + ".dialog", GUICommandStrings.MESSAGE_AGLET_COMMAND, listener);
	this.add(this.dialog);
	this.conditionalButtons.add(this.dialog);

	this.info = JComponentBuilder.createJButton(this.baseKey + ".info", GUICommandStrings.INFO_AGLET_COMMAND, listener);
	this.add(this.info);
	this.conditionalButtons.add(this.info);
	this.addSeparator();

	this.add(Box.createHorizontalGlue()); // fill the space
	ImagePanel logo = JComponentBuilder.createSmallLogoPanel();
	if (logo != null)
	    this.add(logo);

	// disable conditiona buttons
	this.enableConditionalButtons(false);
    }

    /**
     * Enables only some buttons that depends on the selection of an aglet.
     * 
     * @param enabled
     *            the enabling flag
     */
    public final void enableConditionalButtons(boolean enabled) {
	if ((this.conditionalButtons == null)
		|| this.conditionalButtons.isEmpty())
	    return;

	Iterator iter = this.conditionalButtons.iterator();
	while ((iter != null) && iter.hasNext()) {
	    JButton button = (JButton) iter.next();
	    button.setEnabled(enabled);

	}
    }

    /**
     * Manage events for the shrink button
     * 
     * @param event
     *            the event to manage
     */
    @Override
    public void actionPerformed(ActionEvent event) {
	String command = event.getActionCommand();

	if ((command != null)
		&& command.equals(GUICommandStrings.REDUCE_COMMAND)) {
	    String key = this.baseKey + ".shrink2";
	    String text = JComponentBuilder.getTranslator().translate(key);
	    this.shrink.setText(text);
	    this.shrink.setIcon(JComponentBuilder.getIcon(key));
	    this.shrink.setToolTipText(JComponentBuilder.getTooltipText(key));
	    this.shrink.setActionCommand(GUICommandStrings.ENLARGE_COMMAND);

	} else if ((command != null)
		&& command.equals(GUICommandStrings.ENLARGE_COMMAND)) {
	    String key = this.baseKey + ".shrink";
	    String text = JComponentBuilder.getTranslator().translate(key);
	    this.shrink.setText(text);
	    this.shrink.setIcon(JComponentBuilder.getIcon(key));
	    this.shrink.setToolTipText(JComponentBuilder.getTooltipText(key));
	    this.shrink.setActionCommand(GUICommandStrings.REDUCE_COMMAND);
	}
    }
}
