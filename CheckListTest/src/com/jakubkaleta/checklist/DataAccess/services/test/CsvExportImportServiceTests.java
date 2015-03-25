package com.jakubkaleta.checklist.DataAccess.services.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.test.ProviderTestCase;
import android.test.mock.MockContentResolver;

import com.jakubkaleta.checklist.DataAccess.ChecklistDataProvider;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.services.CsvExportImportService;
import com.jakubkaleta.checklist.DataAccess.services.DataAccessService;
import com.jakubkaleta.checklist.DataAccess.services.ImportDuplicateHandling;
import com.jakubkaleta.checklist.DataAccess.services.ImportResult;
import com.jakubkaleta.checklist.DataAccess.test.DataProviderTestHelper;

/**
 * @author Jakub Kaleta Contains tests for methods in the DataAccessService
 *         class.
 */
@SuppressWarnings("deprecation")
public class CsvExportImportServiceTests extends ProviderTestCase<ChecklistDataProvider> {
	private ChecklistDataProvider dataProvider;
	private DataProviderTestHelper helper;
	private CsvExportImportService importService;
	private DataAccessService dataAccessService;
	private final static String authority = "com.jakubkaleta.checklist.DataAccess.Activity;com.jakubkaleta.checklist.DataAccess.Category;com.jakubkaleta.checklist.DataAccess.Entry;com.jakubkaleta.checklist.DataAccess.ApplicationState;com.jakubkaleta.checklist.DataAccess.Configuration";

	/**
	 * Main constructor
	 */
	public CsvExportImportServiceTests() {
		super(ChecklistDataProvider.class, authority);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dataProvider = this.getProvider();
		helper = new DataProviderTestHelper(dataProvider);
		MockContentResolver mockResolver = new MockContentResolver();
		String[] authorities = authority.split(";");
		for (int i = 0; i < authorities.length; i++)
			mockResolver.addProvider(authorities[i], dataProvider);
		importService = new CsvExportImportService(mockResolver);
		dataAccessService = new DataAccessService(mockResolver);

		AssetManager assets = this.getInstrumentation().getContext().getAssets();
		String[] fileNames = assets.list("testImportFiles");
		for (String name : fileNames) {
			// copy all test files from assets to a physical location on the
			// device
			// to better recreate a real-life scenario where files have
			// locations
			// and paths
			copyFileFromAssetsToExternalStorage(assets, new File("testImportFiles", name).getPath());
		}

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * For testing the getAllItemsFromAllListsAsCsvData method
	 */
	public void testGetAllItemsFromAllListsAsCsvData() {
		// arrange
		// let's add a new list, category, and a few items to the new list
		Uri listUri = helper.insertNewList("testlist");
		int listId = Integer.parseInt(listUri.getPathSegments().get(1));
		assertEquals(2, listId);

		Uri categoryUri = helper.insertNewCategory(listId, "dudus", 1);
		int categoryId = Integer.parseInt(categoryUri.getPathSegments().get(1));
		helper.insertNewItem(categoryId, "item1", false);
		helper.insertNewItem(categoryId, "item2", false);
		helper.insertNewItem(categoryId, "item3", true);
		helper.insertNewItem(categoryId, "item4", false);
		helper.insertNewItem(categoryId, "item5", true);
		helper.insertNewItem(categoryId, "item6", false);

		String fileName = "exportFile.csv";

		// Act & Assert
		Boolean methodExecutedSuccessfully = importService.getAllItemsFromAllListsAsCsvData(fileName);

		File databaseExportFile = new File(Environment.getExternalStorageDirectory() + "/ReverseChecklist/DatabaseExport", fileName);
		Boolean fileWritten = databaseExportFile.exists();

		// clean up before the assertions
		if (fileWritten)
			databaseExportFile.delete();

		assertTrue(methodExecutedSuccessfully);
		assertTrue(fileWritten);

		// verify that the file has been written and exists
	}

	/**
	 * For testing the case when selected file is not a cvs file.
	 */
	public void testImportData_InvalidFileExtension() {
		// Arrange
		String fileName = "file.asd";

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(fileName), ImportDuplicateHandling.RenameImportedList);

		// Assert
		assertEquals(ImportResult.ImportFailedInvalidFileName, result);
	}

	/**
	 * For testing the case when selected file does not exist.
	 */
	public void testImportData_FileDoesNotExist() {
		// Arrange
		String fileName = "file.csv";

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(fileName), ImportDuplicateHandling.RenameImportedList);

