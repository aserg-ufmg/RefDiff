package serp.util;


import java.text.*;
import java.util.*;


/**
 *	<p>The Localizer provides convenient access to localized
 *	strings.  It inlcudes built-in support for parameter substitution through
 *	the use of the {@link MessageFormat} utility.</p>
 *
 *	<p>Strings are stored in per-package {@link Properties} files.  
 *	The property file for the default locale must be named 
 *	<code>localizer.properties</code>.   Additional locales can be supported
 *	through additional property files using the naming conventions specified
 *	in the {@link ResourceBundle} class.  For example, the german locale 
 *	could be supported through a <code>localizer_de_DE.properties</code> 
 *	file.</p>
 *
 *	@author		Abe White
 */
public class Localizer
{
	// static cache of package+loc name to resource bundle mappings
	private static Map _bundles = new HashMap ();

	// the locals file name
	private String _file = null;

	// the default locale to localize to
	private Locale _locale = null;


	/**
	 *	Return a Localizer instance that will access the properties file
	 *	in the package of the given class using the system default locale.
	 *
	 *	@see	#forPackage(Class,Locale)
	 */
	public static Localizer forPackage (Class cls)
	{
		return forPackage (cls, null);
	}


	/**
	 *	Return a Localizer instance that will access the properties file
	 *	in the package of the given class using the given locale.
 	 *		
	 *	@param	cls		the class whose package to check for the localized 
	 *					properties file; if null, the system will check for
	 *					a top-level properties file
	 *	@param	locale	the locale to which strings should be localized; if
	 *					null, the system default will be assumed
	 */	
	public static Localizer forPackage (Class cls, Locale locale)
	{
		Localizer loc = new Localizer ();

		int dot = (cls == null) ? -1 : cls.getName ().lastIndexOf ('.');
		if (dot == -1)
			loc._file = "localizer";
		else
			loc._file = cls.getName ().substring (0, dot + 1) + "localizer";
		loc._locale = locale;
		
		return loc;
	}


	/**
	 *	Return the localized string matching the given key.
	 *
	 *	@see	#get(String,Locale)
	 */
	public String get (String key)
	{
		return get (key, _locale);
	}


	/**
	 *	Return the localized string matching the given key.  The given 
 	 *	<code>sub</code> object will be packed into an array and substituted 
	 *	into the found string according to the rules of the 
	 *	{@link MessageFormat} class.
	 *
	 *	@see	#get(String,Locale)
	 */
	public String get (String key, Object sub)
	{
		return get (key, new Object[] { sub }, _locale);
	}


	/**
	 *	Return the localized string matching the given key.  The given 
 	 *	<code>subs</code> objects will be substituted 
	 *	into the found string according to the rules of the 
	 *	{@link MessageFormat} class.
	 *
	 *	@see	#get(String,Locale)
	 */
	public String get (String key, Object[] subs)
	{
		return get (key, subs, _locale);
	}


	/**
	 *	Return the localized string matching the given key according to the
	 *	given locale.  The given <code>sub</code> object will be packed into 
	 *	an array and substituted into the found string according to the rules 
	 *	of the {@link MessageFormat} class.
	 *
	 *	@see	#get(String,Locale)
	 */
	public String get (String key, Object sub, Locale locale)
	{
		return get (key, new Object[] { sub }, locale);
	}


	/**
	 *	Return the localized string matching the given key according to the
	 *	given locale.  The given <code>subs</code> objects will be substituted 
	 *	into the found string according to the rules of the 
	 *	{@link MessageFormat} class.
	 *
	 *	@see	#get(String,Locale)
	 */
	public String get (String key, Object[] subs, Locale locale)
	{
		String str = get (key, locale);
		return MessageFormat.format (str, subs);
	}


	/**
	 *	Return the localized string matching the given key.
	 *
 	 *	@param	key		the key in the properties file of the string to return
	 *	@param	locale	the locale to localize the message for, or null for
	 *					the default locale
 	 *	@throws			MissingResourceException if the localized properties
	 *					file or requested key is not found
	 */
	public String get (String key, Locale locale)
	{
		// check the cache
		String cacheKey = _file;
		if (locale != null)
			cacheKey += locale.toString ();
		ResourceBundle bundle = (ResourceBundle) _bundles.get (cacheKey);

		if (bundle == null)
		{
			if (locale != null)
				bundle = ResourceBundle.getBundle (_file, locale);
			else
				bundle = ResourceBundle.getBundle (_file);
		
			// cache the bundle
			_bundles.put (cacheKey, bundle);
		}
		
		return bundle.getString (key);
	}
}
