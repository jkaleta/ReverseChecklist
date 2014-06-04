package com.jakubkaleta.checklist.DataAccess.services.test;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.test.*;
import android.test.mock.MockContentResolver;
import android.text.Spanned;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.ChecklistDataProvider;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;
import com.jakubkaleta.checklist.DataAccess.test.DataProviderTestHelper;

/**
 * @author Jakub Kaleta Contains tests for methods in the DataAccessService
 *         class.
 */
public class DataAccessServiceTests extends ProviderTestCase2<ChecklistDataProvider>
{
	private ChecklistDataProvider dataProvider;
	private DataProviderTestHelper helper;
	private DataAccessService service;
	private final static String authority = "com.jakubkaleta.checklist.DataAccess.Activity;com.jakubkaleta.checklist.DataAccess.Category;com.jakubkaleta.checklist.DataAccess.Entry;com.jakubkaleta.checklist.DataAccess.ApplicationState;com.jakubkaleta.checklist.DataAccess.Configuration";
	private static final String[] CATEGORY_PROJECTION = new String[]
	{ CategoryColumns._ID, CategoryColumns.SORT_POSITION };
	private static final String[] ENTRY_PROJECTION = new String[]
			{ EntryColumns._ID, EntryColumns.SORT_POSITION };
	

	/**
	 * Main constructor
	 */
	public DataAccessServiceTests()
	{
		super(ChecklistDataProvider.class, authority);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		dataProvider = this.getProvider();
		helper = new DataProviderTestHelper(dataProvider);
		MockContentResolver mockResolver = new MockContentResolver();
		String[] authorities = authority.split(";");
		for (int i = 0; i < authorities.length; i++)
			mockResolver.addProvider(authorities[i], dataProvider);
		service = new DataAccessService(mockResolver);
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

		// delete all lists except the one that was there originally

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
	 * For testing the getSelectedItemsCount method with items selected
	 */
	public void testGetSelectedItemsCountWithItemsSelected()
	{
		// arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", false, 0);
		helper.insertNewItem(categoryId, "item2", false, 1);
		helper.insertNewItem(categoryId, "item3", true, 2);
		helper.insertNewItem(categoryId, "item4", false, 3);
		helper.insertNewItem(categoryId, "item5", true, 4);
		helper.insertNewItem(categoryId, "item6", false, 5);

		// Act & Assert
		int selectedItemsCount = service.getSelectedItemsCount();
		assertEquals(2, selectedItemsCount);
	}

	/**
	 * For testing the getSelectedItemsCount method with no items selected
	 */
	public void testGetSelectedItemsCountWithNoItemsSelected()
	{
		// arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", false, 0);
		helper.insertNewItem(categoryId, "item2", false, 1);
		helper.insertNewItem(categoryId, "item3", false, 2);
		helper.insertNewItem(categoryId, "item4", false, 3);
		helper.insertNewItem(categoryId, "item5", false, 4);
		helper.insertNewItem(categoryId, "item6", false, 5);

		// Act & Assert
		int selectedItemsCount = service.getSelectedItemsCount();
		assertEquals(0, selectedItemsCount);
	}

	/**
	 * For testing the getSelectedItemsCount method with no items in db
	 */
	public void testGetSelectedItemsCountWithNoItemsInDb()
	{
		// arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		// Act & Assert
		int selectedItemsCount = service.getSelectedItemsCount();
		assertEquals(0, selectedItemsCount);
	}

	/**
	 * For testing the getSelectedItemsCount method with empty db
	 */
	public void testGetSelectedItemsCountWithEmptyDb()
	{
		// Act & Assert
		int selectedItemsCount = service.getSelectedItemsCount();
		assertEquals(0, selectedItemsCount);
	}

	/**
	 * Tests if the GetAllToDoItemsEmailContent method returns correct results
	 * when there are no selected items in the database
	 */
	public void testGetAllToDoItemsEmailContentWithNoSelectedItems()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", false, 0);
		helper.insertNewItem(categoryId, "item2", false, 1);
		helper.insertNewItem(categoryId, "item3", false, 2);

		// Act & Assert
		Spanned reportText = service.getAllToDoItemsEmailContent(null);

		assertEquals(0, reportText.length());
	}

