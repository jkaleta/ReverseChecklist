package com.jakubkaleta.checklist.DataAccess;

import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;

/**
 * This enumeration is used by activities that access the Activity List. It
 * represents the many different options for sorting Activities
 * 
 * @author Jakub Kaleta
 */
public enum ActivityListSortOrder
{
	/**
	 * The list with the smallest amount of elements will be displayed first.
	 */
	ShortestListsFirst(0, ActivityColumns.ITEM_COUNT + " ASC, " + ActivityColumns.ACTIVITY_NAME
			+ " COLLATE NOCASE ASC "),
	/**
	 * The list with the greatest amount of elements will be displayed first.
	 */
	LongestListsFirst(1, ActivityColumns.ITEM_COUNT + " DESC, " + ActivityColumns.ACTIVITY_NAME
			+ " COLLATE NOCASE ASC"),
	/**
	 * Sort by the number of selected elements ascending
	 */
	LeastToDoFirst(2, ActivityColumns.SELECTED_ITEM_COUNT + " ASC, "
			+ ActivityColumns.ACTIVITY_NAME + " COLLATE NOCASE ASC"),
	/**
	 * Sort by the amount of selected items descending
	 */
	MostToDoFirst(3, ActivityColumns.SELECTED_ITEM_COUNT + " DESC, "
			+ ActivityColumns.ACTIVITY_NAME + " COLLATE NOCASE ASC"),
	/**
	 * Sort alphabetically ascending
	 */
	AlphabeticallyAsc(4, ActivityColumns.ACTIVITY_NAME + " COLLATE NOCASE ASC "),
	/**
	 * Sort alphabetically descending
	 */
	AlphabeticallyDesc(5, ActivityColumns.ACTIVITY_NAME + " COLLATE NOCASE DESC "),
	/**
	 * Sort by date added ascending
	 */
	DateAddedAsc(6, ActivityColumns.DATE_CREATED + " ASC, " + ActivityColumns.ACTIVITY_NAME
			+ " COLLATE NOCASE ASC"),
	/**
	 * Sort by date added descending
	 */
	DateAddedDesc(7, ActivityColumns.DATE_CREATED + " DESC, " + ActivityColumns.ACTIVITY_NAME
			+ " COLLATE NOCASE ASC");

	private final String sortString;
	private final int enumIndex;

	ActivityListSortOrder(int enumIndex, String sortString)
	{
		this.sortString = sortString;
		this.enumIndex = enumIndex;
	}

	/**
	 * Call to convert enum value to a sort string
	 * 
	 * @return sqlite formatted sort string ready to be plugged into a query
	 */
	public String toSortString()
	{
		return sortString;
	}

	/**
	 * Call to convert enum value to an index number.
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
	 * associated with it enum value
	 * 
	 * @param number
	 *            The number to convert
	 * @return ActivityListSortOrder value corresponding with the number, or
	 *         default enum value if no match is found
	 */
	public static ActivityListSortOrder fromNumber(int number)
	{
		switch (number)
		{
		case 0:
			return ActivityListSortOrder.ShortestListsFirst;
		case 1:
			return ActivityListSortOrder.LongestListsFirst;
		case 2:
			return ActivityListSortOrder.LeastToDoFirst;
		case 3:
		default:
			return ActivityListSortOrder.MostToDoFirst;
		case 4:
			return ActivityListSortOrder.AlphabeticallyAsc;
		case 5:
			return ActivityListSortOrder.AlphabeticallyDesc;
		}
	}

}
