package com.jakubkaleta.checklist.DataAccess.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.ContentValues;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;

/**
 * This class represents a single activity entry in the database It is a
 * convenience class - for storing and manipulating activities
 * 
 * @author Jakub Kaleta
 */
public class ActivityBean
{
	private long id;
	private String name;	
	private HashMap<Long, CategoryBean> existingCategories = new HashMap<Long, CategoryBean>();
	private HashSet<String> existingCategoriesNames = new HashSet<String>();
	private HashMap<String, CategoryBean> newCategories = new HashMap<String, CategoryBean>();;

	/**
	 * Creates a new instance of ActivityBean
	 * 
	 * @param id
	 *            The database id of this activity
	 * @param name
	 *            The name of the activity
	
	 */
	public ActivityBean(long id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @return The database id of the activity
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return The name of the activity.
	 */
	public String getName()
	{
		return name;
	}	
	
	/**
	 * Adds the passed in category to the underlying collection of categories.
	 * Checks for uniqueness by database id, and if this is 0, by name
	 * 
	 * @param category
	 *            The category to add to the collection of categories of this
	 *            activity
	 */
	public void addCategory(CategoryBean category)
	{
		if (category.getId() != 0)
		{
			// if the entry that we are trying to insert has a database id,
			// it is assumed that it must have already been inserted, so we
			// don't care
			// about duplicate names, just duplicate ids
			if (!existingCategories.containsKey(category.getId()))
			{
				existingCategories.put(category.getId(), category);
				existingCategoriesNames.add(category.getName().toLowerCase());
			}

		}
		else
		{
			// if all we have is the name of the new entry,
			// we need to check for duplicate names in both the existing items
			// and the new items
			if (newCategories.containsKey(category.getName()))
				return;

			if (existingCategoriesNames.contains(category.getName()))
				return;

			newCategories.put(category.getName().toLowerCase(), category);
		}
	}

	/**
	 * @return A list of all Categories that belong in this category
	 */
	public List<CategoryBean> getCategories()
	{
		List<CategoryBean> allCategories = new ArrayList<CategoryBean>();
		allCategories.addAll(existingCategories.values());
		allCategories.addAll(newCategories.values());
		return allCategories;
	}

	/**
	 * @return A list of all new Categories that belong in this category
	 */
	public List<CategoryBean> getNewCategories()
	{
		List<CategoryBean> allCategories = new ArrayList<CategoryBean>();
		allCategories.addAll(newCategories.values());
		return allCategories;
	}

	/**
	 * @param categoryId
	 *            The id of the category to find
	 * @return A category with the specified name, if exists
	 */
	public CategoryBean getCategory(long categoryId)
	{
		if (existingCategories.containsKey(categoryId))
			return existingCategories.get(categoryId);

		return null;
	}

	/**
	 * @param categoryName
	 *            The name of the category to find
	 * @return A category with the specified name, if exists
	 */
	public CategoryBean getCategory(String categoryName)
	{
		if (existingCategoriesNames.contains(categoryName.toLowerCase()))
		{
			for (CategoryBean bean : existingCategories.values())
			{
				if (bean.getName().equalsIgnoreCase(categoryName))
					return bean;
			}

		}

		if (newCategories.containsKey(categoryName.toLowerCase()))
			return newCategories.get(categoryName.toLowerCase());

		return null;
	}

	/**
	 * Prepares content values for inserting this bean
	 * 
	 * @return ContentValues for inserting this bean
	 */
	public ContentValues getInsertContentValues()
	{
		ContentValues values = new ContentValues();
		values.put(ActivityColumns.ACTIVITY_NAME, getName());
		return values;
	}
}
