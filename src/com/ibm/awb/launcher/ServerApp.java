package com.ibm.awb.launcher;
/*
 * @(#)ServerApp.java
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

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglet.system.ContextAdapter;
import com.ibm.aglet.system.ContextEvent;
import com.ibm.aglets.MAFAgentSystem_AgletsImpl;
import com.ibm.aglets.tahiti.Tahiti;
import com.ibm.awb.misc.Opt;
import com.ibm.maf.MAFAgentSystem;

/**
 * The ServerApp example illustrates how to embed the AgletsServer facility into
 * an application program.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 * @see com.ibm.aglet.system.AgletRuntime
 * @see com.ibm.aglets.tahiti.Main
 */
public class ServerApp extends ContextAdapter {

	// additional options
	final static Opt options[] = {
		Opt.Entry("-protocol", "maf.protocol", null),
		Opt.Entry("-username", "username", null),
		Opt.Entry("-password", "password", null), };

	public static void main(final String args[]) throws java.lang.Exception {
		Opt.setopt(options);
		final AgletRuntime runtime = AgletRuntime.init(args);

		final String[] r = (String[]) AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				final String[] results = new String[2];
				final String userName = System.getProperty("user.name");

				results[0] = System.getProperty("username", userName);
				results[1] = System.getProperty("password", "");
				return results;
			}
		});
		final String username = r[1];
		final String password = r[2];

		final Object obj = runtime.authenticateOwner(username, password);

		if (obj == null) {
			System.err.println("Cannot authenticate the user \"" + username
					+ "\"");
			throw new Exception("User authentication failed.");
		}

		/*
		 * User authentication (optional) If this program requests user
		 * identification from dialog box, we need the following call.
		 */

		/*
		 * if (Main.login(runtime) == null) return;
		 */

		final MAFAgentSystem maf_system = new MAFAgentSystem_AgletsImpl(runtime);
		String protocol = "atp";

		protocol = (String) AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				return System.getProperty("maf.protocol", "atp");
			}
		});

		MAFAgentSystem.initMAFAgentSystem(maf_system, protocol);
		Tahiti.initializeGUI();
		Tahiti.installFactories();

		/*
		 * Create named contexts. To dispatch to this context, sender has to
		 * secify the destination like, "atp://aglets.trl.ibm.com:434/test"
		 * Defining multiple contexts is also possible.
		 * 
		 * Support of multiple context is experimental function. This feature
		 * may drop in the future.
		 */

		// first context
		final AgletContext cxt = runtime.createAgletContext("test");

		cxt.addContextListener(new ServerApp());

		// second context
		final AgletContext cxt2 = runtime.createAgletContext("test2");

		cxt2.addContextListener(new ServerApp());

		Tahiti.installSecurity();
		MAFAgentSystem.startMAFAgentSystem(maf_system, protocol);

		// start contexts
		cxt.start();
		cxt2.start();

		/*
		 * From this point, you can use contexts. (creating, dispatching an
		 * aglet, etc.) 1. Create HelloAglet in cxt, 2. And, dispatches it to
		 * cxt2. See the source code for the detail of HelloAglet.
		 */
		final AgletProxy p = cxt.createAglet(null, "examples.hello.HelloAglet", null);

		final Message msg = new Message("startTrip", cxt2.getHostingURL().toString());

		p.sendMessage(msg);
	}

	@Override
	public void agletActivated(final ContextEvent ev) {
		System.out.println("Aglet Activated : " + ev.getAgletProxy());
	}

	@Override
	public void agletArrived(final ContextEvent ev) {
		System.out.println("Aglet Arrived : " + ev.getAgletProxy());
	}

	@Override
	public void agletCloned(final ContextEvent ev) {
		System.out.println("Aglet Cloned : " + ev.getAgletProxy());
	}

	@Override
	public void agletCreated(final ContextEvent ev) {
		System.out.println("Aglet Created : " + ev.getAgletProxy());
	}

	@Override
	public void agletDeactivated(final ContextEvent ev) {
		System.out.println("Aglet Deactivated : " + ev.getAgletProxy());
	}

	@Override
	public void agletDispatched(final ContextEvent ev) {
		System.out.println("Aglet Dispatched : " + ev.getAgletProxy());
	}

	@Override
	public void agletDisposed(final ContextEvent ev) {
		System.out.println("Aglet Disposed : " + ev.getAgletProxy());
	}

	@Override
	public void agletReverted(final ContextEvent ev) {
		System.out.println("Aglet Reverted : " + ev.getAgletProxy());
	}

	@Override
	public void showMessage(final ContextEvent ev) {
		System.out.println("message : " + ev.getMessage());
	}
}
