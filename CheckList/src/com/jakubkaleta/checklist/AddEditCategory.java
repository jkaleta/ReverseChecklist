package com.jakubkaleta.checklist;

import java.util.ArrayList;
import java.util.List;

import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;

import android.app.Activity;
import android.content.Intent;
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

/**
 * The activity for managing (adding, removing, editing) categories.
 * @author Jakub Kaleta 
 */
public class AddEditCategory extends Activity
{
	private long activity_id;
	private long category_id;
	private String mode;

	private Button addEditCategory;
	private EditText categoryName;
	private TextView addMultipleItemsHint;
	private Resources resources;

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

	private static final String[] CATEGORY_PROJECTION = new String[]
	{ CategoryColumns._ID, CategoryColumns.CATEGORY_NAME };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_edit_category);

		categoryName = (EditText) findViewById(R.id.etxt_category_name);
		addEditCategory = (Button) findViewById(R.id.btn_add_edit_category);
		addMultipleItemsHint = (TextView) findViewById(R.id.txt_multiple_entries_hint);

		activity_id = getIntent().getLongExtra("activityId", 0);
		mode = getIntent().getStringExtra("mode");

		resources = getResources();

		addEditCategory.setEnabled(mode.equalsIgnoreCase("EDIT"));	
		
		if (mode.equalsIgnoreCase("EDIT"))
		{
			addMultipleItemsHint.setVisibility(View.GONE);
			addEditCategory.setText(resources.getString(R.string.save_changes));
			setTitle(resources.getString(R.string.edit_category));

			category_id = getIntent().getLongExtra("categoryId", 0);

			// query all categories for the given activity id and set up tabs
			// for each category. Get a cursor to access the note
			Cursor mCursor = managedQuery(CategoryColumns.CONTENT_URI, CATEGORY_PROJECTION,
					CategoryColumns.TABLE_NAME + "." + CategoryColumns._ID + " = " + category_id, null,
					CategoryColumns.DEFAULT_SORT_ORDER);

			mCursor.moveToFirst();

			categoryName.setText(mCursor.getString(mCursor
					.getColumnIndex(CategoryColumns.CATEGORY_NAME)));
		}
		else
		{
			addMultipleItemsHint.setVisibility(View.VISIBLE);
			addEditCategory.setText(resources.getString(R.string.add_category));
			setTitle(resources.getString(R.string.add_category));
		}

		addEditCategory.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Intent intentToPassBack = new Intent();
				intentToPassBack.putExtra(CategoryColumns.CATEGORY_NAME, categoryName.getText()
						.toString());
				intentToPassBack.putExtra(CategoryColumns.ACTIVITY_ID, activity_id);
				intentToPassBack.putExtra("MODE", mode);
				if (mode.equalsIgnoreCase("EDIT"))
					intentToPassBack.putExtra(CategoryColumns._ID, category_id);

				setResult(RESULT_OK, intentToPassBack);
				finish();
			}
		});

		categoryName.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				try
				{
					addEditCategory.setEnabled(categoryName.getText().length() > 0);
				}
				catch (NumberFormatException e)
				{
				}
			}
		});

		// Get display items for later interaction
		ImageButton speakButton = (ImageButton) findViewById(R.id.btn_speak_now);

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0)
		{
			speakButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if (v.getId() == R.id.btn_speak_now)
					{
						startVoiceRecognitionActivity();
					}
				}
			});
		}
		else
		{
			speakButton.setEnabled(false);
		}

	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK)
		{
			// Fill the text box with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			if (!matches.isEmpty())
				categoryName.setText(matches.get(0));	
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
