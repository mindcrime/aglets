package com.ibm.atp.auth;

/*
 * @(#)SharedSecrets.java
 * 
 * IBM Confidential-Restricted
 * 
 * OCO Source Materials
 * 
 * 03L7246 (c) Copyright IBM Corp. 1996, 1998
 * 
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;
import org.aglets.log.*;

/**
 * The <tt>SharedSecrets</tt> class is a collection of SharedSecret objects.
 * 
 * @version     1.00    $Date: 2002/02/20 22:17:18 $
 * @author      ONO Kouichi
 */
final public class SharedSecrets {
    private static final LogCategory log
            = LogInitializer.getCategory(SharedSecrets.class.getName() );
            
	/**
	 * currenct SharedSecrets object
	 */
	private static SharedSecrets _currentSecrets = null;

	/**
	 * collection of SharedSecret objects as contents
	 */
	private Vector _secrets = null;

	/**
	 * Gets new line string.
	 */
	private static final String PROPERTY_CRLF = "line.separator";
	private static final String DEFAULT_CRLF = "\r\n";
	private static String _strNewLine = null;
	static {
		try {
			_strNewLine = 
				(String)AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					return System.getProperty(PROPERTY_CRLF, DEFAULT_CRLF);
				} 
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	} 

	// -
	// -   public static void main(String args[]) {
	// -     SharedSecrets secrets = createSharedSecretsFile();
	// -     SharedSecrets secrets = getSharedSecrets();
	// -     System.out.println(secrets.toString());
	// -   }
	/**
	 * Constructor creates a secure random generator, and generate
	 * byte sequence as a shared secret (password) for authentication.
	 */
	protected SharedSecrets() {}
	/**
	 * Adds a shared secret.
	 * @param secret a shared secret
	 */
	public void addSharedSecret(SharedSecret secret) {
		if (secret == null) {
			return;
		} 
		String domainName = secret.getDomainName();
		SharedSecret sec = getSharedSecret(domainName);

		if (sec != null) {
			throw new RuntimeException("The shared secret for '" + domainName 
									   + "' is already added.");
		} 
		if (_secrets == null) {
			_secrets = new Vector();
		} 
		_secrets.addElement(secret);
	}
	/**
	 * Converts lines into shared secrets.
	 */
	protected final static SharedSecrets convertLinesToSharedSecrets(Enumeration lines) {
		if (lines == null) {
			return null;
		} 
		SharedSecrets secrets = null;
		Vector sec = null;

		while (lines.hasMoreElements()) {
			String line = ((String)lines.nextElement()).trim();

			if (line.equals("")) {
				if (sec != null) {

					// - System.out.println("convert to shared secret.");
					SharedSecret secret = 
						SharedSecret
							.convertLinesToSharedSecret(sec.elements());

					if (secret != null) {
						if (secrets == null) {
							secrets = new SharedSecrets();
						} 
						secrets.addSharedSecret(secret);
					} else {
						log.error("secret is null.");
					} 
					sec = null;
				} 
			} else {
				if (sec == null) {
					sec = new Vector();
				} 

				// - System.out.println(line);
				sec.addElement(line);
			} 
		} 
		if (sec != null) {

			// - System.out.println("convert to shared secret.");
			SharedSecret secret = 
				SharedSecret.convertLinesToSharedSecret(sec.elements());

			if (secret != null) {
				if (secrets == null) {
					secrets = new SharedSecrets();
				} 
				secrets.addSharedSecret(secret);
			} else {
				System.err.println("secret is null.");
			} 
			sec = null;
		} 
		return secrets;
	}
	/**
	 * Creates shared secrets file.
	 * @return created shared secrets
	 * NOT NEEDED ???????(HT)
	 */
	protected synchronized static SharedSecrets createSharedSecretsFile() {
		return createSharedSecretsFile(getSharedSecretsFilename());
	}
	/**
	 * Creates shared secrets file.
	 * @param filename filename of the shared secrets file to be created
	 * @return created shared secrets
	 * NOT NEEDED????(HT)
	 */
	protected synchronized static SharedSecrets createSharedSecretsFile(String filename) {

		// shall back up ?
		// SharedSecret secret = SharedSecret.createSampleSharedSecret();
		// if (secret != null) {
		_currentSecrets = new SharedSecrets();

		// _currentSecrets.addSharedSecret(secret);
		_currentSecrets.save(filename);

		// }
		return _currentSecrets;
	}
	/**
	 * Gets domain names of shared secrets.
	 * @return enumeration of domain names of shared secrets
	 */
	public Enumeration getDomainNames() {
		Enumeration secrets = secrets();

		if (secrets == null) {
			return null;
		} 
		Vector domainNames = new Vector();

		while (secrets.hasMoreElements()) {
			SharedSecret secret = (SharedSecret)secrets.nextElement();

			domainNames.addElement(secret.getDomainName());
		} 
		return domainNames.elements();
	}
	/**
	 * Gets a shared secret.
	 * @param domainName domain name of the shared secret
	 * @return shared secret whose domain is the specified domain name
	 */
	public SharedSecret getSharedSecret(String domainName) {
		if (domainName == null || domainName.equals("")) {
			throw new IllegalArgumentException("No domain name for shared secret.");
		} 
		Enumeration secrets = secrets();

		if (secrets == null) {
			return null;
		} 
		while (secrets.hasMoreElements()) {
			SharedSecret secret = (SharedSecret)secrets.nextElement();

			if (domainName.equals(secret.getDomainName())) {
				return secret;
			} 
		} 
		return null;
	}
	/**
	 * Gets shared secrets.
	 * @return loaded shared secrets.
	 * If not loaded, load shared secrets from file and return it.
	 */
	public synchronized static SharedSecrets getSharedSecrets() {
		if (_currentSecrets != null && _currentSecrets.isEmpty()) {
			_currentSecrets = null;
		} 
		if (_currentSecrets == null) {
			_currentSecrets = load();
		} 
		if (_currentSecrets == null || _currentSecrets.isEmpty()) {
			_currentSecrets = createSharedSecretsFile();
		} 
		return _currentSecrets;
	}
	/**
	 * Gets file name of shared secrets for challenge-response authentication with digest function.
	 * @return file name of shared secrets for challenge-response authentication with digest function.
	 */
	private static String getSharedSecretsFilename() {
		final String securityDir = FileUtils.getSecurityDirectory();
		final String default_file = securityDir + File.separator 
									+ "secrets.dat";
		Resource res = Resource.getResourceFor("atp");

		if (res == null) {
			return default_file;
		} 
		return res.getString("atp.auth.secrets", default_file);
	}
	/**
	 * Check empty.
	 * @return true if empty, otherwise false
	 */
	public final boolean isEmpty() {
		if (_secrets == null) {
			return true;
		} 
		return _secrets.isEmpty();
	}
	/**
	 * Loads shared secrets.
	 * @return loaded shared secrets
	 */
	protected synchronized static SharedSecrets load() {
		return load(getSharedSecretsFilename());
	}
	/**
	 * Loads shared secrets.
	 * @param filename filename of the shared secrets file to be loaded
	 * @return loaded shared secrets
	 */
	protected synchronized static SharedSecrets load(String filename) {
		SharedSecrets secrets = null;

		try {
			log.info("Loading shared secrets from file " + filename); 
			FileReader freader = new FileReader(filename);
			BufferedReader breader = new BufferedReader(freader);
			Vector lines = null;

			while (true) {
				String line = breader.readLine();

				if (line == null) {

					// end of line
					break;
				} 
				if (lines == null) {
					lines = new Vector();
				} 
				lines.addElement(line);
			} 
			breader.close();
			if (lines == null) {
				log.warn("empty");
				secrets = null;
			} else {
				log.debug("load done.");
				secrets = convertLinesToSharedSecrets(lines.elements());
			} 
		} catch (FileNotFoundException excpt) {

			// something wrong
			log.info("No shared secret file.");
		} catch (IOException excpt) {

			// something wrong
			log.error("Error loading shared secrets.",excpt);
		} 
		return secrets;
	}
	/**
	 * Removes a shared secret.
	 * @param domainName domain name of the shared secret to be removed
	 */
	public void removeSharedSecret(String domainName) {
		if (domainName == null) {
			return;
		} 
		if (_secrets == null) {
			return;
		} 
		SharedSecret secret = getSharedSecret(domainName);

		if (secret == null) {
			throw new RuntimeException("The shared secret for '" + domainName 
									   + "' does not exist.");
		} 
		_secrets.removeElement(secret);
	}
	/**
	 * Saves to file.
	 */
	public void save() {
		save(getSharedSecretsFilename());
	}
	/**
	 * Saves to file.
	 * @param filename filename of the shared secrets file to be saved
	 */
	public void save(String filename) {
		Enumeration lines = toLines();

		if (lines == null) {
			log.info("No secrets.");
			return;
		} 
		try {
			log.info("[Saving shared secrets into file " + filename); 
			FileWriter fwriter = new FileWriter(filename);
			BufferedWriter bwriter = new BufferedWriter(fwriter);

			while (lines.hasMoreElements()) {
				String line = (String)lines.nextElement();

				bwriter.write(line);
				bwriter.newLine();
			} 
			bwriter.flush();
			bwriter.close();
			log.debug("Save complete.");
		} catch (IOException excpt) {

			// something wrong
			log.error("Error saving file.", excpt);
		} 
	}
	/**
	 * Gets shared secrets.
	 * @return enumeration of shared secrets
	 */
	public Enumeration secrets() {
		if (_secrets == null) {
			return null;
		} 
		return _secrets.elements();
	}
	/**
	 * Selects a shared secret.
	 * @param domainNames enumeration of domain names
	 * @return a shared secret whose domain name is a member of specified domain names
	 */
	protected SharedSecret selectSharedSecret(Enumeration domainNames) {
		if (domainNames == null) {
			return null;
		} 
		while (domainNames.hasMoreElements()) {
			String domainName = (String)domainNames.nextElement();
			SharedSecret secret = getSharedSecret(domainName);

			if (secret != null) {
				return secret;
			} 
		} 

		// not found
		return null;
	}
	/**
	 * Returns lines representation of the shared secret.
	 * @return lines representation of the shared secret
	 */
	public Enumeration toLines() {
		Enumeration secrets = secrets();

		if (secrets == null) {
			return null;
		} 
		boolean bFirst = true;
		Vector lines = new Vector();

		while (secrets.hasMoreElements()) {
			if (!bFirst) {
				lines.addElement("");
			} 
			SharedSecret secret = (SharedSecret)secrets.nextElement();
			Enumeration lns = secret.toLines();

			if (lns != null) {
				while (lns.hasMoreElements()) {
					String line = (String)lns.nextElement();

					lines.addElement(line);
				} 
			} 
			bFirst = false;
		} 
		return lines.elements();
	}
	/**
	 * Returns a string representation of the shared secrets.
	 * @return a string representation of the shared secrets
	 */
	public String toString() {
		Enumeration secrets = secrets();

		if (secrets == null) {
			return null;
		} 
		String str = null;

		while (secrets.hasMoreElements()) {
			SharedSecret secret = (SharedSecret)secrets.nextElement();

			if (str == null) {
				str = secret.toString();
			} else {
				str += _strNewLine + secret.toString();
			} 
		} 
		return str;
	}
}
