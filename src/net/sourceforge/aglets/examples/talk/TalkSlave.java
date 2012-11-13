package net.sourceforge.aglets.examples.talk;

/*
 * @(#)TalkSlave.java
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

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.event.MobilityAdapter;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.message.Message;

/**
 * @version 1.00 96/12/19
 * @author Mitsuru Oshima
 */
public class TalkSlave extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6097401329664140630L;
	transient String name = "Unknown";
	transient TalkWindow window = null;
	AgletProxy masterProxy = null;

	public TalkSlave() {
	}

	private String getProperty(final String key) {
		return System.getProperty(key, "Unknown");
	}

	@Override
	public boolean handleMessage(final Message msg) {
		if (msg.sameKind("dialog")) {
			window.show();

		} else if (msg.sameKind("text")) {
			final String str = (String) msg.getArg();

			if (window.isVisible() == false) {
				window.show();
			}
			window.appendText(str);
			return true;
		} else if (msg.sameKind("bye")) {
			window.appendText("Bye Bye..");
			try {
				Thread.currentThread();
				Thread.sleep(3000);
			} catch (final Exception ex) {
			}
			msg.sendReply();
			dispose();
		}
		return false;
	}

	@Override
	public void onCreation(final Object o) {
		masterProxy = (AgletProxy) o;
		addMobilityListener(new MobilityAdapter() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 303895833937119354L;

			@Override
			public void onArrival(final MobilityEvent ev) {
				window = new TalkWindow(TalkSlave.this);
				window.pack();
				window.show();
				try {
					name = TalkSlave.this.getProperty("user.name");
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onDisposing() {
		if (window != null) {
			window.dispose();
			window = null;
		}
	}

	public void sendText(final String text) {
		try {
			if (masterProxy == null) {
				return;
			}
			masterProxy.sendMessage(new Message("text", name + " : "
					+ text));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
