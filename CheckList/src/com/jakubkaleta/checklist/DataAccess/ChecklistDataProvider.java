package com.jakubkaleta.checklist.DataAccess;

import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * Main data provider for Reverse CheckList. Responsible for providing Activity,
 * Category and Entry data, as well as AppConfig and AppState info.
 * 
 * @author Jakub Kaleta
 */
public class ChecklistDataProvider extends ContentProvider {
        private static final UriMatcher sUriMatcher;
        private static HashMap<String, String> sActivitiesProjectionMap;
        private static HashMap<String, String> sCategoriesProjectionMap;
        private static HashMap<String, String> sApplicationStateProjectionMap;
        private static HashMap<String, String> sEntriesProjectionMap;
        private static HashMap<String, String> sConfigProjectionMap;
        private static String activitiesLeftOuterJoinCategories;
        private static String categoriesLeftOuterJoinEntries;
        private static String categoriesInnerJoinActivities;
        private static String entriesInnerJoinCategories;
        private static String entriesInnerJoinActivityStatistics;
        private static final int ACTIVITIES = 1;
        private static final int ACTIVITY_ID = 2;
        private static final int APP_STATE = 3;
        private static final int CATEGORIES = 4;
        private static final int CATEGORY_ID = 5;
        private static final int ENTRIES = 6;
        private static final int ENTRY_ID = 7;
        private static final int CONFIGURATION = 8;

