package com.ibm.awb.misc;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.awt.Font;
import java.awt.Color;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

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
 * @version     1.00    96/11/15
 * @author      Danny B. Lange
 * @author	Mitsuru Oshima
 */

public class Resource {
	private static Hashtable resourceTable = new Hashtable();
	private static Properties options = null;

	static {

		/*
		 * Create system resource.
		 */
		resourceTable.put("system", new Resource());
	} 

	private static final String PROTOCOL_FILE = "file";

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
	 * public void toString() {
	 * }
	 */

	/*
	 * 
	 */

	/*
	 * Constructs the Resource for System properties.
	 */
	private Resource() {
		try {
			defaults = System.getProperties();
			persistent = new Properties(defaults);
			System.setProperties(persistent);
		} catch (SecurityException ex) {
			defaults = new Properties();
			persistent = new Properties(defaults);
		} 
		option = new Properties(persistent);
	}
	/*
	 * Constructs using user-defined directory for property file.
	 * 
	 * @param path     the directory used to store this property object.
	 * @param defaults the default properties
	 */
	private Resource(URL url, Properties defaults) {
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
			System.out.println("[Could not load resource from " + url + "]");
		} 
	}
	/**
	 * Appends a value to a resource.
	 * @param key   the key
	 * @param value the value to be append at the end of the original
	 * value
	 */
	public void appendResource(String key, String value) {
		if (value == null) {
			return;
		} 
		String r = getString(key, "");

		persistent.put(key, r + ' ' + value);
	}
	/**
	 * Creates named resources with file and default Proeprties object.
	 * @see getResourceFor
	 */
	synchronized static public Resource createResource(String name, 
			String file, 
			Properties defaults) throws java.net.MalformedURLException {
		URL url = file == null ? null : new URL(PROTOCOL_FILE, "", file);

		return createResource(name, url, defaults);
	}
	/**
	 * Creates named resources with file and default Proeprties object.
	 * @see getResourceFor
	 */
	synchronized static private Resource createResource(String name, 
			URL file, Properties defaults) {
		if (resourceTable.contains(name)) {
			throw new SecurityException("cannot re-create existing resource");
		} 
		Resource res = new Resource(file, defaults);

		resourceTable.put(name, res);
		return res;
	}
	/**
	 * Creates named resources with default Proeprties object.
	 * @see getResourceFor
	 */
	synchronized static public Resource createResource(String name, 
			Properties defaults) {
		return createResource(name, (URL)null, defaults);
	}
	public boolean getBoolean(String key, boolean defaultValue) {
		String v = option.getProperty(key);

		return v == null ? defaultValue : v.equalsIgnoreCase("true");
	}
	/*
	 * 
	 */
	public Color getColor(String key, Color defaultColor) {
		String color = option.getProperty(key);

		if (color == null) {
			return defaultColor;
		} 
		try {
			return new Color(Integer.parseInt(color));
		} catch (NumberFormatException ex) {
			return defaultColor;
		} 
	}
	/**
	 * 
	 */
	public Font getFont(String key, Font defaultFont) {
		String value = option.getProperty(key);

		if (value == null) {
			return defaultFont;
		} 
		String fontName = "Dialog";
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
			} catch (NumberFormatException e) {
				return defaultFont;
			} 
		} 
		return new Font(fontName, fontStyle, fontSize);
	}
	public int getInteger(String key, int defaultValue) {
		String v = option.getProperty(key);

		return v == null ? defaultValue : Integer.parseInt(v);
	}
	/**
	 * Get the resources starting with the key
	 * @param key the key to search
	 */
	public String[] getPersistentResourcesStartsWith(String startsWith) {
		Enumeration e = persistent.keys();
		java.util.Vector v = new java.util.Vector();

		while (e.hasMoreElements()) {
			String k = (String)e.nextElement();

			if (k.startsWith(startsWith)) {
				v.addElement(k);
			} 
		} 
		String[] array = new String[v.size()];

		v.copyInto(array);
		return array;
	}
	/**
	 * Get the resource object by name.
	 */
	static public Resource getResourceFor(String name) {
		return (Resource)resourceTable.get(name);
	}
	/**
	 * 
	 */
	public String getString(String key) {
		return (String)option.getProperty(key);
	}
	public String getString(String key, String defaultValue) {
		return (String)option.getProperty(key, defaultValue);
	}
	/**
	 * 
	 */
	public String[] getStringArray(String key, String sep) {
		String v = getString(key, null);

		if (v == null) {
			return new String[0];
		} 
		java.util.StringTokenizer st = new java.util.StringTokenizer(v, sep, 
				false);
		String ret[] = new String[st.countTokens()];
		int i = 0;

		while (st.hasMoreTokens()) {
			ret[i++] = st.nextToken();
		} 
		return ret;
	}
	/**
	 * Gets system properties
	 */
	static private void getSystemProperties() {
		if (options == null) {
			try {
				options = System.getProperties();
			} catch (SecurityException ex) {
				options = new Properties();
			} 
		} 
	}
	/**
	 */
	public URL getURL(String key, URL defaultValue) {
		String v = option.getProperty(key);

		try {
			return v == null ? defaultValue : new URL(v);
		} catch (java.net.MalformedURLException ex) {
			System.err.println("[fail to convert '" + v + "' to URL]");
			return defaultValue;
		} 
	}
	/**
	 * Imports all properties start with "key" into this resource.
	 * All imported properties are removed from "from" object.
	 */
	public void importOptionProperties(String startsWith) {
		getSystemProperties();
		Enumeration e = options.propertyNames();

		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();

			if (key.startsWith(startsWith)) {
				setOptionResource(key, options.getProperty(key));
				options.remove(key);
			} 
		} 
	}
	/*
	 * 
	 */
	public void list(java.io.PrintStream out) {
		option.list(out);
	}
	/**
	 * Loads the properties from the file
	 */
	public boolean load(URL loadFrom) {
		if ("file".equalsIgnoreCase(loadFrom.getProtocol())) {
			if (FileUtils.ensureDirectory(loadFrom.getFile()) == false) {
				System.out.println("[Could not create directory " + loadFrom 
								   + "]");
				return false;
			} 
			if (FileUtils.ensureFile(loadFrom.getFile()) == false) {
				System.out.println("[Could not create file " + loadFrom 
								   + "]");
				return false;
			} 
		} 
		_saveURL = loadFrom;	// set as default save url.

		try {
			InputStream in = loadFrom.openStream();

			persistent.load(in);
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} 
		return true;
	}
	/**
	 * Merge a value to a resource. If the same value exists in the
	 * original value, it is just ignored.
	 * 
	 * @param key   the key
	 * @param value the value to be merged.
	 */
	public void mergeResource(String key, String value) {
		if (value == null) {
			return;
		} 
		String list = getString(key, "");

		if (list.indexOf(value) >= 0) {
			return;
		} else {
			appendResource(key, value);
		} 
	}
	/*
	 * Remves the resource value from option resources specified by the key
	 * @param the key
	 */
	public void removeOptionResource(String key) {
		option.remove(key);
	}
	/**
	 * 
	 */
	public void removePersistentResourcesStartsWith(String startsWith) {
		Enumeration e = persistent.keys();
		java.util.Vector v = new java.util.Vector();

		while (e.hasMoreElements()) {
			String k = (String)e.nextElement();

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
	 * @param the key
	 */
	public void removeResource(String key) {
		persistent.remove(key);
	}
	/**
	 * Saves the properties into the default file.
	 * @param header header string
	 */
	public boolean save(String header) {
		return save(_saveURL, header);
	}
	/**
	 * Saves the properties into the file with given header.
	 * @param saveTo the URL pointing to the file location
	 * @param header string to be saved at the top of file
	 */
	public boolean save(URL saveTo, String header) {
		if (saveTo == null 
				|| "file".equalsIgnoreCase(saveTo.getProtocol()) == false) {

			// throw new IOException("no file name");
			return false;
		} 
		try {
			System.out.println("[saving properties into " + saveTo + " ]");

			// System.out.println(_propertyURL.getFile());
			File file = new File(saveTo.getFile());

			if (file.exists() == false 
					|| (file.isFile() && file.canWrite())) {
				FileOutputStream out = new FileOutputStream(file);

				persistent.store(out, header);
				out.close();
			} 
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		} 
		return true;
	}
	/**
	 * Sets default property. Default properties have last priority and
	 * are not persistent.
	 * 
	 * @param key    the key
	 * @param value  the value to be stored as a default
	 */
	public void setDefaultResource(String key, String value) {

		/*
		 * Initializers cannot override default properties
		 */
		if (defaults.get(key) != null) {
			Exception ex = new Exception("Cannot override default properties:" 
										 + key + " = " + value 
										 + " , current value = " 
										 + defaults.get(key));

			ex.printStackTrace();
			return;
		} 
		defaults.put(key, value);
	}
	/**
	 * Sets default properties. Default properties have last priority and
	 * are not persistent.
	 * 
	 * @param key    the key
	 * @param value  the value to be stored as a default
	 */
	public void setDefaultResources(String[][] key_value_pairs) {
		for (int i = 0; i < key_value_pairs.length; i++) {
			setDefaultResource(key_value_pairs[i][0], key_value_pairs[i][1]);
		} 
	}
	/**
	 * Sets an option property. Option properties have first priority and
	 * are not persistent. These will not be saved in the file.
	 * @param key    the key
	 * @param value  the value to be stored as a option resource
	 */
	public void setOptionResource(String key, String value) {
		option.put(key, value);
	}
	/**
	 * Sets a property. Option properties are persistent and will be saved
	 * in the strage.
	 * @param key    the key
	 * @param value  the value to be stored
	 */
	public void setResource(String key, String value) {
		persistent.put(key, value);
	}
	/**
	 * Convenient function to convert Color object to string representation.
	 */
	static public String toString(Color color) {
		return String.valueOf(color.getRGB());
	}
	/**
	 * Convenient function to convert Font object to string representation.
	 */
	static public String toString(Font font) {
		String style = null;

		if (font.isBold()) {
			style = font.isItalic() ? "bolditalic" : "bold";
		} else {
			style = font.isItalic() ? "italic" : "plain";
		} 
		return font.getName() + '-' + style + '-' + font.getSize();
	}
}
