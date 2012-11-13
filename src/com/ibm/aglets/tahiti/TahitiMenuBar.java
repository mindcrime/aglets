package com.ibm.aglets.tahiti;

import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.sourceforge.aglets.util.gui.GUICommandStrings;
import net.sourceforge.aglets.util.gui.JComponentBuilder;

// TODO cambiare la costruzione dei menu usando il jcomponent

/**
 * The Manu memoryBar of Tahiti, with all menus and their entries.
 */
public class TahitiMenuBar extends JMenuBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 624948583728651720L;

	/**
	 * The base name for this class.
	 */
	private final String baseKey = this.getClass().getName();

	/**
	 * A linked list of components that could be enabled/disabled depending on
	 * other events within the GUI.
	 */
	private LinkedList<JMenuItem> conditionalItems = null;

	/**
	 * Default constructor, creates all menus and their entries.
	 * 
	 * @param listener
	 *            the action listener for each menu entry
	 */
	public TahitiMenuBar(final ActionListener listener) {
		super();
		conditionalItems = new LinkedList<JMenuItem>();

		// all the menus and submenus
		final JMenu aglets = JComponentBuilder.createJMenu(baseKey
				+ ".agletsMenu");
		final JMenu creation = JComponentBuilder.createJMenu(baseKey
				+ ".creationSubMenu");
		final JMenu killing = JComponentBuilder.createJMenu(baseKey
				+ ".killSubMenu");
		final JMenu mobility = JComponentBuilder.createJMenu(baseKey
				+ ".mobilitySubMenu");
		final JMenu activation = JComponentBuilder.createJMenu(baseKey
				+ ".activationSubMenu");
		final JMenu misc = JComponentBuilder.createJMenu(baseKey
				+ ".miscSubMenu");

		// the aglets (sub)menus
		final JMenuItem create = JComponentBuilder.createJMenuItem(baseKey
				+ ".createItem", GUICommandStrings.CREATE_AGLET_COMMAND, listener);

		final JMenuItem clone = JComponentBuilder.createJMenuItem(baseKey
				+ ".cloneItem", GUICommandStrings.CLONE_AGLET_COMMAND, listener);
		conditionalItems.add(clone);

		final JMenuItem dispatch = JComponentBuilder.createJMenuItem(baseKey
				+ ".dispatchItem", GUICommandStrings.DISPATCH_AGLET_COMMAND, listener);
		conditionalItems.add(dispatch);

		final JMenuItem retract = JComponentBuilder.createJMenuItem(baseKey
				+ ".retractItem", GUICommandStrings.RETRACT_AGLET_COMMAND, listener);

		final JMenuItem dispose = JComponentBuilder.createJMenuItem(baseKey
				+ ".disposeItem", GUICommandStrings.DISPOSE_AGLET_COMMAND, listener);
		conditionalItems.add(dispose);

		/*
		 * JMenuItem kill = JComponentBuilder.createJMenuItem(this.baseKey +
		 * ".killItem", GUICommandStrings.KILL_AGLET_COMMAND, listener);
		 * this.conditionalItems.add(kill);
		 */

		final JMenuItem activate = JComponentBuilder.createJMenuItem(baseKey
				+ ".activateItem", GUICommandStrings.ACTIVATE_AGLET_COMMAND, listener);
		conditionalItems.add(activate);

		final JMenuItem deactivate = JComponentBuilder.createJMenuItem(baseKey
				+ ".deactivateItem", GUICommandStrings.DEACTIVATE_AGLET_COMMAND, listener);
		conditionalItems.add(deactivate);

		final JMenuItem exit = JComponentBuilder.createJMenuItem(baseKey
				+ ".exitItem", GUICommandStrings.EXIT_COMMAND, listener);

		final JMenuItem reboot = JComponentBuilder.createJMenuItem(baseKey
				+ ".rebootItem", GUICommandStrings.REBOOT_COMMAND, listener);

		final JMenuItem sleep = JComponentBuilder.createJMenuItem(baseKey
				+ ".sleepItem", GUICommandStrings.SLEEP_AGLET_COMMAND, listener);
		conditionalItems.add(sleep);

		final JMenuItem info = JComponentBuilder.createJMenuItem(baseKey
				+ ".infoItem", GUICommandStrings.INFO_AGLET_COMMAND, listener);
		conditionalItems.add(info);

		final JMenuItem dialog = JComponentBuilder.createJMenuItem(baseKey
				+ ".dialogItem", GUICommandStrings.MESSAGE_AGLET_COMMAND, listener);
		conditionalItems.add(dialog);

		// menu structure
		creation.add(create);
		creation.add(clone);
		killing.add(dispose);
		// killing.add(kill);
		mobility.add(dispatch);
		mobility.add(retract);
		activation.add(activate);
		activation.add(deactivate);

		misc.add(sleep);
		misc.addSeparator();
		misc.add(dialog);
		misc.add(info);

		aglets.add(creation);
		aglets.add(activation);
		aglets.add(mobility);
		aglets.addSeparator();
		aglets.add(killing);
		aglets.addSeparator();
		aglets.add(misc);
		aglets.addSeparator();
		aglets.add(reboot);
		aglets.add(exit);

		// the tools menu
		final JMenu tools = JComponentBuilder.createJMenu(baseKey + ".toolsMenu");
		final JMenu mem = JComponentBuilder.createJMenu(baseKey
				+ ".memorySubMenu");
		final JMenu dbg = JComponentBuilder.createJMenu(baseKey
				+ ".debugSubMenu");

		// the tools (sub)menus
		final JMenuItem memory = JComponentBuilder.createJMenuItem(baseKey
				+ ".memoryItem", GUICommandStrings.MEMORY_COMMAND, listener);
		final JMenuItem gc = JComponentBuilder.createJMenuItem(baseKey
				+ ".gcItem", GUICommandStrings.GARBAGECOLLECTOR_COMMAND, listener);
		JComponentBuilder.createJMenuItem(baseKey + ".logItem", GUICommandStrings.LOG_COMMAND, listener);
		final JMenuItem console = JComponentBuilder.createJMenuItem(baseKey
				+ ".consoleItem", GUICommandStrings.CONSOLE_COMMAND, listener);

		final JMenuItem ref = JComponentBuilder.createJMenuItem(baseKey
				+ ".refItem", GUICommandStrings.REF_COMMAND, listener);
		JComponentBuilder.createJMenuItem(baseKey + ".debugItem", GUICommandStrings.DEBUG_COMMAND, listener);
		final JMenuItem threads = JComponentBuilder.createJMenuItem(baseKey
				+ ".threadsItem", GUICommandStrings.THREAD_COMMAND, listener);

		// menu structure
		// dbg.add(log); // how to show the log produced from log4j??
		// dbg.addSeparator();
		dbg.add(threads);
		dbg.add(ref);
		dbg.addSeparator();
		dbg.add(console);
		mem.add(memory);
		mem.addSeparator();
		mem.add(gc);
		tools.add(mem);
		tools.add(dbg);

		// the preferences menu
		final JMenu prefs = JComponentBuilder.createJMenu(baseKey + ".prefsMenu");

		// the preferences (sub)menus
		final JMenuItem netprefs = JComponentBuilder.createJMenuItem(baseKey
				+ ".netprefsItem", GUICommandStrings.NETPREFS_COMMAND, listener);
		final JMenuItem genprefs = JComponentBuilder.createJMenuItem(baseKey
				+ ".genprefsItem", GUICommandStrings.GENPREFS_COMMAND, listener);
		final JMenuItem serprefs = JComponentBuilder.createJMenuItem(baseKey
				+ ".servprefsItem", GUICommandStrings.SERVPREFS_COMMAND, listener);
		final JMenuItem secprefs = JComponentBuilder.createJMenuItem(baseKey
				+ ".secprefsItem", GUICommandStrings.SECPREFS_COMMAND, listener);

		// the menu structure
		prefs.add(genprefs);
		prefs.add(secprefs);
		prefs.add(netprefs);
		prefs.add(serprefs);

		// the help menu
		final JMenu help = JComponentBuilder.createJMenu(baseKey + ".helpMenu");

		// the help submenu
		final JMenuItem about_tahiti = JComponentBuilder.createJMenuItem(baseKey
				+ ".aboutItem", GUICommandStrings.ABOUT_COMMAND, listener);
		JComponentBuilder.createJMenuItem(baseKey + ".creditsItem", GUICommandStrings.CREDITS_COMMAND, listener);
		final JMenuItem web_page = JComponentBuilder.createJMenuItem(baseKey
				+ ".webItem", GUICommandStrings.WEB_COMMAND, listener);
		final JMenuItem javadoc = JComponentBuilder.createJMenuItem(baseKey
				+ ".docItem", GUICommandStrings.DOC_COMMAND, listener);

		// the menu structure
		help.add(about_tahiti);
		// help.add(about_aglets);
		help.addSeparator();
		help.add(web_page);
		help.add(javadoc);

		// add the menus to the menubar
		this.add(aglets);
		this.add(tools);
		this.add(prefs);
		this.add(help);

		// disable components if I've got no one aglet
		enableConditionalItems(false);
	}

	/**
	 * Enables or disables items in the menu that have been marked as
	 * conditionals.
	 * 
	 * @param enabled
	 *            true if the items must be enabled, false otherwise
	 */
	public final void enableConditionalItems(final boolean enabled) {
		if ((conditionalItems == null) || conditionalItems.isEmpty())
			return;

		final Iterator iter = conditionalItems.iterator();
		while ((iter != null) && iter.hasNext()) {
			final JMenuItem item = (JMenuItem) iter.next();
			item.setEnabled(enabled);

		}
	}
}