        static {
                sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
                sUriMatcher.addURI(AppStateColumns.AUTHORITY, "applicationState",
                                APP_STATE);
                sUriMatcher.addURI(ActivityColumns.AUTHORITY, "activities", ACTIVITIES);
                sUriMatcher.addURI(ActivityColumns.AUTHORITY, "activities/#",
                                ACTIVITY_ID);
                sUriMatcher.addURI(CategoryColumns.AUTHORITY, "categories", CATEGORIES);
                sUriMatcher.addURI(CategoryColumns.AUTHORITY, "categories/#",
                                CATEGORY_ID);
                sUriMatcher.addURI(EntryColumns.AUTHORITY, "entries", ENTRIES);
                sUriMatcher.addURI(EntryColumns.AUTHORITY, "entries/#", ENTRY_ID);
                sUriMatcher.addURI(ConfigurationParametersColumns.AUTHORITY,
                                "configuration", CONFIGURATION);

                sActivitiesProjectionMap = new HashMap<String, String>();
                sActivitiesProjectionMap = new HashMap<String, String>();
                sActivitiesProjectionMap.put(ActivityColumns._ID,
                                ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + " AS "
                                                + ActivityColumns._ID);
                sActivitiesProjectionMap.put(ActivityColumns.ACTIVITY_NAME,
                                ActivityColumns.ACTIVITY_NAME);
                sActivitiesProjectionMap.put(ActivityColumns.DATE_CREATED,
                                ActivityColumns.TABLE_NAME + "." + ActivityColumns.DATE_CREATED
                                                + " AS " + ActivityColumns.DATE_CREATED);
                sActivitiesProjectionMap.put(ActivityColumns.ITEM_COUNT, " COUNT( "
                                + EntryColumns.TABLE_NAME + "." + EntryColumns._ID + ") AS "
                                + ActivityColumns.ITEM_COUNT);
                sActivitiesProjectionMap.put(ActivityColumns.SELECTED_ITEM_COUNT,
                                " SUM( " + EntryColumns.TABLE_NAME + "."
                                                + EntryColumns.IS_SELECTED + ") AS "
                                                + ActivityColumns.SELECTED_ITEM_COUNT);

                sApplicationStateProjectionMap = new HashMap<String, String>();
                sApplicationStateProjectionMap.put(AppStateColumns._ID,
                                AppStateColumns._ID);
                sApplicationStateProjectionMap.put(AppStateColumns.ACTIVITY_ID,
                                AppStateColumns.ACTIVITY_ID);
                sApplicationStateProjectionMap.put(AppStateColumns.CATEGORY_ID,
                                AppStateColumns.CATEGORY_ID);
                sApplicationStateProjectionMap.put(AppStateColumns.MODE,
                                AppStateColumns.MODE);
                sApplicationStateProjectionMap.put(
                                AppStateColumns.ACTIVITY_LIST_SORT_ORDER,
                                AppStateColumns.ACTIVITY_LIST_SORT_ORDER);

                sCategoriesProjectionMap = new HashMap<String, String>();
                sCategoriesProjectionMap.put(CategoryColumns._ID,
                                CategoryColumns.TABLE_NAME + "." + CategoryColumns._ID + " AS "
                                                + CategoryColumns._ID);
                sCategoriesProjectionMap.put(CategoryColumns.DATE_CREATED,
                                CategoryColumns.TABLE_NAME + "." + CategoryColumns.DATE_CREATED
                                                + " AS " + CategoryColumns.DATE_CREATED);
                sCategoriesProjectionMap.put(CategoryColumns.CATEGORY_NAME,
                                CategoryColumns.CATEGORY_NAME);
                sCategoriesProjectionMap.put(CategoryColumns.CATEGORY_SORT_ORDER,
                		CategoryColumns.TABLE_NAME + "." + CategoryColumns.CATEGORY_SORT_ORDER
                        + " AS " + CategoryColumns.CATEGORY_SORT_ORDER);
                /*
                 * sCategoriesProjectionMap.put(CategoryColumns.CATEGORY_NAME,
                 * CategoryColumns.CATEGORY_NAME + " + ' ' + " +
                 * CategoryColumns.SORT_POSITION + " AS " +
                 * CategoryColumns.CATEGORY_NAME);
                 */
                sCategoriesProjectionMap.put(CategoryColumns.SORT_POSITION,
                		CategoryColumns.TABLE_NAME + "." + CategoryColumns.SORT_POSITION
                        + " AS " + CategoryColumns.SORT_POSITION);
                sCategoriesProjectionMap.put(CategoryColumns.ACTIVITY_ID,
                                CategoryColumns.ACTIVITY_ID);
                sCategoriesProjectionMap.put(CategoryColumns.ITEM_COUNT, " COUNT( "
                                + EntryColumns.TABLE_NAME + "." + EntryColumns._ID + ") AS "
                                + CategoryColumns.ITEM_COUNT);
                sCategoriesProjectionMap.put(CategoryColumns.SELECTED_ITEM_COUNT,
                                " SUM( " + EntryColumns.TABLE_NAME + "."
                                                + EntryColumns.IS_SELECTED + ") AS "
                                                + CategoryColumns.SELECTED_ITEM_COUNT);

                sEntriesProjectionMap = new HashMap<String, String>();
                sEntriesProjectionMap.put(EntryColumns._ID, EntryColumns.TABLE_NAME
                                + "." + EntryColumns._ID + " AS " + EntryColumns._ID);
                sEntriesProjectionMap.put(EntryColumns.DATE_CREATED,
                                EntryColumns.TABLE_NAME + "." + EntryColumns.DATE_CREATED
                                                + " AS " + EntryColumns.DATE_CREATED);
                sEntriesProjectionMap.put(EntryColumns.DATE_LAST_PERFORMED,
                                EntryColumns.DATE_LAST_PERFORMED);
                sEntriesProjectionMap.put(EntryColumns.ENTRY_NAME,
                                EntryColumns.ENTRY_NAME);
                sEntriesProjectionMap.put(EntryColumns.CATEGORY_ID,
                                EntryColumns.CATEGORY_ID);
                sEntriesProjectionMap.put(EntryColumns.IS_SELECTED,
                                EntryColumns.IS_SELECTED);
                sEntriesProjectionMap.put(CategoryColumns.CATEGORY_NAME,
                                CategoryColumns.CATEGORY_NAME);
                sEntriesProjectionMap.put(CategoryColumns.CATEGORY_SORT_ORDER,
                		CategoryColumns.TABLE_NAME + "." + CategoryColumns.CATEGORY_SORT_ORDER
                        + " AS " + CategoryColumns.CATEGORY_SORT_ORDER);
                sEntriesProjectionMap.put(CategoryColumns.ACTIVITY_ID,
                                CategoryColumns.TABLE_NAME + "." + CategoryColumns.ACTIVITY_ID);
                sEntriesProjectionMap.put(ActivityColumns.ACTIVITY_NAME,
                                ActivityColumns.ACTIVITY_NAME);
                sEntriesProjectionMap.put(ActivityColumns.ITEM_COUNT,
                                ActivityColumns.ITEM_COUNT);
                sEntriesProjectionMap.put(ActivityColumns.SELECTED_ITEM_COUNT,
                                ActivityColumns.SELECTED_ITEM_COUNT);
                sEntriesProjectionMap.put(EntryColumns.SORT_POSITION,
                				EntryColumns.TABLE_NAME + "." + EntryColumns.SORT_POSITION
                				+ " AS " + EntryColumns.SORT_POSITION);

                sConfigProjectionMap = new HashMap<String, String>();
                sConfigProjectionMap.put(ConfigurationParametersColumns._ID,
                                ConfigurationParametersColumns._ID);
                sConfigProjectionMap.put(
                                ConfigurationParametersColumns.PROMPT_IN_TODO_MODE,
                                ConfigurationParametersColumns.PROMPT_IN_TODO_MODE);
                sConfigProjectionMap.put(
                        ConfigurationParametersColumns.START_IN_TODO_MODE,
                        ConfigurationParametersColumns.START_IN_TODO_MODE);

                activitiesLeftOuterJoinCategories = " LEFT OUTER JOIN "
                                + CategoryColumns.TABLE_NAME + " ON ( "
                                + CategoryColumns.ACTIVITY_ID + " = "
                                + ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + ")";
                categoriesLeftOuterJoinEntries = " LEFT OUTER JOIN "
                                + EntryColumns.TABLE_NAME + " ON ( " + EntryColumns.CATEGORY_ID
                                + " = " + CategoryColumns.TABLE_NAME + "."
                                + CategoryColumns._ID + ")";

                categoriesInnerJoinActivities = " INNER JOIN "
                                + ActivityColumns.TABLE_NAME + " ON ( "
                                + CategoryColumns.TABLE_NAME + "."
                                + CategoryColumns.ACTIVITY_ID + " = "
                                + ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID + ")";
                entriesInnerJoinCategories = " LEFT OUTER JOIN "
                                + CategoryColumns.TABLE_NAME + " ON ( "
                                + EntryColumns.CATEGORY_ID + " = " + CategoryColumns.TABLE_NAME
                                + "." + CategoryColumns._ID + ")";
                entriesInnerJoinActivityStatistics = " INNER JOIN (" + "SELECT "
                                + ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID
                                + " AS " + CategoryColumns.ACTIVITY_ID + "," + "COUNT( "
                                + EntryColumns.TABLE_NAME + "." + EntryColumns._ID + ") AS "
                                + ActivityColumns.ITEM_COUNT + "," + "SUM( "
                                + EntryColumns.IS_SELECTED + ") AS "
                                + ActivityColumns.SELECTED_ITEM_COUNT + " FROM "
                                + EntryColumns.TABLE_NAME + entriesInnerJoinCategories
                                + categoriesInnerJoinActivities + " GROUP BY "
                                + ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID
                                + ") SQ " + " ON SQ.ActivityId = " + ActivityColumns.TABLE_NAME
                                + "." + ActivityColumns._ID;
        }

