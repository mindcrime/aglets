/*
 * Created on Oct 1, 2004
 *
 * @author Luca Ferrari
 */
package com.ibm.aglets.tahiti.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A class to store icons for a GUI.
 * 
 * @author Luca Ferrari <A
 *         HREF="mailto:cat4hire@users.sourceforge.net">cat4hire@users.
 *         sourceforge.net</A>
 */
public class IconRepository {

	/**
	 * Init with the default property file, that is AGLETS_HOME/cnf/icons.prop
	 * 
	 */
	public static void defaultInit() {
		loadIconFromPropertyFile(System.getProperty("aglets.home"
				+ "/cnf/icons.prop"));
	}

	/**
	 * A method to get an icon. The method searches in the hash if the icon
	 * exists, if so it is returned, otherwise it is loaded from a file.
	 * 
	 * @param iconName
	 *            the name of the icon requested
	 * @return the icon object or null
	 */
	public static synchronized Icon getIcon(final String iconName) {
		if (IconRepository._mySelf == null) {
			IconRepository.newInstance();
		}

		return (Icon) IconRepository._mySelf._icons.get(iconName);
	}

	/**
	 * A method to get the image of an icon from its symbolic name.
	 * 
	 * @param iconName
	 *            the name of the icon
	 */
	public static synchronized Image getImage(final String iconName) {
		if (IconRepository._mySelf == null) {
			IconRepository.newInstance();
		}

		if (IconRepository._mySelf._images.contains(iconName) == false) {
			// load the new image
			IconRepository._mySelf._images.put(iconName, Toolkit.getDefaultToolkit().getImage(iconName));
		}

		return (Image) IconRepository._mySelf._images.get(iconName);
	}

	/**
	 * Load all icons in the property file. The method takes the name of a
	 * property file that contains a few lines like key=filename and loads all
	 * the icons.
	 * 
	 * @param propFile
	 *            the property file name
	 */
	public static synchronized void loadIconFromPropertyFile(final String propFile) {

		final String iconPath = System.getProperty("aglets.icons.path", null);

		if (IconRepository._mySelf == null) {
			IconRepository.newInstance();
		}

		final Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propFile));

			// get all the keys
			String key = null, value = null;
			final Enumeration keys = prop.keys();
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				value = prop.getProperty(key);

				// if the filename is relative, add the icon path
				if (!value.startsWith("/") && (iconPath != null)) {
					value = iconPath + "/" + value;
				}

				IconRepository._mySelf.loadAndStore(value, key);

			}
		} catch (final Exception ex) {
			System.err.println("Exception during icon loading from file "
					+ propFile);
			ex.printStackTrace();
		}
	}

	public static void main(final String a[]) {
		IconRepository.loadIconFromPropertyFile(a[0]);
	}

	/**
	 * Stores the icons once they are loaded, the icons are accessible by
	 * symbolic names such as "ok", "cancel", "create", and so on.
	 */
	private final Hashtable _icons;

	/**
	 * An hash table for images.
	 */
	private final Hashtable _images;

	/**
	 * Auto reference to implement a factory like class.
	 */
	private static IconRepository _mySelf;

	/**
	 * A method to instantiate this class.
	 */
	private static void newInstance() {
		if (_mySelf == null) {
			_mySelf = new IconRepository();
		}
	}

	/**
	 * Private constructor.
	 * 
	 */
	private IconRepository() {
		_icons = new Hashtable(10);
		_images = new Hashtable(10);
	}

	/**
	 * A method to store an icon in the hash starting from its file name.
	 * 
	 * @param fileName
	 *            the file that contains the icon
	 * @param key
	 *            the symbolic name of the icon
	 * @return the icon object
	 */
	public Icon loadAndStore(final String fileName, final String key) {
		// check params
		if ((fileName == null) || (key == null)) {
			return null;
		}

		// load the file and store the icon
		final Icon icon = new ImageIcon(fileName);
		if (icon != null) {
			_icons.put(key, icon);
		}

		return icon;
	}
}