		// Assert
		assertEquals(ImportResult.ImportFailedInvalidFileName, result);
	}

	/**
	 * For testing the case when selected file does not exist.
	 */
	public void testImportData_FileExistsAndIsEmpty() {
		// Arrange
		File sourceFileName = new File("testImportFiles", "import_csv_test_empty_file.csv");

		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File outFile = new File(rootPath, sourceFileName.getPath());

		assertTrue(outFile.exists());

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(outFile.getAbsolutePath()), ImportDuplicateHandling.RenameImportedList);

		// Assert
		assertEquals(ImportResult.ImportFailedInvalidFileStructure, result);
	}

	/**
	 * For testing the case when selected file is not properly formatted.
	 */
	public void testImportData_FileExistsAndIsFormattedIncorrectly() {
		// Arrange
		File sourceFileName = new File("testImportFiles", "import_csv_test_invalid_file_structure.csv");

		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File outFile = new File(rootPath, sourceFileName.getPath());

		assertTrue(outFile.exists());

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(outFile.getAbsolutePath()), ImportDuplicateHandling.RenameImportedList);

		// Assert
		assertEquals(ImportResult.ImportFailedInvalidFileStructure, result);
	}

	/**
	 * For testing the case when selected file is properly imported.
	 */
	public void testImportData_FileIsSuccesfullyImported() {
		// Arrange
		File sourceFileName = new File("testImportFiles", "import_csv_test_valid_file.csv");

		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File outFile = new File(rootPath, sourceFileName.getPath());

		assertTrue(outFile.exists());

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(outFile.getAbsolutePath()), ImportDuplicateHandling.RenameImportedList);

		// Assert
		assertEquals(ImportResult.ImportSucceeded, result);
	}

	/**
	 * For testing the case when selected file is properly imported with
	 * selection information
	 */
	public void testImportData_FileIsSuccesfullyImported_SelectionPersisted() {
		// Arrange
		File sourceFileName = new File("testImportFiles", "import_csv_test_valid_file_with_selection.csv");

		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File outFile = new File(rootPath, sourceFileName.getPath());

		assertTrue(outFile.exists());

		// Act
		ImportResult result = importService.importDataFromCsvFile(Uri.parse(outFile.getAbsolutePath()), ImportDuplicateHandling.RenameImportedList);
		// get all items that were imported
		ActivitiesDataSource allLists = dataAccessService.getActivitiesWithChildren(new Long[] {}, false);
		ActivityBean testList = allLists.getActivity("test list");
		ActivityBean otherList = allLists.getActivity("other list");

		// Assert
		assertEquals(ImportResult.ImportSucceeded, result);
		// check selection status
		assertTrue(testList.getCategory("test category").getEntries().get(0).getIsSelected());
		assertFalse(testList.getCategory("test category2").getEntries().get(0).getIsSelected());
		assertTrue(otherList.getCategories().get(0).getEntries().get(0).getIsSelected());
		assertTrue(otherList.getCategories().get(0).getEntries().get(1).getIsSelected());

		// now part II of the test.
		// verify that the selection is properly updated in existing lists
		// when merge lists is selected
		allLists.updateSelectionOfAllItems(false);
		result = importService.importDataFromCsvFile(Uri.parse(outFile.getAbsolutePath()), ImportDuplicateHandling.MergeExistingAndImportedLists);
		// get all items that were imported
		allLists = dataAccessService.getActivitiesWithChildren(new Long[] {}, false);
		testList = allLists.getActivity("test list");
		otherList = allLists.getActivity("other list");

		// Assert
		assertEquals(ImportResult.ImportSucceeded, result);
		// check selection status
		assertTrue(testList.getCategory("test category").getEntries().get(0).getIsSelected());
		assertFalse(testList.getCategory("test category2").getEntries().get(0).getIsSelected());
		assertFalse(otherList.getCategories().get(0).getEntries().get(0).getIsSelected());
		assertTrue(otherList.getCategories().get(0).getEntries().get(1).getIsSelected());

	}

	private String copyFileFromAssetsToExternalStorage(AssetManager assets, String sourceFileName) throws IOException {
		InputStream in = assets.open(sourceFileName);

		String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

		File outFile = new File(rootPath, sourceFileName);
		if (outFile.exists()) {
			outFile.delete();
		} else {
			outFile.mkdirs();
		}

		outFile.createNewFile();

		OutputStream out = new FileOutputStream(outFile);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();

		return outFile.getAbsolutePath();
	}

}
