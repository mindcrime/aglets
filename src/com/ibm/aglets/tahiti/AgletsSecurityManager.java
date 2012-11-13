package com.ibm.aglets.tahiti;

/*
 * @(#)AgletsSecurityManager.java
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

import java.security.Permission;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;

import com.ibm.aglet.system.AgletRuntime;
import com.ibm.aglets.ResourceManager;

public class AgletsSecurityManager extends SecurityManager {

	@Override
	public void checkPermission(final Permission p) {
		if (AgletRuntime.getAgletRuntime().isSecure()) {
			super.checkPermission(p);
		}
	}

	@Override
	public boolean checkTopLevelWindow(final Object window) {
		final ResourceManager rm = ResourceManagerImpl.getResourceManagerContext();

		if (rm != null) {
			rm.addResource(window);
		} else {

			// System.out.println("Warning: ResourceManager not found");
		}

		// return ProtectionDomain.isInAppDomain();
		return true; // ???(HT)
	}

	/**
	 * Gets the certificate of the owner of the current thread.
	 * 
	 * @return Certificate of the owner of the current thread, null if it's
	 *         unknown.
	 */
	public Certificate getCurrentCertificate() {
		final ClassLoader loader = getCurrentNonSecureClassLoader();

		if ((loader != null) && (loader instanceof AgletClassLoader)) {
			return ((AgletClassLoader) loader).getOwnerCertificate();
		}
		return null;
	}

	/**
	 * Gets the current non secure class loader.
	 * 
	 * @return java.lang.ClassLoader
	 */
	public ClassLoader getCurrentNonSecureClassLoader() {
		final Class c[] = getClassContext();

		for (int i = 1; i < c.length; i++) {
			final ClassLoader loader = c[i].getClassLoader();

			if (!(loader instanceof SecureClassLoader) && (loader != null)) {
				return loader;
			}
		}
		return null;
	}

	@Override
	public ThreadGroup getThreadGroup() {
		final ResourceManagerImpl rm = ResourceManagerImpl.getResourceManagerContext();

		if (rm == null) {
			return Thread.currentThread().getThreadGroup();
		} else {
			return rm.getThreadGroup();
		}
	}
}
