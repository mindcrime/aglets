package examples.patterns;

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

import java.util.Date;
import java.io.*;

/**
 * The FingerInfo class defines the Finger Information.
 * 
 * @see Finger
 * @version     1.00    96/12/28
 * @author      Danny B. Lange
 * @author      Yariv Aridor
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
	 * @param kind specify norman notification, expiration, or error exception
	 * @param notifier is URL for identifying the sender aglet.
	 * @param number is used when Notifier stays at remote server to send
	 * multiple instances of Notification. It is used for numbering the message.
	 * @param message an argument Object; contents of the message.
	 */
	FingerInfo(String hostName, String userName, String homeDirectory, 
			   String workingDirectory, String architecture, String osName, 
			   String osVersion, String javaVersion, Date localTime) {
		_hostName = hostName;
		_userName = userName;
		_homeDirectory = homeDirectory;
		_workingDirectory = workingDirectory;
		_architecture = architecture;
		_osName = osName;
		_osVersion = osVersion;
		_javaVersion = javaVersion;
		_localTime = localTime;
	}
	String getArchitecture() {
		return _architecture;
	}
	String getHomeDirectory() {
		return _homeDirectory;
	}
	String getHostName() {
		return _hostName;
	}
	String getJavaVersion() {
		return _javaVersion;
	}
	Date getLocalTime() {
		return _localTime;
	}
	String getOsName() {
		return _osName;
	}
	String getOsVersion() {
		return _osVersion;
	}
	String getUserName() {
		return _userName;
	}
	String getWorkingDirectory() {
		return _workingDirectory;
	}
	String toTextBlock() {
		String str = "Host Name: " + _hostName + "\n" + "User Name: " 
					 + _userName + "\n" + "Home Directory: " + _homeDirectory 
					 + "\n" + "Working Directory: " + _workingDirectory 
					 + "\n" + "Machine Architecture: " + _architecture + "\n" 
					 + "OS Name: " + _osName + "\n" + "OS Version: " 
					 + _osVersion + "\n" + "Java Version: " + _javaVersion 
					 + "\n" + "Local Time: " + _localTime.toString();

		return str;
	}
}
