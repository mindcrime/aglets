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

/*
 * This class is used to hold the set of observers of an observable
 * object whenever there is more than one observer.
 */
final class ListenerList extends java.util.Vector implements ContextListener {

	/* synchronized */
	public void agletActivated(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletActivated(event);
		} 
	}
	/* synchronized */
	public void agletArrived(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletArrived(event);
		} 
	}
	/* synchronized */
	public void agletCloned(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletCloned(event);
		} 
	}
	/* synchronized */
	public void agletCreated(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletCreated(event);
		} 
	}
	/* synchronized */
	public void agletDeactivated(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletDeactivated(event);
		} 
	}
	/* synchronized */
	public void agletDispatched(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletDispatched(event);
		} 
	}
	/* synchronized */
	public void agletDisposed(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletDisposed(event);
		} 
	}
	/* synchronized */
	public void agletResumed(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletResumed(event);
		} 
	}
	/* synchronized */
	public void agletReverted(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletReverted(event);
		} 
	}
	/* synchronized */
	public void agletStateChanged(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletStateChanged(event);
		} 
	}
	/* synchronized */
	public void agletSuspended(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).agletSuspended(event);
		} 
	}
	/* synchronized */
	public void contextShutdown(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).contextShutdown(event);
		} 
	}
	/* synchronized */
	public void contextStarted(ContextEvent event) {

		// int i = size();
		// 
		// while (--i >= 0) {
		// ((ContextListener) elementAt(i)).contextStarted(event);
		// }
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).contextStarted(event);
		} 
	}
	/* synchronized */
	public void showDocument(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).showDocument(event);
		} 
	}
	/* synchronized */
	public void showMessage(ContextEvent event) {
		for (Enumeration e = elements(); e.hasMoreElements(); ) {
			((ContextListener)e.nextElement()).showMessage(event);
		} 
	}
}
