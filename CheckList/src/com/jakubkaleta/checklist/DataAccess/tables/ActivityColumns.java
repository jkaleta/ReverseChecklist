package com.jakubkaleta.checklist.DataAccess.tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 
 * Describes the columns available in the activities table.
 * 
 * @author Jakub Kaleta
 */
public final class ActivityColumns implements BaseColumns
{

	/**
	 * The authority for the Activity data member
	 */
	public static final String AUTHORITY = "com.jakubkaleta.checklist.DataAccess.Activity";

	// This class cannot be instantiated
	private ActivityColumns()
	{
	}

	/**
	 * The name of the database table this object describes.
	 */
	public static final String TABLE_NAME = "Activities";

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/activities");

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of
	 * activities.
	 */
	public static final String CONTENT_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.activity";

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
	 * activity.
	 */
	public static final String CONTENT_ITEM_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.activity";

	/**
	 * The name of activity
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String ACTIVITY_NAME = "ActivityName";

	/**
	 * The timestamp for when the note was created
	 * <P>
	 * Type: DateTime
	 * </P>
	 */
	public static final String DATE_CREATED = "DateCreated";

	/**
	 * The number of selected items associated with this activity
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String SELECTED_ITEM_COUNT = "SelectedItemCount";

	/**
	 * The number of items associated with this activity
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String ITEM_COUNT = "ItemCount";
	
	/**
	 * The default sort order for this table
	 */
	public static final String DEFAULT_SORT_ORDER = TABLE_NAME + "." + DATE_CREATED + " ASC";

}
