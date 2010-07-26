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

import java.io.File;

import com.ibm.aglet.AgletException;
import com.ibm.agletx.patterns.Notifier;

/**
 * WatcherNotifier class is a aglet for the file update notification. This aglet
 * stays on a server and keep watching a specifiecd file has updated or not.
 * 
 * @see Notifier
 * @version 1.02 96/12/28
 * @author Danny B. Lange
 * @author Yariv Aridor
 */

public class WatcherNotifier extends Notifier {
    private long _lastModified = 0;
    private String _filePath = "";
    private File f;

    /**
     * This method is to specify the check method for this notifier.
     * 
     * @return the result of the initial check.
     * @exception AgletException
     *                if fails to complete.
     */
    @Override
    protected boolean doCheck() throws AgletException {
	long time;

	if (this.f != null) {

	    // get the timestamp of the target file
	    time = this.f.lastModified();
	    if (this._lastModified != time) {

		// Getting absolute time stamp by Date(time) is not recommended
		// in ApiDoc. We guess use of Date(time) within the machine
		// that we have got "time" stamp value might be OK.
		this.MESSAGE = this._filePath;

		// update _lastModefied timestamp
		this._lastModified = time;

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
     * 
     * @exception AgletException
     *                if fails to complete.
     */
    @Override
    protected void initializeCheck() throws AgletException {
	this._filePath = (String) this.ARGUMENT;
	this.setText("checking update of " + this._filePath);
	if ((this.f = new File(this._filePath)) == null) {
	    throw new AgletException("Null File object error");
	}

	if (this.f.exists()) {
	    this._lastModified = this.f.lastModified();
	    this.MESSAGE = this._filePath;
	    return;
	} else {
	    throw new AgletException("Non-Existing File Access");
	}
    }
}
