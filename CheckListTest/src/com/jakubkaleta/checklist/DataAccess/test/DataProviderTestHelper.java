package com.jakubkaleta.checklist.DataAccess.test;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import com.jakubkaleta.checklist.DataAccess.ChecklistDataProvider;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * @author Jakub Kaleta This class helps with testing data providers by
 *         providing methods that can be shared across unit test classes.
 */
public class DataProviderTestHelper
{
	private ChecklistDataProvider dataProvider;
	private ContentResolver contentResolver;

	/**
	 * Initializes a new instance of DataProviderTestHelper
	 * 
	 * @param provider
	 *            The ChecklistDataProvider to use.
	 */
	public DataProviderTestHelper(ChecklistDataProvider provider)
	{
		dataProvider = provider;
	}

	/**
	 * Initializes a new instance of DataProviderTestHelper
	 * 
	 * @param resolver
	 *            The ContentResolver to use.
	 */
	public DataProviderTestHelper(ContentResolver resolver)
	{
		contentResolver = resolver;
	}

	/**
	 * Inserts a new list with the specified name
	 * 
	 * @param itemName
	 *            The name of the list to insert
	 * @return The Uri of the inserted object.
	 */
	public Uri insertNewList(String itemName)
	{
		ContentValues values = new ContentValues();
		values.put(ActivityColumns.ACTIVITY_NAME, itemName);

		if (contentResolver == null)
			return dataProvider.insert(ActivityColumns.CONTENT_URI, values);

		return contentResolver.insert(ActivityColumns.CONTENT_URI, values);
	}

	/**
	 * Inserts new category with the given values.
	 * 
	 * @param activityId
	 *            The id of the activity to associate with
	 * @param itemName
	 *            The name of the new category to insert
	 * @param sortPosition
	 *            The expected sort position
	 * @return The Uri of the newly inserted category
	 */
	public Uri insertNewCategory(long activityId, String itemName, int sortPosition)
	{
		ContentValues values = new ContentValues();
		values.put(CategoryColumns.CATEGORY_NAME, itemName);
		values.put(CategoryColumns.ACTIVITY_ID, activityId);
		values.put(CategoryColumns.SORT_POSITION, sortPosition);

		if (contentResolver == null)
			return dataProvider.insert(CategoryColumns.CONTENT_URI, values);

		return contentResolver.insert(CategoryColumns.CONTENT_URI, values);
	}

	/**
	 * Inserts a new item with the specified values
	 * 
	 * @param categoryId
	 *            The id of the category to associate with
	 * @param itemName
	 *            The name of the new item
	 * @param isSelected
	 *            True if the item is supposed to be selected
	 * @param sortPosition 
	 * 			  User assigned sort position
	 * @return The Uri of the newly inserted item
	 */
	public Uri insertNewItem(long categoryId, String itemName, boolean isSelected, int sortPosition)
	{
		ContentValues values = new ContentValues();
		values.put(EntryColumns.ENTRY_NAME, itemName);
		values.put(EntryColumns.CATEGORY_ID, categoryId);
		values.put(EntryColumns.IS_SELECTED, isSelected ? 1 : 0);
		
		if(sortPosition != 0)
		values.put(EntryColumns.SORT_POSITION, sortPosition);

		if (contentResolver == null)
			return dataProvider.insert(EntryColumns.CONTENT_URI, values);

		return contentResolver.insert(EntryColumns.CONTENT_URI, values);
	}
	
	 /** Inserts a new item with the specified values
	 * 
	 * @param categoryId
	 *            The id of the category to associate with
	 * @param itemName
	 *            The name of the new item
	 * @param isSelected
	 *            True if the item is supposed to be selected
	 * @return The Uri of the newly inserted item
	 */
	public Uri insertNewItem(long categoryId, String itemName, boolean isSelected)
	{
		return insertNewItem(categoryId, itemName, isSelected, 0);
	}
}
