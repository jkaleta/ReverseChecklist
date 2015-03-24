package com.jakubkaleta.checklist;

import java.util.ArrayList;
import java.util.List;

import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * This activity is for adding or editing individual list entries.
 * 
 * @author Jakub Kaleta
 */
public class AddEditEntry extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	private long activity_id;
	private EditText entryName;
	private Button addEditEntry;
	private Spinner spinnerCategories;
	private TextView addMultipleItemsHint;
	private long entry_id;
	private long category_id;
	private String mode;

	private Resources resources;

	private static final int ENTRY_LOADER_ID = 4;
	private static final int CATEGORIES_LOADER_ID = 5;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 2;

	private static final String[] CATEGORY_PROJECTION = new String[] { CategoryColumns._ID, CategoryColumns.CATEGORY_NAME };

	private static final String[] ENTRY_PROJECTION = new String[] { EntryColumns._ID, EntryColumns.ENTRY_NAME, EntryColumns.DATE_CREATED,
			EntryColumns.CATEGORY_ID, EntryColumns.IS_SELECTED };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_entry);

		addEditEntry = (Button) findViewById(R.id.btn_add_edit_entry);
		entryName = (EditText) findViewById(R.id.etxt_entry_name);
		spinnerCategories = (Spinner) findViewById(R.id.spinner_category);
		addMultipleItemsHint = (TextView) findViewById(R.id.txt_multiple_entries_hint);

		activity_id = getIntent().getLongExtra("ActivityId", 1);
		category_id = getIntent().getLongExtra("CategoryId", 0);
		mode = getIntent().getStringExtra("mode");

		resources = getResources();
		LoaderManager lm = getLoaderManager();
		lm.initLoader(CATEGORIES_LOADER_ID, null, mCallbacks);

		if (mode.equalsIgnoreCase("EDIT")) {
			addMultipleItemsHint.setVisibility(View.GONE);
			addEditEntry.setText(resources.getString(R.string.save_changes));
			setTitle(resources.getString(R.string.edit_item));

			entry_id = getIntent().getLongExtra("EntryId", 0);

			mCallbacks = this;
			lm.initLoader(ENTRY_LOADER_ID, null, mCallbacks);
			// see the rest in loader callbacks
		} else {
			addMultipleItemsHint.setVisibility(View.VISIBLE);
			addEditEntry.setText(resources.getString(R.string.add_entry));
			setTitle(resources.getString(R.string.add_entry));
		}

		// Get display items for later interaction
		ImageButton speakButton = (ImageButton) findViewById(R.id.btn_speak_now);

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (v.getId() == R.id.btn_speak_now) {
						startVoiceRecognitionActivity();
					}
				}
			});
		} else {
			speakButton.setEnabled(false);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
		case CATEGORIES_LOADER_ID:
			return new CursorLoader(AddEditEntry.this, EntryColumns.CONTENT_URI, ENTRY_PROJECTION, EntryColumns.TABLE_NAME + "."
					+ EntryColumns._ID + " = " + entry_id, null, EntryColumns.DEFAULT_SORT_ORDER);

		case ENTRY_LOADER_ID:
			return new CursorLoader(AddEditEntry.this, CategoryColumns.CONTENT_URI, CATEGORY_PROJECTION, CategoryColumns.ACTIVITY_ID
					+ " = " + activity_id, null, CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (loader.getId()) {
		case CATEGORIES_LOADER_ID:
			bindCategoryDropdown(cursor);

		case ENTRY_LOADER_ID:
			cursor.moveToFirst();
			entryName.setText(cursor.getString(cursor.getColumnIndex(EntryColumns.ENTRY_NAME)));
			category_id = cursor.getLong(cursor.getColumnIndex(EntryColumns.CATEGORY_ID));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case CATEGORIES_LOADER_ID:
			bindCategoryDropdown(null);
		}
	}
	
	public void reloadCategories() {
		getLoaderManager().restartLoader(CATEGORIES_LOADER_ID, null, this);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		
		Boolean categoriesExist = !spinnerCategories.getAdapter().isEmpty();

		String text = entryName.getText().toString().replace(";", "");
		addEditEntry.setEnabled(categoriesExist && text.length() > 0);

		// I'm adding listeners in onPostCreate. If they were added in onCreate,
		// it would be possible for them to be executed as a result of
		// onRestoreInstanceState. The handlers depend on the code in
		// bindCategoryDropdown, so this is the right place to be setting them.
		addEditEntry.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intentToPassBack = new Intent();
				intentToPassBack.putExtra(EntryColumns.ENTRY_NAME, entryName.getText().toString());
				intentToPassBack.putExtra(EntryColumns.CATEGORY_ID, spinnerCategories.getSelectedItemId());
				intentToPassBack.putExtra("MODE", mode);
				if (mode.equalsIgnoreCase("EDIT"))
					intentToPassBack.putExtra(CategoryColumns._ID, entry_id);
				setResult(RESULT_OK, intentToPassBack);
				finish();
			}
		});

		entryName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					String text = entryName.getText().toString().replace(";", "");
					addEditEntry.setEnabled(!spinnerCategories.getAdapter().isEmpty() && text.length() > 0);
				} catch (NumberFormatException e) {
				}
			}
		});

		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Boolean categoriesExist = !spinnerCategories.getAdapter().isEmpty();

		if (!categoriesExist) {
			final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
			dialogBuilder.setTitle(R.string.add_category).setMessage(R.string.add_item_no_categories_defined).setIcon(R.drawable.appicon)
					.setCancelable(true).setNeutralButton(R.string.ok_string, null).show();
		}
	}

	private void bindCategoryDropdown(Cursor cursor) {

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null, new String[] {
				CategoryColumns.CATEGORY_NAME, CategoryColumns._ID }, new int[] { android.R.id.text1 }, 0);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinnerCategories.setAdapter(adapter);
		OnItemSelectedListener spinnerListener = new myOnItemSelectedListener(this, adapter);
		spinnerCategories.setOnItemSelectedListener(spinnerListener);

		// if there were no categories for the selected activity, the category spinner
		// must be disabled. We don't want users adding entries with no category selected.
		// We want to make them enter a category instead.
		spinnerCategories.setEnabled(!adapter.isEmpty());
		addEditEntry.setEnabled(!adapter.isEmpty() && entryName.getText().length() > 0);

		if (!adapter.isEmpty() && category_id > 0) {
			for (int i = 0; i < adapter.getCount(); i++) {
				if (adapter.getItemId(i) == category_id) {
					spinnerCategories.setSelection(i);
					break;
				}
			}
		}
		
		adapter.swapCursor(cursor);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate menu from XML resource
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_entry_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.edit_category).setEnabled(spinnerCategories.isEnabled());
		return super.onPrepareOptionsMenu(menu);
	}

	private final int ADDED_NEW_CATEGORY = 0;
	private final int EDITED_EXISTING_CATEGORY = 1;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent = new Intent(getApplicationContext(), AddEditCategory.class);
		myIntent.putExtra("activityId", activity_id);

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.add_category:
			myIntent.putExtra("mode", "ADD");
			startActivityForResult(myIntent, ADDED_NEW_CATEGORY);
			return true;
		case R.id.edit_category:
			myIntent.putExtra("mode", "EDIT");
			myIntent.putExtra("categoryId", spinnerCategories.getSelectedItemId());
			startActivityForResult(myIntent, EDITED_EXISTING_CATEGORY);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADDED_NEW_CATEGORY && resultCode == RESULT_OK) {
			// insert the new activity and refresh the list
			Bundle e = data.getExtras();
			ContentValues values = new ContentValues();
			String categoryName = e.getString(CategoryColumns.CATEGORY_NAME);
			values.put(CategoryColumns.ACTIVITY_ID, e.getLong(CategoryColumns.ACTIVITY_ID));

			// in add mode, it is actually possible to add multiple items at
			// once
			// if that's the case, we need to detect that and make it happen
			if (categoryName.contains(";")) {
				String[] temp = categoryName.split(";");
				for (int i = 0; i < temp.length; i++) {
					String toInsert = temp[i].trim();
					// skip empty entries
					if (!toInsert.equalsIgnoreCase("")) {
						values.put(CategoryColumns.CATEGORY_NAME, toInsert);
						getContentResolver().insert(CategoryColumns.CONTENT_URI, values);
					}
				}
			} else {
				values.put(CategoryColumns.CATEGORY_NAME, categoryName.trim());
				getContentResolver().insert(CategoryColumns.CONTENT_URI, values);
			}

			reloadCategories();
		} else if (requestCode == EDITED_EXISTING_CATEGORY && resultCode == RESULT_OK) {
			// insert the new activity and refresh the list
			Bundle e = data.getExtras();
			ContentValues values = new ContentValues();
			values.put(CategoryColumns.CATEGORY_NAME, e.getString(CategoryColumns.CATEGORY_NAME).trim());
			Uri itemUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, e.getLong(CategoryColumns._ID));
			getContentResolver().update(itemUri, values, null, null);

			reloadCategories();
		} else if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the text box with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (!matches.isEmpty())
				entryName.setText(matches.get(0));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * A callback listener that implements the
	 * {@link android.widget.AdapterView.OnItemSelectedListener} interface For
	 * views based on adapters, this interface defines the methods available
	 * when the user selects an item from the View.
	 * 
	 */
	public class myOnItemSelectedListener implements OnItemSelectedListener {

		/*
		 * provide local instances of the mLocalAdapter and the mLocalContext
		 */

		SimpleCursorAdapter mLocalAdapter;
		Activity mLocalContext;

		/**
		 * Constructor
		 * 
		 * @param c
		 *            - The activity that displays the Spinner.
		 * @param ad
		 *            - The Adapter view that controls the Spinner. Instantiate
		 *            a new listener object.
		 */
		public myOnItemSelectedListener(Activity c, SimpleCursorAdapter ad) {

			// this.mLocalContext = c;
			// this.mLocalAdapter = ad;

		}

		/**
		 * When the user selects an item in the spinner, this method is invoked
		 * by the callback chain. Android calls the item selected listener for
		 * the spinner, which invokes the onItemSelected method.
		 * 
		 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView,
		 *      android.view.View, int, long)
		 * @param parent
		 *            - the AdapterView for this listener
		 * @param v
		 *            - the View for this listener
		 * @param pos
		 *            - the 0-based position of the selection in the
		 *            mLocalAdapter
		 * @param row
		 *            - the 0-based row number of the selection in the View
		 */
		public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {

			// SpinnerActivity.this.mPos = pos;
			// SpinnerActivity.this.mSelection =
			// parent.getItemAtPosition(pos).toString();
			/*
			 * Set the value of the text field in the UI
			 */
			// TextView resultText = (TextView)
			// findViewById(R.id.SpinnerResult);
			// resultText.setText(SpinnerActivity.this.mSelection);
		}

		/**
		 * The definition of OnItemSelectedListener requires an override of
		 * onNothingSelected(), even though this implementation does not use it.
		 * 
		 * @param parent
		 *            - The View for this Listener
		 */
		public void onNothingSelected(AdapterView<?> parent) {

			// do nothing

		}
	}

}
