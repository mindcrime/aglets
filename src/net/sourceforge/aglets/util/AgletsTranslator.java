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
	 * Provides the current aglets home for the running instance.
	 * @return the aglets home string
	 */
	public static final String getAgletsHome(){
		return System.getProperty( "aglets.home" );
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
	                                           final Class<?> callerClass,
	                                           final Locale currentLocale) {
		final String key = callerClass.getName() + currentLocale.toString();

		// search first in the cache
		if (translators.containsKey(key))
			return translators.get(key);
		else {
			final AgletsTranslator translator = new AgletsTranslator(callerClass, currentLocale);
			translators.put(key, translator);
			return translator;
		}
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
	                                           final String localeBaseName,
	                                           final Locale currentLocale) {
		final String key = localeBaseName + currentLocale.toString();

		// search first in the cache
		if (translators.containsKey(key))
			return translators.get(key);
		else {
			final AgletsTranslator translator = new AgletsTranslator(localeBaseName, currentLocale);
			translators.put(key, translator);
			return translator;
		}
	}

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
	 * Builds this translator object with the specified class object and
	 * locale.
	 * 
	 * @param class
	 *            the class for which the resource bundle is requested
	 * @param currentLocale
	 *            the locale for which the translation is required
	 */
	private AgletsTranslator(final Class<?> callerClass, final Locale currentLocale) {
		super();

		baseName = callerClass.getName();

		// build the resource bundle
		try {
			bundle = ResourceBundle.getBundle(baseName, currentLocale, callerClass.getClassLoader());
		} catch (final MissingResourceException e) {
			logger.error("Exception caught while trying to build a resource bundle", e);
			bundle = null;
		} catch (final NullPointerException e) {
			logger.error("Exception caught while trying to build a resource bundle", e);
			bundle = null;
		}
	}

	/**
	 * Builds this translator object with the specified resource name and
	 * locale.
	 * 
	 * @param localeBaseName
	 *            the resource name (e.g., the name of properties file)
	 * @param currentLocale
	 *            the locale for which the translation is required
	 */
	private AgletsTranslator(final String localeBaseName, final Locale currentLocale) {
		super();

		baseName = localeBaseName;

		// build the resource bundle
		try {
			bundle = ResourceBundle.getBundle(baseName, currentLocale);
		} catch (final MissingResourceException e) {
			logger.error("Exception caught while trying to build a resource bundle", e);
			bundle = null;
		} catch (final NullPointerException e) {
			logger.error("Exception caught while trying to build a resource bundle", e);
			bundle = null;
		}
	}

	/**
	 * Provides the keys of this translator object.
	 * 
	 * @return the enumeration of the keys.
	 */
	public Enumeration<String> getKeys() {
		if (bundle != null)
			return bundle.getKeys();
		else
			return null;
	}

	/**
	 * Provides the locale this object is working with, or null if none.
	 * 
	 * @return the locale for the translations.
	 */
	public final Locale getLocale() {
		if (bundle != null)
			return bundle.getLocale();
		else
			return null;
	}

	/**
	 * Gets the basename of this translator.
	 * 
	 * @return the basename that has been used to initialize this object.
	 */
	public final String getResourceBaseName() {
		return baseName;
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
	public String translate(final String text) {
		// be sure there is something to translate and I have a bundle to
		// ask for translation
		if ((text != null) && (text.length() > 0) && (bundle != null)
				&& bundle.containsKey(text)) {
			String translated = null;
			translated = bundle.getString(text);

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

}
