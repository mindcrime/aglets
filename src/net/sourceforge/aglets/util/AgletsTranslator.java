package net.sourceforge.aglets.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sourceforge.aglets.log.AgletsLogger;

/**
 * This class represnts a "translator" object for anything in the platform (even
 * the platform itself). Internally, this class uses a ResourceBundle object to
 * provide the translation of contents.
 * 
 * @author Luca Ferrari - cat4hire@users.sourceforge.net
 * 
 *         04/set/07
 */
public class AgletsTranslator implements Cloneable {

    /**
     * An hashmap used for the storing of already loaded translators.
     */
    private static HashMap<String, AgletsTranslator> translators = new HashMap<String, AgletsTranslator>();

    /**
     * The resource bundle used to handle locale content.
     */
    private transient ResourceBundle bundle = null;

    /**
     * The basename of the resource bundle.
     */
    private String baseName = null;

    /**
     * The logger of this class.
     */
    private transient static AgletsLogger logger = AgletsLogger.getLogger(AgletsTranslator.class.getName());

    /**
     * Where the localization files should be.
     */
    public static String LOCALIZATION_PATH = "localization";

    /**
     * Builds this translator object with the specified resource name and
     * locale.
     * 
     * @param localeBaseName
     *            the resource name (e.g., the name of properties file)
     * @param currentLocale
     *            the locale for which it is required the translation
     */
    private AgletsTranslator(String localeBaseName, Locale currentLocale) {
	super();

	this.baseName = localeBaseName;

	// build the resource bundle
	try {
	    this.bundle = ResourceBundle.getBundle(this.baseName, currentLocale);
	} catch (MissingResourceException e) {
	    // if here the bundle cannot be found with the standard locale, try
	    // using no locale at all
	    try {
		this.bundle = ResourceBundle.getBundle(localeBaseName);
	    } catch (MissingResourceException ex) {
		logger.error("Exception caught while trying to build a resource bundle", ex);
		this.bundle = null;
	    }
	} catch (NullPointerException e) {
	    logger.error("Exception caught while trying to build a resource bundle", e);
	    this.bundle = null;
	}
    }

    /**
     * Translates a specific part of text, without arguments (i.e., you have to
     * handle escape characters outside this method). If the bundle has not been
     * created or if the text has no way to be translated (e.g., it is the
     * string ""), then the original string will be returned.
     * 
     * @param text
     *            the string to translate
     * @return the translated string
     */
    public String translate(String text) {
	// be sure there is something to translate and I have a bundle to
	// ask for translation
	if ((text != null) && (text.length() > 0) && (this.bundle != null)
		&& this.bundle.containsKey(text)) {
	    String translated = null;
	    translated = this.bundle.getString(text);

	    // do I have the translation?
	    if (translated != null) {
		logger.translation(text, translated);
		return translated;
	    } else
		return text;
	} else
	    // nothing to do, return the string passed as argument
	    return text;
    }

    /**
     * Provides the locale this object is working with, or null if none.
     * 
     * @return the locale for the translations.
     */
    public final Locale getLocale() {
	if (this.bundle != null)
	    return this.bundle.getLocale();
	else
	    return null;
    }

    /**
     * Gets the basename of this translator.
     * 
     * @return the basename that has been used to initialize this object.
     */
    public final String getResourceBaseName() {
	return this.baseName;
    }

    /**
     * Provides the keys of this translator object.
     * 
     * @return the enumeration of the keys.
     */
    public Enumeration<String> getKeys() {
	if (this.bundle != null)
	    return this.bundle.getKeys();
	else
	    return null;
    }

    /**
     * Provides an implementation of the AgletsTranslator. If the translator has
     * already been loaded, the previous one is returned.
     * 
     * @param localeBaseName
     *            the base name for the translation
     * @param currentLocale
     *            the locale
     * @return the aglets translator
     */
    public static AgletsTranslator getInstance(
                                               String localeBaseName,
                                               Locale currentLocale) {
	String key = localeBaseName + currentLocale.toString();

	// search first in the cache
	if (translators.containsKey(key))
	    return translators.get(key);
	else {
	    AgletsTranslator translator = new AgletsTranslator(localeBaseName, currentLocale);
	    translators.put(key, translator);
	    return translator;
	}
    }

}
