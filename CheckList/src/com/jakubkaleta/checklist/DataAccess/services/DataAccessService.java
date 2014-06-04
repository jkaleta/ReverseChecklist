package com.jakubkaleta.checklist.DataAccess.services;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.jakubkaleta.checklist.Utilities;
import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.ApplicationStateBean;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.ConfigurationBean;
import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * Contains methods that abstract certain aspects of data access and can be
 * called from multiple activities.
 * 
 * @author Jakub Kaleta
 */
public class DataAccessService
{
	private ContentResolver contentResolver;

	private final String TAG = this.getClass().toString();

	private static final String[] CONFIGURATION_PROJECTION = new String[]
	{ ConfigurationParametersColumns._ID, ConfigurationParametersColumns.PROMPT_IN_TODO_MODE };

	private static final String[] STATE_PROJECTION = new String[]
	{ AppStateColumns._ID, AppStateColumns.ACTIVITY_ID, AppStateColumns.CATEGORY_ID,
			AppStateColumns.MODE, AppStateColumns.ACTIVITY_LIST_SORT_ORDER };

	final String[] ALL_TODO_ITEMS_PROJECTION = new String[]
	{ EntryColumns._ID, EntryColumns.ENTRY_NAME, ActivityColumns.ACTIVITY_NAME,
			CategoryColumns.CATEGORY_NAME, ActivityColumns.ITEM_COUNT,
			ActivityColumns.SELECTED_ITEM_COUNT, ActivityColumns.DATE_CREATED };

	final String[] ALL_ITEMS_PROJECTION = new String[]
	{ EntryColumns._ID, CategoryColumns.ACTIVITY_ID, EntryColumns.CATEGORY_ID,
			EntryColumns.ENTRY_NAME, ActivityColumns.ACTIVITY_NAME, CategoryColumns.CATEGORY_NAME,
			EntryColumns.IS_SELECTED, CategoryColumns.CATEGORY_SORT_ORDER };

	private static final String[] CATEGORY_PROJECTION = new String[]
	{ CategoryColumns._ID, CategoryColumns.SORT_POSITION };
	
	private static final String[] ENTRY_PROJECTION = new String[]
			{ EntryColumns._ID, EntryColumns.SORT_POSITION };

	/**
	 * Default constructor
	 * 
	 * @param resolver
	 *            ContentResolver that this class uses to query for data
	 */
	public DataAccessService(ContentResolver resolver)
	{
		contentResolver = resolver;
	}

	/**
	 * Call to get the current app configuration, wrapped in a ConfigurationBean
	 * 
	 * @return a ConfigurationBean representing current config
	 */
	public ConfigurationBean getCurrentConfiguration()
	{
		// get configuration. there is only one record in the db.
		Cursor configCursor = contentResolver.query(ConfigurationParametersColumns.CONTENT_URI,
				CONFIGURATION_PROJECTION, null, null, null);
		configCursor.moveToFirst();
		Boolean checked = configCursor.getInt(configCursor
				.getColumnIndex(ConfigurationParametersColumns.PROMPT_IN_TODO_MODE)) == 0;
		configCursor.close();
		return new ConfigurationBean(checked);
	}

	/**
	 * Call to get the current app state, wrapped in an ApplicationStateBean
	 * 
	 * @return an ApplicationStateBean representing the current app state.
	 */
	public ApplicationStateBean getCurrentApplicationState()
	{
		Cursor stateCursor = contentResolver.query(AppStateColumns.CONTENT_URI, STATE_PROJECTION,
				null, null, null);
		stateCursor.moveToFirst();

		long actId = stateCursor.getLong(stateCursor.getColumnIndex(AppStateColumns.ACTIVITY_ID));
		long catId = stateCursor.getLong(stateCursor.getColumnIndex(AppStateColumns.CATEGORY_ID));
		String mod = stateCursor.getString(stateCursor.getColumnIndex(AppStateColumns.MODE));
		int sortId = stateCursor.getInt(stateCursor
				.getColumnIndex(AppStateColumns.ACTIVITY_LIST_SORT_ORDER));

		stateCursor.close();

		return new ApplicationStateBean(actId, catId, mod, sortId);
	}

