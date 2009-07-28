package com.ibm.aglets.tahiti;

/*
 * $Id: TahitiDaemon.java,v 1.5 2009/07/28 07:04:53 cat4hire Exp $
 *
 * @(#)TahitiDaemon.java
 *
 * TahitiDaemon implements a Tahiti service which listens on
 * a configurable control port for commands.
 *
 * The TahitiDaemonClient class is used to communicate with
 * and control a TahitiDaemon.
 */

import com.ibm.aglet.*;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 *  <tt>Daemon</tt> is a simple non-UI interface to an aglet server.
 *  TahitiDaemon implements a Tahiti service which listens on a configurable
 *  control port for commands. The TahitiDaemonClient class is used to
 *  communicate with and control a TahitiDaemon.
 *
 * @author     Lary Spector
 * @created    July 22, 2001
 * @version    $Revision: 1.5 $ $Date: 2009/07/28 07:04:53 $ $Author: cat4hire $
 * @see        com.ibm.aglet.system.ContextListener
 * @see        com.ibm.aglet.system.ContextEvent
 */
public final class TahitiDaemon implements ContextListener, Runnable {

    /*
     * Aglets table
     */
    private Hashtable                   aglets = new Hashtable();

    /*
     * Prompt char.
     */
    private String                      prompt = ">";

    /*
     * AgletContext
     */
    private AgletContext                context = null;

    /*
     * Null print stream to turn off messages.
     * private com.ibm.awb.misc.LogPrintStream null_log;
     */
    private boolean                     message = false;
    private boolean                     debug = false;

    private boolean                     continue_processing = true;
    private boolean                     reboot = false;
    private boolean                     shutdown = false;

    private static int                  _control_port_num = 5545;

    private static UserManager          _userManager = new TahitiDaemonUserManager();

    /*
     * for autonumbering aglets
     */
    private static int                  serial = 0;
    /*
     * Banner and version Strings
     */
    private static String               _banner_string = "TahitiDaemon";
    private static String               _version_string = "1.0";


