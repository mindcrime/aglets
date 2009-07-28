package com.ibm.aglets;

/*
 * @(#)AgletContextImpl.java
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

import com.ibm.maf.*;

import com.ibm.aglet.*;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.util.ImageData;

// import com.ibm.awb.misc.DigestTable;
import com.ibm.awb.misc.Archive;
import com.ibm.awb.misc.Resource;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Permission;
import java.net.SocketPermission;
import java.io.FilePermission;
import com.ibm.aglets.security.AgletPermission;
import com.ibm.aglets.security.MessagePermission;
import com.ibm.aglets.security.ContextPermission;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Vector;

import java.security.Identity;

import java.net.URL;
import java.net.URLConnection;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ObjectOutput;
import java.io.ByteArrayOutputStream;

import java.lang.ClassNotFoundException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.SocketException;

import java.rmi.RemoteException;
import com.ibm.maf.NameInvalid;

/*
 * MM
 */
import java.awt.Image;
import java.awt.Toolkit;
import sun.audio.*;
import java.applet.AudioClip;

/**
 * This class is used to hold a list of context listeners and to forward an event to each 
 * listener. The class works as a linked list, and for each call to notify an event, the class extract
 * each context listener and forwards the event to it.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 *
 * 25/ago/07
 */
public final class ListenerList extends java.util.LinkedList<ContextListener> implements ContextListener {

	/**
	 * Notifies all listeners than the aglet has been activated.
	 */
	public synchronized void agletActivated(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletActivated(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than an aglet has arrived.
	 */
	public synchronized void agletArrived(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletArrived(event);
		
	    } 
	}
	
	/**
	 * Notifies all listeners than an aglet has been cloned.
	 */
	public synchronized void agletCloned(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletCloned(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than an aglet has been created.
	 */
	public synchronized void agletCreated(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletCreated(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than the aglet has been deactivated.
	 */
	public synchronized void agletDeactivated(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletDeactivated(event);
		
	    }

	}
	
	/**
	 * Notifies all listeners than an aglet has been dispatched.
	 */
	public synchronized void agletDispatched(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletDispatched(event);
		
	    }

	}
	
	/**
	 * Notifies all listeners than an aglet has been disposed.
	 */
	public synchronized void agletDisposed(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletDisposed(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than an aglet has been resumed.
	 */
	public synchronized void agletResumed(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletResumed(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than an aglet has been called home.
	 */
	public void agletReverted(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletReverted(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than the state of the agent has changed.
	 */
	public synchronized void agletStateChanged(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletStateChanged(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners than the state of the agent has been suspended.
	 */
	public synchronized void agletSuspended(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).agletSuspended(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners that the context has been shut down.
	 */
	public synchronized void contextShutdown(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).contextShutdown(event);
		
	    }
	}
	
	/**
	 * Notifies all listeners that the context has been started.
	 */
	public synchronized void contextStarted(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).contextStarted(event);
		
	    }
	}
	
	/**
	 * Notifies of a show document call.
	 */
	public synchronized void showDocument(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).showDocument(event);
		
	    }
	}
	
	/**
	 * Notifies of a show message call.
	 */
	public synchronized void showMessage(ContextEvent event) {
	    Iterator listener = this.iterator();
	    while (listener != null && listener.hasNext()) {
		((ContextListener) listener.next()).showMessage(event);
		
	    }
	}
}
