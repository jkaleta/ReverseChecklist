package com.jakubkaleta.checklist.DataAccess.tables;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Describes metadata for ConfigurationParameters table
 * @author Jakub Kaleta
 *
 */
public final class ConfigurationParametersColumns implements BaseColumns {

	/**
	 * The authority to use when querying for app configuration
	 */
	public static final String AUTHORITY = "com.jakubkaleta.checklist.DataAccess.Configuration";

	
	// This class cannot be instantiated
	private ConfigurationParametersColumns() {
	}
	
	 /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/configuration");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of configuration parameters.
     */
    public static final String CONTENT_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.configuration";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single activity.
     */
    public static final String CONTENT_ITEM_TYPE = "com.jakubkaleta.checklist/com.jakubkaleta.checklist.configuration";

    /**
	 * The id of activity
	 * <P>
	 * Type: Boolean
	 * </P>
	 */
	public static final String PROMPT_IN_TODO_MODE = "PromptInToDoMode";
	
	/**
	 * The id of activity
	 * <P>
	 * Type: Boolean
	 * </P>
	 */
	public static final String START_IN_TODO_MODE = "StartInToDoMode";
		
	/**
	 * The name of the database table this object describes.
	 */
	public static final String TABLE_NAME = "ConfigurationParameters";

}
