package examples.talk;

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

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.message.Message;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.URL;

/**
 * @version     1.00    96/12/19
 * @author      Mitsuru Oshima
 */
public class TalkSlave extends Aglet {

	transient String name = "Unknown";
	transient TalkWindow window = null;
	AgletProxy masterProxy = null;

	public TalkSlave() {}
	private String getProperty(String key) {
		return System.getProperty(key, "Unknown");
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			window.show();

		} else if (msg.sameKind("text")) {
			String str = (String)msg.getArg();

			if (window.isVisible() == false) {
				window.show();
			} 
			window.appendText(str);
			return true;
		} else if (msg.sameKind("bye")) {
			window.appendText("Bye Bye..");
			try {
				Thread.currentThread().sleep(3000);
			} catch (Exception ex) {}
			msg.sendReply();
			dispose();
		} 
		return false;
	}
	public void onCreation(Object o) {
		masterProxy = (AgletProxy)o;
		addMobilityListener(new MobilityAdapter() {
			public void onArrival(MobilityEvent ev) {
				window = new TalkWindow(TalkSlave.this);
				window.pack();
				window.show();
				try {
					name = getProperty("user.name");
				} catch (Exception ex) {
					ex.printStackTrace();
				} 
			} 
		});
	}
	public void onDisposing() {
		if (window != null) {
			window.dispose();
			window = null;
		} 
	}
	private void print(String m) {
		System.out.println("Receiver : " + m);
	}
	public void sendText(String text) {
		try {
			if (masterProxy == null) {
				return;
			} 
			masterProxy.sendMessage(new Message("text", name + " : " + text));
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
