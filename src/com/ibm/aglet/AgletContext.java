package com.ibm.aglet;

/*
 * @(#)AgletContext.java
 * 
 * (c) Copyright IBM Corp. 1996, 1997, 1998
 * 
 * IBM grants you a non-exclusive, non-transferrable License to
 * use this program internally solely for the purposes of testing
 * and evaluating Java Aglet API.
 * You may not distribute, sublicense, lease, rent or use this
 * sample program externally.
 * 
 * THIS ROGRAM IS PROVIDED "AS IS" WITHOUT ANY WARRANTY EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE WARRANTY OF
 * NON-INFRINGEMENT AND THE WARRANTIES OF MERCHANTIBILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 * IBM WILL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY YOU AS
 * A RESULT OF USING THIS SAMPLE PROGRAM. IN NO EVENT WILL IBM BE
 * LIABLE FOR ANY SPECIAL, INDIRECT CONSEQUENTIAL DAMAGES OR LOST
 * PROFITS EVEN IF IBM HAS BEEN ADVISED OF THE POSSIBILITY OF THEIR
 * OCCURRENCE OR LOSS OF OR DAMAGE TO YOUR RECORDS OR DATA.
 * IBM WILL NOT BE LIABLE FOR ANY THIRD PARTY CLAIMS AGAINST YOU.
 */

import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglet.system.ContextListener;
import com.ibm.aglet.util.ImageData;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.net.URL;

import java.awt.Image;
import java.applet.AudioClip;

/**
 * The <tt>AgletContext</tt> interface is used by an aglet to get information
 * about its environment and to send messages to the environment and other
 * aglets currently active in that environment. It provides means for
 * maintaining and managing running aglets in an environment where the host
 * system is secured against malicious aglets.
 * 
 * @version     1.50	$Date: 2001/07/28 06:34:04 $
 * @author      Danny B. Lange
 * @author      Mitsuru Oshima
 */
public interface AgletContext {

