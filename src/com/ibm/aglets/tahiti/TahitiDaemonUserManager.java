package com.ibm.aglets.tahiti;

/*
 * $Id: TahitiDaemonUserManager.java,v 1.2 2001/08/01 03:46:59 kbd4hire Exp $
 *
 * @(#)TahitiDaemonUserManager.java
 *
 */

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.awb.misc.Resource;
import com.ibm.awb.misc.FileUtils;

import java.security.cert.Certificate;
import java.security.KeyException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

import java.io.IOException;

/**
 *  Provides user authentication for the Tahiti Daemon.
 *
 * @author     Larry Spector
 * @created    July 20, 2001
 * @version $Revision: 1.2 $ $Date: 2001/08/01 03:46:59 $ $Author: kbd4hire $
 */
public final class TahitiDaemonUserManager extends UserManager {

    private static boolean              _verbose = false;


    /**
     *  Constructor for the TahitiDaemonUserManager object
     *
     * @since 1.0
     */
    public TahitiDaemonUserManager() {
        _verbose = Boolean.getBoolean(System.getProperties().getProperty("verbose"));
    }


    /**
     *  Verify the user and return their certificate.
     *
     * @return    The user's certificate
     * @since 1.0
     */
    public Certificate login() {
        AgletRuntime runtime = AgletRuntime.getAgletRuntime();

        if (runtime == null) {
            return null;
        }
        Certificate cert = null;
        String username = null;

        while (cert == null) {
            while (username == null) {
                username = inputUsername("login");
            }
            String password = input("password", "", false);

            if (password == null) {
                password = "";
            }
            cert = runtime.authenticateOwner(username, password);
            if (cert == null) {
                if (_verbose) {
                    System.out.println("Password is incorrect.");
                }
                username = null;
            }
        }
        setUsername(username);
        setCertificate(cert);
        return cert;
    }


    /**
     *  Loads the aglets properties file.
     *
     * @param  username  Name of the user
     * @return           The string resources for the user.
     * @since 1.0
     */
    private Resource createAgletsResourceForUser(String username) {
        Resource res = Resource.getResourceFor("aglets");

        if (res == null) {
            try {
                String propfile =
                        FileUtils.getPropertyFilenameForUser(username, "aglets");

                res = Resource.createResource("aglets", propfile, null);
                if (_verbose) {
                    System.out.println("reading aglets property from "
                             + propfile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }


    /**
     *  Reads input line by line from standand in.
     *
     * @param  title    Description of Parameter
     * @param  defval   Description of Parameter
     * @param  enforce  Description of Parameter
     * @return          User's keyboard input.
     * @since 1.0
     */
    private String input(String title, String defval, boolean enforce) {
        String line = null;
        LineNumberReader r =
                new LineNumberReader(new InputStreamReader(System.in));

        while (true) {
            if (_verbose) {
                System.out.print(title
                         + (defval == null || defval.length() == 0 ? ":"
                         : "[" + defval + "]:"));
                System.out.flush();
            }
            try {
                line = r.readLine();
            } catch (IOException ex) {
            }
            if (line == null) {
                System.exit(1);
            }
            if (line.trim().length() != 0) {
                return line.trim();
            } else if (defval != null && defval.length() != 0) {
                return defval;
            } else if (enforce == false) {
                return null;
            }
        }
    }


    /**
     *  Description of the Method
     *
     * @param  title  Description of Parameter
     * @return        Description of the Returned Value
     * @since 1.0
     */
    private String inputUsername(String title) {
        return inputUsername(title, getDefaultUsername());
    }


    /**
     *  Description of the Method
     *
     * @param  title            Description of Parameter
     * @param  defaultUsername  Description of Parameter
     * @return                  Description of the Returned Value
     * @since 1.0
     */
    private String inputUsername(String title, String defaultUsername) {
        return input(title, defaultUsername, true);
    }
}

