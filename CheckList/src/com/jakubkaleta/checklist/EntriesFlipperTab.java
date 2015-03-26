package com.jakubkaleta.checklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.commonsware.cwac.tlv.TouchListView;
import com.jakubkaleta.checklist.EntriesFlipper.DisplayedCategoryGetter;
import com.jakubkaleta.checklist.EntriesFlipper.MetadataUpdater;
import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.ConfigurationBean;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * This class represents a single tab in the EntriesFlipper It contains a list
 * that displays all items in a given category.
 * 
 * @author Jakub Kaleta
 * 
 */
public class EntriesFlipperTab extends com.commonsware.cwac.tlv.TouchListView implements LoaderManager.LoaderCallbacks<Cursor> {

	private Activity parent;
	private CategoryBean category;
	private final Boolean inToDoMode;
	private final Context context;
	private final MetadataUpdater updater;
	private final String TAG = this.getClass().getName();
	private final ConfigurationBean currentConfiguration;
	private final DataAccessService service;
	private final DisplayedCategoryGetter categoryGetter;
	private final TabContentObserver contentObserver;
	private SimpleCursorAdapter itemListAdapter;

	private static final int LOADER_ID = 10;
	private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

	private static final String[] ENTRY_PROJECTION = new String[] { EntryColumns._ID, EntryColumns.ENTRY_NAME, EntryColumns.DATE_CREATED,
			EntryColumns.IS_SELECTED };

	/**
	 * Initializes a new object of EntriesFlipperTab, using the passed in
	 * parameters
	 * 
	 * @param context
	 *            The application context to use.
	 * @param category
	 *            The category this tab represents
	 * @param inToDoMode
	 *            Indicates whether this tab is supposed to present data in todo
	 *            mode
	 * @param updater
	 *            Metadata Updater to call if update is necessary
	 * @param displayedCategoryGetter
	 *            Getter providing the currently displayed category id
	 */
	public EntriesFlipperTab(Activity parent, Context context, CategoryBean category, Boolean inToDoMode, MetadataUpdater updater,
			DisplayedCategoryGetter displayedCategoryGetter) {
		super(context, null);

		this.parent = parent;
		this.category = category;
		this.inToDoMode = inToDoMode;
		this.context = context;
		this.updater = updater;
		this.categoryGetter = displayedCategoryGetter;
		service = new DataAccessService(context.getContentResolver());
		currentConfiguration = service.getCurrentConfiguration();
		this.contentObserver = new TabContentObserver(new Handler());
		this.setDrawSelectorOnTop(true);

		// setGrabberIcon(R.id.icon);

		this.setDropListener(onDrop);
	}

