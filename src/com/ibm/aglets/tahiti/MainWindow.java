package com.ibm.aglets.tahiti;

/*
 * @(#)MainWindow.java
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
import javax.swing.*;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletInfo;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletID;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;

// import com.ibm.aglets.agletbox.AgletBox;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.net.URL;
import java.io.File;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.ibm.aglets.log.LoggerFactory;
import com.ibm.aglets.tahiti.utils.*;
import com.ibm.aglets.thread.AgletThreadPool;
import com.ibm.aglets.thread.DeliveryMessageThread;

import net.sourceforge.aglets.rolex.*;
import net.sourceforge.aglets.rolex.gui.*;

/**
 * The <tt>MainWindow</tt> represents the main window for the Tahiti aglet
 * viewer.
 * 
 * @version     2
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 * @author      Yoshiaki Mima
 * @author      Luca Ferrari
 */


public class MainWindow extends JFrame implements ItemListener, ActionListener {
	
    /* Load resources */
    static ResourceBundle bundle = null;
	static {
		bundle = ResourceBundle.getBundle("tahiti");
	} 
	
	/**
	 * The logger for the action of this window
	 */
	Logger logger = LoggerFactory.getLogger(MainWindow.class);

	/**
	 * The log window for this GUI
	 */
	LogWindow logWindow = new LogWindow();
	
	// 
	private Tahiti _tahiti = null;

	/**
	 * The JMenuBar of this window
	 */
	private TahitiMenuBar menuBar;
	
	/** 
	 * The JToolBar of this window.
	 */
	private TahitiToolBar toolBar;
	
	/**
	 * The tahiti status bar
	 */
	private TahitiStatusBar statusBar;
	
	// 
	private Vector _itemList = new Vector();
	Hashtable text = new Hashtable();

	// 
	JPanel _buttonPanel = new JPanel();
	AgentListPanel _agletList = new AgentListPanel();
	boolean shrink = false;

	/* display options */
	static final int ORDER_CREATIONTIME = 0;
	static final int ORDER_CLASSNAME = 1;

	// 
	private int viewOrder = ORDER_CREATIONTIME;
	private boolean isAscent = true;
	private boolean isCompact = false;


