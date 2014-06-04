package com.jakubkaleta.checklist;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ApplicationStateBean;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * This activity displays all items in a selected list and allows for switching
 * between categories, or swiping right and left through the categories.
 * 
 * @author Jakub Kaleta
 * 
 */
public class EntriesFlipper extends Activity implements OnClickListener
{
	// activity id should have been passed in to this view
	private long currentActivityId;
	private String currentMode;
	private long currentCategoryId;
	private String TAG = this.getClass().toString();
	private SafeViewFlipper flipper;
	private Resources resources;
	private DataAccessService dataAccessService;
	private ListViewGestureListener gestureListener;

	private ArrayList<CategoryBean> categories = new ArrayList<CategoryBean>();

	private static final String[] CATEGORY_PROJECTION = new String[]
	{ CategoryColumns._ID, CategoryColumns.CATEGORY_NAME, CategoryColumns.ITEM_COUNT, CategoryColumns.CATEGORY_SORT_ORDER};

	private static final int ADDED_NEW_ENTRY = 0;
	private static final int EDITED_ENTRY = 1;
	private static final int ACTIVITY_ID_NULL_VALUE = 0;
	private static final int CATEGORY_ID_NULL_VALUE = 0;

	private static final String MODE_NULL_VALUE = "EDIT";
	private Button btnPreviousCategory;
	private Button btnNextCategory;
	
	private Button btnSelectAllFromCategory;
	private Button btnUnselectAllFromCategory;

	private TextView txtCategoriesInfo;
	
	private Spinner sortSpinner;

	// delegates
	/**
	 * This class contains a callback method that can be passed into outside
	 * classes, enabling them to call back this class.
	 * 
	 * @author Kuba
	 * 
	 */
	public interface MetadataUpdater
	{
		/**
		 * Call this method to update metadata in the creator of the calling
		 * object.
		 * 
		 * @param completeRebindRequired
		 *            True if this entire flipper requires a complete data
		 *            rebind
		 */
		void updateMetadata(Boolean completeRebindRequired);
	}

	/**
	 * This class contains a callback method that can be passed into outside
	 * classes, enabling them to call back this class.
	 * 
	 * @author Kuba
	 * 
	 */
	public interface DisplayedCategoryGetter
	{
		/**
		 * Call to get currently displayed category id
		 * 
		 * @return Currently displayed category id
		 */
		long getDisplayedCategoryId();
	}

	private void clearAllItemsFromTheList()
	{
		// Get out updates into the provider.
		ContentValues values = new ContentValues();

		values.put(EntryColumns.IS_SELECTED, 0);

		// Commit all of our changes to persistent storage. When
		// the update completes
		// the content provider will notify the cursor of the
		// change, which will cause the UI to be updated.
		try
		{
			Log.i(TAG, "Updating all tasks to Done.");

			String[] strArray =
			{ "" + currentCategoryId };
			getContentResolver().update(EntryColumns.CONTENT_URI, values,
					EntryColumns.CATEGORY_ID + "= ?", strArray);

			setUpActivity(-1);
		}
		catch (NullPointerException e)
		{
			Log.e(TAG, "Exception when updating data " + e.getMessage());
		}
	}

