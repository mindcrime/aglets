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

    transient String name = "Unknown";
    transient TalkWindow window = null;
    AgletProxy masterProxy = null;

    public TalkSlave() {
    }

    private String getProperty(String key) {
	return System.getProperty(key, "Unknown");
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("dialog")) {
	    this.window.show();

	} else if (msg.sameKind("text")) {
	    String str = (String) msg.getArg();

	    if (this.window.isVisible() == false) {
		this.window.show();
	    }
	    this.window.appendText(str);
	    return true;
	} else if (msg.sameKind("bye")) {
	    this.window.appendText("Bye Bye..");
	    try {
		Thread.currentThread();
		Thread.sleep(3000);
	    } catch (Exception ex) {
	    }
	    msg.sendReply();
	    this.dispose();
	}
	return false;
    }

    @Override
    public void onCreation(Object o) {
	this.masterProxy = (AgletProxy) o;
	this.addMobilityListener(new MobilityAdapter() {
	    @Override
	    public void onArrival(MobilityEvent ev) {
		TalkSlave.this.window = new TalkWindow(TalkSlave.this);
		TalkSlave.this.window.pack();
		TalkSlave.this.window.show();
		try {
		    TalkSlave.this.name = TalkSlave.this.getProperty("user.name");
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	});
    }

    @Override
    public void onDisposing() {
	if (this.window != null) {
	    this.window.dispose();
	    this.window = null;
	}
    }

    public void sendText(String text) {
	try {
	    if (this.masterProxy == null) {
		return;
	    }
	    this.masterProxy.sendMessage(new Message("text", this.name + " : "
		    + text));
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