	/**
	 * A panel that will contain the list of agents.
	 */
	protected JPanel centerPanel;
	
	

	
	/**
	 * 
	 * Method to perform ation event handling.
	 * @param event the action event to deal with
	 *
	 */
	public void actionPerformed(ActionEvent event){
	    // get the current action command
	    String command = event.getActionCommand();

	    // if no command (should never happen) don't do anything
	    if(command == null || command.equals("")){
	        return;
	    }

	    
	    // a dialog window for several operations
	    TahitiDialog dialog = null;
	    
	    // the proxy correspondent to the selected item in the list
	    AgletProxy proxy = this.getSelectedProxy();
	    
	    // an array of the selected proxy, useful for some operations
        AgletProxy pp[] = new AgletProxy[1];
        pp[0] = proxy;
	    
	    
	    // creation of a new aglet
	    if(command.equals(TahitiCommandStrings.CREATE_COMMAND) ){
	        dialog = CreateAgletDialog.getInstance(MainWindow.this);
			dialog.popupAtCenterOfParent();
	    }
	    else
	     if(command.equals(TahitiCommandStrings.CLONE_COMMAND) && proxy!=null){
	         // clone an aglet
	        dialog = new CloneAgletDialog(MainWindow.this, proxy);
			dialog.popupAtCenterOfParent();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.DISPOSE_COMMAND) && proxy!=null){
	         // dispose an agent
             dialog = new DisposeAgletDialog(MainWindow.this, pp);
			 dialog.popupAtCenterOfParent();
	         
	     }
	     else
	     if(command.equals(TahitiCommandStrings.KILL_COMMAND) && proxy!=null){
	         // kill the aglet
	         dialog = new KillAgletDialog(MainWindow.this, pp);
			 dialog.popupAtCenterOfParent();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.AGLET_INFO_COMMAND) && proxy!=null){
	         // show info about the selected aglet
	         dialog = new PropertiesDialog(MainWindow.this,proxy);
	         dialog.popupAtCenterOfParent();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.REDUCE_WINDOW_COMMAND)){
	         // reduce the window appearance
	         this._agletList.setVisible(false);
	         //this.getContentPane().remove(this._agletList);
	         this.pack();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.ACTIVATE_COMMAND) && proxy!=null
	             && proxy.isValid() ){
	         // reactivate a sleeping agent
	         try{
		         // check if the aglet is really deactviated
		         if(proxy.isActive()){
		             // the aglet is active, there's a problem
		             JOptionPane.showMessageDialog(this,bundle.getString("dialog.activate.error.message"),bundle.getString("dialog.activate.error.title"),JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("activate"));
		             return;
		         }
		         
		         // reactivate the agent
		         proxy.activate();
		         
	         }catch(InvalidAgletException ex){
	             this.showException(ex);
	         }
	         catch(IOException ex){
	             this.showException(ex);
	         }
	         catch(AgletException ex){
	             this.showException(ex);
	         }
	     }
	     else
	     if(command.equals(TahitiCommandStrings.DEACTIVATE_COMMAND) && proxy!=null
	             && proxy.isValid() ){
	         // deactivate an agent
	         try{
		         // check if the aglet is really active
		         if(!proxy.isActive()){
		             // the aglet is not active, this is an error
		             JOptionPane.showMessageDialog(this,bundle.getString("dialog.deactivate.error.message"),bundle.getString("dialog.deactivate.error.title"),JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("deactivate"));
		             return;
		         }
		         
		         // deactivate the agent
				dialog = new DeactivateAgletDialog(MainWindow.this, proxy);
				dialog.popupAtCenterOfParent();

		         		         
	         }catch(InvalidAgletException ex){
	             this.showException(ex);
	         }
	     }
	     else
	     if(command.equals(TahitiCommandStrings.DISPATCH_COMMAND) && proxy!=null){
	         // dispatch the agent
			dialog = new DispatchAgletDialog(MainWindow.this, proxy);
			dialog.popupAtCenterOfParent();

	     }
	     else
	     if(command.equals(TahitiCommandStrings.RETRACT_COMMAND)){
	         // retract an agent
			dialog = RetractAgletDialog.getInstance(MainWindow.this);
			dialog.popupAtCenterOfParent();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.ENLARGE_WINDOW_COMMAND)){
	         // enlarge the window appearance
	         //this.getContentPane().add("Center",this._agletList);
	         this._agletList.setVisible(true);
	         this.pack();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.SECPREFS_COMMAND)){
	         // opens the policytool editor
	         JOptionPane.showMessageDialog(this,bundle.getString("dialog.tahitidialog.policytool"),bundle.getString("dialog.tahitidialog.externalprocess"),JOptionPane.INFORMATION_MESSAGE,IconRepository.getIcon("run"));
	         try{
	             Process policytool = Runtime.getRuntime().exec("policytool");
	             policytool.waitFor();
	         }catch(Exception ex){
	             this.showException(ex);
	         }
        
	         JOptionPane.showMessageDialog(this,bundle.getString("dialog.tahitidialog.reboot"),bundle.getString("dialog.tahitidialog.reboot.title"),JOptionPane.INFORMATION_MESSAGE,IconRepository.getIcon("warning"));
	     }
	     else
	     if(command.equals(TahitiCommandStrings.GENPREFS_COMMAND)){
	         // show general preferences dialog
	 		dialog = GeneralConfigDialog.getInstance(MainWindow.this);
			dialog.popupAtCenterOfParent();

	     }
	     else
	     if(command.equals(TahitiCommandStrings.JAVADOC_COMMAND)){
	         // show the javadoc documentation
	         File index = new File(System.getProperty("aglets.javadoc"));
	         DocumentationWindow window = new DocumentationWindow(400,400,index,bundle.getString("documentationwindow.title.javadoc"));
	     }
	     else
	     if(command.equals(TahitiCommandStrings.WEB_PAGE_COMMAND)){
	         try{
		         URL index = new URL("http://aglets.sourceforge.net");
		         DocumentationWindow window = new DocumentationWindow(400,400,index,bundle.getString("documentationwindow.title.web"));
	         }catch(Exception e){
	             this.showException(e);
	         }
	     }
	     else
	     if(command.equals(TahitiCommandStrings.MEMORY_COMMAND)){
	         // show the memory window
	        MemoryWindow w = new MemoryWindow(); 
	     }
	     else
	     if(command.equals(TahitiCommandStrings.LOG_COMMAND)){
	         // show the log command
	         this.logWindow.setVisible(true);
	     }
	     else
	     if(command.equals(TahitiCommandStrings.EXIT_COMMAND)){
	         // exit from the server
	         int response = JOptionPane.showConfirmDialog(this,bundle.getString("dialog.exit.message"),bundle.getString("dialog.exit.title"),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("exit"));
	         if(response == JOptionPane.OK_OPTION){
	             // exit from the server
	             System.out.println("exiting Aglets...");
	             this.shutdown();
	             System.out.print("done\n");
	         }
	     }
	     else
	     if(command.equals(TahitiCommandStrings.REBOOT_COMMAND)){
	         // reboot the server, ask for confirmation
	         int response = JOptionPane.showConfirmDialog(this,bundle.getString("dialog.reboot.message"),bundle.getString("dialog.reboot.title"),JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("reboot"));
	         if(response == JOptionPane.OK_OPTION){
	             // reboot
	             System.out.println("rebooting Aglets...");
	             this.reboot();
	         }
	     }
	     else
	     if(command.equals(TahitiCommandStrings.REF_COMMAND)){
	         // show the reference table
	         com.ibm.aglets.RemoteAgletRef.showRefTable(System.err);
	     }
	     else
	     if(command.equals(TahitiCommandStrings.CONSOLE_COMMAND)){
	         // show the java console
			if (com.ibm.awb.launcher.Agletsd.console != null) {
				com.ibm.awb.launcher.Agletsd.console.show();
			} 
	     }
	     else
	     if(command.equals(TahitiCommandStrings.THREADS_COMMAND)){
	         // show the running threads
	         showThreads();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.GC_COMMAND)){
	         // invoke the garbage collector
	         System.gc();
	         JOptionPane.showMessageDialog(this,bundle.getString("mainwindow.gc.message"),bundle.getString("mainwindow.gc.title"),JOptionPane.WARNING_MESSAGE,IconRepository.getIcon("gc"));
	     }
	     else
	     if(command.equals(TahitiCommandStrings.NETPREFS_COMMAND)){
	         // show the network preferences dialog window
	        dialog = NetworkConfigDialog.getInstance(MainWindow.this);
			dialog.popupAtCenterOfParent();
	     }
	     else
	     if(command.equals(TahitiCommandStrings.SERPREFS_COMMAND)){
	         // open the server preferences window
			dialog = ServerPrefsDialog.getInstance(MainWindow.this);
			dialog.popupAtCenterOfParent();
	     }
	     else
	     if( command.equals(TahitiCommandStrings.DIALOG_COMMAND) && proxy!=null){
	     	// send a dialog message to the agent
	     	/*try{
	     		proxy.sendMessage(new Message("dialog"));
	     	}catch(Exception e){
	     		this.showException(e);
	     	}
	     	*/
	    	 // use a glue thread to deliver the message, thus to don't be blocked from a sleeping agent
	    	 // or a delay in the message delivery
	    	 DeliveryMessageThread deliver =  AgletThreadPool.getInstance().getDeliveryMessageThread(proxy, new Message("dialog"));
	    	 deliver.deliverMessage();
	    	 
	    	 // please remember that the thread is automatically re-inserted in the pool
	     }
	     else
	     if( command.equals(TahitiCommandStrings.SLEEP_COMMAND) && proxy!=null){
	    	 // I must make the aglet sleeping
	    	 try{
		    	 String time = JOptionPane.showInputDialog(this, bundle.getString("mainwindow.sleep.message"), 
		    			 									bundle.getString("mainwindow.sleep.title"),
		    			 									JOptionPane.INFORMATION_MESSAGE
		    				 								);
		    	 int timeout = Integer.parseInt(time);
		    	 Aglet a = proxy.getAglet();
		    	 a.sleep(timeout);
	    	 }catch(Exception e){
	    		 System.err.println("Exception caught while making an agent sleeping!");
	    		 this.showException(e);
	    	 }
	    	 
	     }
	    
 
	}
	
	/**
	 * A method to show an exception in a dialog window and on the java console.
	 * The method uses a logger to register the error, then a dialog window is shown in
	 * order to inform the user about the error.
	 *@param ex the exception to show
	 *@since 2.1.0
	 */
	protected void showException(Exception ex){
	    if(ex==null){
	        return;
	    }
	    
	    // log the error
	    this.logger.error("Exception caught ["+ex.getClass()+"]<"+ex.getMessage()+">");
	    // show the user a dialog window
	    ex.printStackTrace(System.err);
	    JOptionPane.showMessageDialog(this,"Exception "+ex.getMessage(),"Exception",JOptionPane.ERROR_MESSAGE,IconRepository.getIcon("error"));
	}
	

	
	/**
	 * A method to create a new aglet thru the GUI. 
	 * @param codebase the codebase of the aglet class
	 * @param name the name of the agent class
	 * @param reload if true, the aglet is created clearing the cache of the classloader
	 * @since 2.1.0
	 */
	protected void createNewAglet(String codebase, String name, boolean reload){
	    // check parameters
	    if(name==null || name.equals("")){
	        return;
	    }
	    
	    // if the codebase is an http remove the tailing '/'
	    while(codebase.startsWith("http") && codebase.endsWith("/")){
	        codebase=codebase.substring(0,codebase.length()-1);
	    }
	    
	    // build an URL around the codebase
	    URL codebaseURL=null;
	    if(codebase!=null && codebase.equals("")==false){
	        try{
	            codebaseURL = new URL(codebase);
	        }catch(Exception ex){
	            // cannot create the aglet, show the error
	            this.showException(ex);
	            return;
	        }
	    }
	    
	    // should I clear the cache?
	    if(reload==true){
	        Tahiti.CONTEXT.clearCache(null);
	    }
	    
	    // load the agent
	    try{
	        Tahiti.CONTEXT.createAglet(codebaseURL,name,null);
	    }catch(Exception ex){
	        this.showException(ex);
	    }
	}
	
	/*
	 * Constructs the instance of the main window for the
	 * Tahiti Aglet viewer.
	 */
	MainWindow(Tahiti tahiti) {
		_tahiti = tahiti;

		TahitiItem.init();

	
		
		// set the layout of this window
		this.getContentPane().setLayout(new BorderLayout());
		
		// create the RoleXListener for the rolex gui
		RoleXListener rlistener = new RoleXListener(this);
		
		// create the menu bar
		this.menuBar = new TahitiMenuBar(this, rlistener);
		this.setJMenuBar(this.menuBar);
		
		
		// create the toolbar
		this.toolBar = new TahitiToolBar(this);
		this.getContentPane().add("North",this.toolBar);
		

		// add the agent list
		this._agletList.setColors(Color.WHITE,Color.BLACK);
		this._agletList.setSelectionColors(Color.BLACK,Color.WHITE);
		this._agletList.setLayout(new FlowLayout());	
		//	 construct the border
		TitledBorder border = new TitledBorder((AbstractBorder)new LineBorder(Color.BLUE,2));
		border.setTitle(bundle.getString("mainwindow.agentlist"));
		border.setTitleColor(Color.BLUE);
		border.setTitlePosition(TitledBorder.TOP);
		border.setTitleJustification(TitledBorder.RIGHT);
		this._agletList.setBorder(border);
		// set the dimension
		this._agletList.setListDimension(new Dimension(300,300));
		this.getContentPane().add("Center",_agletList);

		Resource tahiti_res = Resource.getResourceFor("tahiti");

		shrink = tahiti_res.getBoolean("tahiti.window.shrinked", false);
		if (shrink) {
			_agletList.setVisible(false);
		} 

		
		// add the status bar
		this.statusBar = new TahitiStatusBar("Tahiti viewer up and running");
		this.getContentPane().add("South",this.statusBar);
		
		

		

		updateGUIState();

		AgletRuntime runtime = AgletRuntime.getAgletRuntime();
		String hosting = runtime.getServerAddress();
		String ownerName = runtime.getOwnerName();

		if (ownerName == null) {
			ownerName = "NO USER";
		} 

		Resource aglets_res = Resource.getResourceFor("aglets");
		boolean bsecure = aglets_res.getBoolean("aglets.secure", true);
		Resource atp_res = Resource.getResourceFor("atp");
		boolean brunning = true;

		if (atp_res != null) {
			brunning = atp_res.getBoolean("atp.server", false);
		} 

		// set the title
		this.setTitle(bundle.getString("mainwindow.title")+"["+hosting+" ("+ownerName+")");
	
		// add a window closer
		this.addWindowListener(new WindowCloser(this));
		
		
		this.pack();
	}

	
	private void addListeners() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				TahitiDialog dialog = 
					ShutdownDialog.getInstance(MainWindow.this);

				dialog.popupAtCenterOfParent();
			} 
		});

		//_agletList.addItemListener(this);
		//_agletList.addActionListener(this);
		
	}
	
	/**
	 * Cloning an agent thru its proxy.
	 * @param proxy the proxy of the agent to be cloned
	 */
	public void cloneAglet(AgletProxy proxy) {
		// check params
	    if(proxy==null){
	        return;
	    }
	    
	    // clone the agent
	    try{
	        proxy.clone();
	    }catch(CloneNotSupportedException ex){
	        this.showException(ex);
	    }
	}
	
	/**
	 * A method to create the aglet from the GUI.
	 * @deprecated use #createNewAglet
	 * @param codebase
	 * @param name
	 * @param reload
	 */
	public void createAglet(String codebase, String name, boolean reload) {
		this.createNewAglet(codebase,name,reload);
	}
	
	/**
	 * Deactivates an agent thru its proxy.
	 * @param proxy tyhe proxy of the aglet to be deactivated
	 * @param time the time to deactivate the aglets.
	 */
	public void deactivateAglet(AgletProxy proxy, long time) {
	    try{
	        proxy.deactivate(time * 1000);
	    }catch(Exception ex){
	        this.showException(ex);
	    }
	}
	void dialog(AgletProxy proxy) {
		try {
			proxy.sendAsyncMessage(new Message("dialog"));
		} catch (InvalidAgletException ex) {
			setMessage(ex.getMessage());
		} 
	}
	
	/**
	 * A method to dispatch an agent to the specified destination.
	 * @param proxy the proxy of the agent to be dispatched
	 * @param dest the destination to which dispatch the agent to
	 */
	public void dispatchAglet(AgletProxy proxy, String dest) {
		try{
		    proxy.dispatch(new URL(dest));
		}catch(Exception ex){
		    this.showException(ex);
		}
	}
	
	
	/**
	 * A method to dispose a single aglet.
	 * @param proxy the proxy of the aglet to dispose
	 */
	public void disposeAglet(AgletProxy proxy) {
		if(proxy==null){
		    return;
		}
		
		try{
		    proxy.dispose();
		}catch(InvalidAgletException ex){
		    this.showException(ex);
		}
	}

	
	/**
	 * Retract aglets from an Aglet box
	 * @deprecated it does not do anything
	 */
	void getAglets() {	}


	private String getItemText(TahitiItem tahitiItem) {
		StringBuffer buffer = new StringBuffer();

		buffer.append(tahitiItem.getText());
		AgletProxy proxy = tahitiItem.getAgletProxy();
		String s = (String)text.get(proxy);

		buffer.append(" " + (s == null ? " " : s));

		return buffer.toString();
	}
	public Dimension getPreferredSize() {
		Resource res = Resource.getResourceFor("tahiti");

		if (!shrink) {
			return new Dimension(res.getInteger("tahiti.window.width", 545), 
								 res.getInteger("tahiti.window.height", 350));
		} else {
			return new Dimension(res.getInteger("tahiti.window.s_width", 545), 
								 res.getInteger("tahiti.window.s_height", 
												350));
		} 
	}
	AgletProxy[] getSelectedProxies() {
		int selected[] = _agletList.getSelectedIndexes();
		AgletProxy p[] = new AgletProxy[selected.length];

		for (int i = 0; i < p.length; i++) {
			p[i] = 
				((TahitiItem)_itemList.elementAt(selected[i]))
					.getAgletProxy();
		} 
		return p;


	}
	
	/**
	 * Get a proxy related to the selected item in the agent list.
	 * @return the proxy of the agent
	 */
	public AgletProxy getSelectedProxy() {
		int selected = _agletList.getSelectedIndex();
		
		if (selected != -1) {
			return ((TahitiItem)_itemList.elementAt(selected))
				.getAgletProxy();
		} else {
			return null;
		} 
	}
	
	
	void hideButtons() {
	    this.menuBar.setVisible(false);
	}

	synchronized void insertProxyToList(AgletProxy proxy) {

		if (shrink) {
			return;
		} 

		TahitiItem tahitiItem = new TahitiItem(proxy);

		int index = -1;

		for (int i = 0; i < _itemList.size(); i++) {
			if (tahitiItem.compareTo((TahitiItem)_itemList.elementAt(i)) 
					<= 0) {
				index = i;
				break;
			} 
		} 

		if (index >= 0) {
			_itemList.insertElementAt(tahitiItem, index);
			_agletList.addItem(getItemText(tahitiItem), index);
		} else {
			_itemList.addElement(tahitiItem);
			_agletList.addItem(getItemText(tahitiItem));
		} 

	}
	public void itemStateChanged(ItemEvent ev) {
		updateGUIState();
		if (TahitiItem.isNeedUpdate()) {
			updateProxyList();
		} 
	}

	public void reboot() {
		_tahiti.reboot();
	}
	synchronized void removeProxyFromList(AgletProxy proxy) {
		AgletProxy p = null;

		try {
			text.remove(proxy);
			if (shrink) {
				return;
			} 

			for (int i = _itemList.size() - 1; i >= 0; i--) {
				p = ((TahitiItem)_itemList.elementAt(i)).getAgletProxy();
				if (p.isValid() == false) {
					_itemList.removeElementAt(i);
					_agletList.removeItem(i);
				} 
			} 

			updateGUIState();

			/*
			 * } else {
			 * System.out.println("Not Found!: " + proxy.getAgletInfo());
			 * }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		} 
	}
	public void restoreSize() {
		Resource res = Resource.getResourceFor("tahiti");

		setSize(res.getInteger("tahiti.window.width", 100), 
				res.getInteger("tahiti.window.height", 100));
	}
	
	/**
	 * A method to retract an agent thru its proxy
	 * @param proxy the proxy of the remote agent
	 */
	public void retractAglet(AgletProxy proxy) {
	    try{
			AgletInfo info = proxy.getAgletInfo();
			Tahiti.CONTEXT.retractAglet(new URL(proxy.getAddress()), 	info.getAgletID());
	    }catch(Exception ex){
	        this.showException(ex);
	    }


	}

	void saveSize() {
		java.awt.Rectangle bounds = getBounds();
		Resource res = Resource.getResourceFor("tahiti");

		res.setResource("tahiti.window.x", String.valueOf(bounds.x));
		res.setResource("tahiti.window.y", String.valueOf(bounds.y));
		if (!shrink) {
			res.setResource("tahiti.window.width", 
							String.valueOf(bounds.width));
			res.setResource("tahiti.window.height", 
							String.valueOf(bounds.height));
			res.setResource("tahiti.window.shrinked", "false");
		} else {
			res.setResource("tahiti.window.shrinked", "true");
			res.setResource("tahiti.window.s_width", 
							String.valueOf(bounds.width));
			res.setResource("tahiti.window.s_height", 
							String.valueOf(bounds.height));
		} 
	}
	public void setFont(Font f) {
		MenuBar menubar = getMenuBar();

		if (menubar != null) {
			menubar.setFont(f);

			int c = menubar.getMenuCount();

			for (int i = 0; i < c; i++) {
				Menu m = menubar.getMenu(i);

				m.setFont(f);
			} 
		} 
		super.setFont(f);
		doLayout();
	}

	
	
/**
 * Update the status bar
 * @param message the text to display
 * @deprecated use #setStatusBarText
 */
	void setMessage(String message) {
		this.statusBar.setText(message);
	}
	
	
	/**
	 * A method to display a message in the status bar.
	 * @param msg the message to display
	 */
	public void setStatusBarText(String msg){
	    this.statusBar.setText(msg);
	}
	
	void showButtons() {
		this.menuBar.setVisible(true);
	}
	
	
	/**
	 * A method to print to STDERR the thread status.
	 * @param g the group of thread to print
	 * @param level the indent factor
	 */
	static private void showThreadGroup(ThreadGroup g, int level) {
		int i;
		String indent = "                                 ".substring(0,	level);

		// print the group name
		System.err.println(indent + "{" + g.toString() + "}");

		// the number of active threads
		int n = g.activeCount();

		if (n > 0) {
			System.err.println(indent + " + Threads");

			// get all threads in the group
			Thread t[] = new Thread[g.activeCount()];
			g.enumerate(t);
			
			// iterate on each thread and print it
			for (i = 0; i < t.length; i++) {
				if (t[i]!=null && g == t[i].getThreadGroup()) {
					System.err.println(indent + "  - " + t[i].toString()+ (t[i].isAlive() ? " alive" : " dead"));
				} 
			} 
			
		} 

		// similarly for thread groups
		n = g.activeGroupCount();
		if (n > 0) {
			System.err.println(indent + " + ThreadGroups");

			// get all groups
			ThreadGroup tg[] = new ThreadGroup[n];
			g.enumerate(tg);
			
			// iterate and print
			for (i = 0; i < tg.length; i++) {
				if (tg[i]!=null && g == tg[i].getParent()) {
					showThreadGroup(tg[i], level + 4);
				} 
			} 
		} 
	}
	
	
	/**
	 * A method to print a dump of the running threads.
	 */
	static void showThreads() {
		ThreadGroup g = null;

		for (g = Thread.currentThread().getThreadGroup(); 
				g.getParent() != null; g = g.getParent()) {}

		showThreadGroup(g, 0);
	}
	// -------------------------------------------------------------------
	// -- Window updating

	void showURL(String url) {
		Resource res = Resource.getResourceFor("tahiti");
		String command = res.getString("tahiti.browser_command", null);

		if (command != null) {
			try {
				StringTokenizer st = new StringTokenizer(command);
				final String cmdarray[] = new String[st.countTokens() + 1];
				int count = 0;

				while (st.hasMoreTokens()) {
					cmdarray[count++] = st.nextToken();
				} 
				cmdarray[count] = url;
				AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws IOException {
						Runtime.getRuntime().exec(cmdarray);
						return null;
					} 
				});
			} catch (Exception ex) {
				ex.printStackTrace();
			} 
		} 
	}
	/*
	 * Shows the given dialog at the center
	 */
	private void showWindow(Window window) {}
	public void shutdown() {
		_tahiti.exit();
	}
	
	/**
	 * Repaint the window and make the status coherent.
	 * @deprecated
	 *
	 */
	void updateGUIState() {
	    this.repaint();
	}
	
	public synchronized void updateProxyList() {

		// return all
		_agletList.removeAllItems();
		_itemList.setSize(0);

		// System.out.println("updateProxyList()");

		Enumeration e = Tahiti.CONTEXT.getAgletProxies();

		while (e.hasMoreElements()) {
			insertProxyToList((AgletProxy)e.nextElement());
		} 
	}
	

}
