package serp.util;


import java.lang.reflect.*;
import java.util.*;


/**
 *	<p>A specialization of the {@link Properties} map type with the added
 *	abilities to read application options from the command line and to
 *	use bean patterns to set an object's properties via command-line the
 *	stored mappings.</p>
 *
 *	<p>A typical use pattern for this class is to construct a new instance
 *	in the <code>main</code> method, then call {@link #setFromCmdLine} with the
 *	given args.  Next, an instanceof the class being invoked is created, and
 *	{@link #setInto} is called with that instance as a parameter.  With this
 *	pattern, the user can configure any bean properties of the class, or even
 *	properties of classes reachable from the class, through the command 
 *	line.</p>
 *
 *	@author		Abe White
 */
public class Options
	extends TypedProperties
{
	// maps primitive types to the appropriate wrapper class and default value
	private static Object[][] _primWrappers = new Object[][] { 
		{ boolean.class, Boolean.class, Boolean.FALSE },
		{ byte.class, Byte.class, new Byte ((byte) 0) },
		{ char.class, Character.class, new Character ((char) 0) },
		{ double.class, Double.class, new Double (0D) },
		{ float.class, Float.class, new Float (0F) },
		{ int.class, Integer.class, new Integer (0) },
		{ long.class, Long.class, new Long (0L) },
		{ short.class, Short.class, new Short ((short) 0) },
	};


	/**
	 *	Default constructor.
	 */
	public Options ()
	{
		super ();
	}


	/**
	 *	Construct the options instance with the given set of defaults.
	 *
	 *	@see	Properties#Properties(Properties)
	 */
	public Options (Properties defaults)
	{
		super (defaults);
	}


	/**
	 *	Parses the given argument list into flag/value pairs, which are stored
	 *	as properties.  Flags that are present without values are given
	 *	the value "true".  If any flag is found for which there is already
	 *	a mapping present, the existing mapping will be overwritten.
	 *	Flags should be of the form:<br />
	 *	<code>java Foo -flag1 value1 -flag2 value2 ... arg1 arg2 ...</code>
	 *
	 *	@param	args	the command-line arguments
	 *	@return			all arguments in the original array beyond the 
	 *					flag/value pair list
	 *	@throws			IllegalArgumentException on parse error
 	 *	@author			Patrick Linskey
	 */
	public String[] setFromCmdLine (String[] args)
	{
		if (args == null || args.length == 0)
			return args;

		String key = null;
		String value = null;
		List remainder = new LinkedList ();
		for (int i = 0; i < args.length + 1; i++)
		{
			if (i == args.length || args[i].startsWith ("-"))
			{
				key = trimQuote (key);
				if (key != null)
				{
					if (value != null && value.length () > 0)
						setProperty (key, trimQuote (value));
					else
						setProperty (key, "true");
				}

				if (i == args.length)
					break;
				else
				{
					key = args[i].substring (1);
					value = null;
				}
			}
			else if (key != null)
			{
				setProperty (key, trimQuote (args[i]));
				key = null;
			}
			else
				remainder.add (args[i]);
		}

		return (String[]) remainder.toArray (new String[remainder.size ()]);
	}


	/**
	 *	This method uses reflection to set all the properties in the given
	 *	object that are named by the keys in this map. For a given key 'foo', 
	 *	the algorithm will look for a 'setFoo' method in the given instance.  
	 *	For a given key 'foo.bar', the algorithm will first look for a  
	 *	'getFoo' method in the given instance, then will recurse on the return
	 *	value of that method, now looking for the 'bar'	property.  This allows
	 *	the setting of nested object properties.  If in the above example the 
	 *	'getFoo' method is not present or returns null, the	algorithm will 
	 *	look for a 'setFoo' method; if found it will constrct a new instance
	 *	of the correct type, set it using the 'setFoo' method, then recurse on
	 *	it as above.  Property names can be nested in this way to an arbitrary
	 *	depth.  For setter methods that take multiple parameters, the value
	 *	mapped to the key can use the ',' as an argument separator character.
	 *	If not enough values are present for a given method after splitting 
	 *	the string on ',', the remaining arguments will receive default 
	 *	values.  All arguments are converted from string form to the
	 *	correct type if possible (i.e. if the type is primitive,
	 *	java.lang.Clas, or has a constructor that takes a single string
	 *	argument).  Examples:
	 *	<ul>	
	 *	<li>Map Entry: <code>"age"-&gt;"12"</code><br />
	 *		Resultant method call: <code>obj.setAge (12)</code></li>
	 *	<li>Map Entry: <code>"range"-&gt;"1,20"</code><br />
	 *		Resultant method call: <code>obj.setRange (1, 20)</code></li>
	 *	<li>Map Entry: <code>"range"-&gt;"10"</code><br />
	 *		Resultant method call: <code>obj.setRange (10, 10)</code></li>
	 *	<li>Map Entry: <code>"brother.name"-&gt;"Bob"</code><br />
	 *		Resultant method call: <code>obj.getBrother ().setName ("Bob")
	 *		<code></li>
	 *	</ul>
	 *	Any keys present in the map for which there is no corresponding
	 *	property in the given object will be ignored.
	 *
	 *	@throws		IllegalArgumentException on parse error
	 */
	public void setInto (Object obj)
	{
		// set all defaults that have no explicit value 
		Map.Entry entry = null;
		if (defaults != null)
		{
			for (Iterator itr = defaults.entrySet ().iterator(); itr.hasNext();)
			{
				entry = (Map.Entry) itr.next ();
				if (!containsKey (entry.getKey ()))
					setInto (obj, entry);
			}
		}

		// set from main map
		for (Iterator itr = entrySet ().iterator (); itr.hasNext ();)
			setInto (obj, (Map.Entry) itr.next ());
	}


	/**
	 *	Sets the property named by the key of the given entry in the
	 *	given object.
	 */
	private void setInto (Object obj, Map.Entry entry)
	{
		if (entry.getKey () == null)
			return;

		try
		{
			// look for matching parameter of object
			Object[] match = new Object[] { obj, null };
			if (!matchOptionToSetter (entry.getKey ().toString (), match))
				return;

			Method setter = (Method) match[1];
			Class[] paramTypes = setter.getParameterTypes ();
			Object[] values = new Object[paramTypes.length];
			String[] strValues = (entry.getValue () == null) ? new String[1]
					: Strings.split (entry.getValue ().toString (), ",", -1);

			// convert the string values into parameter values, if not
			// enough string values repeat last one for rest
			for (int i = 0; i < strValues.length; i++)
				values[i] = stringToObject (strValues[i], paramTypes[i]);
			for (int i = strValues.length; i < values.length; i++)	
				values[i] = getDefaultValue (paramTypes[i]);

			// invoke the setter
			setter.invoke (match[0], values);
		}
		catch (Throwable t)
		{
			throw new IllegalArgumentException (obj + "." + entry.getKey () 
				+ " = " + entry.getValue ());
		}
	} 


	/**
	 *	Removes leading and trailing single quotes from the given String, 
	 *	if any.
 	 */
	private static String trimQuote (String val)
	{
		if (val != null && val.startsWith ("'") && val.endsWith ("'"))
			return val.substring (1, val.length () - 1);
		return val;
	}


	/**
	 *	Matches a key to an object/setter pair.  
	 *
	 *	@param	key		the key given at the command line; may be of the form
	 *					'foo.bar' to signify the 'bar' property of the
	 *					'foo' owned object
	 *	@param	match	an array of length 2, where the first index is set
	 *					to the object to retrieve the setter for
	 *	@return			true if a match was made, false otherwise; additionally,
	 *					the first index of the match array will be set to
	 *					the matching object and the second index will be
	 *					set to the setter method for the property named by
	 *					the key
	 */
	private static boolean matchOptionToSetter (String key, Object[] match)
		throws Exception
	{
		if (key == null || key.length () == 0)
			return false;
		
		// unfortunately we can't use bean properties for setters; any
		// setter with more than 1 arg is ignored; calc setter and getter 
		// name to look for
		String[] find = Strings.split (key, ".", 2);
		String base = Strings.capitalize (find[0]);
		String set = "set" + base;
		String get = "get" + base;

		// look for a setter/getter matching the key
		Class type = match[0].getClass ();
		Method[] meths = type.getMethods ();
		Method setter = null;
		Method getter = null;
		for (int i = 0; i < meths.length; i++)
		{
			if (meths[i].getName ().equals (set))
				setter = meths[i];
			else if (meths[i].getName ().equals (get))
				getter = meths[i];
		}
		if (setter == null && getter == null)
			return false;

		// recurse on inner object with remainder of key?
		if (find.length > 1)
		{
			Object inner = null;
			if (getter != null)
				inner = getter.invoke (match[0], null);	

			// if no getter or current inner is null, try to create a new 
			// inner instance and set it in object
			if (inner == null)
			{
				Class innerType = setter.getParameterTypes ()[0];
				inner = innerType.newInstance ();
				setter.invoke (match[0], new Object[] { inner });
			}
			match[0] = inner;
			return matchOptionToSetter (find[1], match);
		}

		// got match; find setter for property
		match[1] = setter;
		return match[1] != null;
	}


	/**
	 *	Converts the given string into an object of the given type, or its
	 *	wrapper type if it is primitive.
	 */
	private static Object stringToObject (String str, Class type)
		throws Exception
	{
		// special case for null and for strings
		if (str == null || type == String.class)
			return str;

		// special case for creating Class instances
		if (type == Class.class)
			return Class.forName (str);

		// special case for numeric types that end in .0; strip the decimal
		// places because it can kill int, short, long parsing
		if (type.isPrimitive () || Number.class.isAssignableFrom (type))
			if (str.length () > 2 && str.endsWith (".0"))
				str = str.substring (0, str.length () - 2);
		
		// for primitives, recurse on wrapper type
		if (type.isPrimitive ())
			for (int i = 0; i < _primWrappers.length; i++)
				if (type == _primWrappers[i][0])
					return stringToObject (str, (Class) _primWrappers[i][1]);	

		// look for a string constructor
		Exception err = null;
		try
		{
			Constructor cons = type.getConstructor 
				(new Class[] { String.class });
			return cons.newInstance (new Object[] { str });	
		}
		catch (Exception e)
		{
			err = e;
		}

		// special case: the arg value is a subtype name and a new instance
		// of that type should be set as the object
		Class subType = null;
		try
		{
			subType = Class.forName (str);
		}
		catch (Exception e)
		{
			throw err;
		}
		if (!type.isAssignableFrom (subType))
			throw err;
		return subType.newInstance ();
	}


	/**
	 *	Returns the default value for the given parameter type.
	 */
	private Object getDefaultValue (Class type)
	{
		for (int i = 0; i < _primWrappers.length; i++)
			if (_primWrappers[i][0] == type)
				return _primWrappers[i][2];
		
		return null;
	}
}
