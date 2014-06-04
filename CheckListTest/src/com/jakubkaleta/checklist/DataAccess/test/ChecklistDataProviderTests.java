package com.jakubkaleta.checklist.DataAccess.test;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.test.*;

import com.jakubkaleta.checklist.DataAccess.ChecklistDataProvider;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * Tests the functionality in ChecklistDataProvider
 * 
 * @author Jakub Kaleta 
 */
public class ChecklistDataProviderTests extends ProviderTestCase2<ChecklistDataProvider>
{
	private ChecklistDataProvider dataProvider;
	private DataProviderTestHelper helper;

	/**
	 * Initializes a new instance of ChecklistDataProviderTests class.
	 */
	public ChecklistDataProviderTests()
	{
		super(
				ChecklistDataProvider.class,
				"com.jakubkaleta.checklist.DataAccess.Activity;com.jakubkaleta.checklist.DataAccess.Category;com.jakubkaleta.checklist.DataAccess.Entry;com.jakubkaleta.checklist.DataAccess.ApplicationState;com.jakubkaleta.checklist.DataAccess.Configuration");
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		dataProvider = this.getProvider();
		helper = new DataProviderTestHelper(dataProvider);
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

		// delete a list with id 2
		dataProvider.delete(ContentUris.withAppendedId(ActivityColumns.CONTENT_URI, 2), null, null);
	}

	/**
	 * This test verifies that the database could be created successfully It
	 * tests if the provider correctly handles all the types it claims it does
	 */
	public void testPreConditions()
	{
		assertNotNull(dataProvider);
		assertEquals(dataProvider.getType(ActivityColumns.CONTENT_URI),
				ActivityColumns.CONTENT_TYPE);
		assertEquals(dataProvider.getType(AppStateColumns.CONTENT_URI),
				AppStateColumns.CONTENT_TYPE);
		assertEquals(dataProvider.getType(CategoryColumns.CONTENT_URI),
				CategoryColumns.CONTENT_TYPE);
		assertEquals(dataProvider.getType(ConfigurationParametersColumns.CONTENT_URI),
				ConfigurationParametersColumns.CONTENT_TYPE);
		assertEquals(dataProvider.getType(EntryColumns.CONTENT_URI), EntryColumns.CONTENT_TYPE);
	}

	/**
	 * For testing the query for the flat view report
	 */
	public void testQueryForAllEntriesFromOneListWithNoFilter()
	{
		// arrange
		final String[] PROJECTION = new String[]
		{ EntryColumns._ID, EntryColumns.ENTRY_NAME, EntryColumns.DATE_CREATED,
				EntryColumns.IS_SELECTED, CategoryColumns.CATEGORY_NAME,
				ActivityColumns.ACTIVITY_NAME };

		// Act
		// Get a cursor to access the entries
		Cursor allEntriesCursor = dataProvider.query(EntryColumns.CONTENT_URI, PROJECTION, null,
				null, EntryColumns.DEFAULT_SORT_ORDER);

		// Assert
		assertNotNull(allEntriesCursor);

		// the cursor should have all items from the default shopping list (114)
		assertEquals(114, allEntriesCursor.getCount());

		allEntriesCursor.close();
	}

	/**
	 * For testing the query for the flat view report
	 */
	public void testQueryForAllEntriesFromTwoListsWithNoFilter()
	{
		// arrange
		final String[] PROJECTION = new String[]
		{ EntryColumns._ID, EntryColumns.ENTRY_NAME, EntryColumns.DATE_CREATED,
				EntryColumns.IS_SELECTED, CategoryColumns.CATEGORY_NAME,
				ActivityColumns.ACTIVITY_NAME };

		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));

		helper.insertNewItem(categoryId, "item1", false);
		helper.insertNewItem(categoryId, "item2", false);
		helper.insertNewItem(categoryId, "item3", false);
		helper.insertNewItem(categoryId, "item4", false);
		helper.insertNewItem(categoryId, "item5", false);
		helper.insertNewItem(categoryId, "item6", false);

		// Act
		Cursor allEntriesCursor = dataProvider.query(EntryColumns.CONTENT_URI, PROJECTION, null,
				null, EntryColumns.DEFAULT_SORT_ORDER);

		// Assert
		assertNotNull(allEntriesCursor);

		// the cursor should have all items from the default shopping list
		// plus all the items from the new list that we added in arrange
		assertEquals(120, allEntriesCursor.getCount());

		allEntriesCursor.close();
	}

	/**
	 * For testing the trigger on inserts into categories Every new category is
	 * expected to have a sort number equal to the amount of elements in the
	 * table.
	 */
	public void testTriggerOnInsertIntoCategories()
	{
		// arrange
		final String[] PROJECTION = new String[]
		{ CategoryColumns.CATEGORY_NAME, CategoryColumns.SORT_POSITION };

		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		helper.insertNewCategory(listId, "dudus", 0);
		helper.insertNewCategory(listId, "dudus2", 0);
		helper.insertNewCategory(listId, "dudus3", 0);
		helper.insertNewCategory(listId, "dudus4", 0);

		// Get a cursor to access the categories
		Cursor allCategoriesCursor = dataProvider.query(CategoryColumns.CONTENT_URI, PROJECTION,
				CategoryColumns.ACTIVITY_ID + " = " + 1, null, CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION);

		// Assert
		assertNotNull(allCategoriesCursor);

		int i = 1;
		allCategoriesCursor.moveToFirst();
		while (!allCategoriesCursor.isAfterLast())
		{
			int position = allCategoriesCursor.getInt(allCategoriesCursor
					.getColumnIndex(CategoryColumns.SORT_POSITION));

			assertEquals(i++, position);

			allCategoriesCursor.moveToNext();
		}

		allCategoriesCursor.close();
	}
	
	/**
	 * For testing the trigger on inserts into entries Every new entry is
	 * expected to have a sort number equal to the amount of elements in the
	 * table.
	 */
	public void testTriggerOnInsertIntoEntries()
	{
		// arrange
		final String[] PROJECTION = new String[]
		{ EntryColumns.ENTRY_NAME, EntryColumns.SORT_POSITION  };

		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		Uri catUri = helper.insertNewCategory(listId, "dudus", 0);
		long catid = Integer.parseInt(catUri.getPathSegments().get(1));
		
		helper.insertNewItem(listId, "dudus", false);
		helper.insertNewItem(listId, "dudus2", false);
		helper.insertNewItem(listId, "dudus3", false);
		helper.insertNewItem(listId, "dudus4", false);

		// Get a cursor to access the entries
		Cursor allEntriesCursor = dataProvider.query(EntryColumns.CONTENT_URI, PROJECTION,
				EntryColumns.CATEGORY_ID + " = " + catid, null, EntryColumns.TABLE_NAME + "." + EntryColumns.SORT_POSITION);

		// Assert
		assertNotNull(allEntriesCursor);

		int i = 1;
		allEntriesCursor.moveToFirst();
		while (!allEntriesCursor.isAfterLast())
		{
			int position = allEntriesCursor.getInt(allEntriesCursor
					.getColumnIndex(EntryColumns.SORT_POSITION));

			assertEquals(i++, position);

			allEntriesCursor.moveToNext();
		}

		allEntriesCursor.close();
	}
}