	/**
	 * Call to update the IsSelected property of the item with the specified id
	 * to the specified boolean value
	 * 
	 * @param id
	 *            The id of the item to be updated
	 * @param selected
	 *            The new state - selected or not
	 */
	public void persistSelectionChangeToTheDatabase(long id, boolean selected)
	{
		ContentValues values = new ContentValues();
		values.put(EntryColumns.IS_SELECTED, selected ? 1 : 0);
		Log.i(TAG, "Updating task with id:" + id + " to " + (selected ? "Done" : "Not Done"));
		Uri entryUri = ContentUris.withAppendedId(EntryColumns.CONTENT_URI, id);
		contentResolver.update(entryUri, values, null, null);
		Log.v(TAG, "Updated task with id:" + id + " to " + (selected ? "Done" : "Not Done"));
	}

	/**
	 * Returns a Spanned object containing HTML - formatted output to be sent in
	 * an email to the user requesting the quick todo report
	 * 
	 * @param activityId
	 *            nullable activity id of the activity for which the report is
	 *            to be created
	 * 
	 * @return Formatted text as Spanned
	 */
	public Spanned getAllToDoItemsEmailContent(Long activityId)
	{
		String sortOrder = getCurrentApplicationState().getActivityListSortOrder().toSortString()
				+ ", " + CategoryColumns.CATEGORY_NAME;

		// Get a cursor to access all items
		String whereClause = EntryColumns.IS_SELECTED + "= 1";

		if (activityId != null)
			whereClause += " AND " + CategoryColumns.TABLE_NAME + "." + CategoryColumns.ACTIVITY_ID
					+ " = " + activityId;

		// Get a cursor to access all items
		Cursor mCursor = contentResolver.query(EntryColumns.CONTENT_URI, ALL_TODO_ITEMS_PROJECTION,
				whereClause, null, sortOrder);

		StringBuffer sb = new StringBuffer();

		String lastActivityName = "", lastCategoryName = "";

		if (!mCursor.isAfterLast())
		{
			mCursor.moveToFirst();
			while (!mCursor.isAfterLast())
			{
				String activityName = mCursor.getString(mCursor
						.getColumnIndex(ActivityColumns.ACTIVITY_NAME));
				String categoryName = mCursor.getString(mCursor
						.getColumnIndex(CategoryColumns.CATEGORY_NAME));
				String entryName = mCursor.getString(mCursor
						.getColumnIndex(EntryColumns.ENTRY_NAME));

				if (!lastActivityName.equalsIgnoreCase(activityName))
					sb.append("<br><br><B><FONT color='#0A0A85'>").append(activityName).append(
							"</FONT></B><br>");

				if (!lastCategoryName.equalsIgnoreCase(categoryName))
					sb.append("<br><i><FONT color='#4C4CA6'>").append(categoryName).append(
							"</FONT></i><br><br>");

				sb.append(" *").append(entryName).append("<BR>");

				lastActivityName = activityName;
				lastCategoryName = categoryName;

				mCursor.moveToNext();
			}
		}

		mCursor.close();

		return Html.fromHtml(sb.toString());
	}

	/**
	 * @return The total number of selected items in all categories
	 */
	public int getSelectedItemsCount()
	{
		Cursor mCursor = contentResolver.query(EntryColumns.CONTENT_URI, ALL_TODO_ITEMS_PROJECTION,
				EntryColumns.IS_SELECTED + "= 1", null, null);

		int count = mCursor.getCount();

		mCursor.close();

		return count;
	}

