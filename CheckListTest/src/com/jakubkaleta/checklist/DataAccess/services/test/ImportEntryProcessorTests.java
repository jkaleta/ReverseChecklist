package com.jakubkaleta.checklist.DataAccess.services.test;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.jakubkaleta.checklist.DataAccess.ChecklistDataProvider;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;
import com.jakubkaleta.checklist.DataAccess.services.ImportDuplicateHandling;
import com.jakubkaleta.checklist.DataAccess.services.ImportEntryProcessor;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.test.DataProviderTestHelper;

/**
 * Contains tests for methods in the ImportEntryProcessor class.
 * 
 * @author Jakub Kaleta
 */
public class ImportEntryProcessorTests extends ProviderTestCase2<ChecklistDataProvider>
{
	private ChecklistDataProvider dataProvider;
	ImportEntryProcessor processor;
	private DataProviderTestHelper helper;
	private final static String authority = "com.jakubkaleta.checklist.DataAccess.Activity;com.jakubkaleta.checklist.DataAccess.Category;com.jakubkaleta.checklist.DataAccess.Entry;com.jakubkaleta.checklist.DataAccess.ApplicationState;com.jakubkaleta.checklist.DataAccess.Configuration";

	/**
	 * Main constructor
	 */
	public ImportEntryProcessorTests()
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
		processor = new ImportEntryProcessor(mockResolver);
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();

