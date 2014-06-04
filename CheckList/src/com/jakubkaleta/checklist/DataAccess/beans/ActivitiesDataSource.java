package com.jakubkaleta.checklist.DataAccess.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * This class represents a collection of activities, with children, for easier
 * manipulation of large quantities of data
 * 
 * @author Jakub Kaleta
 */
public class ActivitiesDataSource
{
	private HashMap<Long, ActivityBean> existingActivities = new HashMap<Long, ActivityBean>();
	private HashSet<String> existingActivitiesNames = new HashSet<String>();
	private HashMap<String, ActivityBean> newActivities = new HashMap<String, ActivityBean>();;

	/**
	 * Adds the passed in activity to the underlying collection of activities.
	 * Checks for uniqueness by database id, and if this is 0, by name
	 * 
	 * @param activity
	 *            The activity to add to the collection of activities of this
	 *            activity
	 */
	public void addActivity(ActivityBean activity)
	{
		if (activity.getId() != 0)
		{
			// if the entry that we are trying to insert has a database id,
			// it is assumed that it must have already been inserted, so we
			// don't care
			// about duplicate names, just duplicate ids
			if (!existingActivities.containsKey(activity.getId()))
			{
				existingActivities.put(activity.getId(), activity);
				existingActivitiesNames.add(activity.getName().toLowerCase());
			}

		}
		else
		{
			// if all we have is the name of the new entry,
			// we need to check for duplicate names in both the existing items
			// and the new items
			if (newActivities.containsKey(activity.getName()))
				return;

			if (existingActivitiesNames.contains(activity.getName()))
				return;

			newActivities.put(activity.getName().toLowerCase(), activity);
		}
	}

	/**
	 * @return A list of all Activities
	 */
	public List<ActivityBean> getActivities()
	{
		List<ActivityBean> allActivities = new ArrayList<ActivityBean>();
		allActivities.addAll(existingActivities.values());
		allActivities.addAll(newActivities.values());
		return allActivities;
	}

	/**
	 * @return A list of all new Activities
	 */
	public List<ActivityBean> getNewActivities()
	{
		List<ActivityBean> allActivities = new ArrayList<ActivityBean>();
		allActivities.addAll(newActivities.values());
		return allActivities;
	}

	/**
	 * Retrieves an activity by id.
	 * 
	 * @param activityId
	 *            The id of the activity to find.
	 * @return A activity with the specified name, if exists
	 */
	public ActivityBean getActivity(long activityId)
	{
		if (existingActivities.containsKey(activityId))
			return existingActivities.get(activityId);

		return null;
	}

	/**
	 * Retrieves an activity by name
	 * 
	 * @param activityName
	 *            The name of the activity to find
	 * @return A activity with the specified name, if exists
	 */
	public ActivityBean getActivity(String activityName)
	{
		if (existingActivitiesNames.contains(activityName.toLowerCase()))
		{
			for (ActivityBean bean : existingActivities.values())
			{
				if (bean.getName().equalsIgnoreCase(activityName))
					return bean;
			}
		}

		if (newActivities.containsKey(activityName.toLowerCase()))
			return newActivities.get(activityName.toLowerCase());

		return null;
	}

	/**
	 * Quickly selects or de-selects all items in all lists
	 * 
	 * @param selected
	 *            True to select, false to de-select
	 */
	public void updateSelectionOfAllItems(boolean selected)
	{
		for (ActivityBean act : getActivities())
		{
			for (CategoryBean cat : act.getCategories())
			{
				for (EntryBean entry : cat.getEntries())
				{
					entry.setIsSelected(selected);
				}
			}
		}
	}
}
