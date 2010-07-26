package examples.protection;

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

    public void onArrival(MobilityEvent event) {
	this.log("onArrival - " + event.toString());
    }

    public void onDispatching(MobilityEvent event) {
	this.log("onDispatching - " + event.toString());
    }

    public void onReverting(MobilityEvent event) {
	this.log("onReverting - " + event.toString());
    }

    public void onClone(CloneEvent event) {
	this.log("onClone - " + event.toString());
    }

    public void onCloned(CloneEvent event) {
	this.log("onCloned - " + event.toString());
    }

    public void onCloning(CloneEvent event) {
	this.log("onCloning - " + event.toString());
    }

    public void onActivation(PersistencyEvent event) {
	this.log("onActivation - " + event.toString());
    }

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
