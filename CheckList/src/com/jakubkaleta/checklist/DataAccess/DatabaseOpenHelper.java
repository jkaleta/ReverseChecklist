package com.jakubkaleta.checklist.DataAccess;

import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.AppStateColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.ConfigurationParametersColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A database wrapper for Reverse CheckList
 * 
 * @author Jakub Kaleta 
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME = "ActivitiesManager";
	private static final int DATABASE_VERSION = 9;

	// Creating the Activities table.
	// ActivityColumns._ID is just an alias over ROWID, an autoincremented
	// field.
	private static final String ACTIVITIES_TABLE_CREATE = "CREATE TABLE "
			+ ActivityColumns.TABLE_NAME + " (" + ActivityColumns._ID + " INTEGER PRIMARY KEY,"
			+ ActivityColumns.ACTIVITY_NAME + " TEXT, " + ActivityColumns.DATE_CREATED
			+ " DATETIME DEFAULT CURRENT_TIMESTAMP);";

	// Creating the categories table.
	private static final String CATEGORIES_TABLE_CREATE = "CREATE TABLE "
			+ CategoryColumns.TABLE_NAME + " (" + CategoryColumns._ID + " INTEGER PRIMARY KEY,"
			+ CategoryColumns.CATEGORY_NAME + " TEXT, " + CategoryColumns.ACTIVITY_ID
			+ " INTEGER, " + CategoryColumns.DATE_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

	// Creating the entries table.
	private static final String ENTRIES_TABLE_CREATE = "CREATE TABLE " + EntryColumns.TABLE_NAME
			+ " (" + EntryColumns._ID + " INTEGER PRIMARY KEY," + EntryColumns.ENTRY_NAME
			+ " TEXT, " + EntryColumns.CATEGORY_ID + " INTEGER, " + EntryColumns.DATE_CREATED
			+ " DATETIME DEFAULT CURRENT_TIMESTAMP, " + EntryColumns.DATE_LAST_PERFORMED
			+ " DATETIME DEFAULT CURRENT_TIMESTAMP, " + EntryColumns.IS_SELECTED + " BOOLEAN);";

	// Creating the state table.
	private static final String STATE_TABLE_CREATE = "CREATE TABLE " + AppStateColumns.TABLE_NAME
			+ " (" + AppStateColumns._ID + " INTEGER PRIMARY KEY," + AppStateColumns.ACTIVITY_ID
			+ " INTEGER, " + AppStateColumns.CATEGORY_ID + " INTEGER, " + AppStateColumns.MODE
			+ " TEXT);";

	private static final String CONFIGURATION_PARAMETERS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
			+ ConfigurationParametersColumns.TABLE_NAME
			+ " ("
			+ ConfigurationParametersColumns._ID
			+ " INTEGER PRIMARY KEY,"
			+ ConfigurationParametersColumns.PROMPT_IN_TODO_MODE
			+ " INTEGER DEFAULT 1);";

	private static final String addTriggerAutoDeleteCategoriesWhenActivityDeleted = " CREATE TRIGGER delete_categories_with_activity "
			+ " BEFORE DELETE ON "
			+ ActivityColumns.TABLE_NAME
			+ " FOR EACH ROW BEGIN "
			+ " DELETE FROM "
			+ CategoryColumns.TABLE_NAME
			+ " WHERE "
			+ CategoryColumns.ACTIVITY_ID + "= OLD._ID; " + " END;";

	private static final String addTriggerAutoDeleteEntriesWhenCategoryDeleted = " CREATE TRIGGER delete_entries_with_category "
			+ " BEFORE DELETE ON "
			+ CategoryColumns.TABLE_NAME
			+ " FOR EACH ROW BEGIN "
			+ " DELETE FROM "
			+ EntryColumns.TABLE_NAME
			+ " WHERE "
			+ EntryColumns.CATEGORY_ID
			+ "= OLD._ID; " + " END;";

	private void insertActivity(SQLiteDatabase db, int id, String activityName)
	{
		ContentValues values = new ContentValues();

		values.put(ActivityColumns._ID, id);
		values.put(ActivityColumns.ACTIVITY_NAME, activityName);
		db.insert(ActivityColumns.TABLE_NAME, null, values);
	}

	private void insertCategory(SQLiteDatabase db, int id, long activityId, String categoryName)
	{
		ContentValues values = new ContentValues();

		values.put(CategoryColumns._ID, id);
		values.put(CategoryColumns.CATEGORY_NAME, categoryName);
		values.put(CategoryColumns.ACTIVITY_ID, activityId);
		db.insert(CategoryColumns.TABLE_NAME, null, values);
	}

	private void insertEntry(SQLiteDatabase db, int categoryId, String entryName)
	{
		ContentValues values = new ContentValues();

		values.put(EntryColumns.ENTRY_NAME, entryName);
		values.put(EntryColumns.CATEGORY_ID, categoryId);
		values.put(EntryColumns.DATE_LAST_PERFORMED, "");
		values.put(EntryColumns.IS_SELECTED, false);
		db.insert(EntryColumns.TABLE_NAME, null, values);
	}

	DatabaseOpenHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		try
		{
			Log.i(getClass().getSimpleName(), "onCreate called. Creating initial database.");

			db.execSQL(ACTIVITIES_TABLE_CREATE);
			Log.i(getClass().getSimpleName(), "onCreate created table ACTIVITIES.");

			db.execSQL(CATEGORIES_TABLE_CREATE);
			Log.i(getClass().getSimpleName(), "onCreate created table CATEGORIES.");

			db.execSQL(ENTRIES_TABLE_CREATE);
			Log.i(getClass().getSimpleName(), "onCreate created table ENTRIES.");

			db.execSQL(STATE_TABLE_CREATE);
			Log.i(getClass().getSimpleName(), "onCreate created table STATE.");

			db.execSQL(addTriggerAutoDeleteCategoriesWhenActivityDeleted);
			db.execSQL(addTriggerAutoDeleteEntriesWhenCategoryDeleted);

			// populate the state table with sample data
			ContentValues values = new ContentValues();
			values.put(AppStateColumns.ACTIVITY_ID, 0);
			values.put(AppStateColumns.CATEGORY_ID, 0);
			values.put(AppStateColumns.MODE, "EDIT");
			db.insert(AppStateColumns.TABLE_NAME, null, values);

			insertActivity(db, 1, "Shopping");
			Log.i(getClass().getSimpleName(), "onCreate populated table ACTIVITIES.");

			insertCategory(db, 7, 1, "Dairy products");
			insertCategory(db, 8, 1, "Fruit and vegetables");
			insertCategory(db, 9, 1, "Cans and Jars");
			insertCategory(db, 10, 1, "Flour");
			insertCategory(db, 11, 1, "Juices and other drinks");
			insertCategory(db, 12, 1, "Meat, fish and cold cuts");
			insertCategory(db, 13, 1, "Spices, condiments etc.");
			insertCategory(db, 14, 1, "Frozen food");
			insertCategory(db, 15, 1, "Indulgences");
			insertCategory(db, 16, 1, "Supplies");

			insertEntry(db, 7, "Milk");
			insertEntry(db, 7, "Yogurt");
			insertEntry(db, 7, "Salted Butter");
			insertEntry(db, 7, "Unsalted Butter");
			insertEntry(db, 7, "Cheese");
			insertEntry(db, 7, "Cream cheese");
			insertEntry(db, 7, "Eggs");
			insertEntry(db, 7, "Cottage cheese");
			insertEntry(db, 7, "Parmesan");

			insertEntry(db, 8, "Carrots");
			insertEntry(db, 8, "Onion");
			insertEntry(db, 8, "Potatoes");
			insertEntry(db, 8, "Garlic");
			insertEntry(db, 8, "Mushrooms");
			insertEntry(db, 8, "Hot Peppers");
			insertEntry(db, 8, "Cucumber");
			insertEntry(db, 8, "Lettuce");
			insertEntry(db, 8, "Yams");
			insertEntry(db, 8, "Chives");
			insertEntry(db, 8, "Cilantro");
			insertEntry(db, 8, "Broccoli");
			insertEntry(db, 8, "Tomatoes");
			insertEntry(db, 8, "Apples");
			insertEntry(db, 8, "Bananas");
			insertEntry(db, 8, "Mango");
			insertEntry(db, 8, "Lemons");
			insertEntry(db, 8, "Oranges");
			insertEntry(db, 8, "Avocado");
			insertEntry(db, 8, "Plums");
			insertEntry(db, 8, "Pears");
			insertEntry(db, 8, "Grapes");
			insertEntry(db, 8, "Raspberries");
			insertEntry(db, 8, "Strawberries");
			insertEntry(db, 8, "Pumelo/grapefruit");
			insertEntry(db, 8, "Peaches");

			insertEntry(db, 9, "Refried Beans");
			insertEntry(db, 9, "Peanut Butter");
			insertEntry(db, 9, "Giardiniera");
			insertEntry(db, 9, "Tuna");
			insertEntry(db, 9, "Sardines");
			insertEntry(db, 9, "Spaghetti sauce");
			insertEntry(db, 9, "Jelly");

			insertEntry(db, 10, "Rice - white");
			insertEntry(db, 10, "Rice - brown");
			insertEntry(db, 10, "Spaghetti");
			insertEntry(db, 10, "Pasta");
			insertEntry(db, 10, "All-Purpose Flour");
			insertEntry(db, 10, "Corn Starch");
			insertEntry(db, 10, "Sugar");
			insertEntry(db, 10, "Brown Sugar");
			insertEntry(db, 10, "Cocoa");
			insertEntry(db, 10, "Bread");
			insertEntry(db, 10, "Corn Flakes");
			insertEntry(db, 10, "Oatmeal");

			insertEntry(db, 11, "Seltzer water");
			insertEntry(db, 11, "Instant coffee");
			insertEntry(db, 11, "Tea");
			insertEntry(db, 11, "Vodka");
			insertEntry(db, 11, "Whiskey");
			insertEntry(db, 11, "White wine");
			insertEntry(db, 11, "Red wine");
			insertEntry(db, 11, "Beer");
			insertEntry(db, 11, "Bottled Water");

			insertEntry(db, 12, "Cold cuts");
			insertEntry(db, 12, "Beef franks");
			insertEntry(db, 12, "Ground meat");
			insertEntry(db, 12, "Chicken Drumsticks");
			insertEntry(db, 12, "Chicken Breast");
			insertEntry(db, 12, "Steak");
			insertEntry(db, 12, "Veal");
			insertEntry(db, 12, "Fish");
			insertEntry(db, 12, "Short ribs");
			insertEntry(db, 12, "Cold smoked salmon");
			insertEntry(db, 12, "Salami");

			insertEntry(db, 13, "Salt");
			insertEntry(db, 13, "Pepper");
			insertEntry(db, 13, "Basil");
			insertEntry(db, 13, "Mayo");
			insertEntry(db, 13, "Honey Mustard");
			insertEntry(db, 13, "Bbq sauce");
			insertEntry(db, 13, "Soy sauce");
			insertEntry(db, 13, "Chipotle tabasco");
			insertEntry(db, 13, "Olive oil");
			insertEntry(db, 13, "Corn oil");
			insertEntry(db, 13, "Vinegar");
			insertEntry(db, 13, "Honey");

			insertEntry(db, 14, "Frozen vegetables");

			insertEntry(db, 15, "Ice cream");
			insertEntry(db, 15, "Chocolate");
			insertEntry(db, 15, "White chocolate");
			insertEntry(db, 15, "Doritos");
			insertEntry(db, 15, "Popcorn");
			insertEntry(db, 15, "Chips");
			insertEntry(db, 15, "Other chips");
			insertEntry(db, 15, "Even more chips");
			insertEntry(db, 15, "Assorted Nuts");
			insertEntry(db, 15, "Cookies");
			insertEntry(db, 15, "Pie");

			insertEntry(db, 16, "Paper towels");
			insertEntry(db, 16, "Toilet paper");
			insertEntry(db, 16, "Swifter");
			insertEntry(db, 16, "Pinesol");
			insertEntry(db, 16, "Murphy’s");
			insertEntry(db, 16, "Windex");
			insertEntry(db, 16, "Detergent");
			insertEntry(db, 16, "Clorox wipes");
			insertEntry(db, 16, "Soft Scrub");
			insertEntry(db, 16, "Easy off");
			insertEntry(db, 16, "Toothpaste");
			insertEntry(db, 16, "Mouthwash");
			insertEntry(db, 16, "Shampoo");
			insertEntry(db, 16, "Ziplocks");
			insertEntry(db, 16, "Aluminium foil");
			insertEntry(db, 16, "Plastic wrap");

			onUpgrade(db, 0, DATABASE_VERSION);
		}
		catch (Exception e)
		{
			Log.e(getClass().getSimpleName(), "onCreate caught an exception creating database."
					+ e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		Log.w(getClass().getSimpleName(), "Upgrading database from version " + oldVersion + " to "
				+ newVersion);

		// upgrade only if the old database version is older than 3
		if (oldVersion < 4)
		{
			db.execSQL(CONFIGURATION_PARAMETERS_TABLE_CREATE);
			ContentValues values = new ContentValues();
			values.put(ConfigurationParametersColumns.PROMPT_IN_TODO_MODE, 1);
			db.insert(ConfigurationParametersColumns.TABLE_NAME, null, values);
		}
		
		if(oldVersion < 5)
		{
			String updateConfigTableAddActivitiesSortOrder = 
				"ALTER TABLE "
				+ AppStateColumns.TABLE_NAME
				+ " ADD "
				+ AppStateColumns.ACTIVITY_LIST_SORT_ORDER
				+ " INTEGER DEFAULT 3;";
			
			db.execSQL(updateConfigTableAddActivitiesSortOrder);			
		}
		
		if(oldVersion < 7)
		{
			String updateCategoryTableAddSortPosition = 
				"ALTER TABLE "
				+ CategoryColumns.TABLE_NAME
				+ " ADD "
				+ CategoryColumns.SORT_POSITION
				+ " INTEGER DEFAULT 0;";
			
			String updateCategoryTableSetDefaultSortPositionsForExistingElements = 
				"UPDATE " + CategoryColumns.TABLE_NAME 
				+ " SET " + CategoryColumns.SORT_POSITION + " = "
				+ "( SELECT COUNT(Inner." + CategoryColumns._ID + ")" 
				+ " FROM " + CategoryColumns.TABLE_NAME + " AS Inner "
				+ " WHERE Inner._id <= " + CategoryColumns.TABLE_NAME  + "._id"
				+ " AND Inner.ActivityId = " + CategoryColumns.TABLE_NAME + "." + CategoryColumns.ACTIVITY_ID
				+ " ); ";
			
			String addTriggerUpdateUserDefinedSortOnCategories = 
				" CREATE TRIGGER IF NOT EXISTS update_category_user_defined_sort_order_on_insert "
				+ " AFTER INSERT ON "
				+ CategoryColumns.TABLE_NAME
				+ " BEGIN UPDATE " + CategoryColumns.TABLE_NAME 
				+ " SET " + CategoryColumns.SORT_POSITION + " = "
				+ "( SELECT COUNT(Inner." + CategoryColumns._ID + ")" 
				+ " FROM " + CategoryColumns.TABLE_NAME + " AS Inner "
				+ " WHERE Inner._id <= " + CategoryColumns.TABLE_NAME  + "._id"				
				+ " AND Inner.ActivityId = " + CategoryColumns.TABLE_NAME + "." + CategoryColumns.ACTIVITY_ID
				+ " )" 
				+ " WHERE " + CategoryColumns._ID + " = NEW._id; "
				+ " END ";
			
			db.execSQL(updateCategoryTableAddSortPosition);	
			db.execSQL(updateCategoryTableSetDefaultSortPositionsForExistingElements);			
			db.execSQL(addTriggerUpdateUserDefinedSortOnCategories);			
		}		
		
		if(oldVersion < 8)
		{
			String updateEntryTableAddSortPosition = 
					"ALTER TABLE "
					+ EntryColumns.TABLE_NAME
					+ " ADD "
					+ EntryColumns.SORT_POSITION
					+ " INTEGER DEFAULT 0;";
				
				String updateEntryTableSetDefaultSortPositionsForExistingElements = 
					"UPDATE " + EntryColumns.TABLE_NAME 
					+ " SET " + EntryColumns.SORT_POSITION + " = "
					+ "( SELECT COUNT(Inner." + EntryColumns._ID + ")" 
					+ " FROM " + EntryColumns.TABLE_NAME + " AS Inner "
					+ " WHERE Inner._id <= " + EntryColumns.TABLE_NAME  + "._id"
					+ " AND Inner.CategoryId = " + EntryColumns.TABLE_NAME + "." + EntryColumns.CATEGORY_ID
					+ " ); ";
				
				String addTriggerUpdateUserDefinedSortOnEntries = 
					" CREATE TRIGGER IF NOT EXISTS update_entry_user_defined_sort_order_on_insert "
					+ " AFTER INSERT ON "
					+ EntryColumns.TABLE_NAME
					+ " BEGIN UPDATE " + EntryColumns.TABLE_NAME 
					+ " SET " + EntryColumns.SORT_POSITION + " = "
					+ "( SELECT COUNT(Inner." + EntryColumns._ID + ")" 
					+ " FROM " + EntryColumns.TABLE_NAME + " AS Inner "
					+ " WHERE Inner._id <= " + EntryColumns.TABLE_NAME  + "._id"				
					+ " AND Inner.CategoryId = " + EntryColumns.TABLE_NAME + "." + EntryColumns.CATEGORY_ID
					+ " )" 
					+ " WHERE " + EntryColumns._ID + " = NEW._id; "
					+ " END ";
				
				String updateCategoriesTableAddSortOrder = 
					"ALTER TABLE "
					+ CategoryColumns.TABLE_NAME
					+ " ADD "
					+ CategoryColumns.CATEGORY_SORT_ORDER
					+ " INTEGER DEFAULT 0;";
				
				db.execSQL(updateEntryTableAddSortPosition);	
				db.execSQL(updateEntryTableSetDefaultSortPositionsForExistingElements);			
				db.execSQL(addTriggerUpdateUserDefinedSortOnEntries);	
				db.execSQL(updateCategoriesTableAddSortOrder);	
		}
		
		if(oldVersion < 9)
		{
			String updateConfigParamsAddStartInToDoMode = 
					"ALTER TABLE "
					+ ConfigurationParametersColumns.TABLE_NAME
					+ " ADD "
					+ ConfigurationParametersColumns.START_IN_TODO_MODE
					+ " INTEGER DEFAULT 0;";
				
				db.execSQL(updateConfigParamsAddStartInToDoMode);	
		}
	}
}
