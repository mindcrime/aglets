/*
 * @(#)WatcherClient.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.net.URL;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.system.Aglets;

/**
 * The WatcherClient example illustrates how to write an application program
 * with Aglets. With this API, you can create and dispatch an aglet, send the
 * aglet a message and receive a result from it. To run this example, you need
 * the server and WatcherSlave Aglet.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @see aglet.system.Aglets
 * @see net.sourceforge.aglets.examples.watcher.ProxyWatcher
 * @see net.sourceforge.aglets.examples.watcher.WatcherSlave
 */
public class WatcherClient {

    public static void main(String a[]) throws java.lang.Exception {

	if (a.length < 1) {
	    System.out.println("WatcherClient firstAddress secondAddress ...");
	    return;
	}

	System.out.println("========= Creating in " + a[0]);

	AgletProxy proxy = Aglets.createAglet(a[0], null, "keio.ics.nak.watcher.WatcherSlave", null);

	System.out.println((String) proxy.sendMessage(new Message("getInfo")));

	int i = 1;

	while (a.length > i) {
	    try {
		System.out.println("========== Dispatching to " + a[i]);
		proxy = proxy.dispatch(new URL(a[i++]));
		String str = (String) proxy.sendMessage(new Message("getInfo"));

		System.out.println(str);
	    } catch (Exception ex) {
		ex.printStackTrace();
		break;
	    }
	}
	proxy.dispose();
    }
}
