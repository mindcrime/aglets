/**
 * 
 */
package com.ibm.aglets.tahiti;

import java.security.cert.Certificate;

/**
 * A wrapper to keep track of the login data for the running Tahiti instance.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         Jul 12, 2010
 */
public class LoginData {

    /**
     * The current username the user is running the Tahiti instance.
     */
    private static String username = null;

    /**
     * The current certificate associated to the user.
     */
    private static Certificate certificate = null;

    /**
     * Gets back the username.
     * 
     * @return the username
     */
    public static synchronized final String getUsername() {
	return username;
    }

    /**
     * Sets the username value.
     * 
     * @param username
     *            the username to set
     */
    static synchronized final void setUsername(String username) {
	LoginData.username = username;
    }

    /**
     * Gets back the certificate.
     * 
     * @return the certificate
     */
    public static synchronized final Certificate getCertificate() {
	return certificate;
    }

    /**
     * Sets the certificate value.
     * 
     * @param certificate
     *            the certificate to set
     */
    static synchronized final void setCertificate(Certificate certificate) {
	LoginData.certificate = certificate;
    }

}
