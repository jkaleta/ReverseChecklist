package com.jakubkaleta.checklist;

import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * This activity displays a configuration screen with configuration options
 * available for this application
 * 
 * @author Jakub Kaleta
 * 
 */
public class Configuration extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String[] CONFIGURATION_PROJECTION = new String[] { ConfigurationParametersColumns._ID,
			ConfigurationParametersColumns.PROMPT_IN_TODO_MODE, ConfigurationParametersColumns.START_IN_TODO_MODE };

	private CheckBox disablePromptsInToDoMode;

	//private CheckBox startInToDoMode;

	private static final int LOADER_ID = 8;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configuration);
		
		mCallbacks = this;
		LoaderManager lm = getLoaderManager();
		lm.initLoader(LOADER_ID, null, mCallbacks);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(Configuration.this, ConfigurationParametersColumns.CONTENT_URI, CONFIGURATION_PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		cursor.moveToFirst();
		bind(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private void bind(Cursor cursor) {
		TextView disablePromptsInToDoModeLabel = (TextView) findViewById(R.id.txt_config_disable_prompt_in_todo_mode);
		disablePromptsInToDoMode = (CheckBox) findViewById(R.id.check_disable_prompt_in_todo_mode);
		Boolean promptChecked = cursor.getInt(cursor.getColumnIndex(ConfigurationParametersColumns.PROMPT_IN_TODO_MODE)) == 0;
		disablePromptsInToDoMode.setChecked(promptChecked);

		disablePromptsInToDoModeLabel.setOnClickListener(new ConfigClickListener(this));
		disablePromptsInToDoMode.setOnClickListener(new ConfigClickListener(this));

		// TextView startInToDoModeLabel = (TextView)
		// findViewById(R.id.txt_config_start_in_todo_mode);
		// startInToDoMode = (CheckBox)
		// findViewById(R.id.check_start_in_todo_mode);
		// Boolean checked = mCursor.getInt(mCursor
		// .getColumnIndex(ConfigurationParametersColumns.START_IN_TODO_MODE))
		// == 0;
		// startInToDoMode.setChecked(checked);
		//
		// startInToDoModeLabel.setOnClickListener(new
		// ConfigClickListener(this));
		// startInToDoMode.setOnClickListener(new ConfigClickListener(this));
	}

	class ConfigClickListener implements OnClickListener {
		public ConfigClickListener(Context context) {
		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.txt_config_disable_prompt_in_todo_mode:
				disablePromptsInToDoMode.toggle();
				break;
			case R.id.check_disable_prompt_in_todo_mode:
				// toggling is done automatically
				break;
			}

			ContentValues values = new ContentValues();
			values.put(ConfigurationParametersColumns.PROMPT_IN_TODO_MODE, disablePromptsInToDoMode.isChecked() ? 0 : 1);

			getContentResolver().update(ConfigurationParametersColumns.CONTENT_URI, values, null, null);
		}
	}
}
