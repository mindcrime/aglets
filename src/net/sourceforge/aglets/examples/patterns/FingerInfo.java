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

	/**
	 * 
	 */
	private static final long serialVersionUID = -858635235655798471L;
	private final String _hostName;
	private final String _userName;
	private final String _homeDirectory;
	private final String _workingDirectory;
	private final String _architecture;
	private final String _osName;
	private final String _osVersion;
	private final String _javaVersion;
	private final Date _localTime;

	// Public methods
	//
	/**
	 * A constructor.
	 * 
	 * @param hostName
	 *            FQDN of the interrogated host.
	 * @param userName
	 *            the user's account name.
	 * @param homeDirectory
	 *            the path to the user's home directory.
	 * @param workingDirectory
	 *            path to the working directory?
	 * @param architecture
	 *            the host's CPU architecture identifier
	 * @param osName
	 *            the identifier of the operating system running on the host.
	 * @param osVersion 
	 *            the version number of the operating system.
	 * @param javaVersion
	 *            the version of the Java Virtual Machine.
	 * @param localTime
	 *            the local time at the moment of the interrogation
	 */
	FingerInfo(final String hostName, final String userName, final String homeDirectory,
			final String workingDirectory, final String architecture, final String osName,
			final String osVersion, final String javaVersion, final Date localTime) {
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
		final String str = "Host Name: " + _hostName + "\n" + "User Name: "
		+ _userName + "\n" + "Home Directory: "
		+ _homeDirectory + "\n" + "Working Directory: "
		+ _workingDirectory + "\n" + "Machine Architecture: "
		+ _architecture + "\n" + "OS Name: " + _osName + "\n"
		+ "OS Version: " + _osVersion + "\n" + "Java Version: "
		+ _javaVersion + "\n" + "Local Time: "
		+ _localTime.toString();

		return str;
	}
}