    /**
     *  Called when an aglet has been activated
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletActivated(ContextEvent event) {
        if (message) {
            System.out.println("Activated " + event.getAgletProxy());
        }
        // added(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has arived
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletArrived(ContextEvent event) {
        added(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been cloned
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletCloned(ContextEvent event) {
        added(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been created
     *
     * @param  event  an AgletEvent
     * @since
     */
    public void agletCreated(ContextEvent event) {
        added(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been deactivated
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletDeactivated(ContextEvent event) {
        if (message) {
            System.out.println("Deactivated " + event.getAgletProxy());
        }
        // removed(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been dispatched
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletDispatched(ContextEvent event) {

        // event.getAgletProxy() is the AgletProxy AFTER dispatching,
        // not BEFORE dispatching.
        // So it cannot be removed because aglets hashtable doesn't contain it.
        // removed(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been disposed
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletDisposed(ContextEvent event) {
        removed(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been resumed
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletResumed(ContextEvent event) {
        if (message) {
            System.out.println("Resumed " + event.getAgletProxy());
        }
        // added(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been reverted
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletReverted(ContextEvent event) {
        removed(event.getAgletProxy());
    }


    /**
     *  Called when an aglet has been updated
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletStateChanged(ContextEvent event) {
        synchronized (aglets) {
            Enumeration e = aglets.keys();

            while (e.hasMoreElements()) {
                Object k = e.nextElement();
                Item i = (Item) aglets.get(k);

                if (i.equals(event.getAgletProxy())) {
                    i.setText((String) event.arg);
                    if (message) {
                        System.out.println(k.toString() + " : " + event.arg);
                    }
                }
            }
        }
    }


    /**
     *  Called when an aglet has been suspended
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void agletSuspended(ContextEvent event) {
        if (message) {
            System.out.println("Suspended " + event.getAgletProxy());
        }
        // removed(event.getAgletProxy());
    }


    /**
     *  Description of the Method
     *
     * @param  ev  Description of Parameter
     * @since
     */
    public void contextShutdown(ContextEvent ev) {

        //
    }


    /**
     *  Initializes the Viewer with the context.
     *
     * @param  ev  Description of Parameter
     * @since
     */
    public void contextStarted(ContextEvent ev) {
        this.context = ev.getAgletContext();
        Resource res = Resource.getResourceFor("aglets");

        //
        // Check to see if this is a registered user.
        //
        if (res.getBoolean("aglets.registered", false) == false) {
        }

        new Thread(this).start();
    }


    /**
     *  Infinite loop to process inputs
     *
     * @since
     */
    public void run() {

        boolean socket_active = false;
        BufferedReader in = null;
        PrintWriter out = null;
        Socket clientSocket = null;

        String controlPortString =
                System.getProperties().getProperty("maf.controlport");

        try {
            if (Integer.parseInt(controlPortString) > 0) {
                _control_port_num = Integer.parseInt(controlPortString);
            } else {
                System.out.println("Controlport: " + controlPortString + " is out of range, defaulting to: " + _control_port_num);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        Thread.currentThread().setPriority(1);

        // Create a socket to control this Tahiti
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(_control_port_num);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + _control_port_num);
            System.exit(1);
        }

        // Main control loop
        while (continue_processing) {
            try {
                clientSocket = serverSocket.accept();
                socket_active = true;
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(_banner_string);
                out.println(_version_string);
                out.println("Connected to:" + serverSocket.toString());
            } catch (IOException e) {
                System.err.println("getOutputStream failed.");
                System.exit(1);
            }

            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("getInputStream failed.");
                System.exit(1);
            }

            //holds any strings to be returned to the client
            String outputString = null;

            // read loop, processes commands as
            // they are read from the socket
            while (continue_processing && socket_active) {
                try {
                    String line = in.readLine();
                    outputString = command(line);
                    out.println(outputString);
                    out.println("done.");
                } catch (Throwable ex) {
                    if (debug) {
                      ex.printStackTrace();
                        System.err.println(ex.getMessage());
                        System.err.println("Socket closed");
                    socket_active = false;
                }
                    out.println("Exception: ["+ex.toString()+"] With the message: ["+ex.getMessage()+"] has occurred, continuing.");
                }
            }
        }

        try {
            AgletRuntime.getAgletRuntime().shutdown();
            out.close();
            in.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("close failed.");
        }
        if (reboot) {
            System.exit(0);
        } else {
            System.exit(1);
        }

    }


    /**
     *  Called when an aglet request to show the document given as URL
     *
     * @param  event  an ContextEvent
     * @since
     * @see           Aglet#showDocument
     */
    public void showDocument(ContextEvent event) {
        if (message) {
            System.out.println("hyper link required :" + event.arg);
        }
    }


    /**
     *  Called to show the message
     *
     * @param  event  an ContextEvent
     * @since
     */
    public void showMessage(ContextEvent event) {
        if (message) {
            System.out.println((String) event.arg);
        }
    }


    /**
     *  Interprets inputs from command line.
     *
     * @param  line           typed string
     * @return                Description of the Returned Value
     * @exception  Exception  Description of Exception
     * @since
     */
    String command(String line) throws Exception {
        Item item = null;
        StringTokenizer st = new StringTokenizer(line, " \t");

        if (st.hasMoreTokens()) {
            String cmd = st.nextToken();

            if ("shutdown".equalsIgnoreCase(cmd)) {
                shutdown = true;
                continue_processing = false;
                return ("shutting down");
            } else if ("reboot".equalsIgnoreCase(cmd)) {
                reboot = true;
                continue_processing = false;
                return ("rebooting");
            } else if ("list".equalsIgnoreCase(cmd)) {
                return (list());
            } else if ("debug".equalsIgnoreCase(cmd)) {
                if (st.hasMoreTokens()) {
                    if ("on".equalsIgnoreCase(st.nextToken())) {
                        com.ibm.awb.misc.Debug.debug(true);
                        debug = true;
                    } else {
                        com.ibm.awb.misc.Debug.debug(false);
                        debug = false;
                    }
                }
                com.ibm.awb.misc.Debug.list(System.err);
                return (debug ? "debug on"
                        : "debug off");
            } else if ("msg".equalsIgnoreCase(cmd)) {
                if (st.hasMoreTokens()) {
                    if ("on".equalsIgnoreCase(st.nextToken())) {
                        message = true;
                    } else {
                        message = false;
                    }
                }
                    return (message ? "message on"
                             : "message off");
            } else if ("help".equalsIgnoreCase(cmd)) {
                return ("help");
            } else if ("create".equalsIgnoreCase(cmd)) {
                URL url = null;
                String name = "";

                try { 
                if (st.countTokens() == 2) {
                    url = new URL(st.nextToken());
                    name = st.nextToken();
                } else if (st.countTokens() == 1) {
                    name = st.nextToken();
                  } else {
                    return ("Usage: create [URL] name");
                  }
                  AgletProxy proxy = null;
                  if (debug)
                    System.err.println("Entering context.createAglet");
                  proxy = context.createAglet(url, name, null);
                  if (debug)
                    System.err.println("Leaving context.createAglet");
                  if (proxy != null) {
                    return("Creation of Aglet "+name+" Succeeded.");
                } else {
                    return("Creation of Aglet "+name+" Failed.");
                }
                } 
                catch (Throwable ex) {
                  if (debug) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
                  }
                  return("Creation of Aglet ["+name+"] Failed. Exception was: ["+ex.toString()+"] Message was: ["+ex.getMessage()+"]");
                }
            }
            /*
             * This handles property, dispatch, clone, dispose, dialog
             */
            else if ((item = (Item)aglets.get(cmd)) != null) {
                if (st.hasMoreTokens()) {
                    String tokenString = st.nextToken();
                    if ("property".equalsIgnoreCase(tokenString)) {
                        return("Properties for: " + cmd + "\n" + item.toString());
                    } else {
                        item.command(tokenString, st);
                    if (!item.isValid()) {
                        removed(item);
                        return ("Removed : " + cmd);
                    }
                    }
                } else {
                    return (item.toString());
                }
            } else {
                return ("unknown command (or aglet not found): " + cmd);
            }
        }
        return ("unknown command: " + line);
    }


    /**
     *  Prints out the list of aglets
     *
     * @return                Description of the Returned Value
     * @exception  Exception  Description of Exception
     * @since
     */
    String list() throws Exception {
        String returnString;
        Enumeration e = aglets.keys();

        returnString = "Aglet List empty";

        if (e.hasMoreElements())
            returnString = "Aglet List:\n";
        while (e.hasMoreElements()) {
            String k = (String) e.nextElement();
            Item item = (Item) aglets.get(k);

            // if (item.isValid()) {
            returnString += k + " [" + item.proxy.getAgletClassName() + "] ";

            // }
        }
        return (returnString);
    }


    /**
     *  Description of the Method
     *
     * @param  proxy  Description of Parameter
     * @since
     */
    private synchronized void added(AgletProxy proxy) {
        aglets.put("aglet" + (serial++), new Item(proxy));
    }


    /**
     *  Description of the Method
     *
     * @param  proxy  Description of Parameter
     * @since
     */
    private synchronized void removed(AgletProxy proxy) {
        Enumeration e = aglets.keys();

        while (e.hasMoreElements()) {
            Object k = e.nextElement();

            if (aglets.get(k).equals(proxy)) {
                aglets.remove(k);
            }
        }
    }


    /**
     *  Description of the Method
     *
     * @param  item  Description of Parameter
     * @since
     */
    private synchronized void removed(Item item) {
        removed(item.proxy);
    }


    /**
     *  Gets the UserManager attribute of the TahitiDaemon class
     *
     * @return    The UserManager value
     * @since
     */
    public final static UserManager getUserManager() {
        return _userManager;
    }
}

