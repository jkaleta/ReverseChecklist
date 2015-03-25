package com.jakubkaleta.checklist.test;

import java.util.ArrayList;
import java.util.List;

import com.jakubkaleta.checklist.ActivityList;
import com.jakubkaleta.checklist.AddEditActivity;
import com.jakubkaleta.checklist.AllToDoItemsReport;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.test.DataProviderTestHelper;
import com.jayway.android.robotium.solo.Solo;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

/**
 * Contains UI tests for the ActivityList activity
 * 
 * @author Jakub Kaleta
 */
public class ActivityListTest extends ActivityInstrumentationTestCase2<ActivityList>
{
	private ActivityList activityList;
	private ListView listView;
	private ContentResolver contentResolver;
	private Solo solo;
	private Instrumentation instrumentation;
	private DataProviderTestHelper helper;

	private List<Integer> listsToDelete = new ArrayList<Integer>();

	/**
	 * Initializes a new instance of ActivityListTest
	 * 
	 * @throws ClassNotFoundException
	 *             If the tested class does not exist
	 */
	public ActivityListTest() throws ClassNotFoundException
	{
		super(ActivityList.class);
	}

	@Override
	protected void setUp() throws Exception
	{
		instrumentation = getInstrumentation();
		solo = new Solo(instrumentation, getActivity());
		activityList = getActivity();
		listView = activityList.getListView();
		contentResolver = listView.getContext().getContentResolver();
		helper = new DataProviderTestHelper(contentResolver);
	}

	/**
	 * Tests the Add New List menu item and the Add/Edit item dialog when adding
	 * a single item
	 */
	public void testAddNewItem_singleItem()
	{
		// first, let's assert there is only one item in the list
		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(1, listView.getCount());

		// first menu item should open the add edit activity dialog
		solo.clickOnMenuItem("Add list");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditActivity.class);
		// verify that the add button is disabled
		assertFalse(solo.getButton(0).isEnabled());
		// now let's enter some text
		solo.enterText(0, "TestActivity");
		assertTrue(solo.getButton(0).isEnabled());
		solo.clickOnButton(0);

		instrumentation.waitForIdleSync();
		assertEquals(2, listView.getCount());

