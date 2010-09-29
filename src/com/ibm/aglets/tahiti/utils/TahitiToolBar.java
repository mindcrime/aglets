/*
 * Created on Oct 3, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * The Tahiti toolbar.
 */
public class TahitiToolBar extends JToolBar implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -2865556110361052045L;
    // The resource bundle component
    static ResourceBundle bundle = null;
    // loading resources from the bundle
    static {
	bundle = ResourceBundle.getBundle("tahiti");
    }

    /**
     * The reduce/enlarge window button.
     */
    private JButton shrink;

    /**
     * Creates the toolbar, placing buttons and labels accordingly to the
     * resource bundle.
     * 
     * @param listener
     *            the action listener to associate to each button of the toolbar
     */
    public TahitiToolBar(ActionListener listener) {
	super("Tahiti ToolBar", SwingConstants.HORIZONTAL);

	// creates all the buttons

	this.shrink = new JButton("", IconRepository.getIcon("reduce_window"));
	this.shrink.setActionCommand(TahitiCommandStrings.REDUCE_WINDOW_COMMAND);
	this.shrink.addActionListener(listener);
	this.shrink.addActionListener(this);
	this.shrink.setToolTipText(bundle.getString("button.reduce.tooltip"));
	this.add(this.shrink);

	JButton create = new JButton(bundle.getString("button.create"), IconRepository.getIcon("create"));
	create.setActionCommand(TahitiCommandStrings.CREATE_COMMAND);
	create.addActionListener(listener);
	create.setToolTipText(bundle.getString("button.create.tooltip"));
	this.add(create);

	JButton clone = new JButton(bundle.getString("button.clone"), IconRepository.getIcon("clone"));
	clone.setActionCommand(TahitiCommandStrings.CLONE_COMMAND);
	clone.addActionListener(listener);
	clone.setToolTipText(bundle.getString("button.clone.tooltip"));
	this.add(clone);

	JButton dispose = new JButton(bundle.getString("button.dispose"), IconRepository.getIcon("dispose"));
	dispose.setActionCommand(TahitiCommandStrings.DISPOSE_COMMAND);
	dispose.addActionListener(listener);
	dispose.setToolTipText(bundle.getString("button.dispose.tooltip"));
	this.add(dispose);

	this.addSeparator();

	JButton dispatch = new JButton(bundle.getString("button.dispatch"), IconRepository.getIcon("dispatch"));
	dispatch.setActionCommand(TahitiCommandStrings.DISPATCH_COMMAND);
	dispatch.addActionListener(listener);
	dispatch.setToolTipText(bundle.getString("button.dispatch.tooltip"));
	this.add(dispatch);

	JButton retract = new JButton(bundle.getString("button.retract"), IconRepository.getIcon("retract"));
	retract.setActionCommand(TahitiCommandStrings.DISPATCH_COMMAND);
	retract.addActionListener(listener);
	retract.setToolTipText(bundle.getString("button.retract.tooltip"));
	this.add(retract);

	this.addSeparator();

	JButton activate = new JButton(bundle.getString("button.activate"), IconRepository.getIcon("activate"));
	activate.setActionCommand(TahitiCommandStrings.ACTIVATE_COMMAND);
	activate.addActionListener(listener);
	activate.setToolTipText(bundle.getString("button.activate.tooltip"));
	this.add(activate);

	JButton deactivate = new JButton(bundle.getString("button.deactivate"), IconRepository.getIcon("deactivate"));
	deactivate.setActionCommand(TahitiCommandStrings.DEACTIVATE_COMMAND);
	deactivate.addActionListener(listener);
	deactivate.setToolTipText(bundle.getString("button.deactivate.tooltip"));
	this.add(deactivate);

	JButton sleep = new JButton(bundle.getString("button.sleep"), IconRepository.getIcon("sleep"));
	sleep.setActionCommand(TahitiCommandStrings.SLEEP_COMMAND);
	sleep.addActionListener(listener);
	sleep.setToolTipText(bundle.getString("button.sleep.tooltip"));
	this.add(sleep);

	this.addSeparator();

	JButton dialog = new JButton(bundle.getString("button.dialog"), IconRepository.getIcon("dialog"));
	dialog.setActionCommand(TahitiCommandStrings.DIALOG_COMMAND);
	dialog.addActionListener(listener);
	dialog.setToolTipText(bundle.getString("button.dialog.tooltip"));
	this.add(dialog);

	JButton info = new JButton(bundle.getString("button.info"), IconRepository.getIcon("info"));
	info.setActionCommand(TahitiCommandStrings.AGLET_INFO_COMMAND);
	info.addActionListener(listener);
	info.setToolTipText(bundle.getString("button.info.tooltip"));
	this.add(info);

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
		&& command.equals(TahitiCommandStrings.REDUCE_WINDOW_COMMAND)) {
	    this.shrink.setIcon(IconRepository.getIcon("enlarge_window"));
	    this.shrink.setActionCommand(TahitiCommandStrings.ENLARGE_WINDOW_COMMAND);
	    this.shrink.setToolTipText(bundle.getString("button.enlarge.tooltip"));
	} else if ((command != null)
		&& command.equals(TahitiCommandStrings.ENLARGE_WINDOW_COMMAND)) {
	    this.shrink.setIcon(IconRepository.getIcon("reduce_window"));
	    this.shrink.setActionCommand(TahitiCommandStrings.REDUCE_WINDOW_COMMAND);
	    this.shrink.setToolTipText(bundle.getString("button.reduce.tooltip"));
	}
    }
}
