package com.jakubkaleta.checklist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

import com.commonsware.cwac.tlv.TouchListView;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;

/**
 * Displays all categories for a single activity
 * 
 * @author Jakub Kaleta
 */
public class CategoryList extends ListActivity
{
	SimpleCursorAdapter categoryListAdapter;
	private DataAccessService dataAccessService;
	private String TAG = this.getClass().toString();
	private long activityId;

	private static final String[] CATEGORY_PROJECTION = new String[]
	{ CategoryColumns.CATEGORY_NAME, CategoryColumns._ID };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dataAccessService = new DataAccessService(getContentResolver());
		activityId = getIntent().getLongExtra("ActivityId", 1);

		// Use a custom layout file
		setContentView(R.layout.category_list);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.sort_options_for_category_list, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		TouchListView lv = (TouchListView) getListView();

		// this is what is going to be showed if the list is empty.
		lv.setEmptyView(findViewById(R.id.txt_empty_list));

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// open activity details:
				Intent intent = new Intent(getApplicationContext(), EntriesFlipper.class);
				Cursor cursor = (Cursor) parent.getItemAtPosition(position);
				long categoryId = cursor.getLong(cursor.getColumnIndex(CategoryColumns._ID));
				intent.putExtra("activityId", activityId);
				intent.putExtra("categoryId", categoryId);
				intent.putExtra("mode", "EDIT");
				startActivity(intent);
			}
		});

		lv.setDropListener(onDrop);

		// register all items in the menu for context menu
		registerForContextMenu(lv);
	}

	private TouchListView.DropListener onDrop = new TouchListView.DropListener()
	{
		@Override
		public void drop(int from, int to)
		{
			// get the id of category at position 'from'
			int itemId = (int) categoryListAdapter.getItemId(from);
			dataAccessService.updateUserDefinedSort(activityId, itemId, from, to);
		}
	};

	@Override
	public void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		bindTheList();
	}

	private void bindTheList()
	{
		try
		{
			// Get a cursor to access the note
			Cursor mCursor = managedQuery(CategoryColumns.CONTENT_URI, CATEGORY_PROJECTION,
					CategoryColumns.ACTIVITY_ID + " = " + activityId, null,
					CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION);

			categoryListAdapter = new SimpleCursorAdapter(this, R.layout.touchlistview_row2,
					mCursor, CATEGORY_PROJECTION, new int[]
					{ R.id.label });

			setListAdapter(categoryListAdapter);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception when querying for data " + e.getMessage());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.categories_context_menu, menu);
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

		Uri categoryUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, info.id);

		switch (item.getItemId())
		{
		case R.id.categories_context_menu_remove_item:
			// Delete the note that the context menu is for
			deleteCategory(categoryUri);
			return true;

		case R.id.categories_context_menu_edit_item:
			// launch the Add/Edit category in edit mode
			Intent myIntent = new Intent(getApplicationContext(), AddEditCategory.class);
			myIntent.putExtra("mode", "EDIT");
			myIntent.putExtra("categoryId", info.id);
			startActivityForResult(myIntent, EDITED_EXISTING_CATEGORY);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private final void deleteCategory(Uri uriToDelete)
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
					getContentResolver().delete(uriToBeDeleted, null, null);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked - do nothing
					break;
				}
			}
		};

		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.remove_category).setMessage(
				R.string.confirm_permanent_deletion_of_category).setPositiveButton(
				R.string.yes_string, dialogClickListener).setNegativeButton(R.string.no_string,
				dialogClickListener).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.categories_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	private final int ADDED_NEW_CATEGORY = 0;
	private final int EDITED_EXISTING_CATEGORY = 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent myIntent = new Intent(getApplicationContext(), AddEditCategory.class);
		myIntent.putExtra("activityId", activityId);

		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.menu_categories_add_category:
			myIntent.putExtra("mode", "ADD");
			startActivityForResult(myIntent, ADDED_NEW_CATEGORY);
			return true;
		case R.id.menu_categories_home:
			Intent newIntent = new Intent(getApplicationContext(), ActivityList.class);
			newIntent.putExtra("Mode", "EDIT");
			startActivity(newIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ADDED_NEW_CATEGORY && resultCode == RESULT_OK)
		{
			Bundle e = data.getExtras();
			ContentValues values = new ContentValues();
			String categoryName = e.getString(CategoryColumns.CATEGORY_NAME);
			values.put(CategoryColumns.ACTIVITY_ID, e.getLong(CategoryColumns.ACTIVITY_ID));

			// in add mode, it is actually possible to add multiple items at
			// once
			// if that's the case, we need to detect that and make it happen
			if (categoryName.contains(";"))
			{
				String[] temp = categoryName.split(";");
				for (int i = 0; i < temp.length; i++)
				{
					String toInsert = temp[i].trim();
					// skip empty entries
					if (!toInsert.equalsIgnoreCase(""))
					{
						values.put(CategoryColumns.CATEGORY_NAME, toInsert);
						getContentResolver().insert(CategoryColumns.CONTENT_URI, values);
					}
				}
			}
			else
			{
				values.put(CategoryColumns.CATEGORY_NAME, categoryName.trim());
				getContentResolver().insert(CategoryColumns.CONTENT_URI, values);
			}
		}
		else if (requestCode == EDITED_EXISTING_CATEGORY && resultCode == RESULT_OK)
		{
			Bundle e = data.getExtras();
			ContentValues values = new ContentValues();
			values.put(CategoryColumns.CATEGORY_NAME, e.getString(CategoryColumns.CATEGORY_NAME).trim());
			Uri itemUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, e
					.getLong(CategoryColumns._ID));
			getContentResolver().update(itemUri, values, null, null);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