		listsToDelete.add(2);
	}

	/**
	 * Tests the Add New List menu item and the Add/Edit item dialog when adding
	 * multiple items separated with semicolons
	 */
	public void testAddNewItem_multipleItemsSeparatedWithSemicolons()
	{
		// first, let's assert there is only one item in the list
		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(1, listView.getCount());

		// first menu item should open the add edit activity dialog
		solo.clickOnMenuItem("Add list");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditActivity.class);
		// verify that the add button is disabled
		assertFalse(solo.getButton(0).isEnabled());
		// now let's enter some text
		solo.enterText(0, "Test1; Test2; Test3");
		assertTrue(solo.getButton(0).isEnabled());
		solo.clickOnButton(0);

		instrumentation.waitForIdleSync();
		assertEquals(4, listView.getCount());
		assertTrue(solo.searchText("Test1"));
		assertTrue(solo.searchText("Test2"));
		assertTrue(solo.searchText("Test3"));

		listsToDelete.add(2);
		listsToDelete.add(3);
		listsToDelete.add(4);
	}

	/**
	 * Tests the Add/Edit item dialog when adding an item with a name that
	 * already exists
	 */
	public void testAddNewItem_dialogWhenItemAlreadyExists()
	{
		// first, let's assert there is only one item in the list
		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(1, listView.getCount());

		// first menu item should open the add edit activity dialog
		solo.clickOnMenuItem("Add list");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditActivity.class);
		// verify that the add button is disabled
		assertFalse(solo.getButton(0).isEnabled());
		// now let's enter some text
		solo.enterText(0, "Shopping");
		assertTrue(solo.getButton(0).isEnabled());
		solo.clickOnButton(0);

		// dialog should be displayed
		assertTrue(solo
				.searchText("A list with the name \"Shopping\" already exists. Please choose a different name."));
		assertTrue(solo.searchText("OK"));
		solo.clickOnButton(0);
		solo.goBack();
		instrumentation.waitForIdleSync();

		assertEquals(1, listView.getCount());

		listsToDelete.add(2);
	}
	
	/**
	 * Tests the Mark All selected context menu item
	 */
	public void testMarkAllItemsSelected_and_MarkAllItemsUnselected()
	{
		// first, let's assert there is only one item in the list
		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(1, listView.getCount());

		solo.clickLongOnText("Shopping");
		solo.clickOnText("Mark All Undone");
		instrumentation.waitForIdleSync();
		
		// there should be 121 of 121 items selected
		assertTrue(solo.searchText("114 / 114"));
		
		solo.clickLongOnText("Shopping");
		solo.clickOnText("Mark All Done");
		instrumentation.waitForIdleSync();
		
		// there should be 0 of 121 items selected
		assertTrue(solo.searchText("0 / 114"));
	}

	/**
	 * Tests the Edit List context menu item and the Add/Edit item dialog when
	 * editing a single item
	 */
	public void testEditItem()
	{
		// Let's insert an item that can later be edited
		String testListName = "testlist";
		Uri listUri = helper.insertNewList(testListName);
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		listsToDelete.add(listId);

		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(2, listView.getCount());
		assertTrue(solo.searchText(testListName));

		// first menu item should open the add edit activity dialog
		solo.clickLongOnText(testListName);
		solo.clickOnText("Edit list");
		solo.assertCurrentActivity("Edit item should have been opened!", AddEditActivity.class);
		// verify that the texts on the dialog are correct
		assertTrue(solo.searchText("List name:"));
		assertTrue(solo.searchText("Save"));

		// verify that the add button is disabled
		assertTrue(solo.getButton(0).isEnabled());
		solo.clearEditText(0);
		assertFalse(solo.getButton(0).isEnabled());
		// now let's enter some text
		solo.enterText(0, "TestList2");
		assertTrue(solo.getButton(0).isEnabled());
		solo.clickOnButton(0);

		instrumentation.waitForIdleSync();
		assertEquals(2, listView.getCount());
		assertTrue(solo.searchText("TestList2"));
	}

	/**
	 * Tests the delete functionality of the ActivityList
	 */
	public void testDeleteItem()
	{
		// Let's insert an item that can later be edited
		String testListName = "testlist";
		Uri listUri = helper.insertNewList(testListName);
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		listsToDelete.add(listId);

		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(2, listView.getCount());
		assertTrue(solo.waitForText(testListName));

		// third menu item should delete the list
		solo.clickLongOnText(testListName);
		solo.clickOnText("Remove list");
		// dialog should be displayed
		assertTrue(solo.searchText("This list will be removed permanently. Continue?"));
		assertTrue(solo.searchText("Yes!"));
		assertTrue(solo.searchText("No!"));
		solo.clickOnButton("No!");

		// assert that nothing was deleted
		assertEquals(2, listView.getCount());

		// repeat, this time click yes
		solo.clickLongOnText(testListName);
		solo.clickOnText("Remove list");
		solo.clickOnButton("Yes!");

		// assert that list was deleted
		solo.sleep(1000);
		assertEquals(1, listView.getCount());
		assertTrue(solo.searchText("Shopping"));
	}

	/**
	 * Tests the copy functionality of the ActivityList
	 */
	public void testCopyItem()
	{
		String defaultListName = "Shopping";

		solo.assertCurrentActivity("Program should be open on ActivityList", ActivityList.class);
		assertNotNull(listView);
		assertEquals(1, listView.getCount());
		assertTrue(solo.searchText(defaultListName));

		// first menu item should open the add edit activity dialog
		solo.clickLongOnText(defaultListName);
		solo.clickOnText("Copy list");
		solo.assertCurrentActivity("Copy item should have been opened!", AddEditActivity.class);
		// verify that the texts on the dialog are correct
		assertTrue(solo.searchText("List name:"));
		assertTrue(solo.searchText("Copy list"));

		// verify that the add button is disabled
		assertFalse(solo.getButton(0).isEnabled());
		// now let's enter some text
		solo.enterText(0, defaultListName);
		assertTrue(solo.getButton(0).isEnabled());
		solo.clickOnButton(0);

		// a dialog should be displayed, notifying about the fact that the name
		// is already in use.
		assertTrue(solo
				.searchText("A list with the name \"Shopping\" already exists. Please choose a different name."));
		assertTrue(solo.searchText("OK"));
		solo.clickOnButton(0);
		instrumentation.waitForIdleSync();

		// so let's try a different name
		solo.clearEditText(0);
		solo.enterText(0, "Shopping copy");
		solo.clickOnButton(0);

		// now the list is supposed to start copying itself
		// a waiting progress control should be displayed with the text
		// "Copying list"
		assertTrue(solo.searchText("Copying list"));
		instrumentation.waitForIdleSync();
		assertEquals(2, listView.getCount());
		assertTrue(solo.searchText("Shopping copy"));

		solo.clickLongOnText("Shopping copy");
		solo.clickOnText("Remove list");
		instrumentation.waitForIdleSync();
		solo.clickOnText("Yes!");
	}

	/**
	 * Verifies that the quick report menu item works as designed
	 */
	public void testMenu_quickReport()
	{		
		// first, there must be items selected in the list
		instrumentation.waitForIdleSync();
		solo.clickOnText("Shopping");
		instrumentation.waitForIdleSync();
		solo.clickOnText("Milk");
		solo.clickOnText("Eggs");
		solo.goBack();
		instrumentation.waitForIdleSync();
		
		solo.clickOnMenuItem("ToDo Report");
		instrumentation.waitForIdleSync();
		solo.assertCurrentActivity("Program should be open on QuickReport",
				AllToDoItemsReport.class);
		
		// now mark everything done
		solo.clickOnMenuItem("Clear Selection");
	}

	/**
	 * Verifies that the about menu item works as designed
	 */
	public void testMenu_about()
	{	
		solo.clickOnMenuItem("About");
		assertTrue(solo.searchText("If you have any questions, suggestions", true));
		solo.clickOnButton(0);
	}

	@Override
	public void tearDown() throws Exception
	{
		try
		{
			// remove the list created
			for (Integer listId : listsToDelete)
			{
				contentResolver.delete(ContentUris.withAppendedId(ActivityColumns.CONTENT_URI,
						listId), null, null);
			}
			listsToDelete.clear();

			solo.finalize();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		getActivity().finish();
		super.tearDown();
	}
}