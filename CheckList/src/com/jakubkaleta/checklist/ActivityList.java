package com.jakubkaleta.checklist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.jakubkaleta.checklist.DataAccess.ActivityListSortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ApplicationStateBean;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;

/**
 * The main activity of this application. Displays a list of todo lists defined
 * by user.
 * 
 * @author Jakub Kaleta
 */
public class ActivityList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	private Resources resources;
	private Spinner sortOptionsSpinner;
	SimpleCursorAdapter activityListAdapter;
	private DataAccessService dataAccessService;
	private String TAG = this.getClass().toString();
	private static final int LOADER_ID = 1;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	private static final String[] PROJECTION = new String[] { ActivityColumns.ACTIVITY_NAME, ActivityColumns.ITEM_COUNT,
			ActivityColumns.SELECTED_ITEM_COUNT, ActivityColumns.DATE_CREATED, ActivityColumns._ID };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resources = getResources();
		dataAccessService = new DataAccessService(getContentResolver());

		// Use a custom layout file
		setContentView(R.layout.activity_list);

		// setup the dropdown list with options to sort the list
		sortOptionsSpinner = (Spinner) findViewById(R.id.spinner_sort_activities);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sort_options_for_activity_list,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortOptionsSpinner.setAdapter(adapter);

		sortOptionsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// ignore the arguments. All we want to do is to
				// save the state and rebind the list
				saveState();
				reload();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// In this implementation of the dropdown list, it is not
				// possible
				// to have nothing selected. The list is always sorted by
				// something
			}
		});

		ListView lv = getListView();
		// this is what is going to be showed if the list is empty.
		lv.setEmptyView(findViewById(R.id.txt_empty_list));

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// open activity details:
				Intent intent = new Intent(getApplicationContext(), EntriesFlipper.class);
				Cursor cursor = (Cursor) parent.getItemAtPosition(position);
				long activityId = cursor.getLong(cursor.getColumnIndex(ActivityColumns._ID));
				intent.putExtra("activityId", activityId);
				intent.putExtra("mode", "EDIT");
				startActivity(intent);
			}
		});

		// register all items in the menu for context menu
		registerForContextMenu(lv);

		mCallbacks = this;

		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);

		activityListAdapter = new SimpleCursorAdapter(this, R.layout.activity_list_item, null, PROJECTION, new int[] {
				R.id.single_item_name, R.id.item_counts, R.id.last_updated }, 0);

		activityListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int column) {
				switch (column) {
				case 1:
					TextView item_counts = (TextView) view;
					int selectedItemsCount = cursor.getInt(cursor.getColumnIndex(ActivityColumns.SELECTED_ITEM_COUNT));
					int allItemsCount = cursor.getInt(cursor.getColumnIndex(ActivityColumns.ITEM_COUNT));

					String text = resources.getString(R.string.empty_list);
					int labelColor = Color.GRAY;
					if (allItemsCount > 0) {
						if (selectedItemsCount == 0)
							labelColor = Color.GREEN;
						else
							labelColor = Color.argb(255, 255, 135, 0); // orange

						text = String.format(resources.getString(R.string.item_counts), selectedItemsCount, allItemsCount);
					}
					item_counts.setText(text);
					item_counts.setTextColor(labelColor);

					return true;

				case 2:
					TextView last_updated = (TextView) view;
					String dateCreated = cursor.getString(cursor.getColumnIndex(ActivityColumns.DATE_CREATED));

					if (dateCreated != null && !dateCreated.equalsIgnoreCase("")) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
						try {
							Date convertedDate = new Date(Long.parseLong(dateCreated));
							String formattedDate = dateFormat.format(convertedDate);
							last_updated.setText(formattedDate);
						} catch (NumberFormatException nfe) {
							Log.e(TAG, "Number format exception occurred: " + nfe.getMessage());
						}
					}
					return true;
				}
				return false;
			}
		});

		setListAdapter(activityListAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(ActivityList.this, ActivityColumns.CONTENT_URI, PROJECTION, null, null, getSortOrderStringFromSpinner());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_ID:

			cursor.registerContentObserver(new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					// hide the list sort drop down if no items in the list
					if (activityListAdapter.isEmpty())
						sortOptionsSpinner.setVisibility(View.GONE);
					else
						sortOptionsSpinner.setVisibility(View.VISIBLE);
				}

				@Override
				public boolean deliverSelfNotifications() {
					return true;
				}
			});

			activityListAdapter.swapCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		activityListAdapter.swapCursor(null);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		restoreState();
		reload();
	}

	public void reload() {
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	private String getSortOrderStringFromSpinner() {
		// get the current sort order from the spinner
		int selectedSortPosition = sortOptionsSpinner.getSelectedItemPosition();
		return ActivityListSortOrder.fromNumber(selectedSortPosition).toSortString();
	}

	private void restoreState() {
		ApplicationStateBean currentState = dataAccessService.getCurrentApplicationState();
		sortOptionsSpinner.setSelection(currentState.getActivityListSortOrder().toNumber());
	}

	private void saveState() {
		ContentValues values = new ContentValues();
		values.put(AppStateColumns.ACTIVITY_LIST_SORT_ORDER, ActivityListSortOrder.fromNumber(sortOptionsSpinner.getSelectedItemPosition())
				.toNumber());

		try {
			Log.i(TAG, "Saving application state to the database.");
			getContentResolver().update(AppStateColumns.CONTENT_URI, values, null, null);
		} catch (NullPointerException e) {
			Log.e(this.getClass().toString(), "Exception when updating data " + e.getMessage());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activities_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e(this.getClass().toString(), "bad menuInfo", e);
			return false;
		}

		Uri activityUri = ContentUris.withAppendedId(ActivityColumns.CONTENT_URI, info.id);

		switch (item.getItemId()) {
		case R.id.activities_context_menu_remove_item:
			// Delete the note that the context menu is for
			deleteActivity(activityUri);
			return true;

		case R.id.activities_context_menu_edit_item:
			// launch the Add/Edit activity in edit mode
			Intent myIntent = new Intent(getApplicationContext(), AddEditActivity.class);
			myIntent.putExtra("activityId", info.id);
			myIntent.putExtra("mode", "EDIT");
			startActivityForResult(myIntent, UPDATED_ACTIVITY);
			return true;

		case R.id.activities_context_menu_copy_item:
			// launch the Add/Edit activity in copy mode
			myIntent = new Intent(getApplicationContext(), AddEditActivity.class);
			myIntent.putExtra("activityId", info.id);
			myIntent.putExtra("mode", "COPY");
			startActivityForResult(myIntent, COPIED_ACTIVITY);
			return true;

		case R.id.activities_context_menu_mark_all_done:
			dataAccessService.markAllItemsSelected(info.id, false);
			reload();
			return true;

		case R.id.activities_context_menu_mark_all_undone:
			dataAccessService.markAllItemsSelected(info.id, true);
			reload();
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// hide the list sort drop down if no items in the list
		if (activityListAdapter.isEmpty())
			sortOptionsSpinner.setVisibility(View.GONE);
		else
			sortOptionsSpinner.setVisibility(View.VISIBLE);

	}

	private final void deleteActivity(Uri uriToDelete) {
		final Uri uriToBeDeleted = uriToDelete;

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
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
		dialogBuilder.setTitle(R.string.remove_activity).setMessage(R.string.confirm_permanent_deletion_of_list)
				.setPositiveButton(R.string.yes_string, dialogClickListener).setNegativeButton(R.string.no_string, dialogClickListener)
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activities_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean anyItemsSelected = dataAccessService.getSelectedItemsCount() > 0;
		menu.findItem(R.id.menu_activity_list_quick_report).setEnabled(anyItemsSelected);

		File path = Environment.getExternalStorageDirectory();
		boolean databaseExchangeSelected = path.exists();
		menu.findItem(R.id.menu_activity_list_exchange).setEnabled(databaseExchangeSelected);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_add_activity:
			Intent myIntent = new Intent(getApplicationContext(), AddEditActivity.class);
			myIntent.putExtra("mode", "ADD");
			startActivityForResult(myIntent, ADDED_NEW_ACTIVITY);
			return true;
		case R.id.menu_activity_list_settings:
			myIntent = new Intent(getApplicationContext(), Configuration.class);
			startActivity(myIntent);
			return true;
		case R.id.menu_activity_list_about:
			final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(R.string.app_name).setMessage(R.string.questions_suggestions).setIcon(R.drawable.appicon)
					.setCancelable(true).setNeutralButton(R.string.ok_string, null).show();
			return true;
		case R.id.menu_activity_list_help:
			Uri uriUrl = Uri.parse("http://checklist.jakubkaleta.com/");
			Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			startActivity(launchBrowser);
			return true;
		case R.id.menu_activity_list_quick_report:
			myIntent = new Intent(getApplicationContext(), AllToDoItemsReport.class);
			startActivity(myIntent);
			return true;
		case R.id.menu_activity_list_exchange:
			myIntent = new Intent(getApplicationContext(), DatabaseExchange.class);
			startActivity(myIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case ADDED_NEW_ACTIVITY:

				Bundle e = data.getExtras();
				String listName = e.getString(ActivityColumns.ACTIVITY_NAME);

				ContentValues values = new ContentValues();
				// in add mode, it is actually possible to add multiple items at
				// once
				// if that's the case, we need to detect that and make it happen
				if (listName.contains(";")) {
					String[] temp = listName.split(";");
					for (int i = 0; i < temp.length; i++) {
						String toInsert = temp[i].trim();
						// skip empty entries
						if (!toInsert.equalsIgnoreCase("")) {
							values.put(ActivityColumns.ACTIVITY_NAME, toInsert);
							getContentResolver().insert(ActivityColumns.CONTENT_URI, values);
						}
					}
				} else {
					values.put(ActivityColumns.ACTIVITY_NAME, listName.trim());
					getContentResolver().insert(ActivityColumns.CONTENT_URI, values);
				}

				break;
			case UPDATED_ACTIVITY:

				// insert the new activity and refresh the list
				e = data.getExtras();
				values = new ContentValues();
				values.put(ActivityColumns.ACTIVITY_NAME, e.getString(ActivityColumns.ACTIVITY_NAME).trim());

				getContentResolver().update(ContentUris.withAppendedId(ActivityColumns.CONTENT_URI, e.getLong("activityId")), values, null,
						null);
				break;

			case COPIED_ACTIVITY:

				e = data.getExtras();
				listName = e.getString(ActivityColumns.ACTIVITY_NAME).trim();
				long listIdToCopy = e.getLong("activityId");

				new CopyListTask().execute(new CopyListArgs(listIdToCopy, listName));

				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private class CopyListArgs {
		private long listId;
		private String newListName;

		public CopyListArgs(long listId, String newName) {
			this.listId = listId;
			this.newListName = newName;
		}

		public String getName() {
			return newListName;
		}

		public long getId() {
			return listId;
		}

	}

	private class CopyListTask extends AsyncTask<CopyListArgs, Void, Boolean> {
		private final ProgressDialog dialog = new ProgressDialog(ActivityList.this);

		// can use UI thread here
		protected void onPreExecute() {
			dialog.setMessage(resources.getString(R.string.copying_list));
			dialog.setCancelable(false);
			dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final CopyListArgs... args) {
			return dataAccessService.copyList(args[0].getId(), args[0].getName());
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean result) {
			if (this.dialog.isShowing()) {
				dialog.hide();

				String toastText = resources.getString(R.string.copying_list_success);

				if (!result)
					toastText = resources.getString(R.string.copying_list_failure);

				Toast toast = Toast.makeText(ActivityList.this, toastText, Toast.LENGTH_SHORT);
				toast.show();

				reload();
			}
		}
	}

	private final int ADDED_NEW_ACTIVITY = 0;
	private final int UPDATED_ACTIVITY = 1;
	private final int COPIED_ACTIVITY = 2;

}