	private TouchListView.DropListener onDrop = new TouchListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			// get the id of category at position 'from'
			long itemId = itemListAdapter.getItemId(from);
			service.updateUserDefinedSortForEntry(category.getId(), itemId, from, to);
		}
	};

	/**
	 * Call to reload this tab with an updated category bean
	 * 
	 * @param gestureListener
	 *            A gesture listener to use.
	 * @param category
	 *            Updated category bean to use to refresh this tab.
	 */
	public void reload(SimpleOnGestureListener gestureListener, CategoryBean category) {
		this.category = category;
		unload();
		load(gestureListener);
	}

	private int getItemLayout() {
		if (inToDoMode)
			return R.layout.entry_list_item_todo_mode;

		if (category.getSortOrder() == CategorySortOrder.Custom)
			return R.layout.single_checkable_list_item_with_dragger;

		return R.layout.single_checkable_list_item;
	}

	private int getTextItemLayout() {
		if (inToDoMode)
			return R.id.single_item_name;

		return R.id.single_checkable_item;
	}

	/**
	 * Call to load data into the tab.
	 * 
	 * @param gestureListener
	 *            A gesture listener to use.
	 */
	public void load(SimpleOnGestureListener gestureListener) {
		setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		setTag(category.getId());

		// Used to map notes entries from the database to views
		try {
			itemListAdapter = new SimpleCursorAdapter(context, getItemLayout(), null, new String[] { EntryColumns.ENTRY_NAME,
					EntryColumns.IS_SELECTED, EntryColumns._ID }, new int[] { getTextItemLayout() }, 0);

			itemListAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				@Override
				public boolean setViewValue(View view, Cursor cursor, int column) {
					String entryName = cursor.getString(cursor.getColumnIndex(EntryColumns.ENTRY_NAME));
					boolean selected = cursor.getInt(cursor.getColumnIndex(EntryColumns.IS_SELECTED)) > 0;

					if (column == 1) {
						if (view.getClass().equals(CheckedTextView.class)) {
							((CheckedTextView) view).setChecked(selected);
							((CheckedTextView) view).setText(entryName);
						} else if (view.getClass().equals(TextView.class)) {
							((TextView) view).setText(entryName);
						}

						return true;
					}

					return true;
				}
			});

			setAdapter(itemListAdapter);

			mCallbacks = this;
			LoaderManager lm = parent.getLoaderManager();
			lm.initLoader(LOADER_ID + (int) category.getId(), null, mCallbacks);

		} catch (Exception e) {
			Log.e(TAG, "Exception when querying for data " + e.getMessage());
		}

		setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (inToDoMode) {
					setEntrySelected(id, false);
				} else {
					boolean selected = ((CheckedTextView) (view.findViewById(R.id.single_checkable_item))).isChecked();
					setEntrySelected(id, !selected);
				}
			}
		});

		final GestureDetector gestureDetector = new GestureDetector(context, gestureListener);

		OnTouchListener touchListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					v.performClick();
					return true;
				}
				return false;
			}
		};

		setOnTouchListener(touchListener);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String condition = EntryColumns.CATEGORY_ID + " = " + category.getId();
		if (inToDoMode)
			condition += " AND " + EntryColumns.IS_SELECTED + " = 1 ";

		String sortOrder = category.getSortOrder().toSortString();

		return new CursorLoader(context, EntryColumns.CONTENT_URI, ENTRY_PROJECTION, condition, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		itemListAdapter.swapCursor(cursor);
		try {
			cursor.registerContentObserver(contentObserver);
		} catch (Exception e) {
			// swallow
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		itemListAdapter.swapCursor(null);
	}

	/**
	 * Call to unload and deactivate the tab.
	 */
	public void unload() {
		Log.v(TAG, "Unload called. CategoryName: " + category.getName());
	}

	private final void setEntrySelected(final long id, final boolean selected) {
		Boolean askForConfirmation = inToDoMode && !currentConfiguration.getDisablePromptInToDoMode();

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					// in case there is only one element in the list
					// after a successful update the list must be removed from
					// the flipper.
					boolean listMustBeRemoved = inToDoMode && (getCount() == 1);
					service.persistSelectionChangeToTheDatabase(id, false);
					updater.updateMetadata(listMustBeRemoved);

					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked - do nothing
					break;
				}
			}
		};

		if (askForConfirmation) {
			final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
			dialogBuilder.setTitle(R.string.mark_item_done).setMessage(R.string.continue_prompt)
					.setPositiveButton(R.string.yes_string, dialogClickListener).setNegativeButton(R.string.no_string, dialogClickListener)
					.show();
		} else {
			// in case there is only one element in the list
			// after a successful update the list must be removed from the
			// flipper.
			boolean listMustBeRemoved = inToDoMode && (getCount() == 1);
			service.persistSelectionChangeToTheDatabase(id, selected);
			updater.updateMetadata(listMustBeRemoved);
		}
	}

	private class TabContentObserver extends ContentObserver {
		public TabContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			if (category.getId() == categoryGetter.getDisplayedCategoryId()) {
				Log.v(TAG,
						"onChange called on the main cursor. SelfChange: " + (selfChange ? "True" : "False") + " Category: "
								+ category.getId());
			}

		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}
	}

}
