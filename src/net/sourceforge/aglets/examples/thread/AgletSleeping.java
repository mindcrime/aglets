package net.sourceforge.aglets.examples.thread;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.AgletException;
import com.ibm.aglet.message.Message;

/**
 * An example of aglet that sleeps for a specific amount of time.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0 7/10/2005
 * 
 */
public class AgletSleeping extends Aglet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 764665884852314091L;

	@Override
	public boolean handleMessage(final Message msg) {
		System.out.println("Handling a message of kind " + msg.getKind());
		System.out.println("The thread in charge of the message handling is:");
		System.out.println("\t" + Thread.currentThread());

		return true;
	}

	@Override
	public void run() {
		long startTime = 0;
		long endTime = 0;
		final long waitTime = 20000;

		System.out.println("\n\tSleeping aglet!");
		System.out.println("\n\tThe agent will sleep for " + waitTime / 1000
				+ " seconds");
		startTime = System.currentTimeMillis();
		System.out.println("Current time is " + startTime);
		try {
			sleep(waitTime);
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final AgletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		System.out.println("Current time is " + endTime);
		System.out.println("Aglet slept for " + (endTime - startTime)
				+ " millisecs.");

	}
}
