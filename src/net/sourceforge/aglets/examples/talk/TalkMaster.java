package net.sourceforge.aglets.examples.talk;

/*
 * @(#)TalkMaster.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletContext;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.InvalidAgletException;
import com.ibm.aglet.message.Message;

/**
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:54 $
 * @author Mitsuru Oshima
 * @see net.sourceforge.aglets.examples.talk.TalkSlave
 */
public class TalkMaster extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6463228915907125022L;

	transient AgletProxy remoteProxy = null;

	String name = "Unknown";

	TalkWindow window = null;

	public void dispatchSlave(final String dest) {
		try {
			if (remoteProxy != null) {
				remoteProxy.sendMessage(new Message("bye"));
			}

			final AgletContext context = getAgletContext();

			final AgletProxy proxy = context.createAglet(null, "examples.talk.TalkSlave", getProxy());

			final URL url = new URL(dest);

			remoteProxy = proxy.dispatch(url);

		} catch (final InvalidAgletException ex) {
			ex.printStackTrace();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private String getProperty(final String key) {
		return System.getProperty(key, "Unknown");
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("dialog")) {
			window.show();
		} else if (msg.sameKind("text")) {
			if (window.isVisible() == false) {
				window.show();
			}
			window.appendText((String) msg.getArg());
			return true;
		}
		return false;
	}

	@Override
	public void onCreation(final Object o) {
		window = new TalkWindow(this);
		window.pack();
		window.show();
		try {
			name = getProperty("user.name");
		} catch (final Exception ex) {
		}
	}

	@Override
	public void onDisposing() {
		if (window != null) {
			window.dispose();
			window = null;
		}
		if (remoteProxy != null) {
			try {
				remoteProxy.sendMessage(new Message("bye"));
			} catch (final AgletException ex) {
				ex.printStackTrace();
			}
		}
	}

	void sendText(final String text) {
		try {
			if (remoteProxy != null) {
				remoteProxy.sendMessage(new Message("text", name
						+ " : " + text));
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
