package com.jakubkaleta.checklist.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jakubkaleta.checklist.AddEditEntry;
import com.jakubkaleta.checklist.CategoryList;
import com.jakubkaleta.checklist.EntriesFlipper;
import com.jakubkaleta.checklist.R;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.test.DataProviderTestHelper;
import com.robotium.solo.Solo;

/**
 * Contains UI tests for the EntriesFlipper activity and related activities that
 * are launched from EntriesFlipper
 * 
 * @author Jakub Kaleta
 */
public class EntriesFlipperTest extends ActivityInstrumentationTestCase2<EntriesFlipper> {
	private EntriesFlipper entriesFlipper;
	private ContentResolver contentResolver;
	private Solo solo;
	private Instrumentation instrumentation;
	private DataProviderTestHelper helper;

	private List<Long> listsToDelete = new ArrayList<Long>();

	/**
	 * Initializes a new instance of EntriesFlipperTest
	 * 
	 * @throws ClassNotFoundException
	 *             If the tested class does not exist
	 */
	public EntriesFlipperTest() throws ClassNotFoundException {
		super(EntriesFlipper.class);
	}

	@Override
	protected void setUp() throws Exception {
		instrumentation = getInstrumentation();
		contentResolver = instrumentation.getContext().getContentResolver();
		helper = new DataProviderTestHelper(contentResolver);

		// create a test list with no items in in
		String testListName = "testlist";
		Uri listUri = helper.insertNewList(testListName);
		long listId = Integer.parseInt(listUri.getPathSegments().get(1));
		listsToDelete.add(listId);

		Intent intent = new Intent(getInstrumentation().getContext(), EntriesFlipper.class);
		intent.putExtra("activityId", listId);
		intent.putExtra("mode", "EDIT");
		setActivityIntent(intent);
		entriesFlipper = getActivity();
		solo = new Solo(getInstrumentation(), entriesFlipper);
		solo.setActivityOrientation(Solo.LANDSCAPE);
	}

	/**
	 * Tests that when user attempts to add a new item to the list when there
	 * are no categories defined, a pop-up window shows up informing user that
	 * they need to insert a category first. Further, it verifies that correct
	 * texts are displayed and that UI components are enabled/disabled as they
	 * should.
	 */
	public void testAddItem_NoCategory_InitialLook() {
		solo.assertCurrentActivity("Program should be open on EntriesFlipper", EntriesFlipper.class);
		assertTrue(solo.searchText("This list is empty."));

		// when Add Entry is selected from the menu, AddNew item dialog is
		// opened
		solo.clickOnMenuItem("Add entry");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditEntry.class);

		// When the activity is open for the first time, with no categories
		// added
		// the message box should be displayed with the message
		assertTrue(solo.searchText("Add Category"));
		assertTrue(solo.searchText("You have to add a category"));
		solo.clickOnButton("OK");

		// verify if the activity is set up with correct text resources
		assertTrue(solo.searchText("Category"));
		assertTrue(solo.searchText("Entry"));
		assertTrue(solo.searchText("Add entry"));
		assertTrue(solo.searchText("To add multiple items"));

