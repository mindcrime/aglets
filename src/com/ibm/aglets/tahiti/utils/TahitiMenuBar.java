/*
 * Created on Oct 3, 2004
 *
 * @author Luca Ferrari, <a href="mailto:cat4hire@users.sourceforge.net">cat4hire@users.sourceforge.net</a>
 */
package com.ibm.aglets.tahiti.utils;

import javax.swing.JMenuBar;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;

/**
 * The Manu bar of Tahiti, with all menus and their entries.
 */
public class TahitiMenuBar extends JMenuBar {
    // The resource bundle component
    static ResourceBundle bundle = null;
    // loading resources from the bundle
    static {
        bundle = ResourceBundle.getBundle("tahiti");
    }
    
    
    /**
     * Default constructor, creates all menus and their entries.
     * @param listener the action listener for each menu entry
     * @param RoleXListener the listener for the RoleX GUI 
     */
    public TahitiMenuBar(ActionListener listener, ActionListener RoleXListener){
        super();
        
        // all the menus and submenus
        JMenu aglets = new JMenu(bundle.getString("menu.aglets"));
        JMenu creation = new JMenu(bundle.getString("menu.creation"));
        JMenu killing = new JMenu(bundle.getString("menu.killing"));
        JMenu mobility = new JMenu(bundle.getString("menu.mobility"));
        JMenu activation = new JMenu(bundle.getString("menu.activation"));
        
        // the aglets (sub)menus
        JMenuItem create = new JMenuItem(bundle.getString("menuitem.create"),IconRepository.getIcon("create"));
        create.setActionCommand(TahitiCommandStrings.CREATE_COMMAND);
        create.setToolTipText(bundle.getString("menuitem.create.tooltip"));
        create.addActionListener(listener);
        JMenuItem clone = new JMenuItem(bundle.getString("menuitem.clone"),IconRepository.getIcon("clone"));
        clone.setActionCommand(TahitiCommandStrings.CLONE_COMMAND);
        clone.addActionListener(listener);
        clone.setToolTipText(bundle.getString("menuitem.clone.tooltip"));
        JMenuItem dispatch = new JMenuItem(bundle.getString("menuitem.dispatch"),IconRepository.getIcon("dispatch"));
        dispatch.setActionCommand(TahitiCommandStrings.DISPATCH_COMMAND);
        dispatch.addActionListener(listener);
        dispatch.setToolTipText(bundle.getString("menuitem.dispatch.tooltip"));
        JMenuItem retract = new JMenuItem(bundle.getString("menuitem.retract"),IconRepository.getIcon("retract"));
        retract.setActionCommand(TahitiCommandStrings.RETRACT_COMMAND);
        retract.addActionListener(listener);
        retract.setToolTipText(bundle.getString("menuitem.retract.tooltip"));
        JMenuItem dispose = new JMenuItem(bundle.getString("menuitem.dispose"),IconRepository.getIcon("dispose"));
        dispose.setActionCommand(TahitiCommandStrings.DISPOSE_COMMAND);
        dispose.addActionListener(listener);
        dispose.setToolTipText(bundle.getString("menuitem.dispose.tooltip"));
        JMenuItem kill = new JMenuItem(bundle.getString("menuitem.kill"),IconRepository.getIcon("kill"));
        kill.setActionCommand(TahitiCommandStrings.KILL_COMMAND);
        kill.addActionListener(listener);
        JMenuItem activate = new JMenuItem(bundle.getString("menuitem.activate"),IconRepository.getIcon("activate"));
        activate.setActionCommand(TahitiCommandStrings.ACTIVATE_COMMAND);
        activate.addActionListener(listener);
        activate.setToolTipText(bundle.getString("menuitem.activate.tooltip"));
        JMenuItem deactivate = new JMenuItem(bundle.getString("menuitem.deactivate"),IconRepository.getIcon("deactivate"));
        deactivate.setActionCommand(TahitiCommandStrings.DEACTIVATE_COMMAND);
        deactivate.addActionListener(listener);
        deactivate.setToolTipText(bundle.getString("menuitem.deactivate.tooltip"));
        JMenuItem exit = new JMenuItem(bundle.getString("menuitem.exit"),IconRepository.getIcon("exit"));
        exit.setActionCommand(TahitiCommandStrings.EXIT_COMMAND);
        exit.addActionListener(listener);
        exit.setToolTipText(bundle.getString("menuitem.exit.tooltip"));
        JMenuItem reboot = new JMenuItem(bundle.getString("menuitem.reboot"),IconRepository.getIcon("reboot"));
        reboot.setActionCommand(TahitiCommandStrings.REBOOT_COMMAND);
        reboot.addActionListener(listener);
        reboot.setToolTipText(bundle.getString("menuitem.reboot.tooltip"));

        
        
         // menu structure
        creation.add(create);
        creation.add(clone);
        killing.add(dispose);
        killing.add(kill);
        mobility.add(dispatch);
        mobility.add(retract);
        activation.add(activate);
        activation.add(deactivate);
        
        aglets.add(creation);
        aglets.add(activation);
        aglets.add(mobility);
        aglets.addSeparator();
        aglets.add(killing);
        aglets.addSeparator();
        aglets.add(reboot);
        aglets.add(exit);
        
        
        // the tools menu
        JMenu tools = new JMenu(bundle.getString("menu.tools"));
        JMenu mem = new JMenu(bundle.getString("menu.memorymanagement"));
        JMenu dbg = new JMenu(bundle.getString("menu.debug"));
        
        
        // the tools (sub)menus
        JMenuItem memory = new JMenuItem(bundle.getString("menuitem.memory"),IconRepository.getIcon("memory"));
        memory.setActionCommand(TahitiCommandStrings.MEMORY_COMMAND);
        memory.addActionListener(listener);
        memory.setToolTipText(bundle.getString("menuitem.memory.tooltip"));
        JMenuItem gc = new JMenuItem(bundle.getString("menuitem.gc"),IconRepository.getIcon("gc"));
        gc.setActionCommand(TahitiCommandStrings.GC_COMMAND);
        gc.addActionListener(listener);
        gc.setToolTipText(bundle.getString("menuitem.gc.tooltip"));
        JMenuItem log = new JMenuItem(bundle.getString("menuitem.log"),IconRepository.getIcon("log"));
        log.setActionCommand(TahitiCommandStrings.LOG_COMMAND);
        log.addActionListener(listener);
        log.setToolTipText(bundle.getString("menuitem.log.tooltip"));
        JMenuItem console = new JMenuItem(bundle.getString("menuitem.console"),IconRepository.getIcon("console"));
        console.setActionCommand(TahitiCommandStrings.CONSOLE_COMMAND);
        console.addActionListener(listener);
        console.setToolTipText(bundle.getString("menuitem.console.tooltip"));
        JMenuItem ref = new JMenuItem(bundle.getString("menuitem.ref"),IconRepository.getIcon("ref"));
        ref.setActionCommand(TahitiCommandStrings.REF_COMMAND);
        ref.addActionListener(listener);
        ref.setToolTipText(bundle.getString("menuitem.ref.tooltip"));
        JMenuItem debug = new JMenuItem(bundle.getString("menuitem.debug"),IconRepository.getIcon("debug"));
        debug.setActionCommand(TahitiCommandStrings.DEBUG_COMMAND);
        debug.addActionListener(listener);
        debug.setToolTipText(bundle.getString("menuitem.debug.tooltip"));
        JMenuItem threads = new JMenuItem(bundle.getString("menuitem.threads"),IconRepository.getIcon("threads"));
        threads.setActionCommand(TahitiCommandStrings.THREADS_COMMAND);
        threads.addActionListener(listener);
        threads.setToolTipText(bundle.getString("menuitem.threads.tooltip"));
        
        // menu structure
        dbg.add(log);
        dbg.addSeparator();
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
        JMenu prefs = new JMenu(bundle.getString("menu.preferences"));
        
        
        // the preferences (sub)menus
        JMenuItem netprefs = new JMenuItem(bundle.getString("menuitem.netprefs"),IconRepository.getIcon("netprefs"));
        netprefs.setActionCommand(TahitiCommandStrings.NETPREFS_COMMAND);
        netprefs.addActionListener(listener);
        netprefs.setToolTipText(bundle.getString("menuitem.netprefs.tooltip"));
        JMenuItem genprefs = new JMenuItem(bundle.getString("menuitem.genprefs"),IconRepository.getIcon("genprefs"));
        genprefs.setActionCommand(TahitiCommandStrings.GENPREFS_COMMAND);
        genprefs.addActionListener(listener);
        genprefs.setToolTipText(bundle.getString("menuitem.genprefs.tooltip"));
        JMenuItem serprefs = new JMenuItem(bundle.getString("menuitem.serprefs"),IconRepository.getIcon("serprefs"));
        serprefs.setActionCommand(TahitiCommandStrings.SERPREFS_COMMAND);
        serprefs.addActionListener(listener);
        serprefs.setToolTipText(bundle.getString("menuitem.serprefs.tooltip"));
        JMenuItem secprefs = new JMenuItem(bundle.getString("menuitem.secprefs"),IconRepository.getIcon("secprefs"));
        secprefs.setActionCommand(TahitiCommandStrings.SECPREFS_COMMAND);
        secprefs.addActionListener(listener);
        secprefs.setToolTipText(bundle.getString("menuitem.secprefs.tooltip"));
        
        // the menu structure
        prefs.add(genprefs);
        prefs.add(secprefs);
        prefs.add(netprefs);
        prefs.add(serprefs);
        
        
        // the help menu
        JMenu help = new JMenu(bundle.getString("menu.help"));
        
        // the help submenu
        JMenuItem about_tahiti = new JMenuItem(bundle.getString("menuitem.about_tahiti"),IconRepository.getIcon("about_tahiti"));
        about_tahiti.setActionCommand(TahitiCommandStrings.ABOUT_TAHITI_COMMAND);
        about_tahiti.addActionListener(listener);
        about_tahiti.setToolTipText(bundle.getString("menuitem.about_tahiti.tooltip"));
        JMenuItem about_aglets = new JMenuItem(bundle.getString("menuitem.about_aglets"),IconRepository.getIcon("about_aglets"));
        about_aglets.setActionCommand(TahitiCommandStrings.ABOUT_AGLETS_COMMAND);
        about_aglets.addActionListener(listener);
        about_aglets.setToolTipText(bundle.getString("menuitem.about_aglets.tooltip"));
        JMenuItem web_page = new JMenuItem(bundle.getString("menuitem.web_page"),IconRepository.getIcon("web_page"));
        web_page.setActionCommand(TahitiCommandStrings.WEB_PAGE_COMMAND);
        web_page.addActionListener(listener);
        web_page.setToolTipText(bundle.getString("menuitem.web_page.tooltip"));
        JMenuItem javadoc = new JMenuItem(bundle.getString("menuitem.javadoc"),IconRepository.getIcon("javadoc"));
        javadoc.setActionCommand(TahitiCommandStrings.JAVADOC_COMMAND);
        javadoc.addActionListener(listener);
        javadoc.setToolTipText(bundle.getString("menuitem.javadoc.tooltip"));
        
        // the menu structure
        help.add(about_tahiti);
        help.add(about_aglets);
        help.addSeparator();
        help.add(web_page);
        help.add(javadoc);
        
        // add the menus to the menubar
        this.add(aglets);
        this.add(tools);
        this.add(prefs);
        this.add(help);
        
    }
    
}