	/**
	 * Adds the specified context listener to receive context events
	 * from this context.
	 * @param listener the context listener
	 * @exception SecurityException
	 */
	abstract public void addContextListener(ContextListener listener);
	/**
	 * Clears class cache in memory.
	 * @param codebase the codebase of the cache to clean up. if null,
	 * entire cache in this context is cleared.
	 */
	abstract public void clearCache(URL codebase);
	/**
	 * Creates an instance of the specified aglet class. The aglet's class
	 * code file can be located on the local file system as well as on a
	 * remote server. If the <tt>codeBase</tt> is <tt>null</tt>, the context
	 * will search for the code in the local system's aglet search path
	 * (<tt>AGLET_PATH</tt>). The createAglet method takes three arguments:
	 * <tt>codeBase</tt>, <tt>code</tt>,  and <tt>init</tt>:
	 * 
	 * @param codeBase
	 * <tt>codeBase</tt> specifies the base URL of the aglet class file,
	 * in other words, the (possibly remote) directory that contains the
	 * aglet's code. If this argument is <tt>null</tt>, then the directories
	 * specified in the local host's aglet search path are searched.
	 * The aglet search path works in a similar way to Java's class path.
	 * It is typically an environment variable that specifies a list of
	 * directories to be searched for aglet code files.
	 * @param code
	 * <tt>code</tt> gives the name of the file that contains the
	 * aglet's compiled class  code. This file is relative to the base URL
	 * of the aglet, and cannot be absolute.
	 * @param init
	 * <tt>init</tt> is an object passed on to the aglet's onCreation
	 * method.
	 * @return the proxy of the new aglet.
	 * @exception AgletException if the aglets can not be created.
	 * @exception UnknownHostException if the given host could not be found.
	 * @exception ServerNotFoundException if the server could not be found.
	 * @exception InstantiationException if the instantiation failed.
	 * @exception ClassNotFoundException if the class not found.
	 * @see Aglet#onCreation
	 */
	abstract public AgletProxy createAglet(URL codeBase, String code, 
										   Object init) throws IOException, 
										   AgletException, 
										   ClassNotFoundException, 
										   InstantiationException;
	/**
	 * Gets an enumeration of all aglets in the current context including
	 * deactivated aglets.
	 * @return a list of proxies.
	 */
	abstract public Enumeration getAgletProxies();
	/**
	 * Gets an array of aglet proxies in the current context.
	 * @see Aglet#ACTIVE
	 * @see Aglet#INACTIVE
	 * @param type the type of aglets.  ACTIVE, INACTIVE or ACTIVE | INACTIVE,
	 * @return a list of proxies that matches the given state.
	 */
	abstract public Enumeration getAgletProxies(int type);
	/**
	 * Gets a proxy for an aglet in the current context. The selected aglet
	 * is specified by its identity.
	 * @param id the identity of the aglet.
	 * @return the proxy.
	 */
	abstract public AgletProxy getAgletProxy(AgletID id);
	/**
	 * Gets a proxy for an aglet in a remote context. The remote context is
	 * identified by its URL, and the aglet is indicated by its identifier.
	 * 
	 * @param contextAddress the address specifing a remote context.
	 * @param id the identity of the aglet.
	 * @return the proxy.
	 * @see getName
	 * @deprecated
	 */
	abstract public AgletProxy getAgletProxy(URL contextAddress, AgletID id);
	/**
	 * Gets an audio clip.
	 * @param audio an absolute URL giving the location of the audio file.
	 * @return the Audio clip object give by the URL.
	 */
	abstract public AudioClip getAudioClip(URL audio);
	/**
	 * Returns the URL of the daemon serving this context.
	 * @return the URL of the daemon. <tt>null</tt> if the hosting information is not available.
	 */
	abstract public URL getHostingURL();
	/**
	 * Gets an image. This is a tempolary solution.
	 * 
	 * @param image a serializable image data.
	 * @return the image object converted from the image data.
	 */
	abstract public Image getImage(ImageData image);
	/**
	 * Gets an image.
	 * @param image an absolute URL giving the location of the image file.
	 * @return the image object give by the URL.
	 */
	abstract public Image getImage(URL image);
	/**
	 * Gets an image data. This is a tempolary solution.
	 * @param image an absolute URL giving the location of the image file.
	 * @return the serializable image data.
	 */
	abstract public ImageData getImageData(URL image);
	/**
	 * Gets the name of the context. Each context running in the same
	 * server can be distinguished by the name. The example of an address for
	 * contexts is, "atp://host.com:4434/name".
	 * 
	 * @return a name of aglet context
	 */
	abstract public String getName();
	/**
	 * Gets the context property indicated by the key.
	 * @param key the name of the context property.
	 * @return the value of the specified key.
	 */
	abstract public Object getProperty(String key);
	/**
	 * Gets the context property indicated by the key and default value.
	 * @param key the name of the context property.
	 * @param def the value to use if this property is not set.
	 * @return the value of the specified key.
	 */
	abstract public Object getProperty(String key, Object def);
	/**
	 * Sends a multicast message to the subscribers in the context.
	 * @param message to send
	 * @return ReplySet containing FutureReplies
	 */
	abstract public ReplySet multicastMessage(Message msg);
	/**
	 * Removes the specified context listener.
	 * @param listener the context listener
	 * @exception SecurityException
	 */
	abstract public void removeContextListener(ContextListener listener);
	/**
	 * Retracts the aglet specified by its url:
	 * <tt>atp://host-domain-name/#aglet-identity</tt>.
	 * @param url the location and aglet identity of the aglet to be retracted.
	 * @return the aglet proxy for the retracted aglet.
	 * @exception AgletException when the method failed to retract the aglet.
	 * @exception UnknownHostException if the specified HOST is not found.
	 * @exception ServerNotFoundException if the aglet server specified
	 * in the URL is not available.
	 * @exception MalformedURLException if the given url is not URI for
	 * an aglet.
	 * @exception RequestRefusedException if the retraction refused.
	 * @exception AgletNotFoundException if the aglet could not be found.
	 * 
	 * @deprecated
	 */
	abstract public AgletProxy retractAglet(URL url) 
			throws IOException, AgletException;
	/**
	 * Retracts the aglet specified by its url and id
	 * @param url the location of the aglet to be retracted.
	 * @param id  the aglet identity of the aglet to be retracted.
	 * @return the aglet proxy for the retracted aglet.
	 * @exception AgletException when the method failed to retract the aglet.
	 * @exception UnknownHostException if the specified HOST is not found.
	 * @exception ServerNotFoundException if the aglet server specified
	 * in the URL is not available.
	 * @exception MalformedURLException if the given url is not URI for
	 * an aglet.
	 * @exception RequestRefusedException if the retraction refused.
	 * @exception AgletNotFoundException if the aglet could not be found.
	 */
	abstract public AgletProxy retractAglet(URL url, AgletID aid) 
			throws IOException, AgletException;
	/**
	 * Sets the context property indicated by the key and value.
	 * @param key the name of the context property.
	 * @param value the value to be stored.
	 * @return the value of the specified key.
	 */
	abstract public void setProperty(String key, Object value);
	/**
	 * Shows a new document. This may be ignored by the aglet context.
	 * @param url an url to be shown
	 */
	abstract public void showDocument(URL url);
	/**
	 * Shutdown the context. This is ignored if the context is already stopped.
	 * @exception SecurityException if the current execution context is not
	 * allowd to shutdown
	 * @see start
	 */
	abstract public void shutdown();
	/**
	 * Shutdown the context with the specific message object. This message
	 * object is delivered to all aglets in the context before all aglets
	 * are killed. This is ignored if the context is already stopped.
	 * @exception SecurityException if the current execution context is not
	 * allowd to shutdown
	 * @see start
	 */
	abstract public void shutdown(Message msg);
	/**
	 * Starts the context. This is ignored if the context is already runnig.
	 * @exception SecurityException
	 */
	abstract public void start();
	/**
	 * Starts the context. This is ignored if the context is already runnig.
	 * @param reactivate  if false, it does not activate aglets in the deactivation spool
	 * @exception SecurityException
	 */
	abstract public void start(boolean reactivate);
}
