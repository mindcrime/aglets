package com.ibm.aglets.tahiti;

import javax.swing.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.LinkedList;

import org.aglets.util.gui.*;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

// TODO cambiare la costruzione dei menu usando il jcomponent


/**
 * The Manu memoryBar of Tahiti, with all menus and their entries.
 */
public class TahitiMenuBar extends JMenuBar {
    
    /**
     * The base name for this class.
     */
    private String baseKey = this.getClass().getName();
    
    /**
     * A linked list of components that could be enabled/disabled depending on other
     * events within the GUI.
     */
    private LinkedList<JMenuItem> conditionalItems = null;
    
    
    
    /**
     * Default constructor, creates all menus and their entries.
     * @param listener the action listener for each menu entry
     */
    public TahitiMenuBar(ActionListener listener){
        super();
        this.conditionalItems = new LinkedList<JMenuItem>();
        
        // all the menus and submenus
        JMenu aglets = JComponentBuilder.createJMenu(this.baseKey + ".agletsMenu");
        JMenu creation = JComponentBuilder.createJMenu(this.baseKey + ".creationSubMenu");
        JMenu killing = JComponentBuilder.createJMenu(this.baseKey + ".killSubMenu");
        JMenu mobility = JComponentBuilder.createJMenu(this.baseKey + ".mobilitySubMenu");
        JMenu activation = JComponentBuilder.createJMenu(this.baseKey + ".activationSubMenu");
        JMenu misc = JComponentBuilder.createJMenu(this.baseKey + ".miscSubMenu");
        
        // the aglets (sub)menus
        JMenuItem create = JComponentBuilder.createJMenuItem(this.baseKey + ".createItem",
        	                                             GUICommandStrings.CREATE_AGLET_COMMAND, 
        	                                             listener);

        
        
        
        
        JMenuItem clone = JComponentBuilder.createJMenuItem(this.baseKey + ".cloneItem",
                					    GUICommandStrings.CLONE_AGLET_COMMAND, 
                                                            listener);
        this.conditionalItems.add(clone);
        
        
        JMenuItem dispatch = JComponentBuilder.createJMenuItem(this.baseKey + ".dispatchItem",
		    					       GUICommandStrings.DISPATCH_AGLET_COMMAND, 
                                                               listener);
        this.conditionalItems.add(dispatch);
        
        
        JMenuItem retract = JComponentBuilder.createJMenuItem(this.baseKey + ".retractItem",
		                                              GUICommandStrings.RETRACT_AGLET_COMMAND, 
                                                              listener);
        
            

        JMenuItem dispose = JComponentBuilder.createJMenuItem(this.baseKey + ".disposeItem",
                                                              GUICommandStrings.DISPOSE_AGLET_COMMAND, 
                                                              listener);
        this.conditionalItems.add(dispose);
        
        
        
        /*JMenuItem kill = JComponentBuilder.createJMenuItem(this.baseKey + ".killItem",
                                                           GUICommandStrings.KILL_AGLET_COMMAND, 
                                                           listener);
        this.conditionalItems.add(kill);
        */
        
        JMenuItem activate = JComponentBuilder.createJMenuItem(this.baseKey + ".activateItem",
                                                               GUICommandStrings.ACTIVATE_AGLET_COMMAND, 
                                                               listener);
        this.conditionalItems.add(activate);
        
        JMenuItem deactivate = JComponentBuilder.createJMenuItem(this.baseKey + ".deactivateItem",
                                                                 GUICommandStrings.DEACTIVATE_AGLET_COMMAND, 
                                                                 listener);
        this.conditionalItems.add(deactivate);
        
        
        JMenuItem exit = JComponentBuilder.createJMenuItem(this.baseKey + ".exitItem",
                                                           GUICommandStrings.EXIT_COMMAND, 
                                                           listener);
        
        JMenuItem reboot = JComponentBuilder.createJMenuItem(this.baseKey + ".rebootItem",
                                                             GUICommandStrings.REBOOT_COMMAND, 
                                                             listener);

        
        JMenuItem sleep = JComponentBuilder.createJMenuItem(this.baseKey + ".sleepItem",
                                                            GUICommandStrings.SLEEP_AGLET_COMMAND, 
                                                            listener);
        this.conditionalItems.add(sleep);
        
        JMenuItem info = JComponentBuilder.createJMenuItem(this.baseKey + ".infoItem",
                                                            GUICommandStrings.INFO_AGLET_COMMAND, 
                                                            listener);
        this.conditionalItems.add(info);
        
        JMenuItem dialog = JComponentBuilder.createJMenuItem(this.baseKey + ".dialogItem",
                                                             GUICommandStrings.MESSAGE_AGLET_COMMAND, 
                                                             listener);
        this.conditionalItems.add(dialog);

        
        
         // menu structure
        creation.add(create);
        creation.add(clone);
        killing.add(dispose);
        //killing.add(kill);
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
        JMenu tools = JComponentBuilder.createJMenu(this.baseKey + ".toolsMenu");
        JMenu mem = JComponentBuilder.createJMenu(this.baseKey + ".memorySubMenu");
        JMenu dbg = JComponentBuilder.createJMenu(this.baseKey + ".debugSubMenu");
        
        
        // the tools (sub)menus
        JMenuItem memory = JComponentBuilder.createJMenuItem(this.baseKey + ".memoryItem",
        		                                     GUICommandStrings.MEMORY_COMMAND, 
                                                             listener);
        JMenuItem gc = JComponentBuilder.createJMenuItem(this.baseKey + ".gcItem",
                                                         GUICommandStrings.GARBAGECOLLECTOR_COMMAND, 
                                                         listener);
        JMenuItem log = JComponentBuilder.createJMenuItem(this.baseKey + ".logItem",
                		                          GUICommandStrings.LOG_COMMAND, 
                                                          listener);
        JMenuItem console = JComponentBuilder.createJMenuItem(this.baseKey + ".consoleItem",
                                                              GUICommandStrings.CONSOLE_COMMAND, 
                                                              listener);
        
        JMenuItem ref = JComponentBuilder.createJMenuItem(this.baseKey + ".refItem",
                                                          GUICommandStrings.REF_COMMAND, 
                                                          listener);
        JMenuItem debug = JComponentBuilder.createJMenuItem(this.baseKey + ".debugItem",
                                                            GUICommandStrings.DEBUG_COMMAND, 
                                                            listener);
        JMenuItem threads = JComponentBuilder.createJMenuItem(this.baseKey + ".threadsItem",
                                                              GUICommandStrings.THREAD_COMMAND, 
                                                              listener);
        
        // menu structure
        //dbg.add(log);		// how to show the log produced from log4j??
        //dbg.addSeparator();
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
        JMenu prefs = JComponentBuilder.createJMenu(this.baseKey + ".prefsMenu");
        
        
        // the preferences (sub)menus
        JMenuItem netprefs = JComponentBuilder.createJMenuItem(this.baseKey + ".netprefsItem",
                                                               GUICommandStrings.NETPREFS_COMMAND, 
                                                               listener);
        JMenuItem genprefs = JComponentBuilder.createJMenuItem(this.baseKey + ".genprefsItem",
                                                               GUICommandStrings.GENPREFS_COMMAND, 
                                                               listener);
        JMenuItem serprefs = JComponentBuilder.createJMenuItem(this.baseKey + ".servprefsItem",
                                                               GUICommandStrings.SERVPREFS_COMMAND, 
                                                               listener);
        JMenuItem secprefs = JComponentBuilder.createJMenuItem(this.baseKey + ".secprefsItem",
                                                               GUICommandStrings.SECPREFS_COMMAND, 
                                                               listener);
        
        // the menu structure
        prefs.add(genprefs);
        prefs.add(secprefs);
        prefs.add(netprefs);
        prefs.add(serprefs);
        
        
        // the help menu
        JMenu help = JComponentBuilder.createJMenu(this.baseKey + ".helpMenu");
        
        // the help submenu
        JMenuItem about_tahiti = JComponentBuilder.createJMenuItem(this.baseKey + ".aboutItem",
                                                                   GUICommandStrings.ABOUT_COMMAND, 
                                                                   listener);
        JMenuItem about_aglets = JComponentBuilder.createJMenuItem(this.baseKey + ".creditsItem",
                				                   GUICommandStrings.CREDITS_COMMAND, 
                                                                   listener);
        JMenuItem web_page = JComponentBuilder.createJMenuItem(this.baseKey + ".webItem",
                                                               GUICommandStrings.WEB_COMMAND, 
                                                               listener);
        JMenuItem javadoc = JComponentBuilder.createJMenuItem(this.baseKey + ".docItem",
                                                              GUICommandStrings.DOC_COMMAND, 
                                                              listener);
        
        // the menu structure
        help.add(about_tahiti);
        //help.add(about_aglets);
        help.addSeparator();
        help.add(web_page);
        help.add(javadoc);
        
        // add the menus to the menubar
        this.add(aglets);
        this.add(tools);
        this.add(prefs);
        this.add(help);
        
        // disable components if I've got no one aglet
        this.enableConditionalItems(false);
    }
    
    
    /**
     * Enables or disables items in the menu that have been marked as conditionals.
     * @param enabled true if the items must be enabled, false otherwise
     */
    public final void enableConditionalItems(boolean enabled){
	if( this.conditionalItems == null  || this.conditionalItems.isEmpty())
	    return;
	
	Iterator iter = this.conditionalItems.iterator();
	while (iter != null && iter.hasNext()) {
	    JMenuItem item = (JMenuItem) iter.next();
	    item.setEnabled(enabled);
	    
	}
    }
}
