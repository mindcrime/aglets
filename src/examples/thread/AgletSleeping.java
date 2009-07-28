package examples.thread;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;

/**
 * An example of aglet that sleeps for a specific amount of time.
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * @version 1.0
 * 7/10/2005
 *
 */
public class AgletSleeping extends Aglet {
	public void run(){
		long startTime = 0;
		long endTime   = 0;
		long waitTime  = 20000;
		
		System.out.println("\n\tSleeping aglet!");
		System.out.println("\n\tThe agent will sleep for "+waitTime/1000+" seconds");
		startTime = System.currentTimeMillis();
		System.out.println("Current time is " + startTime);
		this.sleep(waitTime);
		endTime = System.currentTimeMillis();
		System.out.println("Current time is "+endTime);
		System.out.println("Aglet slept for "+(endTime-startTime)+" millisecs.");
		
	}
	
	public boolean handleMessage(Message msg){
		System.out.println("Handling a message of kind "+msg.getKind());
		System.out.println("The thread in charge of the message handling is:");
		System.out.println("\t"+Thread.currentThread());
		
		return true;
	}
}