		// delete a list with id 2
		dataProvider.delete(ContentUris.withAppendedId(ActivityColumns.CONTENT_URI, 2), null, null);
	}

	/**
	 * For testing the processDataImport method
	 */
	public void testProcessDataImport()
	{
		// Arrange
		List<String[]> lines = new ArrayList<String[]>();

		lines.add(new String[]
		{ "listName", "category1", "item1" });
		lines.add(new String[]
		{ "listName", "category2", "item1" });
		// this line should be skipped from import, because is a duplicate
		lines.add(new String[]
		{ "listName", "category2", "item1" });
		// this line should import fine
		lines.add(new String[]
		{ "listName", "category2", "item2" });
		// so should this one
		lines.add(new String[]
		{ "listName2", "category2", "item1" });

		ActivitiesDataSource dataSource = processor.processDataImport(lines,
				ImportDuplicateHandling.MergeExistingAndImportedLists);

		List<ActivityBean> newActivities = dataSource.getNewActivities();

		// Assert

		// there should be 2 lists, the first one with 2 categories, 1 item each
		// and the second one with one category and one item
		assertEquals(2, newActivities.size());
		
		ActivityBean list1 = newActivities.get(1);
		ActivityBean list2 = newActivities.get(0);
		
		assertEquals("listName", list1.getName());
		assertEquals(2, list1.getCategories().size());
		assertEquals("category1", list1.getCategories().get(1).getName());
		assertEquals("item1", list1.getCategories().get(1).getEntries().get(0).getName());
		assertEquals("category2", list1.getCategories().get(0).getName());
		assertEquals("item1", list1.getCategories().get(0).getEntries().get(1).getName());
		assertEquals("item2", list1.getCategories().get(0).getEntries().get(0).getName());
		assertEquals(2, list1.getCategories().size());
		assertEquals("listName2", list2.getName());
		assertEquals("category2", list2.getCategories().get(0).getName());
		assertEquals("item1", list2.getCategories().get(0).getEntries().get(0).getName());
		
	}
	
	/**
	 * For testing the processDataImport method, the case when 
	 * the selected mode is to merge lists, 
	 * and there are lists that need merging
	 */
	public void testProcessDataImport_MergeWithExistingList()
	{
		// Arrange
		
		// First, insert a new list with one category, one item
		// arrange
		Uri listUri = helper.insertNewList("testList");
		long listId = Long.parseLong(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "testCategory", 1);
		long categoryId = Long.parseLong(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", true);
		
		List<String[]> lines = new ArrayList<String[]>();

		lines.add(new String[]
		{ "testList", "testCategory", "item1" });
		lines.add(new String[]
		{ "testList", "testCategory", "item2" });
		lines.add(new String[]
		{ "testList", "category2", "item1" });
		lines.add(new String[]
		{ "testList2", "category2", "item2" });

		ActivitiesDataSource dataSource = processor.processDataImport(lines,
				ImportDuplicateHandling.MergeExistingAndImportedLists);

		// Assert

		// there should be 3 lists
		assertEquals(3, dataSource.getActivities().size());
		
		ActivityBean list1 = dataSource.getActivity("testList");
		ActivityBean list2 = dataSource.getActivity("testList2");
		
		// the first list should have two categories, one old and one new
		assertEquals(2, list1.getCategories().size());
		CategoryBean category1 = list1.getCategory("testCategory");
		CategoryBean category2 = list1.getCategory("category2");
		assertNotSame(1l, category1.getId());
		assertSame(0l, category2.getId());
		
		// the old category should now have two items, one old and one new
		EntryBean entry1 = category1.getEntry("item1");
		EntryBean entry2 = category1.getEntry("item2");
		assertNotSame(0l, entry1.getId());
		assertSame(0l, entry2.getId());
		
		// finally there should also be a new list, with one new category and one new item
		assertEquals(1, list2.getCategories().size());
		CategoryBean category3 = list1.getCategory("category2");
		assertSame(0l, category3.getId());
		EntryBean entry3 = category1.getEntry("item2");
		assertSame(0l, entry3.getId());
	}
	
	/**
	 * For testing the processDataImport method, the case when 
	 * the selected mode is to rename imported lists, 
	 * and there are lists that need renaming
	 */
	public void testProcessDataImport_RenameImportedList()
	{
		// Arrange
		
		// First, insert a new list with one category, one item
		// arrange
		Uri listUri = helper.insertNewList("testList");
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));

		Uri categoryUri = helper.insertNewCategory(listId, "testCategory", 1);
		long categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", true);
		
		List<String[]> lines = new ArrayList<String[]>();

		lines.add(new String[]
		{ "testList", "testCategory", "item1" });
		lines.add(new String[]
		{ "testList", "testCategory", "item2" });
		lines.add(new String[]
		{ "testList", "category2", "item1" });
		lines.add(new String[]
		{ "testList2", "category2", "item2" });

		ActivitiesDataSource dataSource = processor.processDataImport(lines,
				ImportDuplicateHandling.RenameImportedList);

		// Assert

		// there should be 4 lists
		assertEquals(4, dataSource.getActivities().size());
		
		ActivityBean list1 = dataSource.getActivity("testList");
		ActivityBean list2 = dataSource.getActivity("testList_new");
		ActivityBean list3 = dataSource.getActivity("testList2");
		
		// the first list should have 1 category, old
		assertEquals(1, list1.getCategories().size());
		CategoryBean category1 = list1.getCategory("testCategory");
		assertNotSame(0, category1.getId());		
		// the old category should  have 1 item, old
		EntryBean entry1 = category1.getEntry("item1");
		assertNotSame(0, entry1.getId());
		
		// the second list should have two new categories
		assertEquals(2, list2.getCategories().size());
		CategoryBean category2 = list2.getCategory("testCategory");
		CategoryBean category3 = list2.getCategory("category2");
		assertSame(0l, category2.getId());
		assertSame(0l, category3.getId());
		// both categories should have 1 new item each
		EntryBean entry2 = category2.getEntry("item2");
		EntryBean entry3 = category3.getEntry("item1");
		assertSame(0l, entry2.getId());
		assertSame(0l, entry3.getId());
		
		// the third list should have one new category and one new item
		assertEquals(1, list3.getCategories().size());
		CategoryBean category4 = list3.getCategory("category2");
		assertSame(0l, category4.getId());
		EntryBean entry4 = category4.getEntry("item2");
		assertSame(0l, entry4.getId());
	}
}
