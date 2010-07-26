package examples.mdispatcher;

/*
 * @(#)MethodDispatcher.java
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;

import com.ibm.aglet.message.Message;

/**
 * MethodDispatcher class binds a message and a corresponding method. All
 * methods which has a signature <tt> method(Message msg); </tt> are binded and
 * invoked when it receives the corresponding message.
 * 
 * <pre>
 * MethodDispatcher mdispatcher = null;
 * public void onCreation(Object obj){
 * mdispatcher = new MethodDispatcher(this);
 * }
 * 
 * pubic boolean handleMessage(Message m) {
 * return mdispatcher(m);
 * }
 * 
 * // This method is called when the message("doJob") is sent to the aglet.
 * public void doJob(Message m) {
 * // do your job.
 * }
 * </pre>
 * 
 * This may be incorporated with MessageManager in the future.
 * 
 * @version 1.00 $Date: 2009/07/28 07:04:53 $
 * @author Mitsuru Oshima
 */
public class MethodDispatcher implements Serializable {

    private static Class TYPE = null;
    private Object target;

    static {
	try {
	    TYPE = Class.forName("com.ibm.aglet.Message");
	} catch (Exception ex) {
	    System.out.println(ex);
	}
    }

    transient Hashtable method_table = new Hashtable();

    public MethodDispatcher(Object a) {
	this.target = a;
	this.makeTable();
    }

    public boolean handleMessage(Message msg) {
	Method m = (Method) this.method_table.get(msg.getKind());

	if (m != null) {
	    try {
		Object args[] = new Object[1];

		args[0] = msg;
		m.invoke(this.target, args);
	    } catch (IllegalAccessException ex) {

		// should not happen
		return false;
	    } catch (IllegalArgumentException ex) {

		// should not happen
		return false;
	    } catch (InvocationTargetException ex) {

		// if the exception is thrown in the method
		try {
		    if (ex.getTargetException() instanceof Exception) {
			msg.sendException((Exception) ex.getTargetException());
		    } else {

			// temporary
			msg.sendException(ex);
		    }

		    // shold not happen
		} catch (IllegalAccessError exx) {

		    // if a reply has already been sent.
		}
		return false;
	    }
	    return true;
	}
	return false;
    }

    void makeTable() {
	this.method_table = new Hashtable();
	Class clazz = this.target.getClass();
	Method methods[] = clazz.getMethods();

	for (Method m : methods) {
	    Class[] types = m.getParameterTypes();

	    //
	    // select the method whose signature is like
	    // type method(Message msg);
	    //
	    if ((types.length == 1) && (types[0] == TYPE)
		    && Modifier.isPublic(m.getModifiers())) {
		this.method_table.put(m.getName(), m);
	    }
	}
    }

    private void readObject(ObjectInputStream s) throws IOException,
	    ClassNotFoundException {
	this.target = s.readObject();
	this.makeTable();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
	s.writeObject(this.target);
    }
}
