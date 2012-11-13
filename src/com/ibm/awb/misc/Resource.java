package com.ibm.awb.misc;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import net.sourceforge.aglets.log.AgletsLogger;

/*
 * @(#)Resource.java
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

/**
 * The <tt>Resource </tt> class
 * 
 * @version 1.00 96/11/15
 * @author Danny B. Lange
 * @author Mitsuru Oshima
 */

public class Resource {
	private static Hashtable resourceTable = new Hashtable();
	private static Properties options = null;
	private static AgletsLogger logger = AgletsLogger.getLogger(Resource.class.getName());
	static {

		/*
		 * Create system resource.
		 */
		resourceTable.put("system", new Resource());
	}

	private static final String PROTOCOL_FILE = "file";

	/**
	 * Creates named resources with default Proeprties object.
	 * 
	 * @see #getResourceFor
	 */
	synchronized static public Resource createResource(
	                                                   final String name,
	                                                   final Properties defaults) {
		return createResource(name, (URL) null, defaults);
	}

	/**
	 * Creates named resources with file and default Proeprties object.
	 * 
	 * @see #getResourceFor
	 */
	synchronized static public Resource createResource(
	                                                   final String name,
	                                                   final String file,
	                                                   final Properties defaults)
	throws java.net.MalformedURLException {
		final URL url = file == null ? null : new URL(PROTOCOL_FILE, "", file);

		return createResource(name, url, defaults);
	}

	/**
	 * Creates named resources with file and default Proeprties object.
	 * 
	 * @see #getResourceFor
	 */
	synchronized static private Resource createResource(
	                                                    final String name,
	                                                    final URL file,
	                                                    final Properties defaults) {
		if (resourceTable.contains(name)) {
			throw new SecurityException("cannot re-create existing resource");
		}
		logger.debug("Creating resource: " + name + " url: " + file);
		final Resource res = new Resource(file, defaults);

		resourceTable.put(name, res);
		return res;
	}

	/**
	 * Get the resource object by name.
	 */
	static public Resource getResourceFor(final String name) {
		return (Resource) resourceTable.get(name);
	}

	/*
	 * public void toString() { }
	 */

	/*
	 * 
	 */

	/**
	 * Gets system properties
	 */
	static private void getSystemProperties() {
		if (options == null) {
			try {
				options = System.getProperties();
			} catch (final SecurityException ex) {
				options = new Properties();
			}
		}
	}

	/**
	 * Convenient function to convert Color object to string representation.
	 */
	static public String toString(final Color color) {
		return String.valueOf(color.getRGB());
	}

	/**
	 * Convenient function to convert Font object to string representation.
	 */
	static public String toString(final Font font) {
		String style = null;

		if (font.isBold()) {
			style = font.isItalic() ? "bolditalic" : "bold";
		} else {
			style = font.isItalic() ? "italic" : "plain";
		}
		return font.getName() + '-' + style + '-' + font.getSize();
	}

	/*
	 * The property for command option.
	 */
	private Properties option = null;

	/*
	 * The default property
	 */
	private Properties defaults = null;

	/*
	 * The persistent property
	 */
	private Properties persistent = null;

	/*
	 * The directory to save the property file in.
	 */
	private URL _saveURL = null;

	/*
	 * Constructs the Resource for System properties.
	 */
	private Resource() {
		try {
			defaults = System.getProperties();
			persistent = new Properties(defaults);
			System.setProperties(persistent);
		} catch (final SecurityException ex) {
			defaults = new Properties();
			persistent = new Properties(defaults);
		}
		option = new Properties(persistent);
	}

	/*
	 * Constructs using user-defined directory for property file.
	 * 
	 * @param path the directory used to store this property object.
	 * 
	 * @param defaults the default properties
	 */
	private Resource(final URL url, Properties defaults) {
		if (defaults == null) {
			defaults = new Properties();
		}
		this.defaults = defaults;
		persistent = new Properties(defaults);
		option = new Properties(persistent);
		if (url == null) {
			return;
		}
		if (load(url) == false) {
			logger.error("Could not load resource from [" + url + "]");
		}
	}

