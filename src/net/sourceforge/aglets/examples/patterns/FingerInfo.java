package net.sourceforge.aglets.examples.patterns;

/*
 * @(#)FingerInfo.java
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

import java.io.Serializable;
import java.util.Date;

/**
 * The FingerInfo class defines the Finger Information.
 * 
 * @see Finger
 * @version 1.00 96/12/28
 * @author Danny B. Lange
 * @author Yariv Aridor
 */

final class FingerInfo implements Serializable {

    // Private variables
    //

    private String _hostName;
    private String _userName;
    private String _homeDirectory;
    private String _workingDirectory;
    private String _architecture;
    private String _osName;
    private String _osVersion;
    private String _javaVersion;
    private Date _localTime;

    // Public methods
    //
    /**
     * A constructor.
     * 
     * @param kind
     *            specify norman notification, expiration, or error exception
     * @param notifier
     *            is URL for identifying the sender aglet.
     * @param number
     *            is used when Notifier stays at remote server to send multiple
     *            instances of Notification. It is used for numbering the
     *            message.
     * @param message
     *            an argument Object; contents of the message.
     */
    FingerInfo(String hostName, String userName, String homeDirectory,
	    String workingDirectory, String architecture, String osName,
	    String osVersion, String javaVersion, Date localTime) {
	this._hostName = hostName;
	this._userName = userName;
	this._homeDirectory = homeDirectory;
	this._workingDirectory = workingDirectory;
	this._architecture = architecture;
	this._osName = osName;
	this._osVersion = osVersion;
	this._javaVersion = javaVersion;
	this._localTime = localTime;
    }

    String getArchitecture() {
	return this._architecture;
    }

    String getHomeDirectory() {
	return this._homeDirectory;
    }

    String getHostName() {
	return this._hostName;
    }

    String getJavaVersion() {
	return this._javaVersion;
    }

    Date getLocalTime() {
	return this._localTime;
    }

    String getOsName() {
	return this._osName;
    }

    String getOsVersion() {
	return this._osVersion;
    }

    String getUserName() {
	return this._userName;
    }

    String getWorkingDirectory() {
	return this._workingDirectory;
    }

    String toTextBlock() {
	String str = "Host Name: " + this._hostName + "\n" + "User Name: "
	+ this._userName + "\n" + "Home Directory: "
	+ this._homeDirectory + "\n" + "Working Directory: "
	+ this._workingDirectory + "\n" + "Machine Architecture: "
	+ this._architecture + "\n" + "OS Name: " + this._osName + "\n"
	+ "OS Version: " + this._osVersion + "\n" + "Java Version: "
	+ this._javaVersion + "\n" + "Local Time: "
	+ this._localTime.toString();

	return str;
    }
}
