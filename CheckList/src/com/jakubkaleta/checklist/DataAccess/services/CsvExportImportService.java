package com.jakubkaleta.checklist.DataAccess.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.tables.ActivityColumns;
import com.jakubkaleta.checklist.DataAccess.tables.CategoryColumns;
import com.jakubkaleta.checklist.DataAccess.tables.EntryColumns;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Contains methods that provide functionality for csv export and import into
 * the application
 * 
 * @author Jakub Kaleta
 */
public class CsvExportImportService {
	private ContentResolver contentResolver;
	private ImportEntryProcessor entryProcessor;
	private DataAccessService dataAccessService;

	final String[] ALL_ITEMS_PROJECTION = new String[] { EntryColumns._ID, EntryColumns.ENTRY_NAME, ActivityColumns.ACTIVITY_NAME,
			CategoryColumns.CATEGORY_NAME, EntryColumns.IS_SELECTED };

	/**
	 * Default constructor
	 * 
	 * @param resolver
	 *            ContentResolver that this class uses to query for data
	 */
	public CsvExportImportService(ContentResolver resolver) {
		contentResolver = resolver;
		entryProcessor = new ImportEntryProcessor(resolver);
		dataAccessService = new DataAccessService(resolver);
	}

	/**
	 * This method performs an export of the entire database to the default
	 * location and writes it as a csv file.
	 * 
	 * @param fileName
	 *            The expected name of the file to which the database is to be
	 *            exported.
	 * 
	 * @return True if writing to file succeeded. False if it failed
	 */
	public Boolean getAllItemsFromAllListsAsCsvData(String fileName) {
		FileWriter fWriter = null;
		CSVWriter writer = null;
		try {
			File path = Environment.getExternalStorageDirectory();

			if (path.exists()) {
				Log.d(getClass().getSimpleName(), "Path exists: " + path.toString());

				// create folder structure for the export data:
				File folder = new File(path, "ReverseChecklist");
				folder.mkdir();
				folder = new File(folder, "DatabaseExport");
				folder.mkdir();

				// now create a new file for writing
				File databaseExportFile;
				databaseExportFile = new File(folder, fileName);
				if (!databaseExportFile.exists()) {
					Log.d(getClass().getSimpleName(), "Creating a new database export file, because it was not found: "
							+ databaseExportFile.toString());
					databaseExportFile.createNewFile();
				}

				fWriter = new FileWriter(databaseExportFile);

				writer = new CSVWriter(fWriter, ',');

				// get all items from the entries table,
				String sortOrder = ActivityColumns.ACTIVITY_NAME + ", " + CategoryColumns.CATEGORY_NAME + "," + EntryColumns.ENTRY_NAME;

				Cursor mCursor = contentResolver.query(EntryColumns.CONTENT_URI, ALL_ITEMS_PROJECTION, null, null, sortOrder);

				String[] items = new String[mCursor.getCount()];

				if (!mCursor.isAfterLast()) {
					mCursor.moveToFirst();
					while (!mCursor.isAfterLast()) {
						String activityName = mCursor.getString(mCursor.getColumnIndex(ActivityColumns.ACTIVITY_NAME));
						String categoryName = mCursor.getString(mCursor.getColumnIndex(CategoryColumns.CATEGORY_NAME));
						String entryName = mCursor.getString(mCursor.getColumnIndex(EntryColumns.ENTRY_NAME));
						String isSelected = mCursor.getInt(mCursor.getColumnIndex(EntryColumns.IS_SELECTED)) > 0 ? "true" : "false";

						items[mCursor.getPosition()] = activityName + ";" + categoryName + ";" + entryName + ";" + isSelected;

						mCursor.moveToNext();
					}
				}

				mCursor.close();

				// feed in your array (or convert your data to an array)
				for (int i = 0; i < items.length; i++) {
					String[] entries = items[i].split(";");
					writer.writeNext(entries);
				}
			} else {
				Log.d(getClass().getSimpleName(), "Path does not exist: " + path.toString());
				return false;
			}
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "Writing to CSV file failed with: " + e.toString());
			return false;
		} finally {
			if (fWriter != null) {
				try {
					fWriter.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Closing file stream failed with: " + e.toString());
				}
			}

			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Closing file stream failed with: " + e.toString());
				}
			}
		}

		return true;

	}

	/**
	 * Validates the input parameter and, if the input file is valid, performs
	 * import from it to the database.
	 * 
	 * @param dataUri
	 *            The uri of the resource to import
	 * @param handlingDuplicates
	 *            Describes how to handle duplicate lists
	 * @return ImportResult enum value describing the outcome of import
	 */
	public ImportResult importDataFromCsvFile(Uri dataUri, ImportDuplicateHandling handlingDuplicates) {
		CSVReader csvreader = null;
		InputStreamReader reader = null;

		try {
			InputStream stream = contentResolver.openInputStream(dataUri);
			reader = new InputStreamReader(stream);
			csvreader = new CSVReader(reader);

			List<String[]> lines = csvreader.readAll();

			// if the file was empty, return ImportFailedNothingToImport
			if (lines.isEmpty()) {
				Log.d(getClass().getSimpleName(), "importDataFromCsvFile: the file is empty");
				return ImportResult.ImportFailedNothingToImport;
			}

			// HERE Is where the magic happens!
			// 1. Validate if all items in the list have at least 3 fields.
			// Return ImportFailedInvalidFileStructure if they don't
			for (String[] fields : lines) {
				Log.d(getClass().getSimpleName(), "importDataFromCsvFile: validating lines from file");

				if (fields == null)
					continue;

				if (fields.length < 3) {
					Log.d(getClass().getSimpleName(), "importDataFromCsvFile: file not formatted correctly");
					return ImportResult.ImportFailedInvalidFileStructure;
				}

				for (int i = 0; i < 3; i++) {
					if (fields[i] == null || fields[i].trim().equalsIgnoreCase("")) {
						Log.d(getClass().getSimpleName(), "importDataFromCsvFile: file not formatted correctly");
						return ImportResult.ImportFailedInvalidFileStructure;
					}
				}

				// validate that if the fourth field exists, it is parsable as
				// boolean
				if (fields.length >= 4) {
					if (fields[3] == null || fields[3].trim().equalsIgnoreCase("")) {
						Log.d(getClass().getSimpleName(), "importDataFromCsvFile: file not formatted correctly");
						return ImportResult.ImportFailedInvalidFileStructure;
					}
				}
			}

			// 2. Create Activity, Category and Entry beans. Structure them
			// Insert beans and complete import.
			ActivitiesDataSource dataSource = entryProcessor.processDataImport(lines, handlingDuplicates);

			dataAccessService.updateExistingItems(dataSource);
			dataAccessService.insertNewItems(dataSource.getNewActivities());
			
			
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Reading data from file failed with: " + e.toString());

			return ImportResult.ImportFailedOtherError;
		} finally {
			try {
				if(reader!= null)
					reader.close();
				if(csvreader != null)
					csvreader.close();
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Closing file stream failed with: " + e.toString());
			}
		}

		return ImportResult.ImportSucceeded;
	}
}