	/**
	 * Appends a value to a resource.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be append at the end of the original value
	 */
	public void appendResource(final String key, final String value) {
		if (value == null) {
			return;
		}
		final String r = this.getString(key, "");

		persistent.put(key, r + ' ' + value);
	}

	public boolean getBoolean(final String key, final boolean defaultValue) {
		final String v = option.getProperty(key);

		return v == null ? defaultValue : v.equalsIgnoreCase("true");
	}

	/*
	 * 
	 */
	public Color getColor(final String key, final Color defaultColor) {
		final String color = option.getProperty(key);

		if (color == null) {
			return defaultColor;
		}
		try {
			return new Color(Integer.parseInt(color));
		} catch (final NumberFormatException ex) {
			return defaultColor;
		}
	}

	/**
	 * 
	 */
	public Font getFont(final String key, final Font defaultFont) {
		String value = option.getProperty(key);

		if (value == null) {
			return defaultFont;
		}
		String fontName = "Dialogger";
		int fontSize = 12;
		int fontStyle = Font.PLAIN;

		int i = value.indexOf('-');

		if (i >= 0) {
			fontName = value.substring(0, i);
			value = value.substring(i + 1);
			if ((i = value.indexOf('-')) >= 0) {
				if (value.startsWith("plain-")) {
					fontStyle = Font.PLAIN;
				} else if (value.startsWith("bold-")) {
					fontStyle = Font.BOLD;
				} else if (value.startsWith("italic-")) {
					fontStyle = Font.ITALIC;
				} else if (value.startsWith("bolditalic-")) {
					fontStyle = Font.BOLD | Font.ITALIC;
				}
				value = value.substring(i + 1);
			}
			try {
				fontSize = Integer.valueOf(value).intValue();
			} catch (final NumberFormatException e) {
				return defaultFont;
			}
		}
		return new Font(fontName, fontStyle, fontSize);
	}

	public int getInteger(final String key, final int defaultValue) {
		final String v = option.getProperty(key);

		return v == null ? defaultValue : Integer.parseInt(v);
	}

	/**
	 * Get the resources starting with the key
	 * 
	 * @param startsWith
	 *            the key to search
	 */
	public String[] getPersistentResourcesStartsWith(final String startsWith) {
		final Enumeration e = persistent.keys();
		final java.util.Vector v = new java.util.Vector();

		while (e.hasMoreElements()) {
			final String k = (String) e.nextElement();

			if (k.startsWith(startsWith)) {
				v.addElement(k);
			}
		}
		final String[] array = new String[v.size()];

		v.copyInto(array);
		return array;
	}

	/**
	 * 
	 */
	public String getString(final String key) {
		return option.getProperty(key);
	}

	public String getString(final String key, final String defaultValue) {
		return option.getProperty(key, defaultValue);
	}

	/**
	 * 
	 */
	public String[] getStringArray(final String key, final String sep) {
		final String v = this.getString(key, null);

		if (v == null) {
			return new String[0];
		}
		final java.util.StringTokenizer st = new java.util.StringTokenizer(v, sep, false);
		final String ret[] = new String[st.countTokens()];
		int i = 0;

		while (st.hasMoreTokens()) {
			ret[i++] = st.nextToken();
		}
		return ret;
	}

	/**
	 */
	public URL getURL(final String key, final URL defaultValue) {
		final String v = option.getProperty(key);

		try {
			return v == null ? defaultValue : new URL(v);
		} catch (final java.net.MalformedURLException ex) {
			logger.error("fail to convert '" + v + "' to URL", ex);
			return defaultValue;
		}
	}

	/**
	 * Imports all properties start with "key" into this resource. All imported
	 * properties are removed from "from" object.
	 */
	public void importOptionProperties(final String startsWith) {
		getSystemProperties();
		final Enumeration e = options.propertyNames();

		while (e.hasMoreElements()) {
			final String key = (String) e.nextElement();

			if (key.startsWith(startsWith)) {
				setOptionResource(key, options.getProperty(key));
				options.remove(key);
			}
		}
	}

	/*
	 * 
	 */
	public void list(final java.io.PrintStream out) {
		option.list(System.out);
	}