		// verify that UI components are enabled/disabled as they should
		assertTrue(solo.getText(0).isEnabled());
		assertFalse(solo.getButton(0).isEnabled());
		assertFalse(solo.getCurrentViews(Spinner.class).get(0).isEnabled());
	}

	/**
	 * Tests that when user attempts to add a new item to the list when there
	 * already is a category defined. Further, it verifies that correct texts
	 * are displayed and that UI components are enabled/disabled as they should.
	 */
	public void testAddItem_WithCategoryAdded_InitialLook() {
		solo.assertCurrentActivity("Program should be open on EntriesFlipper", EntriesFlipper.class);
		assertTrue(solo.searchText("This list is empty."));

		String catName = "Test Category";
		helper.insertNewCategory(listsToDelete.get(0), catName, 0);

		// when Add Entry is selected from the menu, AddNew item dialog is
		// opened
		solo.clickOnMenuItem("Add entry");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditEntry.class);

		// When the activity is open for the first time, with categories added
		// no message box should be displayed with the message
		assertFalse(solo.searchText("Add Category"));
		assertFalse(solo.searchText("You have to add a category"));

		// verify if the activity is set up with correct text resources
		assertTrue(solo.searchText("Category"));
		assertTrue(solo.searchText("Test Category"));
		assertTrue(solo.searchText("Entry"));
		assertTrue(solo.searchText("Add entry"));
		assertTrue(solo.searchText("To add multiple items"));

		// verify that UI components are enabled/disabled as they should
		assertTrue(solo.getText(0).isEnabled());
		assertFalse(solo.getButton(0).isEnabled());
		assertTrue(solo.getCurrentViews(Spinner.class).get(0).isEnabled());
	}

	/**
	 * Tests adding a single item to the existing category Happy path.
	 */
	public void testAddItem_WithCategoryAdded_AddSingleItem() {
		solo.assertCurrentActivity("Program should be open on EntriesFlipper", EntriesFlipper.class);
		assertTrue(solo.searchText("This list is empty."));

		String catName = "Test Category";
		helper.insertNewCategory(listsToDelete.get(0), catName, 0);

		// when Add Entry is selected from the menu, AddNew item dialog is
		// opened
		solo.clickOnMenuItem("Add entry");
		solo.assertCurrentActivity("Add new item should have been opened!", AddEditEntry.class);

		solo.enterText(0, "TestItem1");

		// verify that UI components are enabled/disabled as they should
		assertTrue(solo.getText(0).isEnabled());
		assertTrue(solo.getButton(0).isEnabled());
		assertTrue(solo.getCurrentViews(Spinner.class).get(0).isEnabled());

		// add item
		solo.clickOnButton(0);

		instrumentation.waitForIdleSync();

		// verify that the current view is a list with the item present
		assertTrue(solo.searchText("TestItem1"));
	}

	/**
	 * Tests deleting of an item from the list when user selects "No" on the
	 * confirmation dialog.
	 */
	public void testDeleteEntry_ConfirmationDialog_NoSelected() {
		String catName = "Test Category";
		Uri catUri = helper.insertNewCategory(listsToDelete.get(0), catName, 0);
		long catId = Integer.parseInt(catUri.getPathSegments().get(1));
		String itemName = "Test item";
		helper.insertNewItem(catId, itemName, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);

		// third menu item should delete the list
		assertTrue(solo.searchText(itemName));
		solo.clickLongOnText(itemName);
		solo.clickOnText("Remove entry");
		// dialog should be displayed
		assertTrue(solo.searchText("This entry will be removed permanently. Continue?"));
		assertTrue(solo.searchText("Yes!"));
		assertTrue(solo.searchText("No!"));
		solo.clickOnButton("No!");

		// assert that nothing was deleted
		assertTrue(solo.searchText(itemName));

		// repeat, this time click yes
		solo.clickLongOnText(itemName);
		solo.clickOnText("Remove entry");
		solo.clickOnButton("Yes!");

		// assert that list was deleted
		solo.sleep(1000);
		assertTrue(solo.searchText("This list is empty."));
	}

	/**
	 * Tests adding multiple items separated with semicolons to the existing
	 * category Happy path.
	 */
	public void testAddedNewEntry_MultipleItemsSeparatedWithSemicolon() {
		assertTrue(solo.searchText("This list is empty."));

		String catName = "Test Category";
		helper.insertNewCategory(listsToDelete.get(0), catName, 0);

		// when Add Entry is selected from the menu, AddNew item dialog is
		// opened
		solo.clickOnMenuItem("Add entry");
		solo.enterText(0, "TestItem1; TestItem2; TestItem3     ");

		// verify that UI components are enabled/disabled as they should
		assertTrue(solo.getText(0).isEnabled());
		assertTrue(solo.getButton(0).isEnabled());
		assertTrue(solo.getCurrentViews().get(0).isEnabled());

		// add item
		solo.clickOnButton(0);

		// verify that the current view is a list with the item present
		assertTrue(solo.searchText("TestItem1"));
		assertTrue(solo.searchText("TestItem2"));
		assertTrue(solo.searchText("TestItem3"));
	}

	/**
	 * Tests if the dialog looks correct when it is first displayed
	 */
	public void testEditEntry_initialDisplayOfDialog() {
		String catName = "Test Category";
		Uri catUri = helper.insertNewCategory(listsToDelete.get(0), catName, 0);
		long catId = Integer.parseInt(catUri.getPathSegments().get(1));
		String itemName = "Test item";
		helper.insertNewItem(catId, itemName, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);

		// third menu item should delete the list
		assertTrue(solo.searchText(itemName));
		solo.clickLongOnText(itemName);
		solo.clickOnText("Edit entry");
		solo.assertCurrentActivity("Program should be open on EntriesFlipper", AddEditEntry.class);

		// verify if the activity is set up with correct text resources
		assertTrue(solo.searchText("Category"));
		assertTrue(solo.searchText("Test Category"));
		assertTrue(solo.searchText("Entry"));
		assertTrue(solo.searchText("Save"));

		// verify that UI components are enabled/disabled as they should
		assertTrue(solo.getText(0).isEnabled());
		assertTrue(solo.getButton(0).isEnabled());
		assertTrue(solo.getCurrentViews(Spinner.class).get(0).isEnabled());
	}

	/**
	 * Verifies that UI components that are expected to be visible are visible
	 * when there is only one category in the list.
	 */
	public void testInterfaceWhenOneCategory() {
		String catName = "Test Category";
		Uri catUri = helper.insertNewCategory(listsToDelete.get(0), catName, 0);
		long catId = Integer.parseInt(catUri.getPathSegments().get(1));
		String itemName = "Test item";
		helper.insertNewItem(catId, itemName, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);

		// verify if the activity is set up with correct text resources
		assertTrue(solo.searchText("Test item"));

		// verify that UI components are enabled/disabled as they should
		Button prevCat = (Button) solo.getView(R.id.btn_previous_category);
		Button nextCat = (Button) solo.getView(R.id.btn_next_category);
		TextView catInfo = (TextView) solo.getView(R.id.txt_categories_info);

		assertEquals(android.view.View.INVISIBLE, prevCat.getVisibility());
		assertEquals(android.view.View.INVISIBLE, nextCat.getVisibility());
		assertEquals(android.view.View.VISIBLE, catInfo.getVisibility());

		assertEquals(catInfo.getText(), "0 selected\nof 1 entries");
	}

	/**
	 * This test tests whether the UI of EntriesFlipper is set up properly when
	 * there are two categories with items. This is just a UI layout test, it
	 * does not test switching between categories.
	 */
	public void testInterfaceWhenMoreThanOneCategory() {
		setUpTwoCategoriesWithOneItemEach();
		verifyCategorySwitch("Category 2", "Item 1");
	}

	/**
	 * Clicking on next category should advance to the next category and display
	 * a list of items that belong to that category
	 */
	public void testOnClick_NextCategory() {
		setUpTwoCategoriesWithOneItemEach();

		solo.clickOnText("Category 2");

		verifyCategorySwitch("Category 1", "Item 2");
	}

	/**
	 * Clicking on previous category should advance to the next category and
	 * display a list of items that belong to that category
	 */
	public void testOnClick_PreviousCategory() {
		setUpTwoCategoriesWithOneItemEach();

		solo.clickOnText("Category 2");

		verifyCategorySwitch("Category 1", "Item 2");
	}

	/*
	 * Swiping right should have the same effect as clicking on previous
	 * category Swiping left should have the same effect as clicking on next
	 * category
	 * 
	 * public void
	 * testSwipeLeftAndSwipeRight_SwitchingCategories_MultipleCategories() {
	 * setUpTwoCategoriesWithOneItemEach();
	 * 
	 * solo.drag(150, 450, 350, 345, 1); verifyCategorySwitch("Category 1",
	 * "Item 2"); solo.drag(150, 450, 350, 345, 1);
	 * verifyCategorySwitch("Category 2", "Item 1"); }
	 */

	private void setUpTwoCategoriesWithOneItemEach() {
		String cat1Name = "Category 1";
		Uri cat1Uri = helper.insertNewCategory(listsToDelete.get(0), cat1Name, 0);
		int cat1Id = Integer.parseInt(cat1Uri.getPathSegments().get(1));
		String item1Name = "Item 1";
		helper.insertNewItem(cat1Id, item1Name, false);
		String cat2Name = "Category 2";
		Uri cat2Uri = helper.insertNewCategory(listsToDelete.get(0), cat2Name, 0);
		int cat2Id = Integer.parseInt(cat2Uri.getPathSegments().get(1));
		String item2Name = "Item 2";
		helper.insertNewItem(cat2Id, item2Name, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);
		instrumentation.waitForIdleSync();
	}

	private void verifyCategorySwitch(String catName, String itemName) {
		assertTrue(solo.searchText(itemName));

		// verify that UI components are enabled/disabled as they should
		Button prevCat = (Button) solo.getView(R.id.btn_previous_category);
		Button nextCat = (Button) solo.getView(R.id.btn_next_category);
		TextView catInfo = (TextView) solo.getView(R.id.txt_categories_info);

		assertEquals(android.view.View.VISIBLE, prevCat.getVisibility());
		assertEquals(android.view.View.VISIBLE, nextCat.getVisibility());
		assertEquals(android.view.View.VISIBLE, catInfo.getVisibility());

		assertEquals(catInfo.getText(), "0 selected\nof 1 entries");
		assertEquals(prevCat.getText(), catName);
		assertEquals(nextCat.getText(), catName);
	};

	/**
	 * Changing screen orientation should resume the right state The state
	 * consists of List, Category and mode. Verifying the list and category is
	 * done through checking if the right item shows up. Verifying mode is done
	 * through calling the private helper method isInToDoMode
	 */
	public void testSwitchingOrientation_CorrectStateResumed() {
		setUpTwoCategoriesWithOneItemEach();

		// By default, Entries Flipper starts in Edit mode.
		// Let's click on the first item in the list and select it.
		solo.clickOnText("Item 1");

		// now, toggle mode to TO-do
		solo.clickOnMenuItem("Toggle mode");
		instrumentation.waitForIdleSync();

		assertTrue(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));

		// now switch orientation a few times
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.setActivityOrientation(Solo.LANDSCAPE);

		assertTrue(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));

		// switch back to edit mode
		solo.clickOnMenuItem("Toggle mode");
		instrumentation.waitForIdleSync();

		assertFalse(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));

		// now switch orientation a few times
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.setActivityOrientation(Solo.LANDSCAPE);
		solo.setActivityOrientation(Solo.PORTRAIT);
		solo.setActivityOrientation(Solo.LANDSCAPE);

		assertFalse(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));
	}

	/**
	 * Toggling current mode switches the list from TO-DO to EDIT mode and back
	 * In To-do mode items do not have context menu. In Edit mode, there are
	 * checkboxes visible.
	 */
	public void testMenu_ToggleMode() {
		setUpTwoCategoriesWithOneItemEach();

		// By default, Entries Flipper starts in Edit mode.
		// Let's click on the first item in the list and select it.
		solo.clickOnText("Item 1");

		// now, toggle mode to TO-do
		solo.clickOnMenuItem("Toggle mode");
		instrumentation.waitForIdleSync();

		assertTrue(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));

		// switch back to edit mode
		solo.clickOnMenuItem("Toggle mode");
		instrumentation.waitForIdleSync();

		assertFalse(isInToDoMode());
		assertTrue(solo.searchText("Item 1"));
	}

	private Boolean isInToDoMode() {
		TextView editItem = (TextView) solo.getView(R.id.single_checkable_item);
		TextView todoItem = (TextView) solo.getView(R.id.single_item_name);
		return editItem == null && todoItem != null;
	}

	/**
	 * Clicking ManageCategories should open the CategoryList activity. The
	 * activity is not expected to produce a result.
	 */
	public void testMenu_ManageCategories() {
		setUpTwoCategoriesWithOneItemEach();
		solo.clickOnMenuItem("Categories");
		instrumentation.waitForIdleSync();
		solo.assertCurrentActivity("Category List should be open", CategoryList.class);
	}

	/**
	 * Pressing All lists should take the user back to the home screen All lists
	 * should be visible
	 */
	public void testMenu_Home() {
		setUpTwoCategoriesWithOneItemEach();
		solo.clickOnMenuItem("All lists");
		instrumentation.waitForIdleSync();
		assertTrue(solo.searchText("Shopping"));
	}

	/**
	 * Tests if clearAllItem works correctly in to-do mode
	 */
	public void testMenu_ClearAllItemsFromTheList_ToDoMode() {
		String cat1Name = "Category 1";
		Uri cat1Uri = helper.insertNewCategory(listsToDelete.get(0), cat1Name, 0);
		long cat1Id = Integer.parseInt(cat1Uri.getPathSegments().get(1));
		String item1Name = "Item 1";
		String item2Name = "Item 2";
		helper.insertNewItem(cat1Id, item1Name, false);
		helper.insertNewItem(cat1Id, item2Name, false);
		String cat2Name = "Category 2";
		Uri cat2Uri = helper.insertNewCategory(listsToDelete.get(0), cat2Name, 0);
		long cat2Id = Integer.parseInt(cat2Uri.getPathSegments().get(1));
		String item3Name = "Item 3";
		String item4Name = "Item 4";
		helper.insertNewItem(cat2Id, item3Name, false);
		helper.insertNewItem(cat2Id, item4Name, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);
		instrumentation.waitForIdleSync();

		// The application is in edit mode, no items are selected
		solo.clickOnText("Item 1");
		solo.clickOnText("Item 2");

		// Items should be selected. Switch to next category
		solo.clickOnText("Category 2");

		solo.clickOnText("Item 3");
		solo.clickOnText("Item 4");

		// toggle mode. All items should be selected and visible in both lists
		solo.clickOnMenuItem("Toggle mode");
		instrumentation.waitForIdleSync();

		assertTrue(solo.searchText("Item 3"));
		assertTrue(solo.searchText("Item 4"));

		solo.clickOnMenuItem("Mark all done");

		// at this point, the list should be removed from the screen, the
		// remaining list
		// list one, should have two items in it. Buttons for switching back and
		// forward
		// should be disabled.
		assertTrue(solo.searchText("Item 1"));
		assertTrue(solo.searchText("Item 2"));

		// verify that UI components are enabled/disabled as they should
		Button prevCat = (Button) solo.getView(R.id.btn_previous_category);
		Button nextCat = (Button) solo.getView(R.id.btn_next_category);
		TextView catInfo = (TextView) solo.getView(R.id.txt_categories_info);

		assertEquals(android.view.View.INVISIBLE, prevCat.getVisibility());
		assertEquals(android.view.View.INVISIBLE, nextCat.getVisibility());
		assertEquals(android.view.View.VISIBLE, catInfo.getVisibility());
		assertEquals("2 selected\nof 2 entries", catInfo.getText());

		// clear items again
		solo.clickOnMenuItem("Mark all done");

		// all items should be removed from selection
		// and the user should be notified that there is nothing selected
		assertTrue(solo.searchText("This list is empty."));
	}

	/**
	 * Tests if clearAllItem works correctly in edit mode
	 */
	public void testMenu_ClearAllItemsFromTheList_EditMode() {
		String cat1Name = "Category 1";
		Uri cat1Uri = helper.insertNewCategory(listsToDelete.get(0), cat1Name, 0);
		long cat1Id = Integer.parseInt(cat1Uri.getPathSegments().get(1));
		String item1Name = "Item 1";
		String item2Name = "Item 2";
		helper.insertNewItem(cat1Id, item1Name, false);
		helper.insertNewItem(cat1Id, item2Name, false);
		String cat2Name = "Category 2";
		Uri cat2Uri = helper.insertNewCategory(listsToDelete.get(0), cat2Name, 0);
		long cat2Id = Integer.parseInt(cat2Uri.getPathSegments().get(1));
		String item3Name = "Item 3";
		String item4Name = "Item 4";
		helper.insertNewItem(cat2Id, item3Name, false);
		helper.insertNewItem(cat2Id, item4Name, false);

		// force requery the list by switching orientation
		solo.setActivityOrientation(Solo.PORTRAIT);
		instrumentation.waitForIdleSync();

		solo.clickOnText("Item 1");
		solo.clickOnText("Item 2");

		// go to the second list
		solo.clickOnText("Category 2");
		instrumentation.waitForIdleSync();
		// nothing should be selected
		TextView catInfo = (TextView) solo.getView(R.id.txt_categories_info);
		assertEquals("0 selected\nof 2 entries", catInfo.getText());
		solo.clickOnText("Category 1");
		instrumentation.waitForIdleSync();
		assertEquals("2 selected\nof 2 entries", catInfo.getText());

		solo.clickOnMenuItem("Clear Selection");

		assertTrue(solo.searchText("Item 1"));
		assertTrue(solo.searchText("Item 2"));

		assertEquals(android.view.View.VISIBLE, catInfo.getVisibility());
		assertEquals(catInfo.getText(), "0 selected\nof 2 entries");
	}

	/**
	 * Tests that in edit mode, when there are selected items, Clear list menu
	 * item is enabled, otherwise it is disabled.
	 */
	public void testMenu_ClearList_DisabledWhenNoItemsSelectedAndEnabledWhenItemsSelected() {
		// TODO
	}

	@Override
	public void tearDown() throws Exception {
		try {
			// remove the list created
			for (Long listId : listsToDelete) {
				contentResolver.delete(ContentUris.withAppendedId(ActivityColumns.CONTENT_URI, listId), null, null);
			}
			listsToDelete.clear();

			solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		getActivity().finish();
		super.tearDown();
	}
}