	/**
	 * Updates the user defined sort order in the database to match the new user
	 * defined sort order.
	 * 
	 * @param activityId
	 *            ID of the activity that contains all the categories to be
	 *            updated
	 * @param categoryId
	 *            ID of the category for which the sort order was updated
	 * @param oldPosition
	 *            The old sort position - 0 based
	 * @param newPosition
	 *            The new sort position - 0 based
	 */
	public void updateUserDefinedSort(long activityId, long categoryId, int oldPosition,
			int newPosition)
	{
		// skip updates if position did not change
		if (oldPosition == newPosition)
			return;

		// the logic in here is 1-based. The method is called with
		// 0-based numbers
		oldPosition++;
		newPosition++;

		// Example: 1, 2, 3, 4, 5, 6, 7, 8, 9 <- before update
		// After : 1, 2, 3, 7, 5, 6, , 8, 8
		// When dragged and dropped item 7 to position 4
		// 7 becomes 4, 5 and 6 become 6 and 7, 8 and 9 do not change

		// If the old position is greater than the new, we need to subtract one
		// if the old position is smaller than the new, we need to add one
		boolean movedForward = newPosition > oldPosition;

		// The categories that will have to have their sort positions updated
		// are the ones with sort positions between the old and the new
		Cursor categoriesToBeUpdatedCursor = contentResolver.query(CategoryColumns.CONTENT_URI,
				CATEGORY_PROJECTION, CategoryColumns.ACTIVITY_ID + " = " + activityId + " AND "
						+ CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION + (movedForward ? ">" : ">=")
						+ (movedForward ? oldPosition : newPosition) + " AND "
						+ CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION + (movedForward ? "<=" : "<")
						+ (movedForward ? newPosition : oldPosition), null, null);

		if (categoriesToBeUpdatedCursor.isAfterLast())
			return;

		categoriesToBeUpdatedCursor.moveToFirst();
		while (!categoriesToBeUpdatedCursor.isAfterLast())
		{
			long catId = categoriesToBeUpdatedCursor.getLong(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns._ID));

			int position = categoriesToBeUpdatedCursor.getInt(categoriesToBeUpdatedCursor
					.getColumnIndex(CategoryColumns.SORT_POSITION));

			int newPos = position + (movedForward ? -1 : 1);

			ContentValues updateContentValues = new ContentValues();
			updateContentValues.put(CategoryColumns.SORT_POSITION, newPos);
			Uri itemUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, catId);
			contentResolver.update(itemUri, updateContentValues, null, null);

