package com.jakubkaleta.checklist.DataAccess.tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Metadata for Entry table
 * @author Jakub Kaleta
 *
 */
public final class EntryColumns implements BaseColumns
{

	/**
	 * The authority to use when querying for Entries
	 */
	public static final String AUTHORITY = "com.jakubkaleta.checklist.DataAccess.Entry";

	// This class cannot be instantiated
	private EntryColumns()
	{
	}

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/entries");

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a directory of entries.
	 */
	public static final String CONTENT_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.entry";

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single entry.
	 */
	public static final String CONTENT_ITEM_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.entry";

	/**
	 * The reference to the category this entry is under
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String CATEGORY_ID = "CategoryId";

	/**
	 * The name of entry
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String ENTRY_NAME = "EntryName";

	/**
	 * The timestamp for when the entry was created
	 * <P>
	 * Type: DateTime
	 * </P>
	 */
	public static final String DATE_CREATED = "DateCreated";

	/**
	 * Indicates whether the entry is currently selected
	 * <P>
	 * Type: Boolean
	 * </P>
	 */
	public static final String IS_SELECTED = "IsSelected";

	 /** The current user defined sort position associated with this entry
	 * <P>
	 * Type: INT
	 * </P>
	 */	
	public static final String SORT_POSITION = "SortPosition";
	
	/**
	 * The timestamp for when the entry was last performed
	 * <P>
	 * Type: DateTime
	 * </P>
	 */
	public static final String DATE_LAST_PERFORMED = "DateLastPerformed";

	/**
	 * The default sort order for this table
	 */
	public static final String DEFAULT_SORT_ORDER = ENTRY_NAME + " COLLATE NOCASE ASC";
	
	/**
	 * The name of the database table this object describes.
	 */
	public static final String TABLE_NAME = "Entries";

}
