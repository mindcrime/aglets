package examples.thread;

import com.ibm.aglet.AgletProxy;
import com.ibm.aglet.message.Message;

public class ReentrantThreadAgent extends ThreadAgent {

    protected boolean postMessage = false;
    
    public void run(){
	System.out.println("This agent shows the re-entrant message dispatching, that is");
	System.out.println("once it receives a message, it post another message to itself.");
	System.out.println("To avoid looping, the thread posts only one message once it has received");
	System.out.println("a message");
    }
    
    public boolean handleMessage(Message msg){
	try{
        	// change the post message flag
        	this.postMessage = ! this.postMessage;
        	System.out.println("\n\t++++++++++++ MESSAGE RECEIVED: " + msg);
        	this.printThreadInfo();
        	
        	if( this.postMessage ){
        	    System.out.println("\n\t POSTING A MESSAGE TO MYSELF\n");
        	    AgletProxy myself = this.getProxy();
        	    Message mess = new Message("REENTRANT_MESSAGE_TO_MYSELF");
        	    //myself.sendOnewayMessage(mess);
        	    myself.sendMessage(mess);
        	}
        	
        	return true;
	}catch(Exception e){
	    System.err.println("Exception while posting a message");
	    e.printStackTrace();
	    return false;
	}
    }
}
