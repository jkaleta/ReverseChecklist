package com.jakubkaleta.checklist.DataAccess.beans;

import com.jakubkaleta.checklist.DataAccess.ActivityListSortOrder;

/**
 * Plain old java object representing the current state of the application
 * 
 * @author Jakub Kaleta
 * 
 */
public class ApplicationStateBean
{
	private long activityId;
	private long categoryId;
	private String mode;
	private int activityListSortOrderInt;

	/**
	 * Initializes an object of ApplicationStateBean, seeding the properties
	 * with the passed in parameters.
	 * 
	 * @param activityId
	 *            The database id of the currently selected activity
	 * @param categoryId
	 *            The database id of the currently displayed category
	 * @param mode
	 *            Current application mode. TODO or EDIT
	 * @param activityListSortOrderInt
	 *            Currently selected sort order for the list of lists
	 *            (activities)
	 */
	public ApplicationStateBean(long activityId, long categoryId, String mode,
			int activityListSortOrderInt)
	{
		this.activityId = activityId;
		this.categoryId = categoryId;
		this.mode = mode;
		this.activityListSortOrderInt = activityListSortOrderInt;
	}

	/**
	 * Returns the database id of the currently selected activity
	 * 
	 * @return Database id of the currently selected activity
	 */
	public long getActivityId()
	{
		return activityId;
	};

	/**
	 * Returns the database id of the currently displayed category
	 * 
	 * @return The database id of the currently displayed category
	 */
	public long getCategoryId()
	{
		return categoryId;
	};

	/**
	 * Returns current application mode. TODO or EDIT
	 * 
	 * @return Current application mode. TODO or EDIT
	 */
	public String getMode()
	{
		return mode;
	};

	/**
	 * Returns currently selected sort order for the list of lists (activities)
	 * 
	 * @return currently selected sort order for the list of lists (activities)
	 */
	public ActivityListSortOrder getActivityListSortOrder()
	{
		return ActivityListSortOrder.fromNumber(activityListSortOrderInt);
	};
}
