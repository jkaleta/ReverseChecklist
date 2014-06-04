package com.jakubkaleta.checklist.DataAccess.services;

import java.util.HashMap;
import java.util.List;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;

import android.content.ContentResolver;
import android.util.Log;

/**
 * Contains methods that provide functionality for processing import entries
 * that have already been read from a file into memory
 * 
 * @author Jakub Kaleta
 */
public class ImportEntryProcessor
{
	private DataAccessService dataAccessService;

	/**
	 * Default constructor
	 * 
	 * @param resolver
	 *            ContentResolver that this class uses to query for data
	 */
	public ImportEntryProcessor(ContentResolver resolver)
	{
		dataAccessService = new DataAccessService(resolver);
	}

	/**
	 * Processes all lines, adding items to the list of all activities. Handles
	 * duplicates.
	 * 
	 * @param lines
	 *            The lines to process
	 * @param handlingDuplicates
	 *            Describes how to handle duplicates.
	 * @return ActivitiesDataSource containing merged existing and new data.
	 */
	public ActivitiesDataSource processDataImport(List<String[]> lines,
			ImportDuplicateHandling handlingDuplicates)
	{
		Log.d(getClass().getSimpleName(),
				"processDataImport: file read succesfully. Processing import.");

		// to properly handle duplicate lists being imported,
		// we need to get all list names from the db

		ActivitiesDataSource allData = dataAccessService.getActivitiesWithChildren(null, true);

		// In case the selected mode is to rename imported lists,
		// this hashmap will map the names from the list
		// to the renamed names
		HashMap<String, String> oldToNewNameMapping = new HashMap<String, String>();

		String listName = "";
		String categoryName = "";
		String entryName = "";
		boolean isSelected = false;
		
		ActivityBean aBean = null;
		CategoryBean cBean = null;
		EntryBean eBean = null;
		for (String[] fields : lines)
		{
			listName = fields[0];
			categoryName = fields[1];
			entryName = fields[2];			
			
			if(fields.length >= 4)
				isSelected = Boolean.parseBoolean(fields[3]);
			
			Log.d(getClass().getSimpleName(), "processDataImport: processing entry " + listName
					+ " " + categoryName + " " + entryName);

			// rename the list if necessary
			if (handlingDuplicates == ImportDuplicateHandling.RenameImportedList)
			{
				if (oldToNewNameMapping.containsKey(listName.toLowerCase()))
				{
					listName = oldToNewNameMapping.get(listName.toLowerCase());
				}
				else
				{
					String newName = verifyNameAvailabilityAndFindNewNameForActivity(listName);

					if (!newName.equalsIgnoreCase(listName))
						oldToNewNameMapping.put(listName.toLowerCase(), newName);

					listName = newName;
				}
			}

			aBean = allData.getActivity(listName);
			if (aBean == null)
			{
				aBean = new ActivityBean(0, listName);
				allData.addActivity(aBean);
			}

			cBean = aBean.getCategory(categoryName);
			if (cBean == null)
			{
				cBean = new CategoryBean(0, categoryName, CategorySortOrder.AlphabeticallyAsc);
				aBean.addCategory(cBean);
			}

			eBean = new EntryBean(0, entryName, isSelected);
			cBean.addEntry(eBean);
		}

		return allData;
	}

	private String verifyNameAvailabilityAndFindNewNameForActivity(String suggestedName)
	{
		String newName = suggestedName;

		while (!dataAccessService.checkActivityNameAvailability(newName, 0))
		{
			if (newName.endsWith("_new"))
			{
				// when the new name has already been renamed, but the new name
				// is still
				// unavailable, we'll have to append _1 to it
				newName = newName + "_1";
			}
			else
			{
				// if the new name conains the string "_new_, it means it has
				// already been renamed
				// at least once.
				// We'll have to extract the last number appended to the list,
				// and increment it
				// In case the suffix is not a number, we'll append _new to it.
				if (newName.contains("_new_"))
				{
					String suffix = newName.substring(newName.lastIndexOf("_new_") + 5);
					try
					{
						int suffixNumber = Integer.parseInt(suffix);
						newName = newName.substring(0, newName.lastIndexOf("_new_") + 5)
								+ ++suffixNumber;
					}
					catch (NumberFormatException e)
					{
						newName = newName + "_new";
					}
				}
				else
				{
					// in the simple case when the name was not yet renamed,
					// just append _new to it
					// this will happen 99% of the time, and will break the
					// loop.
					newName = newName + "_new";
				}
			}
		}

		return newName;
	}

}