			categoriesToBeUpdatedCursor.moveToNext();
		}

		// finally, update the selected item from old to new position
		ContentValues updateContentValues = new ContentValues();
		updateContentValues.put(CategoryColumns.SORT_POSITION, newPosition);
		Uri itemUri = ContentUris.withAppendedId(CategoryColumns.CONTENT_URI, categoryId);
		contentResolver.update(itemUri, updateContentValues, null, null);
	}

	/**
	 * Updates the user defined sort order in the database to match the new user
	 * defined sort order.
	 * 
	 * @param categoryId
	 *            ID of the category that contains all the entries to be
	 *            updated
	 * @param entryId
	 *            ID of the entry for which the sort order was updated
	 * @param oldPosition
	 *            The old sort position - 0 based
	 * @param newPosition
	 *            The new sort position - 0 based
	 */
	public void updateUserDefinedSortForEntry(long categoryId, long entryId, int oldPosition,
			int newPosition)
	{
		// skip updates if position did not change
		if (oldPosition == newPosition)
			return;

		// the logic in here is 1-based. The method is called with
		// 0-based numbers
		oldPosition++;
		newPosition++;

		// Example: 1, 2, 3, 4, 5, 6, 7, 8, 9 <- before update
		// After : 1, 2, 3, 7, 5, 6, , 8, 8
		// When dragged and dropped item 7 to position 4
		// 7 becomes 4, 5 and 6 become 6 and 7, 8 and 9 do not change

		// If the old position is greater than the new, we need to subtract one
		// if the old position is smaller than the new, we need to add one
		boolean movedForward = newPosition > oldPosition;

		// The entries that will have to have their sort positions updated
		// are the ones with sort positions between the old and the new
		Cursor entriesToBeUpdatedCursor = contentResolver.query(EntryColumns.CONTENT_URI,
				ENTRY_PROJECTION, EntryColumns.CATEGORY_ID + " = " + categoryId + " AND "
						+ EntryColumns.TABLE_NAME + "." + EntryColumns.SORT_POSITION + (movedForward ? ">" : ">=")
						+ (movedForward ? oldPosition : newPosition) + " AND "
						+ EntryColumns.TABLE_NAME + "." + EntryColumns.SORT_POSITION + (movedForward ? "<=" : "<")
						+ (movedForward ? newPosition : oldPosition), null, null);

		if (entriesToBeUpdatedCursor.isAfterLast())
			return;

		entriesToBeUpdatedCursor.moveToFirst();
		while (!entriesToBeUpdatedCursor.isAfterLast())
		{
			long entId = entriesToBeUpdatedCursor.getLong(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns._ID));

			int position = entriesToBeUpdatedCursor.getInt(entriesToBeUpdatedCursor
					.getColumnIndex(EntryColumns.SORT_POSITION));

			int newPos = position + (movedForward ? -1 : 1);

			ContentValues updateContentValues = new ContentValues();
			updateContentValues.put(EntryColumns.SORT_POSITION, newPos);
			Uri itemUri = ContentUris.withAppendedId(EntryColumns.CONTENT_URI, entId);
			contentResolver.update(itemUri, updateContentValues, null, null);

			entriesToBeUpdatedCursor.moveToNext();
		}

		// finally, update the selected item from old to new position
		ContentValues updateContentValues = new ContentValues();
		updateContentValues.put(EntryColumns.SORT_POSITION, newPosition);
		Uri itemUri = ContentUris.withAppendedId(EntryColumns.CONTENT_URI, entryId);
		contentResolver.update(itemUri, updateContentValues, null, null);
	}
	
	/**
	 * Retrieves all existing Activity names and ids from the database
	 * 
	 * @param idsOfActivitiesToGet
	 *            An array of ids of activities that are supposed to be
	 *            retrieved with children from the database. May be empty. If it
	 *            is empty, then this method retrieves all activities.
	 * 
	 * @param includeEmptyLists
	 *            If this parameter is set to true, the method also retrieves
	 *            empty lists
	 * 
	 * @return A new ActivitiesDataSource - complete data source from the
	 *         database.
	 */

	public ActivitiesDataSource getActivitiesWithChildren(Long[] idsOfActivitiesToGet,
			Boolean includeEmptyLists)
	{
		ActivitiesDataSource dataSource = new ActivitiesDataSource();

		String filter = null;

		if (idsOfActivitiesToGet != null && idsOfActivitiesToGet.length > 0)
		{
			filter = ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + " IN ( "
					+ Utilities.join(", ", idsOfActivitiesToGet) + " ) ";
		}

		String sortOrder = ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + " ASC, "
				+ CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION + " ASC , " + EntryColumns.TABLE_NAME + "."
				+ EntryColumns._ID + " ASC ";

		// Get a cursor to access all items
		Cursor mCursor = contentResolver.query(EntryColumns.CONTENT_URI, ALL_ITEMS_PROJECTION,
				filter, null, sortOrder);

		{
			mCursor.moveToFirst();

			ActivityBean aBean = null;
			CategoryBean cBean = null;
			EntryBean eBean = null;
			while (!mCursor.isAfterLast())
			{
				String activityName = mCursor.getString(mCursor
						.getColumnIndex(ActivityColumns.ACTIVITY_NAME));
				long activityId = mCursor.getLong(mCursor
						.getColumnIndex(CategoryColumns.ACTIVITY_ID));
				String categoryName = mCursor.getString(mCursor
						.getColumnIndex(CategoryColumns.CATEGORY_NAME));
				long categoryId = mCursor.getLong(mCursor.getColumnIndex(EntryColumns.CATEGORY_ID));
				String entryName = mCursor.getString(mCursor
						.getColumnIndex(EntryColumns.ENTRY_NAME));
				long entryId = mCursor.getLong(mCursor.getColumnIndex(EntryColumns._ID));
				int isSelected = mCursor.getInt(mCursor.getColumnIndex(EntryColumns.IS_SELECTED));
				int categorySortOrder = mCursor.getInt(mCursor.getColumnIndex(CategoryColumns.CATEGORY_SORT_ORDER));

				aBean = dataSource.getActivity(activityId);
				if (aBean == null)
				{
					aBean = new ActivityBean(activityId, activityName);
					dataSource.addActivity(aBean);
				}

				cBean = aBean.getCategory(categoryId);
				if (cBean == null)
				{
					cBean = new CategoryBean(categoryId, categoryName, CategorySortOrder.fromNumber(categorySortOrder));
					aBean.addCategory(cBean);
				}

				eBean = new EntryBean(entryId, entryName, isSelected > 0);
				cBean.addEntry(eBean);

				mCursor.moveToNext();
			}
		}

		mCursor.close();

		if (includeEmptyLists)
		{
			// additionally, this method also needs to return all empty lists
			final String[] empty_lists_PROJECTION = new String[]
			{ ActivityColumns.ACTIVITY_NAME, ActivityColumns.ITEM_COUNT, ActivityColumns._ID };

			// Get a cursor to access all items
			mCursor = contentResolver.query(ActivityColumns.CONTENT_URI, empty_lists_PROJECTION,
					filter, null, null);

			if (!mCursor.isAfterLast())
			{
				mCursor.moveToFirst();

				ActivityBean aBean = null;
				while (!mCursor.isAfterLast())
				{
					String activityName = mCursor.getString(mCursor
							.getColumnIndex(ActivityColumns.ACTIVITY_NAME));
					long activityId = mCursor.getLong(mCursor.getColumnIndex(ActivityColumns._ID));

					aBean = dataSource.getActivity(activityId);
					if (aBean == null)
					{
						aBean = new ActivityBean(activityId, activityName);
						dataSource.addActivity(aBean);
					}

					mCursor.moveToNext();
				}
			}

			mCursor.close();
		}

		return dataSource;
	}

	/**
	 * Inserts the new activities with children, to the database
	 * 
	 * @param newItems
	 *            The new items to insert
	 */
	public void insertNewItems(List<ActivityBean> newItems)
	{
		for (ActivityBean activity : newItems)
		{
			// insert the activity
			Uri listUri = contentResolver.insert(ActivityColumns.CONTENT_URI, activity
					.getInsertContentValues());
			int listId = Integer.parseInt(listUri.getPathSegments().get(1));

			for (CategoryBean category : activity.getNewCategories())
			{
				// insert the category
				Uri categoryUri = contentResolver.insert(CategoryColumns.CONTENT_URI, category
						.getInsertContentValues(listId));
				int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));

				for (EntryBean entry : category.getNewEntries())
				{
					// insert the entry
					contentResolver.insert(EntryColumns.CONTENT_URI, entry
							.getInsertContentValues(categoryId));
				}
			}

		}
	}

	/**
	 * Updates all dirty items in the underlying data source
	 * 
	 * @param dataSource
	 *            The data source containing items to update
	 */
	public void updateExistingItems(ActivitiesDataSource dataSource)
	{
		for (ActivityBean act : dataSource.getActivities())
		{
			for (CategoryBean cat : act.getCategories())
			{
				for (EntryBean entry : cat.getEntries())
				{
					if (entry.getId() != 0 && entry.getIsDirty())
					{
						Uri entryUri = ContentUris.withAppendedId(EntryColumns.CONTENT_URI, entry
								.getId());
						contentResolver
								.update(entryUri, entry.getUpdateContentValues(), null, null);
					}
				}
			}
		}
	}

	/**
	 * Checks if the passed in activity name is available
	 * 
	 * @param activityName
	 *            The activity name to check
	 * @param activityId
	 *            Optionally, the id of the activity to exclude from the search
	 *            0 by default.
	 * @return True if name is available, false if it is taken
	 */
	public Boolean checkActivityNameAvailability(String activityName, long activityId)
	{
		if (activityName == null)
		{
			Log.e(TAG, "activityName is null in checkActivityNameAvailability");
			return false;
		}

		Cursor mCursor = contentResolver.query(ActivityColumns.CONTENT_URI, new String[]
		{ ActivityColumns._ID }, ActivityColumns.ACTIVITY_NAME + " LIKE '" + activityName + "'"
				+ " AND " + ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + " != "
				+ activityId, null, null);

		if (mCursor == null)
		{
			Log.e(TAG, "cursor is null in checkActivityNameAvailability");
			return true;
		}

		int count = mCursor.getCount();

		mCursor.close();

		return count == 0;
	}

	/**
	 * Copies the list with the given id into a new list, with the given name.
	 * 
	 * @param idOfTheListToCopy
	 *            The id of the list to be copied
	 * @param newListName
	 *            The name to give to the new list
	 * @return True if copied successfully, false if failed.
	 */
	public Boolean copyList(long idOfTheListToCopy, String newListName)
	{
		// Content providers don't support transactions, and what needs to
		// happen here
		// is transactional, so let's handle the rollback manually:
		Uri insertedListUri = null;

		try
		{
			Log.i(TAG, "copyList called with idOfTheListToCopy:" + idOfTheListToCopy
					+ " and new list name :" + newListName);

			// First insert the new list
			ContentValues values = new ContentValues();
			values.put(ActivityColumns.ACTIVITY_NAME, newListName);
			insertedListUri = contentResolver.insert(ActivityColumns.CONTENT_URI, values);
			int insertedListId = Integer.parseInt(insertedListUri.getPathSegments().get(1));

			// Now get all categories and items from the old list...
			ActivitiesDataSource itemsToCopy = getActivitiesWithChildren(new Long[]
			{ idOfTheListToCopy }, true);

			// ... and copy them to the new list
			for (CategoryBean category : itemsToCopy.getActivities().get(0).getCategories())
			{
				Log.i(TAG, "copyList copying category:" + category.getName());

				// insert the category and process entries
				Uri categoryUri = contentResolver.insert(CategoryColumns.CONTENT_URI, category
						.getInsertContentValues(insertedListId));
				int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));

				for (EntryBean entry : category.getEntries())
				{
					Log.v(TAG, "copyList copying entry:" + entry.getName());

					// insert the entry
					contentResolver.insert(EntryColumns.CONTENT_URI, entry
							.getInsertContentValues(categoryId));
				}
			}

			return true;
		}
		catch (Exception e)
		{
			Log.e(TAG, "Copying list failed with: " + e.getMessage());

			// if anything bad happened, remove the newly inserted list
			// if it was inserted
			if (insertedListUri != null)
				contentResolver.delete(insertedListUri, null, null);

			return false;
		}
	}

	/**
	 * This method sets all items within a list selected or unselected
	 * 
	 * @param activityId
	 *            The activity to process
	 * @param selected
	 *            True to select all items, false to unselect
	 */
	public void markAllItemsSelected(Long activityId, Boolean selected)
	{
		try
		{
			ContentValues updateContentValues = new ContentValues();
			updateContentValues.put(EntryColumns.IS_SELECTED, selected);

			String where = EntryColumns.CATEGORY_ID + " IN ( Select "+ CategoryColumns._ID +" from Categories where ActivityId = "+ activityId +")";
			
			contentResolver.update(EntryColumns.CONTENT_URI, updateContentValues, where, null);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Marking all items to selected in list: " + selected.toString()
					+ " failed. Exeption: " + e.getMessage());
		}
	}
	
	/**
	 * This method sets all items within a category selected or unselected
	 * 
	 * @param categoryId
	 *            The category to process
	 * @param selected
	 *            True to select all items, false to unselect
	 */
	public void markAllItemsSelectedInCategory(Long categoryId, Boolean selected)
	{
		try
		{
			ContentValues updateContentValues = new ContentValues();
			updateContentValues.put(EntryColumns.IS_SELECTED, selected);

			String where = EntryColumns.CATEGORY_ID + " = "+ categoryId;
			
			contentResolver.update(EntryColumns.CONTENT_URI, updateContentValues, where, null);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Marking all items to selected in category: " + selected.toString()
					+ " failed. Exeption: " + e.getMessage());
		}
	}

}
