package com.jakubkaleta.checklist.DataAccess.services;

/**
 * Enumeration describing all possible outcomes from the import process
 * 
 * @author Jakub Kaleta
 */
public enum ImportResult
{
	/**
	 * Everything went ok
	 */
	ImportSucceeded,

	/**
	 * The file name was incorrect or file not found
	 */
	ImportFailedInvalidFileName,

	/**
	 * File was found, but it wasn't a valid csv file, or the csv was not
	 * formatted correctly
	 */
	ImportFailedInvalidFileStructure,

	/**
	 * The file was empty
	 */
	ImportFailedNothingToImport,

	/**
	 * Import failed for unknown reason
	 */
	ImportFailedOtherError
}