	/**
	 * Loads the properties from the file
	 */
	public boolean load(final URL loadFrom) {
		if ("file".equalsIgnoreCase(loadFrom.getProtocol())) {
			if (FileUtils.ensureDirectory(loadFrom.getFile()) == false) {
				logger.error("Could not create directory [" + loadFrom + "]");
				return false;
			}
			if (FileUtils.ensureFile(loadFrom.getFile()) == false) {
				logger.error("Could not create file [" + loadFrom + "]");
				return false;
			}
		}
		_saveURL = loadFrom; // set as default save url.

		try {
			final InputStream in = loadFrom.openStream();

			persistent.load(in);
			in.close();
		} catch (final Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Merge a value to a resource. If the same value exists in the original
	 * value, it is just ignored.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be merged.
	 */
	public void mergeResource(final String key, final String value) {
		if (value == null) {
			return;
		}
		final String list = this.getString(key, "");

		if (list.indexOf(value) >= 0) {
			return;
		} else {
			appendResource(key, value);
		}
	}

	/*
	 * Remves the resource value from option resources specified by the key
	 * 
	 * @param the key
	 */
	public void removeOptionResource(final String key) {
		option.remove(key);
	}

	/**
	 * 
	 */
	public void removePersistentResourcesStartsWith(final String startsWith) {
		Enumeration e = persistent.keys();
		final java.util.Vector v = new java.util.Vector();

		while (e.hasMoreElements()) {
			final String k = (String) e.nextElement();

			if (k.startsWith(startsWith)) {
				v.addElement(k);
			}
		}
		e = v.elements();
		while (e.hasMoreElements()) {
			persistent.remove(e.nextElement());
		}
	}

	/*
	 * Remves the resource value specified by the key
	 * 
	 * @param the key
	 */
	public void removeResource(final String key) {
		persistent.remove(key);
	}

	/**
	 * Saves the properties into the default file.
	 * 
	 * @param header
	 *            header string
	 */
	public boolean save(final String header) {
		return this.save(_saveURL, header);
	}

	/**
	 * Saves the properties into the file with given header.
	 * 
	 * @param saveTo
	 *            the URL pointing to the file location
	 * @param header
	 *            string to be saved at the top of file
	 */
	public boolean save(final URL saveTo, final String header) {
		if ((saveTo == null)
				|| ("file".equalsIgnoreCase(saveTo.getProtocol()) == false)) {

			// throw new IOException("no file name");
			return false;
		}
		try {
			logger.info("saving properties into [" + saveTo + " ]");

			// System.out.println(_propertyURL.getFile());
			final File file = new File(saveTo.getFile());

			if ((file.exists() == false) || (file.isFile() && file.canWrite())) {
				final FileOutputStream out = new FileOutputStream(file);

				persistent.store(out, header);
				out.close();
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Sets default property. Default properties have last priority and are not
	 * persistent.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be stored as a default
	 */
	public void setDefaultResource(final String key, final String value) {

		/*
		 * Initializers cannot override default properties
		 */
		if (defaults.get(key) != null) {
			final Exception ex = new Exception("Cannot override default properties:"
					+ key + " = " + value + " , current value = "
					+ defaults.get(key));

			ex.printStackTrace();
			return;
		}
		defaults.put(key, value);
	}

	/**
	 * Sets default properties. Default properties have last priority and are
	 * not persistent.
	 * 
	 * @param key_value_pairs
	 *            array of two-element arrays, with the key on the position 0
	 *            and the value on position 1
	 */
	public void setDefaultResources(final String[][] key_value_pairs) {
		for (final String[] key_value_pair : key_value_pairs) {
			setDefaultResource(key_value_pair[0], key_value_pair[1]);
		}
	}

	/**
	 * Sets an option property. Option properties have first priority and are
	 * not persistent. These will not be saved in the file.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be stored as a option resource
	 */
	public void setOptionResource(final String key, final String value) {
		option.put(key, value);
	}

	/**
	 * Sets a property. Option properties are persistent and will be saved in
	 * the strage.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be stored
	 */
	public void setResource(final String key, final String value) {
		persistent.put(key, value);
	}
}
