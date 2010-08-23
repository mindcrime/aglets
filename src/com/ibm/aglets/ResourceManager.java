package com.ibm.aglets;

/*
 * @(#)ResourceManager.java
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

import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.awb.misc.Archive;
import com.ibm.maf.ClassName;

public interface ResourceManager {

    /*
     * ================================================== Misc
     * ==================================================
     */

    // public java.net.URL getResource(String name);

    // public byte[] getResourceAsByteArray(String name);

    // public java.io.InputStream getResourceAsStream(String name);

    // public void putResource(String name, byte[] res);

    /*
     * ================================================== Window
     * ==================================================
     */
    public void addResource(Object obj);

    /**
     * return false if not found.
     */
    public boolean contains(Class cls);

    public void disposeAllResources();

    /**
     * Archives that this resource manager is managing. public Archive
     * getArchive(DigestTable table);
     */
    public Archive getArchive(ClassName[] table);

    /**
     * 
     * public DigestTable getDigestTable(Class[] classes);
     */
    public ClassName[] getClassNames(Class[] classes);

    /*
     * 
     */
    public void importArchive(Archive a);

    /*
     * ================================================== Byte Code Management.
     * ==================================================
     */
    public Class loadClass(String name) throws ClassNotFoundException;

    /*
     * ================================================== Thread Management
     * ==================================================
     */
    public AgletThread newAgletThread(MessageManager mm);

    public void resumeAllThreads();

    /*
     * ================================================== Context Management
     * ==================================================
     */
    void setResourceManagerContext();

    public void stopAllThreads();

    public void stopThreadGroup();

    public void suspendAllThreads();

    void unsetResourceManagerContext();
}
