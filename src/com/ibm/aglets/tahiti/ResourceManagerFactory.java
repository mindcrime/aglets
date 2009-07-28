package com.ibm.aglets.tahiti;
import com.ibm.aglet.message.MessageManager;
import com.ibm.aglets.AgletRuntime;
import com.ibm.aglets.ResourceManager;
import com.ibm.aglets.security.PolicyImpl;
import com.ibm.aglets.thread.AgletThread;
import com.ibm.awb.misc.*;
import com.ibm.maf.ClassName;
import com.ibm.maf.MAFAgentSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 *  @(#)ResourceManagerFactory.java
 *
 *  IBM Confidential-Restricted
 *
 *  OCO Source Materials
 *
 *  03L7246 (c) Copyright IBM Corp. 1996, 1998
 *
 *  The source code for this program is not published or otherwise
 *  divested of its trade secrets, irrespective of what has been
 *  deposited with the U.S. Copyright Office.
 */
import java.net.URL;

import java.security.AccessController;
//import java.security.Identity;
import java.security.CodeSource;

import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.aglets.log.*;

/**
 * @author     Mitsuru Oshima
 * @version    1.10 $Date: 2009/07/28 07:04:53 $
 */
final class ResourceManagerFactory
         implements com.ibm.aglets.ResourceManagerFactory {

    static Class exportedClass[];

    static String[] exportedClassName = {
            "com.ibm.aglets.AgletProxyImpl", "com.ibm.aglets.DeactivationInfo",
            "com.ibm.aglets.MessageImpl", "com.ibm.aglets.MessageManagerImpl",
            "com.ibm.aglets.SystemMessage", "com.ibm.aglets.AgletImageData",
            "com.ibm.aglets.AgletAudioClip",
            "com.ibm.aglets.ByteArrayImageSource", "com.ibm.awb.misc.Resource",
            "com.ibm.awb.weakref.VirtualRef",
            };

    private static String _agletsClassPath[];
    private static Hashtable _manifests;
    private static String _publicRoot;
    private static String _localAddr;

    private static AgletsLogger logger = AgletsLogger.getLogger(ResourceManagerFactory.class.getName());
    
    private ResourceManager _appResourceManager;
    private Hashtable _map = new Hashtable();

    static {
        Resource res = Resource.getResourceFor("aglets");

        _agletsClassPath = res.getStringArray("aglets.class.path",
                File.pathSeparator);

        _publicRoot = res.getString("aglets.public.root", null);

        if (_publicRoot != null) {
            try {
                _publicRoot = getCanonicalDirectory(_publicRoot);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        _localAddr = MAFAgentSystem.getLocalMAFAgentSystem().getAddress();
        Policy pol = Policy.getPolicy();

        if (pol instanceof PolicyImpl) {
            PolicyImpl policy = (PolicyImpl) pol;

            policy.setSystemCodeBase(_localAddr);
            policy.setPublicRoot(_publicRoot);
        }

        //
        // Search jar/zip files.
        //
        _manifests = new Hashtable();

        for (int i = 0; i < _agletsClassPath.length; i++) {
            lookupJarFiles(_agletsClassPath[i], true);
            if (logger.isDebugEnabled()) {
                logger.debug("Aglet CP: " + _agletsClassPath[i]);
            }
        }

        ClassLoader loader = ResourceManagerFactory.class.getClassLoader();

        exportedClass = new Class[exportedClassName.length];
        for (int i = 0; i < exportedClassName.length; i++) {
            try {
                if (loader == null) {
                    exportedClass[i] = Class.forName(exportedClassName[i]);
                } else {
                    exportedClass[i] = loader.loadClass(exportedClassName[i]);
                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     *  Constructor for the ResourceManagerFactory object
     */
    public ResourceManagerFactory() {

        // - 	ClassLoader loader = this.getClass().getClassLoader();
        // tentative
        // ClassLoader loader = SecureClassLoader.getSecureClassLoader();
        _appResourceManager = new AppResourceManager();
    }


    /**
     *  Gets the canonicalDirectory attribute of the ResourceManagerFactory
     *  class
     *
     * @param  path             Description of Parameter
     * @return                  The canonicalDirectory value
     * @exception  IOException  Description of Exception
     */
    private static String getCanonicalDirectory(String path)
             throws IOException {
        final File file = new File(path);

        try {
            return (String) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        String cp = file.getCanonicalPath();

                        if (!(file.isAbsolute())) {

                            // due to the URL formatting bugs
                            cp = File.separatorChar + cp;
                        }
                        if (cp.charAt(cp.length() - 1) != File.separatorChar) {
                            cp = cp + File.separatorChar;
                        }
                        return cp;
                    }
                });
        } catch (PrivilegedActionException ex) {
            throw (IOException) ex.getException();
        }
    }


    /**
     *  Gets the jarFile attribute of the ResourceManagerFactory class
     *
     * @param  p  Description of Parameter
     * @return    The jarFile value
     */
    private static boolean isJarFile(String p) {
        return p.endsWith(".jar");
    }


    /**
     *  Description of the Method
     *
     * @param  name             Description of Parameter
     * @param  pathList         Description of Parameter
     * @return                  Description of the Returned Value
     * @exception  IOException  Description of Exception
     */
    private static String lookupCodeBaseFrom(final String name, String[] pathList)
             throws IOException {
        final String[] pl = pathList;
        final String classFileName = name.replace('.', File.separatorChar)
                 + ".class";
        logger.debug("lookupCodeBaseFrom()++");
        try {
            return (String) AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        for (int i = 0; i < pl.length; i++) {
                            final File f = new File(pl[i] + File.separator
                                     + classFileName);

                            if (f.exists()) {
                                logger.debug("Found ["+name+"] in "+getCanonicalDirectory(pl[i]));
                                return getCanonicalDirectory(pl[i]);
                                // REMIND:
                                // file URL automatically adds local address to URL.
                                // return new URL(new URL("file", "", absolute).toExternalForm());
                            }
                        }
                        return null;
                    }
                });
        } catch (PrivilegedActionException ex) {
            logger.error("PrivilegedAction error",ex);
            throw (IOException) ex.getException();
        }
    }


    /**
     *  Description of the Method
     *
     * @param  name             Description of Parameter
     * @return                  Description of the Returned Value
     * @exception  IOException  Description of Exception
     */
    private static String lookupCodeBaseInManifest(String name)
             throws IOException {
                 
        logger.debug("lookupCodeBaseInManifest() : ["+name+"]");
        Enumeration e = _manifests.keys();

        while (e.hasMoreElements()) {
            Manifest m = (Manifest) e.nextElement();

            if (m.contains(name.replace('.', '/') + ".class")) {
                logger.debug("Found in manifest.");
                return (String) _manifests.get(m);
            }
        }
        return null;
    }


    /**
     *  Description of the Method
     *
     * @param  path       Description of Parameter
     * @param  recursive  Description of Parameter
     */
    private static void lookupJarFiles(String path, boolean recursive) {
        final File f = new File(path);
        Boolean b =
                (Boolean) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return new Boolean(f.isDirectory());
                }
            });
        boolean isDir = b.booleanValue();

        if (isDir && recursive) {
            AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        String[] list = f.list();
                        String front = f.getPath() + File.separator;

                        for (int i = 0; i < list.length; i++) {
                            lookupJarFiles(front + list[i], false);
                        }
                        return null;
                    }
                });
        } else if (isJarFile(path)) {
            readManifest(path);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  path  Description of Parameter
     */
    private static void readManifest(String path) {
        try {
            path = new File(path).getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        logger.debug("Reading manifest .. " + path);
        try {
            JarArchive j = new JarArchive(path);
            Manifest m = j.getManifest();

            _manifests.put(m, path);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //

    /**
     *  Gets the currentResourceManager attribute of the ResourceManagerFactory
     *  object
     *
     * @return    The currentResourceManager value
     */
    public ResourceManager getCurrentResourceManager() {
        ResourceManager rm = ResourceManagerImpl.getResourceManagerContext();
        
        
        if (rm == null) {
            SecurityManager sm = System.getSecurityManager();

            if (sm != null && sm instanceof AgletsSecurityManager) {
                AgletsSecurityManager asm = (AgletsSecurityManager) sm;
                ClassLoader loader = asm.getCurrentNonSecureClassLoader();

                if (loader instanceof AgletClassLoader) {
                    rm = ((AgletClassLoader) loader);
                } else {
                    logger.debug("Using appResourceManager???");
                    // then, that's secure class loader.
                    rm = _appResourceManager;
                }
            }
        }
        logger.debug("getCurrentResourceManager() : "+ rm );
        return rm;
    }


    /**
     */
    public synchronized void clearCache() {
        _map = new Hashtable();
    }


    /**
     * @param  codebase  Description of Parameter
     * @param  owner     Description of Parameter
     */
    public synchronized void clearCache(URL codebase, Certificate owner) {
        if (codebase == null) {
            clearCache();
        } else {
            CodeSource cs = new CodeSource(codebase, new Certificate[]{
                    owner
                    });

            _map.remove(cs);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  codebase  Description of Parameter
     * @param  owner     Description of Parameter
     * @param  t         Description of Parameter
     * @return           Description of the Returned Value
     */
    public synchronized ResourceManager createResourceManager(URL codebase,
            Certificate owner, ClassName[] t) {
        AgletClassLoader loader = getClassLoaderInCache(codebase, owner, t);
        logger.info("Creating ResourceManager.");
        if (loader == null) {
            loader = createClassLoader(codebase, owner);
        } else {
            logger.debug("Using cached loader: " + codebase);
        }

        String ownerName = null;

        if (owner != null) {
            ownerName =
                    ((java.security.cert.X509Certificate) owner).getSubjectDN()
                    .getName();
        }
        return new ResourceManagerImpl(loader, ownerName);
    }


    /**
     * @param  name  Description of Parameter
     * @return       Description of the Returned Value
     */
    public URL lookupCodeBaseFor(String name) {
        String codebase = null;
        logger.debug("lookupCodeBaseFor()++ : ["+name+"]");
        try {
            codebase = lookupCodeBaseInManifest(name);

            // System.out.println("lookupCodeBaseInManifest("+name+")="+codebase);
            if (codebase == null) {
                codebase = lookupCodeBaseFrom(name, _agletsClassPath);

                // System.out.println("lookupCodeBaseFrom("+name+", _agletsClassPath)="+codebase);
            }
            if (codebase == null) {
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // System.out.println("export root="+_exportRoot);
        if (codebase.startsWith(_publicRoot)) {
            codebase = _localAddr + File.separatorChar
                     + codebase.substring(_publicRoot.length());
        } else {
            if (codebase.startsWith("/")) {
                codebase = "file:" + codebase;
            } else {
                codebase = "file:/" + codebase;
            }
        }

        URL u;
        
        try {
            u = new URL(codebase.replace(File.separatorChar, '/'));
        } catch (Exception ex) {
            logger.error("Error creating URL: ", ex); 
            u = null;
        }
        logger.debug("lookupCodeBaseFor()-- : ["+u+"]");
        return u;
    }


    /**
     *  Gets the classLoaderInCache attribute of the ResourceManagerFactory
     *  object
     *
     * @param  codebase  Description of Parameter
     * @param  owner     Description of Parameter
     * @param  table     Description of Parameter
     * @return           The classLoaderInCache value
     */
    private AgletClassLoader getClassLoaderInCache(URL codebase,
            Certificate owner, ClassName[] table) {

        Certificate[] owners;

        if (owner != null) {
            owners = new Certificate[]{
                    owner
                    };
        } else {
            owners = new Certificate[0];
        }
        CodeSource cs = new CodeSource(codebase, owners);
        Vector v = (Vector) _map.get(cs);

        logger.debug("Looking for cached loader: " + codebase);

        if (table == null && JarAgletClassLoader.isJarFile(codebase)) {
            logger.debug("Codebase is jar file.");
            try {
                final URL fCodebase = codebase;
                ClassName[] tmpTab = (ClassName[]) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws IOException {
                            java.io.InputStream in = fCodebase.openStream();
                            JarArchive jar = new JarArchive(in);

                            // table = jar.getClassNames();
                            Archive.Entry ae[] = jar.entries();

                            ClassName[] tab = new ClassName[ae.length];
                            for (int i = 0; i < ae.length; i++) {
                                tab[i] = new ClassName(ae[i].name(),
                                        DigestTable.toByteArray(ae[i].digest()));
                            }

                            // tab = jar.getDigestTable();
                            in.close();
                            return tab;
                            // Nothing to retrun.
                        }
                    }
                        );
                table = new ClassName[tmpTab.length];
                System.arraycopy(tmpTab, 0, table, 0, table.length);
            } catch (PrivilegedActionException ex) {
                logger.error(ex);
            }
        }

        // REMIND: synchronization. MO
        if (v != null) {
            Enumeration e = v.elements();

            while (e.hasMoreElements()) {
                AgletClassLoader loader = (AgletClassLoader) e.nextElement();

                if (table == null || loader.matchAndImport(table)) {
                    return loader;
                }
            }
        }
        return null;
    }


    /**
     *  Description of the Method
     *
     * @param  codeBase  Description of Parameter
     * @param  owner     Description of Parameter
     * @return           Description of the Returned Value
     */
    private AgletClassLoader createClassLoader(URL codeBase,
            Certificate owner) {
        String oo = null;

        if (owner != null) {
            oo =
                    ((java.security.cert.X509Certificate) owner).getSubjectDN()
                    .getName();
        }
        logger.debug("creating AgletClassLoader: for " + codeBase + " : " + oo);

        // do not forget the scope!
        Certificate[] owners;

        if (owner != null) {
            owners = new Certificate[]{
                    owner
                    };
        } else {
            owners = new Certificate[0];
        }
        CodeSource cs = new CodeSource(codeBase, owners);
        Vector v = (Vector) _map.get(cs);

        if (v == null) {
            v = new Vector();
            _map.put(cs, v);
        }
        AgletClassLoader loader = null;

        try {
            if (JarAgletClassLoader.isJarFile(codeBase)) {
                loader = new JarAgletClassLoader(codeBase, owner);
            } else {
                String f = codeBase.getFile();

                if (f != null && f.endsWith("/") == false) {
                    codeBase = new URL(codeBase, codeBase.getFile() + "/");
                }
                loader = new AgletClassLoader(codeBase, owner);
                for (int i = 0; i < exportedClass.length; i++) {
                    loader.cacheResolvedClass(exportedClass[i]);
                }
            }
            v.insertElementAt(loader, 0);
        } catch (IOException ex) {
            ex.printStackTrace();
            loader = null;
        }
        return loader;
    }


    /*
     *  Just to load classes in the app domain.
     */
    /**
     *  Description of the Class
     *
     * @author    robert
     */
    final class AppResourceManager implements ResourceManager {

        // private ClassLoader _loader;

        /**
         *  Constructor for the AppResourceManager object
         */
        AppResourceManager() { }


        /**
         *  Sets the resourceManagerContext attribute of the AppResourceManager
         *  object
         */
        public void setResourceManagerContext() { }


        /**
         *  Gets the archive attribute of the AppResourceManager object
         *
         * @param  table  Description of Parameter
         * @return        The archive value
         */
        public Archive getArchive(ClassName[] table) {
            return null;
        }


        /**
         *  Gets the classNames attribute of the AppResourceManager object
         *
         * @param  classes  Description of Parameter
         * @return          The classNames value
         */
        public ClassName[] getClassNames(Class[] classes) {
            return null;
        }


        /**
         *  Description of the Method
         *
         * @param  name                        Description of Parameter
         * @return                             Description of the Returned Value
         * @exception  ClassNotFoundException  Description of Exception
         */
        public Class loadClass(String name) throws ClassNotFoundException {
            return Class.forName(name);
        }


        /**
         *  Description of the Method
         *
         * @param  cls  Description of Parameter
         * @return      Description of the Returned Value
         */
        public boolean contains(Class cls) {
            return cls != null && cls.getClassLoader() == null;
        }


        /**
         *  Description of the Method
         *
         * @param  a  Description of Parameter
         */
        public void importArchive(Archive a) { }


        /**
         *  Adds a feature to the Resource attribute of the AppResourceManager
         *  object
         *
         * @param  obj  The feature to be added to the Resource attribute
         */
        public void addResource(Object obj) { }


        /**
         *  Description of the Method
         */
        public void disposeAllResources() { }


        /**
         *  Description of the Method
         *
         * @param  mm  Description of Parameter
         * @return     Description of the Returned Value
         */
        public AgletThread newAgletThread(MessageManager mm) {
            return null;
        }


        /**
         *  Description of the Method
         */
        public void stopAllThreads() { }


        /**
         *  Description of the Method
         */
        public void stopThreadGroup() { }


        /**
         *  Description of the Method
         */
        public void suspendAllThreads() { }


        /**
         *  Description of the Method
         */
        public void resumeAllThreads() { }


        /**
         *  Description of the Method
         */
        public void unsetResourceManagerContext() { }
    }

}

