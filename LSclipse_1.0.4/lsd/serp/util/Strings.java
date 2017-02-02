package serp.util;


import java.util.*;


/**
 *	<p>String utiltity methods.</p>
 *
 *	@author		Abe White
 */
public class Strings
{
	private static final Object[][] _codes = new Object[][] {
		{ byte.class, "byte" },
		{ char.class, "char" },
		{ double.class, "double" },
		{ float.class, "float" },
		{ int.class, "int" },
		{ long.class, "long" },
		{ short.class, "short" },
		{ boolean.class, "boolean" },
		{ void.class, "void" },
	};


	/**
	 *	Splits the given string on the given token.  Follows the semantics
	 *	of the Java 1.4 {@link String#split(String,int)} method, but does 
	 *	not treat the given token as a regular expression.
	 */
	public static String[] split (String str, String token, int max)
	{
		if (str == null || str.length () == 0)
			return new String[0];
		if (token == null || token.length () == 0)
			throw new IllegalArgumentException ("token: [" + token + "]");

		// split on token 
		LinkedList ret = new LinkedList ();
		int start = 0;
		for (int split = 0; split != -1;)
		{
			split = str.indexOf (token, start);
			if (split == -1 && start >= str.length ())
				ret.add ("");
			else if (split == -1)
				ret.add (str.substring (start));
			else
			{
				ret.add (str.substring (start, split));
				start = split + token.length ();
			}
		}

		// now take max into account; this isn't the most efficient way
		// of doing things since we split the maximum number of times
		// regardless of the given parameters, but it makes things easy
		if (max == 0)
		{
			// discard any trailing empty splits
			while (ret.getLast ().equals (""))
				ret.removeLast ();	
		}
		else if (max > 0 && ret.size () > max)
		{
			// move all splits over max into the last split
			StringBuffer buf = new StringBuffer (ret.removeLast ().toString ());
			while (ret.size () >= max)
			{
				buf.insert (0, token);
				buf.insert (0, ret.removeLast ());
			}
			ret.add (buf.toString ());
		}

		return (String[]) ret.toArray (new String[ret.size ()]);
	}


	/**
	 *	Joins the given strings, placing the given token between them.
	 */
	public static String join (Object[] strings, String token)
	{
		if (strings == null)
			return null;

		StringBuffer buf = new StringBuffer (20 * strings.length);
		for (int i = 0; i < strings.length; i++)
		{
			if (i > 0)
				buf.append (token);
			if (strings[i] != null)
				buf.append (strings[i]);	
		}
		return buf.toString ();
	}


	/**
	 *	Capitlizes the first letter of the given string.  Null or empty 
	 *	strings are returned without modification.
	 */
	public static String capitalize (String str)
	{
		if (str == null || str.length () == 0)
			return str;

		char first = Character.toUpperCase (str.charAt (0));
		if (str.length () == 1)
			return String.valueOf (first);

		return first + str.substring (1);
	}


	/**
	 *	Return the class for the given string, correctly handling
	 *	primitive types.  If the given class loader is null, the context
	 *	loader of the current thread will be used.
	 *
	 *	@throws		IllegalArgumentException on load error	
	 */
	public static Class toClass (String str, ClassLoader loader)
	{
		if (str == null)
			throw new NullPointerException ("str = null");

		// check against primitive types
		for (int i = 0; i < _codes.length; i++)
			if (_codes[i][1].toString ().equals (str))
				return (Class) _codes[i][0];

		if (loader == null)
			loader = Thread.currentThread ().getContextClassLoader ();

		try
		{
			return Class.forName (str, true, loader);
		}
		catch (Throwable t)
		{
			throw new IllegalArgumentException (t.getClass ().getName () + ": "
				+ t.getMessage ());
		}
	}
}
