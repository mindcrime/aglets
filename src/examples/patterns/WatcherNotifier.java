package examples.patterns;

/*
 * @(#)WatcherNotifier.java
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

import java.io.File;
import java.util.Date;

/**
 * WatcherNotifier class is a aglet for the file update notification.
 * This aglet stays on a server and keep watching a specifiecd
 * file has updated or not.
 * 
 * @see Notifier
 * @version     1.02    96/12/28
 * @author      Danny B. Lange
 * @author      Yariv Aridor
 */

public class WatcherNotifier extends Notifier {
	private long _lastModified = 0;
	private String _filePath = "";
	private File f;

	/**
	 * This method is to specify the check method for this notifier.
	 * @return the result of the initial check.
	 * @exception AgletException if fails to complete.
	 */
	protected boolean doCheck() throws AgletException {
		long time;

		if (f != null) {

			// get the timestamp of the target file
			time = f.lastModified();
			if (_lastModified != time) {

				// Getting absolute time stamp by Date(time) is not recommended
				// in ApiDoc. We guess use of Date(time) within the machine
				// that we have got "time" stamp value might be OK.
				MESSAGE = _filePath;

				// update _lastModefied timestamp
				_lastModified = time;

				// file has changed on time "Date(time)"
				return true;
			} 
		} else {
			throw new AgletException("Null File object error");
		} 

		// no change on the taget file
		return false;
	}
	/**
	 * This method is to specify the intial check performed by this notifier.
	 * @exception AgletException if fails to complete.
	 */
	protected void initializeCheck() throws AgletException {
		_filePath = (String)ARGUMENT;
		setText("checking update of " + _filePath);
		if ((f = new File(_filePath)) == null) {
			throw new AgletException("Null File object error");
		} 

		if (f.exists()) {
			_lastModified = f.lastModified();
			MESSAGE = _filePath;
			return;
		} else {
			throw new AgletException("Non-Existing File Access");
		} 
	}
}
