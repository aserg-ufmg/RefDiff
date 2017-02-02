package serp.util;


import java.util.*;


/**
 *	<p>A specialization of the {@link Properties} map type with added
 *	convenience methods to retrieve	and set options as primitive values.  
 *	The internal representation of all data is kept in string form.</p>
 *
 *	@author		Abe White
 */
public class TypedProperties
	extends Properties
{
	/**
	 *	Default constructor.
	 */
	public TypedProperties ()
	{
		super ();
	}


	/**
	 *	Construct the properties instance with the given set of defaults.
	 *
	 *	@see	Properties#Properties(Properties)
	 */
	public TypedProperties (Properties defaults)
	{
		super (defaults);
	}


	/**
	 *	Return the property under the given key as a boolean, or false if 
	 *	it does not exist and has no set default.
	 */
	public boolean getBooleanProperty (String key)
	{
		return getBooleanProperty (key, false);
	}


	/**
	 *	Return the property under the given key as a boolean, or the given
	 *	default if it does not exist.
	 */
	public boolean getBooleanProperty (String key, boolean def)
	{
		String val = getProperty (key);
		return (val == null) ? def : Boolean.valueOf (val).booleanValue ();
	}


	/**
	 *	Return the property under the given key as a float, or 0 if 
	 *	it does not exist and has no set default.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public float getFloatProperty (String key)
	{
		return getFloatProperty (key, 0F);
	}


	/**
	 *	Return the property under the given key as a float, or the given
	 *	default if it does not exist.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public float getFloatProperty (String key, float def)
	{
		String val = getProperty (key);
		return (val == null) ? def : Float.parseFloat (val);
	}


	/**
	 *	Return the property under the given key as a double, or 0 if 
	 *	it does not exist and has no set default.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public double getDoubleProperty (String key)
	{
		return getDoubleProperty (key, 0D);
	}


	/**
	 *	Return the property under the given key as a double, or the given
	 *	default if it does not exist.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public double getDoubleProperty (String key, double def)
	{
		String val = getProperty (key);
		return (val == null) ? def : Double.parseDouble (val);
	}


	/**
	 *	Return the property under the given key as a long, or 0 if 
	 *	it does not exist and has no set default.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public long getLongProperty (String key)
	{
		return getLongProperty (key, 0L);
	}


	/**
	 *	Return the property under the given key as a double, or the given
	 *	default if it does not exist.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public long getLongProperty (String key, long def)
	{
		String val = getProperty (key);
		return (val == null) ? def : Long.parseLong (val);
	}


	/**
	 *	Return the property under the given key as an int, or 0 if 
	 *	it does not exist and has no set default.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public int getIntProperty (String key)
	{
		return getIntProperty (key, 0);
	}


	/**
	 *	Return the property under the given key as an int, or the given
	 *	default if it does not exist.
	 *
	 *	@throws		NumberFormatException on parse error
	 */
	public int getIntProperty (String key, int def)
	{
		String val = getProperty (key);
		return (val == null) ? def : Integer.parseInt (val);
	}


	/**
	 *	Overrides {@link Properties#setProperty(String,String)} to remove
	 *	the key if the given value is <code>null</code>.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public Object setProperty (String key, String val)
	{
		if (val == null)
			return remove (key);
		return super.setProperty (key, val);
	}


	/**
	 *	Set the given key to a string version of the given value.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public void setProperty (String key, boolean val)
	{
		setProperty (key, String.valueOf (val));
	}


	/**
	 *	Set the given key to a string version of the given value.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public void setProperty (String key, double val)
	{
		setProperty (key, String.valueOf (val));
	}


	/**
	 *	Set the given key to a string version of the given value.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public void setProperty (String key, float val)
	{
		setProperty (key, String.valueOf (val));
	}


	/**
	 *	Set the given key to a string version of the given value.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public void setProperty (String key, int val)
	{
		setProperty (key, String.valueOf (val));
	}


	/**
	 *	Set the given key to a string version of the given value.
	 *
	 *	@see	Properties#setProperty(String,String)
	 */
	public void setProperty (String key, long val)
	{
		setProperty (key, String.valueOf (val));
	}
}
