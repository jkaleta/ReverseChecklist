package com.jakubkaleta.checklist.DataAccess.tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Describes metadata for Category table
 * @author Jakub Kaleta
 *
 */
public final class CategoryColumns implements BaseColumns {

	/**
	 * The authority to use when querying for Categories
	 */
	public static final String AUTHORITY = "com.jakubkaleta.checklist.DataAccess.Category";

	
	// This class cannot be instantiated
	private CategoryColumns() {
	}
	
	/**
	 * The name of the database table this object describes.
	 */
	public static final String TABLE_NAME = "Categories";
	
	 /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/categories");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of categories.
     */
    public static final String CONTENT_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.category";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single category.
     */
    public static final String CONTENT_ITEM_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.category";

    /**
	 * The name of category
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String CATEGORY_NAME = "CategoryName";

	/**
	 * The timestamp for when the note was created
	 * <P>
	 * Type: DateTime
	 * </P>
	 */
	public static final String DATE_CREATED = "DateCreated";
	
	/**
	 * The number of selected items associated with this category
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String SELECTED_ITEM_COUNT = "SelectedItemCount";

	/**
	 * The number of items associated with this category
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String ITEM_COUNT = "ItemCount";
	
	/**
	 * The sort order selected in the Category when the state was persisted
	 * <P>
	 * Type: Integer
	 * </P>
	 */
	public static final String CATEGORY_SORT_ORDER = "CategorySortOrder";
    
	/**
	 * The default sort order for this table
	 */
	public static final String DEFAULT_SORT_ORDER = TABLE_NAME + "." + DATE_CREATED + " DESC";;
	
	/**
	 * The reference to the activity this entry is under
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String ACTIVITY_ID = "ActivityId";
	
	/**
	 * The current user defined sort position associated with this category
	 * <P>
	 * Type: INT
	 * </P>
	 */
	public static final String SORT_POSITION = "SortPosition";

	

}
