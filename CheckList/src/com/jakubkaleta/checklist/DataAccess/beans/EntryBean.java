package com.jakubkaleta.checklist.DataAccess.beans;

import android.content.ContentValues;

import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

/**
 * This class represents a single entry in the database It is a convenience
 * class - for storing and manipulating entries
 * 
 * @author Jakub Kaleta
 */
public class EntryBean
{
	private long id;
	private String name;
	private Boolean isSelected;
	private boolean isDirty;

	/**
	 * Creates a new instance of EntryBean
	 * 
	 * @param id
	 *            The database id of this entry
	 * @param name
	 *            The name of the entry
	 * @param isSelected
	 *            isSelected The selection status of this item
	 */
	public EntryBean(long id, String name, Boolean isSelected)
	{
		this.id = id;
		this.name = name;
		this.isSelected = isSelected;
	}

	/**
	 * @return The database id of the entry
	 */
	public long getId()
	{
		return id;
	}

	/**
	 * @return The name of the entry.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return Selected status of this item
	 */
	public Boolean getIsSelected()
	{
		return isSelected;
	}
	
	/**
	 * Sets the IsSelected property to the specified value
	 * @param selected The current selected value of the bean
	 */
	public void setIsSelected(boolean selected)
	{
		isSelected = selected;
		isDirty = true;
	}
	
	/**
	 * Returns a value indicating whether the bean has been updated
	 * since it was created
	 * @return True if the bean was updated, false otherwise
	 */
	public boolean getIsDirty()
	{
		return isDirty;
	}
	
	/**
	 * Prepares content values for inserting this bean
	 * 
	 * @param categoryId
	 *            The category id of the category this item is supposed to be
	 *            associated with
	 * @return ContentValues for inserting this bean
	 */
	public ContentValues getInsertContentValues(int categoryId)
	{
		ContentValues values = new ContentValues();
		values.put(EntryColumns.ENTRY_NAME, getName());
		values.put(EntryColumns.CATEGORY_ID, categoryId);
		values.put(EntryColumns.IS_SELECTED, getIsSelected());
		return values;
	}
	
	/**
	 * Prepares content values for updating this bean
	 * 
	 * @return ContentValues for updating this bean
	 */
	public ContentValues getUpdateContentValues()
	{
		ContentValues values = new ContentValues();
		values.put(EntryColumns.IS_SELECTED, getIsSelected());
		return values;
	}
}
