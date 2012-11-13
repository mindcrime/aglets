package net.sourceforge.aglets.examples.thread;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Message;

public class SleepingAgent extends Aglet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4471670993642296326L;
	protected long sleepingMillisec = 5000;

	@Override
	public boolean handleMessage(final Message msg) {
		System.out.println("Received a message " + msg + " at current time "
				+ System.currentTimeMillis());
		return true;
	}

	@Override
	public void run() {
		try {
			System.out.println("Hi, I'm the sleeping agent. I will now suspend");
			System.out.println("my thread for " + sleepingMillisec
					+ " millisecs");
			System.out.println("While I'm sleeping you can send me messages, but they will be processed");
			System.out.println("only after I woke up again.");
			System.out.println("Current time is " + System.currentTimeMillis());
			suspend(sleepingMillisec);
			System.out.println("Hi, I'm back again!");
			System.out.println("Current time is " + System.currentTimeMillis());
		} catch (final AgletException e) {
			e.printStackTrace();
		}
	}
}
