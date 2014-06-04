package com.jakubkaleta.checklist.DataAccess.services;

/**
 * This enumeration lists possible modes of importing new lists
 * 
 * @author Jakub Kaleta 
 */
public enum ImportDuplicateHandling
{
	
	/**
	 * If the import process finds an existing list with the same name, 
	 * it will merge the imported list into the existing one.  
	 */
	MergeExistingAndImportedLists,		
	
	/**
	 * Pick this option to not try to merge lists, but rather import
	 * the entire new list, and if there has already been a list 
	 * with the same name defined, rename the imported list to something else.
	 */
	RenameImportedList
}
