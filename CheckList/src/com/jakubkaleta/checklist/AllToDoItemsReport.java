package com.jakubkaleta.checklist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;

import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * Quick report that shows all todo items currently selected by the user in any
 * of the lists that the user defined.
 * 
 * @author Jakub Kaleta
 */
public class AllToDoItemsReport extends ListActivity
{
	private DataAccessService dataAccessService;
	private long activity_id;
	private String TAG = this.getClass().toString();

	final String[] PROJECTION = new String[]
	{ EntryColumns.ENTRY_NAME, ActivityColumns.ACTIVITY_NAME, CategoryColumns.CATEGORY_NAME,
			ActivityColumns.ITEM_COUNT, ActivityColumns.SELECTED_ITEM_COUNT,
			ActivityColumns.DATE_CREATED, EntryColumns._ID };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		dataAccessService = new DataAccessService(getContentResolver());

		activity_id = getIntent().getLongExtra("ActivityId", 0);

		ListView lv = getListView();
		// this is what is going to be showed if the list is empty.
		lv.setEmptyView(findViewById(R.id.txt_empty_list));

		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				setEntrySelected(id, false);
			}
		});

		// register all items in the menu for context menu
		registerForContextMenu(lv);
	}

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
			String sortOrder = dataAccessService.getCurrentApplicationState()
					.getActivityListSortOrder().toSortString()
					+ ", " + CategoryColumns.CATEGORY_NAME;

			// Get a cursor to access all items
			String whereClause = EntryColumns.IS_SELECTED + "= 1";

			if (singleActivityReport())
				whereClause += " AND " + CategoryColumns.TABLE_NAME + "."
						+ CategoryColumns.ACTIVITY_ID + " = " + activity_id;

			Cursor mCursor = managedQuery(EntryColumns.CONTENT_URI, PROJECTION, whereClause, null,
					sortOrder);

			
			int layout = singleActivityReport() ? R.layout.activity_todo_report_list_item
					: R.layout.all_todo_report_list_item;

			SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(this, layout, mCursor,
					PROJECTION, new int[]
					{ R.id.item_name, R.id.activity_name, R.id.category_name });

			setListAdapter(dataAdapter);
		}
		catch (Exception e)
		{
			Log.e(this.getClass().toString(), "Exception when querying for data " + e.getMessage());
		}
	}

	private final void setEntrySelected(final long id, final boolean selected)
	{
		Boolean askForConfirmation = !dataAccessService.getCurrentConfiguration()
				.getDisablePromptInToDoMode();

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				switch (which)
				{
				case DialogInterface.BUTTON_POSITIVE:
					dataAccessService.persistSelectionChangeToTheDatabase(id, selected);
					break;
				}
			}
		};

		if (askForConfirmation)
		{
			final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(R.string.mark_item_done).setMessage(R.string.continue_prompt)
					.setPositiveButton(R.string.yes_string, dialogClickListener).setNegativeButton(
							R.string.no_string, dialogClickListener).show();
		}
		else
		{
			dataAccessService.persistSelectionChangeToTheDatabase(id, selected);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_todo_items_report_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		boolean listHasItems = !this.getListView().getAdapter().isEmpty();
		menu.findItem(R.id.menu_all_todo_report_email_me_this_report).setEnabled(listHasItems);
		menu.findItem(R.id.menu_all_todo_report_clear_list).setEnabled(listHasItems);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{

		case R.id.menu_all_todo_report_email_me_this_report:

			sendEmailToTheUser();
			return true;

		case R.id.menu_all_todo_report_home:
			Intent newIntent = new Intent(getApplicationContext(), ActivityList.class);
			newIntent.putExtra("Mode", dataAccessService.getCurrentApplicationState().getMode());
			startActivity(newIntent);
			return true;

		case R.id.menu_all_todo_report_clear_list:
			clearAllItemsFromTheList();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void clearAllItemsFromTheList()
	{
		ContentValues values = new ContentValues();
		values.put(EntryColumns.IS_SELECTED, 0);
		Log.i(this.getClass().toString(), "Updating all tasks to Done.");
		Uri entryUri = EntryColumns.CONTENT_URI;
		getContentResolver().update(entryUri, values, null, null);
	}

	private void sendEmailToTheUser()
	{
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/html");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]
		{});
		i.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.all_to_do_items_email_subject));
		i.putExtra(Intent.EXTRA_TEXT, dataAccessService.getAllToDoItemsEmailContent(activity_id));
		try
		{
			startActivity(Intent.createChooser(i, getText(R.string.send_email_text)));
		}
		catch (android.content.ActivityNotFoundException ex)
		{
			Log.e(TAG, ex.getMessage());
		}
	}

	private Boolean singleActivityReport()
	{
		return activity_id > 0;
	}
}
