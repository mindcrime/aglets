package examples.patterns;

/*
 * @(#)WriterSlave.java
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

import com.ibm.aglet.*;
import com.ibm.agletx.patterns.*;
import com.ibm.aglet.util.*;

import java.util.Vector;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Properties;

/**
 * WriterSlave is a aglet of Slave pattern that moves to a remote
 * site and displays a message
 * 
 * @see Slave
 * @version     1.00    96/12/28
 * @author      Yariv Aridor
 * 
 */

public class WriterSlave extends Slave {

	// time (in secs) of showing the message window.
	private final int SHOW_TIME = 10;

	protected void doJob() throws AgletException {
		WriterSlaveWindow win = null;

		try {
			Arguments args = (Arguments)ARGUMENT;
			String from = new String((String)(args.getArg("user")) + "@" 
									 + new URL(getOrigin()).getHost());

			win = new WriterSlaveWindow(this, (String)(args.getArg("msg")), 
										from);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AgletException("unable to create a remote message window");
		} 
		suspend();
		win.dispose();
		setResult("returned" 
				  + ((RESULT != null) ? ":" + (String)RESULT : "."));
	}
	protected void initializeJob() {
		RESULT = null;
	}
	void setResult(String text) {
		RESULT = text;
	}
	public synchronized void suspend() {
		try {
			wait(SHOW_TIME * 1000);
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}
	public synchronized void wakeup() {
		notify();
	}
}
