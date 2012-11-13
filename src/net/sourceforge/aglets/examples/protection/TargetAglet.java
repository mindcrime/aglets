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
	public boolean handleMessage(final Message msg) {
		log("received '" + msg.getKind() + "'");
		if (msg.sameKind("setProtections")) {
			final String name = (String) msg.getArg("name");
			final String actions = (String) msg.getArg("actions");
			log("setProtections(\"" + name + "\", \"" + actions + "\"");

			final Protections protections = new Protections();
			if ((actions != null) && !actions.equals("")) {
				final AgletProtection ap = new AgletProtection(name, actions);
				protections.add(ap);
			}
			log("setAgletProtection: " + protections.toString());
			setProtections(protections);
		}
		msg.sendReply();
		return true;
	}

	private void log(final String s) {
		System.out.println("-----TargetAglet[" + getAgletID() + "]: " + s);
	}

	@Override
	public void onActivation(final PersistencyEvent event) {
		log("onActivation - " + event.toString());
	}

	@Override
	public void onArrival(final MobilityEvent event) {
		log("onArrival - " + event.toString());
	}

	@Override
	public void onClone(final CloneEvent event) {
		log("onClone - " + event.toString());
	}

	@Override
	public void onCloned(final CloneEvent event) {
		log("onCloned - " + event.toString());
	}

	@Override
	public void onCloning(final CloneEvent event) {
		log("onCloning - " + event.toString());
	}

	@Override
	public void onCreation(final Object init) {
		log("onCreation");
		final String owner = (String) init;
		final Protections protections = new Protections();
		protections.add(new MessageProtection(owner, "setProtections"));
		setProtections(protections);
	}

	@Override
	public void onDeactivating(final PersistencyEvent event) {
		log("onDeactivating - " + event.toString());
	}

	@Override
	public void onDispatching(final MobilityEvent event) {
		log("onDispatching - " + event.toString());
	}

	@Override
	public void onDisposing() {
		log("onDisposing");
	}

	@Override
	public void onReverting(final MobilityEvent event) {
		log("onReverting - " + event.toString());
	}

	@Override
	public void run() {
		// do nothing
	}
}
