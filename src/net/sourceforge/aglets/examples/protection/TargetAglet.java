package net.sourceforge.aglets.examples.protection;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.event.CloneEvent;
import com.ibm.aglet.event.CloneListener;
import com.ibm.aglet.event.MobilityEvent;
import com.ibm.aglet.event.MobilityListener;
import com.ibm.aglet.event.PersistencyEvent;
import com.ibm.aglet.event.PersistencyListener;
import com.ibm.aglet.message.Message;
import com.ibm.aglet.security.AgletProtection;
import com.ibm.aglet.security.MessageProtection;
import com.ibm.aglet.security.Protections;

public class TargetAglet extends Aglet implements MobilityListener,
CloneListener, PersistencyListener {
    /**
     * 
     */
    private static final long serialVersionUID = -5710029075498194848L;

    @Override
    public void onCreation(Object init) {
	this.log("onCreation");
	String owner = (String) init;
	Protections protections = new Protections();
	protections.add(new MessageProtection(owner, "setProtections"));
	this.setProtections(protections);
    }

    @Override
    public void onDisposing() {
	this.log("onDisposing");
    }

    @Override
    public void onArrival(MobilityEvent event) {
	this.log("onArrival - " + event.toString());
    }

    @Override
    public void onDispatching(MobilityEvent event) {
	this.log("onDispatching - " + event.toString());
    }

    @Override
    public void onReverting(MobilityEvent event) {
	this.log("onReverting - " + event.toString());
    }

    @Override
    public void onClone(CloneEvent event) {
	this.log("onClone - " + event.toString());
    }

    @Override
    public void onCloned(CloneEvent event) {
	this.log("onCloned - " + event.toString());
    }

    @Override
    public void onCloning(CloneEvent event) {
	this.log("onCloning - " + event.toString());
    }

    @Override
    public void onActivation(PersistencyEvent event) {
	this.log("onActivation - " + event.toString());
    }

    @Override
    public void onDeactivating(PersistencyEvent event) {
	this.log("onDeactivating - " + event.toString());
    }

    @Override
    public void run() {
	// do nothing
    }

    @Override
    public boolean handleMessage(Message msg) {
	this.log("received '" + msg.getKind() + "'");
	if (msg.sameKind("setProtections")) {
	    String name = (String) msg.getArg("name");
	    String actions = (String) msg.getArg("actions");
	    this.log("setProtections(\"" + name + "\", \"" + actions + "\"");

	    Protections protections = new Protections();
	    if ((actions != null) && !actions.equals("")) {
		AgletProtection ap = new AgletProtection(name, actions);
		protections.add(ap);
	    }
	    this.log("setAgletProtection: " + protections.toString());
	    this.setProtections(protections);
	}
	msg.sendReply();
	return true;
    }

    private void log(String s) {
	System.out.println("-----TargetAglet[" + this.getAgletID() + "]: " + s);
    }
}