	/**
	 * Tests if the GetAllToDoItemsEmailContent method returns correct results
	 * when there are selected items in the database
	 */
	public void testGetAllToDoItemsEmailContentWithSelectedItems()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", true, 0);
		helper.insertNewItem(categoryId, "item2", true, 1);
		helper.insertNewItem(categoryId, "item3", false, 2);

		// Act & Assert
		Spanned reportText = service.getAllToDoItemsEmailContent(null);

		assertEquals(33, reportText.length());
	}

	/**
	 * Tests if the GetAllToDoItemsEmailContent method returns correct results
	 * when there are selected items in the database, and the report is printed
	 * for one list only
	 */
	public void testGetAllToDoItemsEmailContentWithSelectedItems_activityIdSet()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", true, 0);
		helper.insertNewItem(categoryId, "item2", true, 1);
		helper.insertNewItem(categoryId, "item3", false, 2);

		// Act & Assert
		Spanned reportText = service.getAllToDoItemsEmailContent(new Long(listId));

		assertEquals(33, reportText.length());
	}

	/**
	 * Tests if the GetAllToDoItemsEmailContent method returns correct results
	 * when there are no selected items in the database and the report is
	 * printed for one list only
	 */
	public void testGetAllToDoItemsEmailContentWithSelectedItems_activityIdSet_NothingPrinted()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", false, 1);
		helper.insertNewItem(categoryId, "item2", false, 2);
		helper.insertNewItem(categoryId, "item3", false, 3);

		// Act & Assert
		Spanned reportText = service.getAllToDoItemsEmailContent(new Long(listId));

		assertEquals(0, reportText.length());
	}

	/**
	 * Tests if the updateUserDefinedSort method produces correct results
	 */
	public void testUpdateUserDefinedSortWithMultipleElementsInList()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));

		long[] categoryIds = new long[5];
		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		categoryIds[0] = Integer.parseInt(categoryUri.getPathSegments().get(1));
		categoryUri = helper.insertNewCategory(listId, "dudus", 2);
		categoryIds[1] = Integer.parseInt(categoryUri.getPathSegments().get(1));
		categoryUri = helper.insertNewCategory(listId, "dudus", 3);
		categoryIds[2] = Integer.parseInt(categoryUri.getPathSegments().get(1));
		categoryUri = helper.insertNewCategory(listId, "dudus", 4);
		categoryIds[3] = Integer.parseInt(categoryUri.getPathSegments().get(1));
		categoryUri = helper.insertNewCategory(listId, "dudus", 5);
		categoryIds[4] = Integer.parseInt(categoryUri.getPathSegments().get(1));

		// Act
		// This test updates the sort order of category from 3 to 1 (0-based)
		service.updateUserDefinedSort(listId, categoryIds[3], 3, 1);

		long[] expectedCategoryIds = new long[5];
		expectedCategoryIds[0] = categoryIds[0];
		expectedCategoryIds[1] = categoryIds[3];
		expectedCategoryIds[2] = categoryIds[1];
		expectedCategoryIds[3] = categoryIds[2];
		expectedCategoryIds[4] = categoryIds[4];

		// now get all categories for activity
		Cursor categoriesToBeUpdatedCursor = dataProvider.query(CategoryColumns.CONTENT_URI,
				CATEGORY_PROJECTION, CategoryColumns.ACTIVITY_ID + " = " + listId, null,
				CategoryColumns.SORT_POSITION);

		long[] updatedCategoryIds = new long[5];
		categoriesToBeUpdatedCursor.moveToFirst();
		while (!categoriesToBeUpdatedCursor.isAfterLast())
		{
			long catId = categoriesToBeUpdatedCursor.getLong(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns._ID));

			int position = categoriesToBeUpdatedCursor.getInt(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns.SORT_POSITION));

			updatedCategoryIds[position - 1] = catId;
			categoriesToBeUpdatedCursor.moveToNext();
		}

		// Assert
		for (int i = 0; i < 5; i++)
			assertEquals(expectedCategoryIds[i], updatedCategoryIds[i]);
	}

	/**
	 * Tests if the updateUserDefinedSort method produces correct results when
	 * there are only two elements in the list
	 */
	public void testUpdateUserDefinedSortWithOnlyTwoElementsInList()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));

		long[] categoryIds = new long[5];
		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		categoryIds[0] = Integer.parseInt(categoryUri.getPathSegments().get(1));
		categoryUri = helper.insertNewCategory(listId, "dudus", 2);
		categoryIds[1] = Integer.parseInt(categoryUri.getPathSegments().get(1));

		// Act & Assert
		// This test updates the sort order of category from 2 to 1 (0-based)
		service.updateUserDefinedSort(listId, categoryIds[1], 1, 0);

		long[] expectedCategoryIds = new long[2];
		expectedCategoryIds[0] = categoryIds[1];
		expectedCategoryIds[1] = categoryIds[0];

		// now get all categories for activity
		Cursor categoriesToBeUpdatedCursor = dataProvider.query(CategoryColumns.CONTENT_URI,
				CATEGORY_PROJECTION, CategoryColumns.ACTIVITY_ID + " = " + listId, null,
				CategoryColumns.SORT_POSITION);

		categoriesToBeUpdatedCursor.moveToFirst();
		while (!categoriesToBeUpdatedCursor.isAfterLast())
		{
			long catId = categoriesToBeUpdatedCursor.getLong(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns._ID));

			int position = categoriesToBeUpdatedCursor.getInt(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns.SORT_POSITION));

			assertEquals(expectedCategoryIds[position - 1], catId);
			categoriesToBeUpdatedCursor.moveToNext();
		}
	}
	
	/**
	 * Tests if the updateUserDefinedSort method produces correct results
	 */
	public void testUpdateUserDefinedSortForEntriesWithMultipleElementsInList()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));
		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		long categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		
		long[] entryIds = new long[5];
		Uri entryUri = helper.insertNewItem(categoryId, "dudus", false, 1);
		entryIds[0] = Integer.parseInt(entryUri.getPathSegments().get(1));
		entryUri = helper.insertNewItem(categoryId, "dudus", false, 2);
		entryIds[1] = Integer.parseInt(entryUri.getPathSegments().get(1));
		entryUri = helper.insertNewItem(categoryId, "dudus", false, 3);
		entryIds[2] = Integer.parseInt(entryUri.getPathSegments().get(1));
		entryUri = helper.insertNewItem(categoryId, "dudus", false, 4);
		entryIds[3] = Integer.parseInt(entryUri.getPathSegments().get(1));
		entryUri = helper.insertNewItem(categoryId, "dudus", false, 5);
		entryIds[4] = Integer.parseInt(entryUri.getPathSegments().get(1));

		// Act
		// This test updates the sort order of entry from 3 to 1 (0-based)
		service.updateUserDefinedSortForEntry(categoryId, entryIds[3], 3, 1);

		long[] expectedEntryIds = new long[5];
		expectedEntryIds[0] = entryIds[0];
		expectedEntryIds[1] = entryIds[3];
		expectedEntryIds[2] = entryIds[1];
		expectedEntryIds[3] = entryIds[2];
		expectedEntryIds[4] = entryIds[4];

		// now get all entries for category
		Cursor entriesToBeUpdatedCursor = dataProvider.query(EntryColumns.CONTENT_URI,
				ENTRY_PROJECTION, EntryColumns.CATEGORY_ID + " = " + categoryId, null,
				EntryColumns.SORT_POSITION);

		long[] updatedEntryIds = new long[5];
		entriesToBeUpdatedCursor.moveToFirst();
		while (!entriesToBeUpdatedCursor.isAfterLast())
		{
			long entId = entriesToBeUpdatedCursor.getLong(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns._ID));

			int position = entriesToBeUpdatedCursor.getInt(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns.SORT_POSITION));

			updatedEntryIds[position - 1] = entId;
			entriesToBeUpdatedCursor.moveToNext();
		}

		// Assert
		for (int i = 0; i < 5; i++)
			assertEquals(expectedEntryIds[i], updatedEntryIds[i]);
	}

	/**
	 * Tests if the updateUserDefinedSortForEntry method produces correct results when
	 * there are only two elements in the list
	 */
	public void testUpdateUserDefinedSortForEntryWithOnlyTwoElementsInList()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));
		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		long categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));

		long[] entryIds = new long[2];
		Uri entryUri = helper.insertNewItem(categoryId, "dudus", true, 1);
		entryIds[0] = Integer.parseInt(entryUri.getPathSegments().get(1));
		entryUri = helper.insertNewItem(categoryId, "dudus2", true, 2);
		entryIds[1] = Integer.parseInt(entryUri.getPathSegments().get(1));

		// Act & Assert
		// This test updates the sort order of entry from 2 to 1 (0-based)
		service.updateUserDefinedSortForEntry(categoryId, entryIds[1], 1, 0);

		long[] expectedEntryIds = new long[2];
		expectedEntryIds[0] = entryIds[1];
		expectedEntryIds[1] = entryIds[0];

		// now get all entries for category
		Cursor entriesToBeUpdatedCursor = dataProvider.query(EntryColumns.CONTENT_URI,
				ENTRY_PROJECTION, EntryColumns.CATEGORY_ID + " = " + categoryId, null,
				EntryColumns.SORT_POSITION);

		long[] updatedEntryIds = new long[2];
		entriesToBeUpdatedCursor.moveToFirst();
		while (!entriesToBeUpdatedCursor.isAfterLast())
		{
			long entId = entriesToBeUpdatedCursor.getLong(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns._ID));

			int position = entriesToBeUpdatedCursor.getInt(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns.SORT_POSITION));

			updatedEntryIds[position - 1] = entId;
			entriesToBeUpdatedCursor.moveToNext();
		}
		// Assert
		for (int i = 0; i < 2; i++)
			assertEquals(expectedEntryIds[i], updatedEntryIds[i]);
	}


	/**
	 * Tests if the getAllActivitiesWithChildren method returns correct results
	 */
	public void testGetAllActivitiesDataSource()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", true, 1);
		helper.insertNewItem(categoryId, "item2", true, 2);
		helper.insertNewItem(categoryId, "item3", false, 3);

		// Act & Assert
		ActivitiesDataSource dataSource = service.getActivitiesWithChildren(null, true);

		assertNotNull(dataSource);
		assertEquals("testlist", dataSource.getActivity(listId).getName());

		ActivityBean act = dataSource.getActivity(listId);

		assertEquals("dudus", act.getCategory(categoryId).getName());

		CategoryBean cat = act.getCategory(categoryId);

		assertEquals(3, cat.getEntries().size());
	}

	/**
	 * Tests if the getAllActivitiesWithChildren method returns correct results
	 * when the database contains empty lists
	 */
	public void testGetAllActivitiesDataSource_EmptyList()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		long listId = Long.parseLong(listUri.getPathSegments().get(1));

		// Act & Assert
		ActivitiesDataSource dataSource = service.getActivitiesWithChildren(new Long[]
		{ listId }, true);

		assertNotNull(dataSource);
		assertEquals("testlist", dataSource.getActivity((int) listId).getName());
	}

	/**
	 * Tests the insertNewItems method
	 */
	public void testInsertNewItems()
	{
		// Arrange
		List<ActivityBean> newItems = new ArrayList<ActivityBean>();

		EntryBean entry = new EntryBean(0, "entry", true);
		CategoryBean cat = new CategoryBean(0, "category", CategorySortOrder.Custom);
		ActivityBean act = new ActivityBean(0, "activity");

		cat.addEntry(entry);
		act.addCategory(cat);
		newItems.add(act);

		// Act
		service.insertNewItems(newItems);
		ActivitiesDataSource allActivities = service.getActivitiesWithChildren(null, true);

		// Assert
		ActivityBean insertedBean = allActivities.getActivity("activity");
		assertNotNull(insertedBean);
		assertFalse(insertedBean.getId() == 0);
		CategoryBean insertedCat = insertedBean.getCategory("category");
		assertNotNull(insertedCat);
		assertFalse(insertedCat.getId() == 0);
		EntryBean insertedEntry = insertedCat.getEntries().get(0);
		assertNotNull(insertedEntry);
		assertFalse(insertedEntry.getId() == 0);
		assertTrue(insertedEntry.getIsSelected());

	}

	/**
	 */
	public void testCheckActivityNameAvailability_withDefaultListId()
	{
		// arrange
		helper.insertNewList("testlist");

		// Act
		Boolean nameAvailable = service.checkActivityNameAvailability("testlist", 0);
		assertFalse(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList", 0);
		assertFalse(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList1", 0);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("1testList", 0);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList   ", 0);
		assertTrue(nameAvailable);
	}

	/**
	 * Tests the checkActivityNameAvailability method when the list id is not 0.
	 */
	public void testCheckActivityNameAvailability_withListIdSet()
	{
		// arrange
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));

		// Act
		Boolean nameAvailable = service.checkActivityNameAvailability("testlist", listId);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList", listId);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList1", listId);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("1testList", listId);
		assertTrue(nameAvailable);
		nameAvailable = service.checkActivityNameAvailability("testList   ", listId);
		assertTrue(nameAvailable);
	}

	/**
	 * Tests the copyList method when the list to copy is empty.
	 */
	public void testCopyList_copyEmptyList()
	{
		// Arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		// Act
		service.copyList(listId, "testList2");
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testList2");

		// Assert
		assertNotNull(newList);
		assertTrue(newList.getCategories().isEmpty());
	}

	/**
	 * Tests the copyList method when the list to copy does not exist.
	 */
	public void testCopyList_copyNullList()
	{
		// Arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		// Act
		service.copyList(-13, "testList2");
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testList2");

		// Assert
		assertNull(newList);
	}

	/**
	 * Tests the copyList method when the new list name is empty.
	 */
	public void testCopyList_emptyNewName()
	{
		// Arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		// Act
		service.copyList(listId, "");
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testList2");

		// Assert
		assertNull(newList);
	}

	/**
	 * Tests the copyList method when the list to copy is valid and has valid
	 * categories and entries
	 */
	public void testCopyList_copyValidList()
	{
		// Arrange
		long listId = addListWith6Items("testlist");

		// Act
		service.copyList(listId, "testList2");
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testList2");

		// Assert
		assertNotNull(newList);
		CategoryBean newCategory1 = newList.getCategory("cat1");
		CategoryBean newCategory2 = newList.getCategory("cat2");
		assertNotNull(newCategory1);
		assertNotNull(newCategory2);
		assertNotNull(newCategory1.getEntry("item1"));
		assertNotNull(newCategory1.getEntry("item2"));
		assertNotNull(newCategory1.getEntry("item3"));
		assertNotNull(newCategory2.getEntry("item4"));
		assertNotNull(newCategory2.getEntry("item5"));
		assertNotNull(newCategory2.getEntry("item6"));
	}
	
	/**
	 * Tests the markAllItemsSelected method
	 */
	public void testMarkAllItemsSelected()
	{
		// Arrange
		addListWith6Items("testlist");

		// Act
		service.markAllItemsSelected(2l, true);
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testlist");

		// Assert
		CategoryBean newCategory1 = newList.getCategory("cat1");
		CategoryBean newCategory2 = newList.getCategory("cat2");
		assertTrue(newCategory1.getEntry("item1").getIsSelected());
		assertTrue(newCategory1.getEntry("item2").getIsSelected());
		assertTrue(newCategory1.getEntry("item3").getIsSelected());
		assertTrue(newCategory2.getEntry("item4").getIsSelected());
		assertTrue(newCategory2.getEntry("item5").getIsSelected());
		assertTrue(newCategory2.getEntry("item6").getIsSelected());		
		
		// Act
		service.markAllItemsSelected(2l, false);
		allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		newList = allLists.getActivity("testlist");

		// Assert
		newCategory1 = newList.getCategory("cat1");
		newCategory2 = newList.getCategory("cat2");
		assertFalse(newCategory1.getEntry("item1").getIsSelected());
		assertFalse(newCategory1.getEntry("item2").getIsSelected());
		assertFalse(newCategory1.getEntry("item3").getIsSelected());
		assertFalse(newCategory2.getEntry("item4").getIsSelected());
		assertFalse(newCategory2.getEntry("item5").getIsSelected());
		assertFalse(newCategory2.getEntry("item6").getIsSelected());	
	}
	
	/**
	 * Tests the markAllItemsSelectedInCategory method
	 */
	public void testMarkAllItemsSelectedInCategory()
	{
		// Arrange
		addListWith6Items("testlist");

		// Act
		service.markAllItemsSelectedInCategory(17l, true);
		ActivitiesDataSource allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		ActivityBean newList = allLists.getActivity("testlist");

		// Assert
		CategoryBean newCategory1 = newList.getCategory("cat1");
		CategoryBean newCategory2 = newList.getCategory("cat2");
		assertTrue(newCategory1.getEntry("item1").getIsSelected());
		assertTrue(newCategory1.getEntry("item2").getIsSelected());
		assertTrue(newCategory1.getEntry("item3").getIsSelected());
		assertFalse(newCategory2.getEntry("item4").getIsSelected());
		assertFalse(newCategory2.getEntry("item5").getIsSelected());
		assertFalse(newCategory2.getEntry("item6").getIsSelected());
		
		// Act
		service.markAllItemsSelectedInCategory(17l, false);
		allLists = service.getActivitiesWithChildren(new Long[]
		{}, true);
		newList = allLists.getActivity("testlist");

		// Assert
		newCategory1 = newList.getCategory("cat1");
		newCategory2 = newList.getCategory("cat2");
		assertFalse(newCategory1.getEntry("item1").getIsSelected());
		assertFalse(newCategory1.getEntry("item2").getIsSelected());
		assertFalse(newCategory1.getEntry("item3").getIsSelected());
		assertFalse(newCategory2.getEntry("item4").getIsSelected());
		assertFalse(newCategory2.getEntry("item5").getIsSelected());
		assertFalse(newCategory2.getEntry("item6").getIsSelected());
	}
	
	private long addListWith6Items(String listName)
	{
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		Uri category1Uri = helper.insertNewCategory(listId, "cat1", 1);
		int category1Id = Integer.parseInt(category1Uri.getPathSegments().get(1));

		Uri category2Uri = helper.insertNewCategory(listId, "cat2", 1);
		int category2Id = Integer.parseInt(category2Uri.getPathSegments().get(1));

		helper.insertNewItem(category1Id, "item1", false, 0);
		helper.insertNewItem(category1Id, "item2", false, 1);
		helper.insertNewItem(category1Id, "item3", false, 2);
		helper.insertNewItem(category2Id, "item4", false, 3);
		helper.insertNewItem(category2Id, "item5", false, 4);
		helper.insertNewItem(category2Id, "item6", false, 5);
		
		return listId;
	}
}
