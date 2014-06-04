package com.jakubkaleta.checklist.DataAccess;

import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * This enumeration represents the many different options for sorting Entries within one category
 * 
 * @author Jakub Kaleta
 */
public enum CategorySortOrder
{	
	/**
	 * Sort alphabetically ascending
	 */
	AlphabeticallyAsc(0, EntryColumns.ENTRY_NAME + " COLLATE NOCASE ASC "),
	/**
	 * Sort alphabetically descending
	 */
	AlphabeticallyDesc(1, EntryColumns.ENTRY_NAME + " COLLATE NOCASE DESC "),
	/**
	 * Sort by custom sort order
	 */
	Custom(2, EntryColumns.TABLE_NAME +"."+ EntryColumns.SORT_POSITION + " ASC ");
	
	private final String sortString;
	private final int enumIndex;

	CategorySortOrder(int enumIndex, String sortString)
	{
		this.sortString = sortString;
		this.enumIndex = enumIndex;
	}

	/**
	 * Call to convert enumeration value to a sort string
	 * 
	 * @return sqlite formatted sort string ready to be plugged into a query
	 */
	public String toSortString()
	{
		return sortString;
	}

	/**
	 * Call to convert enumeration value to an index number.
	 * 
	 * @return Unique index associated with the specific sort order, to be
	 *         stored in the database
	 */
	public int toNumber()
	{
		return enumIndex;
	}

	/**
	 * Call to convert a database index of a specific sort order to the
	 * associated with it enumeration value
	 * 
	 * @param number
	 *            The number to convert
	 * @return CategorySortOrder value corresponding with the number, or
	 *         default enumeration value if no match is found
	 */
	public static CategorySortOrder fromNumber(int number)
	{
		switch (number)
		{
		case 0:
		default:
			return CategorySortOrder.AlphabeticallyAsc;
		case 1:
			return CategorySortOrder.AlphabeticallyDesc;
		case 2: 
			return CategorySortOrder.Custom;
		}
	}

}
