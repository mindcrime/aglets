package examples.protection;

import com.ibm.aglet.*;
import com.ibm.aglet.event.*;
import com.ibm.aglet.security.Protections;
import com.ibm.aglet.security.AgletProtection;
import com.ibm.aglet.security.MessageProtection;

public class TargetAglet extends Aglet
	implements MobilityListener, CloneListener, PersistencyListener
{
	public void onCreation(Object init) {
		log("onCreation");
		String owner = (String)init;
		Protections protections = new Protections();
		protections.add(new MessageProtection(owner, "setProtections"));
		setProtections(protections);
	}

	public void onDisposing() {
		log("onDisposing");
	}

	public void onArrival(MobilityEvent event) {
		log("onArrival - " + event.toString());
	}

	public void onDispatching(MobilityEvent event) {
		log("onDispatching - " + event.toString());
	}

	public void onReverting(MobilityEvent event) {
		log("onReverting - " + event.toString());
	}

	public void onClone(CloneEvent event) {
		log("onClone - " + event.toString());
	}

	public void onCloned(CloneEvent event) {
		log("onCloned - " + event.toString());
	}

	public void onCloning(CloneEvent event) {
		log("onCloning - " + event.toString());
	}

	public void onActivation(PersistencyEvent event) {
		log("onActivation - " + event.toString());
	}

	public void onDeactivating(PersistencyEvent event) {
		log("onDeactivating - " + event.toString());
	}

	public void run() {
		// do nothing
	}

	public boolean handleMessage(Message msg) {
		log("received '" + msg.getKind() + "'");
		if (msg.sameKind("setProtections")) {
			String name = (String)msg.getArg("name");
			String actions = (String)msg.getArg("actions");
			log("setProtections(\"" + name + "\", \"" + actions + "\"");

			Protections protections = new Protections();
			if (actions != null &&!actions.equals("")) {
				AgletProtection ap = new AgletProtection(name, actions);
				protections.add(ap);
			}
			log("setAgletProtection: " + protections.toString());
			setProtections(protections);
		}
		msg.sendReply();
		return true;
	}

	private void log(String s) {
		System.out.println("-----TargetAglet[" + getAgletID() + "]: " + s);
	}
}
