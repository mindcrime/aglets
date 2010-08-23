package net.sourceforge.aglets.examples.thread;

import com.ibm.aglet.Aglet;
import com.ibm.aglet.message.Message;

/**
 * Demonstrates the use of threads in the platform.
 * 
 * @author Luca Ferrari cat4hire@users.sourceforge.net 29-mag-2005
 * @version 1.0
 */
public class AgletThread extends Aglet {

    /**
     * Indicates the thread that is executing the run method.
     */
    private transient Thread myThread = null;

    @Override
    public void onCreation(Object init) {
	System.out.println("Welcome to the AgletThread example.");
	System.out.println("This example demonstrates the use of threads in the Aglets platform,");
	System.out.println("please send different messages to this agent. You can do this programmatically (e.g.,");
	System.out.println("using another agent) or by clicking the \"Dialog\" button in the Tahiti GUI.");
    }

    @Override
    public void run() {
	this.myThread = Thread.currentThread();

	System.out.println("The run method is being executed by the thread:");
	System.out.println("\t" + this.myThread);
	System.out.println("Leaving the run method\n");

    }

    @Override
    public boolean handleMessage(Message msg) {
	System.out.println("Handling a message of kind " + msg.getKind());
	System.out.println("The thread in charge of the message handling is:");
	System.out.println("\t" + Thread.currentThread());

	if (this.myThread.equals(Thread.currentThread()) == false) {
	    System.out.println("ATTENTION: the thread that is delivering/handling this message is");
	    System.out.println("different from the one has executed the run method!");
	}
	return true;
    }
}
