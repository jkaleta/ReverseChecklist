package com.jakubkaleta.checklist.DataAccess.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;

/**
 * This class represents a single category entry in the database It is a
 * convenience class - for storing and manipulating categories
 * 
 * @author Jakub Kaleta
 */
public class CategoryBean
{
	private long categoryId;
	private String categoryName;
	private CategorySortOrder sortOrder;
	private HashMap<Long, EntryBean> existingEntries = new HashMap<Long, EntryBean>();
	private HashMap<String, Long> existingEntriesNamesAndIds = new HashMap<String, Long>();
	private HashMap<String, EntryBean> newEntries = new HashMap<String, EntryBean>();

	/**
	 * Creates a new instance of CategoryBean
	 * 
	 * @param categoryId
	 *            The database id of this category
	 * @param categoryName
	 *            The name of the category
	 * @param sortOrder 
	 * 			  Current chosen sort order for this category
	 */
	public CategoryBean(long categoryId, String categoryName, CategorySortOrder sortOrder)
	{
		this.categoryId = categoryId;
		this.categoryName = categoryName;
		this.sortOrder = sortOrder;
	}

	/**
	 * @return The database id of the category
	 */
	public long getId()
	{
		return categoryId;
	}

	/**
	 * @return The name of the category.
	 */
	public String getName()
	{
		return categoryName;
	}

	/**
	 * @return The current sort order chosen for this category
	 */
	public CategorySortOrder getSortOrder()
	{
		return sortOrder;
	}
	
	/**
	 * Call to update the current sort order to the new one
	 * @param newSortOrder
	 * 			Sort order to update to.
	 */
	public void setSortOrder(CategorySortOrder newSortOrder)
	{
		sortOrder = newSortOrder;
	}
	
	/**
	 * Adds the passed in entry to the underlying collection of entries Checks
	 * for uniqueness by database id, and if this is 0, by name
	 * 
	 * @param entry
	 *            The entry to add to the collection of entries of this category
	 */
	public void addEntry(EntryBean entry)
	{
		if (entry.getId() != 0)
		{
			// if the entry that we are trying to insert has a database id,
			// it is assumed that it must have already been inserted, so we
			// don't care
			// about duplicate names, just duplicate ids
			if (!existingEntries.containsKey(entry.getId()))
			{
				existingEntries.put(entry.getId(), entry);
				existingEntriesNamesAndIds.put(entry.getName().toLowerCase(), entry.getId());
			}
		}
		else
		{
			// if all we have is the name of the new entry,
			// we need to check for duplicate names in both the existing items
			// and the new items
			if (newEntries.containsKey(entry.getName().toLowerCase()))
				return;

			if (existingEntriesNamesAndIds.containsKey(entry.getName().toLowerCase()))
			{
				Long id = existingEntriesNamesAndIds.get(entry.getName().toLowerCase());
				EntryBean existingEntry = existingEntries.get(id);
				existingEntry.setIsSelected(entry.getIsSelected());
				return;
			}				

			newEntries.put(entry.getName().toLowerCase(), entry);
		}
	}

	/**
	 * @return A list of all entries that belong in this category
	 */
	public List<EntryBean> getEntries()
	{
		List<EntryBean> allEntries = new ArrayList<EntryBean>();
		allEntries.addAll(existingEntries.values());
		allEntries.addAll(newEntries.values());
		return allEntries;
	}

	/**
	 * @return A list of all entries that belong in this category
	 */
	public List<EntryBean> getNewEntries()
	{
		List<EntryBean> allEntries = new ArrayList<EntryBean>();
		allEntries.addAll(newEntries.values());
		return allEntries;
	}

	/**
	 * @param entryName
	 *            The name of the entry to find
	 * 
	 * @return An entry with the specified name, if exists
	 */
	public EntryBean getEntry(String entryName)
	{
		if (existingEntriesNamesAndIds.containsKey(entryName.toLowerCase()))
		{
			for (EntryBean bean : existingEntries.values())
			{
				if (bean.getName().equalsIgnoreCase(entryName))
					return bean;
			}

		}

		if (newEntries.containsKey(entryName.toLowerCase()))
			return newEntries.get(entryName.toLowerCase());

		return null;
	}

	/**
	 * Prepares content values for inserting this bean
	 * 
	 * @param activityId
	 *            The activity id of the activity to associate this category
	 *            with
	 * @return ContentValues for inserting this bean
	 */
	public ContentValues getInsertContentValues(long activityId)
	{
		ContentValues values = new ContentValues();
		values.put(CategoryColumns.CATEGORY_NAME, getName());
		values.put(CategoryColumns.ACTIVITY_ID, activityId);
		values.put(CategoryColumns.CATEGORY_SORT_ORDER, sortOrder.toNumber());
		return values;
	}

}
