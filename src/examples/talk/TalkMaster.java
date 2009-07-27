package examples.talk;

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

import com.ibm.aglet.*;
import com.ibm.aglet.message.Message;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;
import java.net.URL;

/**
 * 
 * @version     1.00    $Date: 2009/07/27 10:31:42 $
 * @author      Mitsuru Oshima
 * @see examples.talk.TalkSlave
 */
public class TalkMaster extends Aglet {

	transient AgletProxy remoteProxy = null;

	String name = "Unknown";

	TalkWindow window = null;

	public void dispatchSlave(String dest) {
		try {
			if (remoteProxy != null) {
				remoteProxy.sendMessage(new Message("bye"));
			} 

			AgletContext context = getAgletContext();

			AgletProxy proxy = context.createAglet(null, 
												   "examples.talk.TalkSlave", 
												   getProxy());

			URL url = new URL(dest);

			remoteProxy = proxy.dispatch(url);

		} catch (InvalidAgletException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	private String getProperty(String key) {
		return System.getProperty(key, "Unknown");
	}
	public boolean handleMessage(Message msg) {
		if (msg.sameKind("dialog")) {
			window.show();
		} else if (msg.sameKind("text")) {
			if (window.isVisible() == false) {
				window.show();
			} 
			window.appendText((String)msg.getArg());
			return true;
		} 
		return false;
	}
	public void onCreation(Object o) {
		window = new TalkWindow(this);
		window.pack();
		window.show();
		try {
			name = getProperty("user.name");
		} catch (Exception ex) {}
	}
	public void onDisposing() {
		if (window != null) {
			window.dispose();
			window = null;
		} 
		if (remoteProxy != null) {
			try {
				remoteProxy.sendMessage(new Message("bye"));
			} catch (AgletException ex) {
				ex.printStackTrace();
			} 
		} 
	}
	private void print(String m) {
		System.out.println("Sender: " + m);
	}
	void sendText(String text) {
		try {
			if (remoteProxy != null) {
				remoteProxy.sendMessage(new Message("text", 
													name + " : " + text));
			} 
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
}
