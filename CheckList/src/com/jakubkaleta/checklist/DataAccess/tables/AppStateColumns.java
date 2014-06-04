package com.jakubkaleta.checklist.DataAccess.tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Describes metadata for AppState table
 * @author Jakub Kaleta
 *
 */
public final class AppStateColumns implements BaseColumns {

	/**
	 * The authority to use when querying for application state
	 */
	public static final String AUTHORITY = "com.jakubkaleta.checklist.DataAccess.ApplicationState";

	
	// This class cannot be instantiated
	private AppStateColumns() {
	}
	
	 /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/applicationState");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of activities.
     */
    public static final String CONTENT_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.applicationState";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single activity.
     */
    public static final String CONTENT_ITEM_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.applicationState";

    /**
	 * The id of activity
	 * <P>
	 * Type: Integer
	 * </P>
	 */
	public static final String ACTIVITY_ID = "ActivityId";
	
	/**
	 * The id of category
	 * <P>
	 * Type: Integer
	 * </P>
	 */
	public static final String CATEGORY_ID = "CategoryId";
	
	/**
	 * The mode the application was the state was persisted
	 * <P>
	 * Type: Text
	 * </P>
	 */
	public static final String MODE = "Mode";
	
	/**
	 * The sort order selected in the ActivityList when the state was persisted
	 * <P>
	 * Type: Integer
	 * </P>
	 */
	public static final String ACTIVITY_LIST_SORT_ORDER = "ActivityListSortOrder";
	
	/**
	 * The name of the database table this object describes.
	 */
	public static final String TABLE_NAME = "ApplicationState";

}