        private DatabaseOpenHelper mOpenHelper;

        @Override
        public boolean onCreate() {
                mOpenHelper = new DatabaseOpenHelper(getContext());

                // uncomment to force database update
                // mOpenHelper.onUpgrade(mOpenHelper.getWritableDatabase(), 5, 6);
                return true;
        }

        @Override
        public int delete(Uri uri, String where, String[] whereArgs) {
                Log.i(getClass().getSimpleName(), "delete called.");

                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int count = 0;
                switch (sUriMatcher.match(uri)) {
                case ACTIVITY_ID:
                        String activityId = uri.getPathSegments().get(1);

                        try {
                                Log.i(getClass().getSimpleName(),
                                                "Attempting to delete activity activity with id:"
                                                                + activityId + ".");

                                count = db.delete(
                                                ActivityColumns.TABLE_NAME,
                                                ActivityColumns._ID
                                                                + "="
                                                                + activityId
                                                                + (!TextUtils.isEmpty(where) ? " AND (" + where
                                                                                + ')' : ""), whereArgs);

                                getContext().getContentResolver().notifyChange(uri, null);
                        } catch (Exception e) {
                                Log.e(getClass().getSimpleName(),
                                                "delete caught an exception trying to delete activity with id:"
                                                                + activityId + ". Exception message:"
                                                                + e.getMessage());
                        }
                        break;

                case CATEGORY_ID:
                        String categoryId = uri.getPathSegments().get(1);
                        count = db.delete(
                                        CategoryColumns.TABLE_NAME,
                                        CategoryColumns._ID
                                                        + "="
                                                        + categoryId
                                                        + (!TextUtils.isEmpty(where) ? " AND (" + where
                                                                        + ')' : ""), whereArgs);
                        break;

                case ENTRY_ID:
                        String entryId = uri.getPathSegments().get(1);
                        count = db.delete(
                                        EntryColumns.TABLE_NAME,
                                        EntryColumns._ID
                                                        + "="
                                                        + entryId
                                                        + (!TextUtils.isEmpty(where) ? " AND (" + where
                                                                        + ')' : ""), whereArgs);
                        break;

                default:
                        throw new IllegalArgumentException("Unknown URI " + uri);
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return count;
        }

        @Override
        public String getType(Uri uri) {
                switch (sUriMatcher.match(uri)) {
                case ACTIVITIES:
                        Log.i(getClass().getSimpleName(), "getType called. Returning "
                                        + ActivityColumns.CONTENT_TYPE);
                        return ActivityColumns.CONTENT_TYPE;

                case APP_STATE:
                        Log.i(getClass().getSimpleName(), "getType called. Returning "
                                        + AppStateColumns.CONTENT_TYPE);
                        return AppStateColumns.CONTENT_TYPE;

                case CATEGORIES:
                        Log.i(getClass().getSimpleName(), "getType called. Returning "
                                        + CategoryColumns.CONTENT_TYPE);
                        return CategoryColumns.CONTENT_TYPE;

                case ENTRIES:
                        Log.i(getClass().getSimpleName(), "getType called. Returning "
                                        + EntryColumns.CONTENT_TYPE);
                        return EntryColumns.CONTENT_TYPE;

                case CONFIGURATION:
                        Log.i(getClass().getSimpleName(), "getType called. Returning "
                                        + ConfigurationParametersColumns.CONTENT_TYPE);
                        return ConfigurationParametersColumns.CONTENT_TYPE;

                default:
                        throw new IllegalArgumentException("Unknown URI " + uri);
                }

        }

        @Override
        public Uri insert(Uri uri, ContentValues initialValues) {
                Log.i(getClass().getSimpleName(),
                                "insert called with uri " + uri.toString()
                                                + " and initialValues of cardinality "
                                                + initialValues.size());

                ContentValues values;
                values = new ContentValues(initialValues);
                Long now = Long.valueOf(System.currentTimeMillis());

                long insertedId = 0;

                SQLiteDatabase db = mOpenHelper.getWritableDatabase();

                switch (sUriMatcher.match(uri)) {
                case ACTIVITIES:
                        // Make sure that the fields are all set
                        if (!values.containsKey(ActivityColumns.ACTIVITY_NAME)) {
                                throw new SQLException(
                                                "Activity Name was not provided! Insert expects ActivityName!");
                        }

                        if (!values.containsKey(ActivityColumns.DATE_CREATED)) {
                                values.put(ActivityColumns.DATE_CREATED, now);
                        }

                        insertedId = db.insert(ActivityColumns.TABLE_NAME, null, values);
                        break;

                case CATEGORIES:
                        // Make sure that the fields are all set
                        if (!values.containsKey(CategoryColumns.CATEGORY_NAME))
                                throw new SQLException(
                                                "Entry Name was not provided! Insert expects EntryName!");

                        if (!values.containsKey(CategoryColumns.DATE_CREATED))
                                values.put(CategoryColumns.DATE_CREATED, now);
                        
                        if (!values.containsKey(CategoryColumns.CATEGORY_SORT_ORDER))
                            values.put(CategoryColumns.CATEGORY_SORT_ORDER, CategorySortOrder.AlphabeticallyAsc.toNumber());

                        if (!values.containsKey(CategoryColumns.ACTIVITY_ID))
                                throw new SQLException(
                                                "ACTIVITY_ID was not provided! Insert expects ACTIVITY_ID!");

                        insertedId = db.insert(CategoryColumns.TABLE_NAME, null, values);
                        break;

                case ENTRIES:
                        // Make sure that the fields are all set
                        if (!values.containsKey(EntryColumns.ENTRY_NAME))
                                throw new SQLException(
                                                "Entry Name was not provided! Insert expects EntryName!");

                        if (!values.containsKey(EntryColumns.DATE_CREATED))
                                values.put(EntryColumns.DATE_CREATED, now);

                        if (!values.containsKey(EntryColumns.CATEGORY_ID))
                                throw new SQLException(
                                                "CATEGORY_ID was not provided! Insert expects CATEGORY_ID!");

                        if (!values.containsKey(EntryColumns.IS_SELECTED))
                                values.put(EntryColumns.IS_SELECTED, 0);

                        insertedId = db.insert(EntryColumns.TABLE_NAME, null, values);
                        break;
                }

                if (insertedId > 0) {
                        // notify all listeners about the change
                        Uri itemUri = ContentUris.withAppendedId(uri, insertedId);
                        getContext().getContentResolver().notifyChange(itemUri, null);
                        return itemUri;
                }

                throw new SQLException("Failed to insert row into " + uri);
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                String groupBy = "";
                String having = "";

                switch (sUriMatcher.match(uri)) {
                case ACTIVITIES:
                        qb.setTables("Activities " + activitiesLeftOuterJoinCategories
                                        + categoriesLeftOuterJoinEntries);
                        qb.setProjectionMap(sActivitiesProjectionMap);
                        groupBy = ActivityColumns.TABLE_NAME + "." + ActivityColumns._ID;
                        break;

                case APP_STATE:

                        qb.setTables("ApplicationState");
                        qb.setProjectionMap(sApplicationStateProjectionMap);
                        break;

                case CATEGORIES:
                        qb.setTables("Categories" + categoriesLeftOuterJoinEntries);
                        qb.setProjectionMap(sCategoriesProjectionMap);
                        groupBy = CategoryColumns.TABLE_NAME + "." + CategoryColumns._ID;
                        break;

                case ENTRIES:
                        qb.setTables("Entries " + entriesInnerJoinCategories
                                        + categoriesInnerJoinActivities
                                        + entriesInnerJoinActivityStatistics);
                        qb.setProjectionMap(sEntriesProjectionMap);

                        break;

                case CONFIGURATION:
                        qb.setTables(ConfigurationParametersColumns.TABLE_NAME);
                        qb.setProjectionMap(sConfigProjectionMap);
                        break;

                default:
                        throw new IllegalArgumentException("Unknown URI " + uri);
                }

                try {
                        // Get the database and run the query
                        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

                        Cursor c = qb.query(db, projection, selection, selectionArgs,
                                        groupBy, having, sortOrder);

                        // Tell the cursor what uri to watch, so it knows when its source
                        // data changes

                        c.setNotificationUri(getContext().getContentResolver(), uri);
                        return c;
                } catch (Exception e) {
                        Log.e(getClass().getSimpleName(),
                                        "Query Failed with: " + e.getMessage());

                        return null;
                }
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection,
                        String[] selectionArgs) {
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int updatedCount = 0;

                switch (sUriMatcher.match(uri)) {
                case ACTIVITY_ID:
                        // Make sure that the fields are all set
                        if (!values.containsKey(ActivityColumns.ACTIVITY_NAME))
                                throw new SQLException(
                                                "Entry Name was not provided! Insert expects EntryName!");

                        String activityId = uri.getPathSegments().get(1);
                        String[] strArray = { "" + activityId };

                        updatedCount = db.update(ActivityColumns.TABLE_NAME, values,
                                        ActivityColumns._ID + " = ?", strArray);
                        break;

                case APP_STATE:
                        updatedCount = db.update(AppStateColumns.TABLE_NAME, values, null,
                                        selectionArgs);
                        break;

                case CATEGORY_ID:

                        SQLiteDatabase db1 = mOpenHelper.getWritableDatabase();

                        String categoryId = uri.getPathSegments().get(1);
                        String[] array = { "" + categoryId };

                        updatedCount = db1.update(CategoryColumns.TABLE_NAME, values,
                                        CategoryColumns._ID + " = ?", array);
                        break;

                case ENTRY_ID:
                        String entryId = uri.getPathSegments().get(1);
                        updatedCount = db.update(
                                        EntryColumns.TABLE_NAME,
                                        values,
                                        EntryColumns._ID
                                                        + "="
                                                        + entryId
                                                        + (!TextUtils.isEmpty(selection) ? " AND ("
                                                                        + selection + ')' : ""), selectionArgs);
                        break;
                case ENTRIES:
                        updatedCount = db.update(EntryColumns.TABLE_NAME, values,
                                        selection, selectionArgs);
                        break;

                case CONFIGURATION:
                        updatedCount = db.update(ConfigurationParametersColumns.TABLE_NAME,
                                        values, selection, selectionArgs);
                        break;
                }

                if (updatedCount > 0) {
                        getContext().getContentResolver().notifyChange(uri, null);
                        return updatedCount;
                }

                return 0;
        }

}