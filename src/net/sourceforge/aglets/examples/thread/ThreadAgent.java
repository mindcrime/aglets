package net.sourceforge.aglets.examples.thread;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;

public class ThreadAgent extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1911523676221093402L;
	public static int messageCounter = 0;

	@Override
	public boolean handleMessage(final Message m) {
		printThreadInfo();
		return true;
	}

	protected void printThreadInfo() {
		messageCounter++;
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("\n\tMessagge number " + messageCounter);
		final Thread t = Thread.currentThread();
		final com.ibm.aglets.thread.AgletThread at = (com.ibm.aglets.thread.AgletThread) t;
		System.out.println("Thread name and hash code " + at.getName() + " "
				+ at.hashCode());
		System.out.println("Aglet thread id " + at.getId());
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

	@Override
	public void run() {
		System.out.println("Agent running");
		System.out.println("Hi, I'm ana agent used to check the thread that will deliver");
		System.out.println("a message. You can create several agents and send me messages and I'll");
		System.out.println("show you the thread info.");
		printThreadInfo();
	}
}
