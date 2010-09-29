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

    public void dispatchSlave(String dest) {
	try {
	    if (this.remoteProxy != null) {
		this.remoteProxy.sendMessage(new Message("bye"));
	    }

	    AgletContext context = this.getAgletContext();

	    AgletProxy proxy = context.createAglet(null, "examples.talk.TalkSlave", this.getProxy());

	    URL url = new URL(dest);

	    this.remoteProxy = proxy.dispatch(url);

	} catch (InvalidAgletException ex) {
	    ex.printStackTrace();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private String getProperty(String key) {
	return System.getProperty(key, "Unknown");
    }

    @Override
    public boolean handleMessage(Message msg) {
	if (msg.sameKind("dialog")) {
	    this.window.show();
	} else if (msg.sameKind("text")) {
	    if (this.window.isVisible() == false) {
		this.window.show();
	    }
	    this.window.appendText((String) msg.getArg());
	    return true;
	}
	return false;
    }

    @Override
    public void onCreation(Object o) {
	this.window = new TalkWindow(this);
	this.window.pack();
	this.window.show();
	try {
	    this.name = this.getProperty("user.name");
	} catch (Exception ex) {
	}
    }

    @Override
    public void onDisposing() {
	if (this.window != null) {
	    this.window.dispose();
	    this.window = null;
	}
	if (this.remoteProxy != null) {
	    try {
		this.remoteProxy.sendMessage(new Message("bye"));
	    } catch (AgletException ex) {
		ex.printStackTrace();
	    }
	}
    }

    void sendText(String text) {
	try {
	    if (this.remoteProxy != null) {
		this.remoteProxy.sendMessage(new Message("text", this.name
			+ " : " + text));
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
