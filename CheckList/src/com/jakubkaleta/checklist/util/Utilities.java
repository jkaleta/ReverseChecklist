package com.jakubkaleta.checklist.util;


/**
 * This class provides some utility methods to be used by various components
 * throughout the app
 * 
 * @author Jakub Kaleta
 * 
 */
public class Utilities
{
	/**
	 * Given the delimiter and a list of strings joins the strings in the list
	 * with the specified delimiter
	 * 
	 * @param <T>
	 *            Generic type parameter. May be any object.
	 * 
	 * @param delim
	 *            The delimiter to use for joining the strings
	 * @param strings
	 *            A list of strings to join
	 * @return Joined string containing all the strings from the passed in list.
	 */
	public static <T> String join(String delim, T[] strings)
	{
		StringBuilder builder = new StringBuilder();

		if (strings != null && strings.length > 0)
		{
			for (int i = 0; i < strings.length; i++)
			{
				if (builder.length() > 0)
				{
					builder.append(delim).append(" ");
				}
				builder.append(strings[i].toString());
			}
		}
		return builder.toString();
	}

	/**
	 * Converts a sql timestamp to a date
	 * 
	 * @param timestamp
	 *            The timestamp to convert
	 * @return The resulting date
	 */
	public static java.util.Date toDate(java.sql.Timestamp timestamp)
	{
		long milliseconds = timestamp.getTime() + (timestamp.getNanos() / 1000000);
		return new java.util.Date(milliseconds);
	}
}