	private final void deleteEntry(Uri uriToDelete)
	{
		final Uri uriToBeDeleted = uriToDelete;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					// in case there is only one element in the list
					// after a successful update the list must be removed from
					// the flipper.
					boolean listMustBeRemoved = ((ListView) flipper.getCurrentView()).getCount() == 1;
					// Delete the note that the context menu is for
					getContentResolver().delete(uriToBeDeleted, null, null);
					if (listMustBeRemoved)
						setUpActivity(-1);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked - do nothing
					break;
				}
			}
		};

		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.remove_item).setMessage(
				R.string.confirm_permanent_deletion_of_item).setPositiveButton(R.string.yes_string,
				dialogClickListener).setNegativeButton(R.string.no_string, dialogClickListener)
				.show();

	}

	private Pair<Integer, CategoryBean> findCategory(long catId)
	{
		for (int i = 0; i < categories.size(); i++)
		{
			CategoryBean category = categories.get(i);
			if (category.getId() == catId)
				return new Pair<Integer, CategoryBean>(i, category);
		}

		return null;
	}

	private int getSelectedItemsCount(ListView lv)
	{
		// In todo mode all items are always selected	
		if(inToDoMode())
			return lv.getCount();
		
		SparseBooleanArray array = lv.getCheckedItemPositions();

		int count = 0;
		for (int i = 0; i < array.size(); i++)
		{
			if (array.get(i))
				count++;
		}

		return count;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK
				&& (requestCode == ADDED_NEW_ENTRY || requestCode == EDITED_ENTRY))
		{
			// the activity results with those request codes use the same
			// content types.
			// so let's extract all the extras here:
			Bundle e = data.getExtras();
			String entryName = e.getString(EntryColumns.ENTRY_NAME);
			long categoryId = e.getLong(EntryColumns.CATEGORY_ID);

			ContentValues values = new ContentValues();
			values.put(EntryColumns.ENTRY_NAME, entryName.trim());
			values.put(EntryColumns.CATEGORY_ID, categoryId);
			values.put(EntryColumns.IS_SELECTED, inToDoMode() ? 1 : 0);

			switch (requestCode)
			{
			case ADDED_NEW_ENTRY:
				// in add mode, it is actually possible to add multiple items at
				// once
				// if that's the case, we need to detect that and make it happen
				if (entryName.contains(";"))
				{
					String[] temp = entryName.split(";");
					for (int i = 0; i < temp.length; i++)
					{
						String toInsert = temp[i].trim();
						// skip empty entries
						if (!toInsert.equalsIgnoreCase(""))
						{
							values.put(EntryColumns.ENTRY_NAME, toInsert);
							getContentResolver().insert(EntryColumns.CONTENT_URI, values);
						}
					}
				}
				else
				{
					getContentResolver().insert(EntryColumns.CONTENT_URI, values);
				}
				break;

			case EDITED_ENTRY:
				long editedEntryId = e.getLong(EntryColumns._ID);
				getContentResolver().update(
						ContentUris.withAppendedId(EntryColumns.CONTENT_URI, editedEntryId),
						values, null, null);
				break;
			}

			// at this point it is not necessary to manually call refresh.
			// it will be called automatically, when onResume is executed
			saveState(currentActivityId, categoryId, currentMode);
		}
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.btn_next_category:
			flipper.showNext();
			setCurrentCategoryTitleAndInfo(true);
			break;
		case R.id.btn_previous_category:
			flipper.showPrevious();
			setCurrentCategoryTitleAndInfo(true);
			break;
		case R.id.btn_unselect_all_in_category:
			dataAccessService.markAllItemsSelectedInCategory(currentCategoryId, false);
			break;
		case R.id.btn_select_all_in_category:
			dataAccessService.markAllItemsSelectedInCategory(currentCategoryId, true);
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		}
		catch (ClassCastException e)
		{
			Log.e(TAG, "bad menuInfo", e);
			return false;
		}

		Uri itemUri = ContentUris.withAppendedId(EntryColumns.CONTENT_URI, info.id);
		switch (item.getItemId())
		{
		case R.id.entries_context_menu_remove_item:
			// Delete the note that the context menu is for
			deleteEntry(itemUri);
			return true;
		case R.id.entries_context_menu_edit_item:
			// launch the Add/Edit activity in edit mode
			Intent myIntent = new Intent(getApplicationContext(), AddEditEntry.class);
			myIntent.putExtra("ActivityId", currentActivityId);
			myIntent.putExtra("EntryId", info.id);
			myIntent.putExtra("mode", "EDIT");
			startActivityForResult(myIntent, EDITED_ENTRY);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.entries_flipper);
		resources = getResources();
		
		flipper = (SafeViewFlipper) findViewById(R.id.flipper_view);

		flipper = (SafeViewFlipper) findViewById(R.id.flipper_view);
		
		btnPreviousCategory = (Button) findViewById(R.id.btn_previous_category);
		btnNextCategory = (Button) findViewById(R.id.btn_next_category);
		txtCategoriesInfo = (TextView) findViewById(R.id.txt_categories_info);
		
		btnSelectAllFromCategory = (Button) findViewById(R.id.btn_select_all_in_category);
		btnUnselectAllFromCategory = (Button) findViewById(R.id.btn_unselect_all_in_category);		
		btnSelectAllFromCategory.setText(resources.getString(R.string.mark_all_undone));
		btnUnselectAllFromCategory.setText(resources.getString(R.string.mark_all_done));
				
		sortSpinner = (Spinner) findViewById(R.id.spinner_sort_items);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.sort_options_for_item_list, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortSpinner.setAdapter(adapter);

		sortSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				// ignore the arguments. All we want to do is to
				// save the state and rebind the list
				Pair<Integer,CategoryBean> currentCategory = findCategory(currentCategoryId);
				if(currentCategory != null)
				{
					boolean savedState = saveCurrentCategorySortSelection(currentCategory.second);
					if(savedState)
					{
						EntriesFlipperTab currentTab = (EntriesFlipperTab) flipper.getCurrentView();						
						currentTab.reload(gestureListener, currentCategory.second);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// In this implementation of the dropdown list, it is not
				// possible
				// to have nothing selected. The list is always sorted by
				// something
			}
		});
		
		btnPreviousCategory.setOnClickListener(this);
		btnNextCategory.setOnClickListener(this);	
		btnSelectAllFromCategory.setOnClickListener(this);	
		btnUnselectAllFromCategory.setOnClickListener(this);	

		dataAccessService = new DataAccessService(getContentResolver());

		// There are two possible ways we could have gotten to this point:
		// 1. Just launching the app,
		// 2. Switching orientation or resuming the app
		// In the first case, savedInstanceState will not have values necessary
		// to restore state.
		if (savedInstanceState != null && savedInstanceState.containsKey("CategoryId")
				&& savedInstanceState.containsKey("ActivityId")
				&& savedInstanceState.containsKey("Mode"))
		{
			currentActivityId = savedInstanceState.getLong("ActivityId");
			currentCategoryId = savedInstanceState.getLong("CategoryId");
			currentMode = savedInstanceState.getString("Mode");
		}
		else
		{
			// because the activity is just being created,
			// the state must be reset to default values
			saveState(ACTIVITY_ID_NULL_VALUE, CATEGORY_ID_NULL_VALUE, MODE_NULL_VALUE);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);

		// we only want to show the menu in edit mode,
		if (inEditMode())
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.entries_context_menu, menu);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.entry_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		int clearListModeTitle = inEditMode() ? R.string.clear_list_in_edit_mode
				: R.string.clear_list_in_todo_mode;

		MenuItem clearListItem = menu.findItem(R.id.clear_list);
		clearListItem.setTitle(clearListModeTitle);

		Object view = flipper.getCurrentView();

		if (view instanceof ListView)
		{
			ListView lv = (ListView) view;

			Boolean enableMenuItem = inEditMode() ? getSelectedItemsCount(lv) > 0
					: lv.getCount() > 0;
			clearListItem.setEnabled(enableMenuItem);
		}
		else
		{
			clearListItem.setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.menu_toggle_mode:

			if (inEditMode())
				currentMode = "TODO";
			else
				currentMode = "EDIT";

			setUpActivity(currentCategoryId);

			return true;

		case R.id.add_new_item:

			Intent addNewItemIntent = new Intent(getApplicationContext(), AddEditEntry.class);
			addNewItemIntent.putExtra("ActivityId", currentActivityId);
			addNewItemIntent.putExtra("CategoryId", currentCategoryId);
			addNewItemIntent.putExtra("mode", "ADD");
			startActivityForResult(addNewItemIntent, ADDED_NEW_ENTRY);
			return true;

		case R.id.entry_menu_manage_categories:

			Intent manageCategories = new Intent(getApplicationContext(), CategoryList.class);
			manageCategories.putExtra("ActivityId", currentActivityId);
			manageCategories.putExtra("mode", currentMode);
			startActivity(manageCategories);

			return true;

		case R.id.home:
			Intent newIntent = new Intent(getApplicationContext(), ActivityList.class);
			newIntent.putExtra("Mode", currentMode);
			startActivity(newIntent);
			return true;

		case R.id.clear_list:
			clearAllItemsFromTheList();
			return true;
			
		case R.id.menu_entries_flipper_quick_report:
			Intent myIntent = new Intent(getApplicationContext(), AllToDoItemsReport.class);
			myIntent.putExtra("ActivityId", currentActivityId);
			startActivity(myIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		saveState(currentActivityId, currentCategoryId, currentMode);
	}

	/** Called when the activity is first created. */
	@Override
	public void onPostCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "onPostCreate called.");

		super.onPostCreate(savedInstanceState);

		// activity id should have been passed in to this view
		currentActivityId = getIntent().getLongExtra("activityId", 1);
		currentCategoryId = getIntent().getLongExtra("categoryId", -1);
		currentMode = getIntent().getStringExtra("mode");
		
		flipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		gestureListener = new ListViewGestureListener();

		setUpActivity(currentCategoryId);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		Log.i(TAG, "onRestoreInstanceState called.");

		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		currentActivityId = savedInstanceState.getLong("ActivityId");
		currentCategoryId = savedInstanceState.getLong("CategoryId");
		currentMode = savedInstanceState.getString("Mode");
		setUpActivity(currentCategoryId);
	}

	@Override
	protected void onResume()
	{
		Log.i(TAG, "onResume called.");

		super.onResume();
		restoreState();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		savedInstanceState.putLong("ActivityId", currentActivityId);
		savedInstanceState.putLong("CategoryId", currentCategoryId);
		savedInstanceState.putString("Mode", currentMode);
		super.onSaveInstanceState(savedInstanceState);
	}

	private void restoreState()
	{
		ApplicationStateBean currentState = dataAccessService.getCurrentApplicationState();
		// load state only if there has been a saved state.
		// default values mean that the state was never saved.
		if (currentState.getActivityId() != 0 && currentState.getCategoryId() != 0)
		{
			currentActivityId = currentState.getActivityId();
			currentCategoryId = currentState.getCategoryId();
			currentMode = currentState.getMode();
			setUpActivity(currentCategoryId);
		}
	}

	/**
	 * Call this method to persist application state when it changes
	 * 
	 * @param activityId
	 *            Current ActivityId
	 * @param categoryId
	 *            Current CategoryId
	 * @param mode
	 *            Current Mode
	 */
	private void saveState(long activityId, long categoryId, String mode)
	{
		ContentValues values = new ContentValues();
		values.put(AppStateColumns.ACTIVITY_ID, activityId);
		values.put(AppStateColumns.CATEGORY_ID, categoryId);
		values.put(AppStateColumns.MODE, mode);

		try
		{
			Log.i(TAG, "Saving application state to the database.");
			getContentResolver().update(AppStateColumns.CONTENT_URI, values, null, null);
		}
		catch (NullPointerException e)
		{
			Log.e(TAG, "Exception when updating data " + e.getMessage());
		}
	}

	private void setCurrentCategoryTitleAndInfo(boolean hideInfo)
	{
		LinearLayout buttonsAndInfo = (LinearLayout) findViewById(R.id.InnerRelativeLayout);

		if (flipper.getChildCount() > 0)
		{
			buttonsAndInfo.setVisibility(android.view.View.VISIBLE);

			ListView lv = (ListView) flipper.getCurrentView();
			// set current category id and title in the title view
			currentCategoryId = ((Number) lv.getTag()).intValue();
			Pair<Integer, CategoryBean> currentCategory = findCategory(currentCategoryId);

			int currentCategoryIndex = currentCategory.first;
			int categoriesCount = categories.size();

			String currentCategoryTitle = currentCategory.second.getName() + " ("
					+ (currentCategoryIndex + 1) + " " + resources.getString(R.string.of) + " "
					+ categoriesCount + ")";
			setTitle(currentCategoryTitle);

			int itemsSelected = getSelectedItemsCount(lv);
			int allItemsCount = lv.getCount();
			String currentCategoryInfo = itemsSelected + " "
					+ resources.getString(R.string.selected) + System.getProperty("line.separator")
					+ resources.getString(R.string.of) + " " + allItemsCount + " "
					+ resources.getString(R.string.items);
			txtCategoriesInfo.setText(currentCategoryInfo);

			// Set category names to buttons
			int previousCategoryIndex = currentCategoryIndex == 0 ? categoriesCount - 1
					: currentCategoryIndex - 1;
			int nextCategoryIndex = currentCategoryIndex == (categoriesCount - 1) ? 0
					: currentCategoryIndex + 1;

			// hide both buttons if there is only one category left
			if (categoriesCount > 1)
			{
				btnPreviousCategory.setVisibility(android.view.View.VISIBLE);
				btnNextCategory.setVisibility(android.view.View.VISIBLE);
			}
			else
			{
				btnPreviousCategory.setVisibility(android.view.View.INVISIBLE);
				btnNextCategory.setVisibility(android.view.View.INVISIBLE);
			}

			btnPreviousCategory.setText(categories.get(previousCategoryIndex).getName());
			btnNextCategory.setText(categories.get(nextCategoryIndex).getName());
			sortSpinner.setSelection(currentCategory.second.getSortOrder().toNumber());
			
			// hide the list sort drop down if no items in the list
			if (allItemsCount == 0)
				sortSpinner.setVisibility(View.GONE);
			else
				sortSpinner.setVisibility(View.VISIBLE);

		}
		else
		{
			buttonsAndInfo.setVisibility(android.view.View.INVISIBLE);

			View emptyListView = getLayoutInflater().inflate(R.layout.empty_list_defaut_screen,
					null);
			flipper.addView(emptyListView);
		}
	}
	
	private boolean saveCurrentCategorySortSelection(CategoryBean category)
	{
		CategorySortOrder newSortOrder = CategorySortOrder.fromNumber(
				sortSpinner.getSelectedItemPosition());
		
		ContentValues values = new ContentValues();
		values.put(CategoryColumns.CATEGORY_SORT_ORDER, newSortOrder.toNumber());	

		try
		{
			Log.i(TAG, "Saving category sort state to the database.");	
			Uri entryUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, category.getId());
			getContentResolver().update(entryUri, values, null, null);
			category.setSortOrder(newSortOrder);
			return true;
		}
		catch (NullPointerException e)
		{
			Log.e(this.getClass().toString(), "Exception when updating data " + e.getMessage());
			return false;
		}
	}	
	
	private void setUpActivity(long categoryIdToSwitchTo)
	{
		Log.i(TAG, "SetUpActivity called.");

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try
		{
			// start fresh with a clean flipper and a clean category list.
			for (int i = 0; i < flipper.getChildCount(); i++)
			{
				// dispose of the old views, close cursors, clean up
				if (flipper.getChildAt(i) instanceof EntriesFlipperTab)
				{
					EntriesFlipperTab tab = (EntriesFlipperTab) flipper.getChildAt(i);
					tab.unload();
				}
			}

			flipper.removeAllViews();
			categories.clear();

			// query all categories for the given activity id and set up tabs
			// for each category. Get a cursor to access the note
			String selection = CategoryColumns.ACTIVITY_ID + " = " + currentActivityId;
			if (inToDoMode())
				selection += " AND " + EntryColumns.IS_SELECTED + "= 1";

			Cursor mCursor = managedQuery(CategoryColumns.CONTENT_URI, CATEGORY_PROJECTION,
					selection, null, CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION);

			int viewToDisplayIndex = 0;

			if (mCursor.isAfterLast())
			{
				flipper.setDisplayedChild(viewToDisplayIndex);
				setCurrentCategoryTitleAndInfo(false);
				return;
			}

			mCursor.moveToFirst();
			while (!mCursor.isAfterLast())
			{
				String categoryName = mCursor.getString(1);
				long catId = mCursor.getLong(0);
				int itemCount = mCursor.getInt(mCursor.getColumnIndex(CategoryColumns.ITEM_COUNT));
				CategorySortOrder sortOrder = CategorySortOrder.fromNumber(mCursor.getInt(mCursor.getColumnIndex(CategoryColumns.CATEGORY_SORT_ORDER)));
				if (itemCount > 0)
				{
					CategoryBean currentCategory = new CategoryBean(catId, categoryName, sortOrder);
					categories.add(currentCategory);
					loadTab(currentCategory, itemCount);

					// If we want to select a particular tab, as in case
					// when a new item is added,
					// we need to mark the tab to be selected later.
					if (categoryIdToSwitchTo > 0 && currentCategory.getId() == categoryIdToSwitchTo)
					{
						viewToDisplayIndex = flipper.getChildCount() - 1;
					}
				}

				mCursor.moveToNext();
			}

			mCursor.close();
			flipper.setDisplayedChild(viewToDisplayIndex);
			setCurrentCategoryTitleAndInfo(flipper.getChildCount() != 0);
		}
		catch (Exception e)
		{
			Log.e(TAG, "SetUpActivity: Exception when querying for data: " + e.getMessage());
		}
	}

	private void loadTab(CategoryBean category, int itemCount)
	{
		// Initialize a TabSpec for each tab and add it to the
		// TabHost
		String tabLabel = category.getName() + " (" + itemCount + ")";
		Log.v(TAG, "Creating tab with tab label:" + tabLabel);

		MetadataUpdater updater = new MetadataUpdater()
		{
			public void updateMetadata(Boolean completeRebindRequired)
			{
				if (completeRebindRequired)
					setUpActivity(-1);
				else
					setCurrentCategoryTitleAndInfo(false);
			}
		};

		DisplayedCategoryGetter categoryGetter = new DisplayedCategoryGetter()
		{
			public long getDisplayedCategoryId()
			{
				return currentCategoryId;
			}
		};

		EntriesFlipperTab tab = new EntriesFlipperTab(EntriesFlipper.this, category, inToDoMode(),
				updater, categoryGetter);
		tab.load(gestureListener);
		registerForContextMenu(tab);
		flipper.addView(tab);
	}

	/**
	 * Indicates whether EntriesFlipper is currently in TO-DO mode.
	 * 
	 * @return True if to-do mode, false otherwise
	 */
	private Boolean inToDoMode()
	{
		return currentMode.equalsIgnoreCase("TODO");
	}

	/**
	 * Indicates whether EntriesFlipper is currently in EDIT mode.
	 * 
	 * @return True if edit mode, false otherwise
	 */
	private Boolean inEditMode()
	{
		return currentMode.equalsIgnoreCase("EDIT");
	}

	private class ListViewGestureListener extends SimpleOnGestureListener
	{
		private final int SWIPE_MAX_OFF_PATH = 300;
		private final int SWIPE_MIN_DISTANCE = 150;
		private final int SWIPE_THRESHOLD_VELOCITY = 100;
		private final String TAG = this.getClass().getName();

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			try
			{
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
				{
					flipper.showNext();
					setCurrentCategoryTitleAndInfo(true);
					return true;

				}
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
				{
					flipper.showPrevious();
					setCurrentCategoryTitleAndInfo(true);
					return true;
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "An exception occurred trying to flip to next tab.");
			}
			return false;
		}
	}

}
