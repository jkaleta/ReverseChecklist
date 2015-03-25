package com.jakubkaleta.checklist;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;

/**
 * This class contains functionality pertaining to adding or editing new lists.
 * 
 * @author Jakub Kaleta
 */
public class AddEditActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	Button addEditActivity;
	EditText activityName;
	TextView addMultipleItemsHint;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

	private long activity_id;
	private String mode;
	private Resources resources;
	private DataAccessService dataAccessService;

	private static final int LOADER_ID = 2;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	private static final String[] PROJECTION = new String[] { ActivityColumns._ID, // 0
			ActivityColumns.ACTIVITY_NAME, // 1
			ActivityColumns.DATE_CREATED, // 2
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_edit_activity);

		dataAccessService = new DataAccessService(getContentResolver());

		addEditActivity = (Button) findViewById(R.id.btn_add_edit_activity);
		activityName = (EditText) findViewById(R.id.etxt_activity_name);
		addMultipleItemsHint = (TextView) findViewById(R.id.txt_multiple_entries_hint);

		mode = getIntent().getStringExtra("mode");

		resources = getResources();

		addEditActivity.setEnabled(mode.equalsIgnoreCase("EDIT"));

		if (mode.equalsIgnoreCase("EDIT")) {
			addMultipleItemsHint.setVisibility(View.GONE);
			addEditActivity.setText(resources.getString(R.string.save_changes));
			setTitle(resources.getString(R.string.edit_activity));
			activity_id = getIntent().getLongExtra("activityId", 0);

			mCallbacks = this;
			LoaderManager lm = getLoaderManager();
			lm.initLoader(LOADER_ID, null, mCallbacks);

		} else if (mode.equalsIgnoreCase("COPY")) {
			activity_id = getIntent().getLongExtra("activityId", 0);
			addMultipleItemsHint.setVisibility(View.GONE);
			addEditActivity.setText(resources.getString(R.string.copy_list));
			setTitle(resources.getString(R.string.copy_list));
		} else {
			addMultipleItemsHint.setVisibility(View.VISIBLE);
			addEditActivity.setText(resources.getString(R.string.add_activity));
			setTitle(resources.getString(R.string.add_activity));
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

		addEditActivity.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// in all modes, validate the name, it has to be unique

				String newActivityName = activityName.getText().toString();

				// only in edit mode, the name can match the edited name
				// this means that the name will not change
				long idToExcludeFromCheck = mode.equalsIgnoreCase("EDIT") ? activity_id : 0;
				Boolean nameAvailable = dataAccessService.checkActivityNameAvailability(newActivityName, idToExcludeFromCheck);

				if (nameAvailable) {
					Intent intentToPassBack = new Intent();
					intentToPassBack.putExtra(ActivityColumns.ACTIVITY_NAME, activityName.getText().toString());
					intentToPassBack.putExtra("activityId", activity_id);
					intentToPassBack.putExtra("MODE", mode);

					setResult(RESULT_OK, intentToPassBack);
					finish();
				} else {
					String text = String.format(resources.getString(R.string.duplicate_name_exists_message), newActivityName);

					final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
					dialogBuilder.setTitle(R.string.duplicate_name).setMessage(text).setIcon(R.drawable.appicon).setCancelable(true)
							.setNeutralButton(R.string.ok_string, null).show();
				}
			}
		});

		activityName.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					String text = activityName.getText().toString().replace(";", "");
					addEditActivity.setEnabled(text.length() > 0);
				} catch (NumberFormatException e) {
				}
			}
		});
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(AddEditActivity.this, ActivityColumns.CONTENT_URI, PROJECTION, ActivityColumns.TABLE_NAME + "."
				+ ActivityColumns._ID + " = " + activity_id, null, ActivityColumns.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cursor.moveToFirst();
		activityName.setText(cursor.getString(cursor.getColumnIndex(ActivityColumns.ACTIVITY_NAME)));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the text box with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (!matches.isEmpty())
				activityName.setText(matches.get(0));
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
