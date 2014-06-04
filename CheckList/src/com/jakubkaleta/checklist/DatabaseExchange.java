package com.jakubkaleta.checklist;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jakubkaleta.checklist.DataAccess.services.CsvExportImportService;
import com.jakubkaleta.checklist.DataAccess.services.ImportDuplicateHandling;
import com.jakubkaleta.checklist.DataAccess.services.ImportResult;

/**
 * This activity handles data import and export. Both import and export are from
 * and to a csv file.
 * 
 * @author Jakub Kaleta
 */
public class DatabaseExchange extends Activity
{
	private Resources resources;
	private int PICK_FILE = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database_exchange);

		resources = getResources();

		Button exportAllData = (Button) findViewById(R.id.btn_export_all_data);

		exportAllData.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				new ExportDataTask().execute();
			}
		});

		Button importData = (Button) findViewById(R.id.btn_import_data);

		importData.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{

				Intent intent = new Intent().setType("text/csv").setAction(
						Intent.ACTION_GET_CONTENT)
						.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(
						Intent.createChooser(intent,
								resources.getString(R.string.import_select_file)), PICK_FILE);
			}
		});

		ImageButton exportDataInfo = (ImageButton) findViewById(R.id.btn_export_all_data_help);

		exportDataInfo.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
				dialogBuilder.setTitle(R.string.export_data)
						.setMessage(R.string.export_data_description).setIcon(R.drawable.appicon)
						.setCancelable(true).setNeutralButton(R.string.ok_string, null).show();
			}
		});

		ImageButton importDataInfo = (ImageButton) findViewById(R.id.btn_import_data_help);

		importDataInfo.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
				dialogBuilder.setTitle(R.string.import_data)
						.setMessage(R.string.import_data_description).setIcon(R.drawable.appicon)
						.setCancelable(true).setNeutralButton(R.string.ok_string, null).show();
			}
		});

		ImageButton importDataOption = (ImageButton) findViewById(R.id.rbg_import_mode_selection_help);

		importDataOption.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
				dialogBuilder.setTitle(R.string.import_data_mode)
						.setMessage(R.string.import_data_mode_explanation)
						.setIcon(R.drawable.appicon).setCancelable(true)
						.setNeutralButton(R.string.ok_string, null).show();
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK)
		{
			if (requestCode == PICK_FILE)
			{
				Uri selectedUri = data.getData();
				new ImportDataTask().execute(selectedUri);
			}
		}
	}

	private class ExportDataTask extends AsyncTask<String, Void, Boolean>
	{
		private final ProgressDialog dialog = new ProgressDialog(DatabaseExchange.this);

		// can use UI thread here
		protected void onPreExecute()
		{
			dialog.setMessage(resources.getString(R.string.exporting_data_to_csv));
			dialog.setCancelable(false);
			dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args)
		{

			CsvExportImportService service = new CsvExportImportService(
					DatabaseExchange.this.getContentResolver());
			Date dateNow = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String fileName = "export_" + dateformat.format(dateNow) + ".csv";
			return service.getAllItemsFromAllListsAsCsvData(fileName);
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean result)
		{
			if (this.dialog.isShowing())
			{
				dialog.hide();

				String toastText = result ? resources
						.getString(R.string.exporting_data_to_csv_success) : resources
						.getString(R.string.exporting_data_to_csv_fail);

				Toast toast = Toast.makeText(DatabaseExchange.this, toastText, Toast.LENGTH_SHORT);
				toast.show();

			}
		}
	}

	private class ImportDataTask extends AsyncTask<Uri, Void, ImportResult>
	{
		private final ProgressDialog dialog = new ProgressDialog(DatabaseExchange.this);

		// can use UI thread here
		protected void onPreExecute()
		{
			dialog.setMessage(resources.getString(R.string.importing_data_from_csv));
			dialog.setCancelable(false);
			dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected ImportResult doInBackground(final Uri... args)
		{
			CsvExportImportService service = new CsvExportImportService(
					DatabaseExchange.this.getContentResolver());

			RadioGroup modeSelection = (RadioGroup) findViewById(R.id.rbg_import_mode_selection);

			ImportDuplicateHandling userSelectedDuplicateHandlingMode = modeSelection
					.getCheckedRadioButtonId() == R.id.radio_merge_lists ? ImportDuplicateHandling.MergeExistingAndImportedLists
					: ImportDuplicateHandling.RenameImportedList;

			return service.importDataFromCsvFile(args[0], userSelectedDuplicateHandlingMode);
		}

		// can use UI thread here
		protected void onPostExecute(final ImportResult result)
		{
			if (this.dialog.isShowing())
			{
				dialog.hide();

				String toastText = resources.getString(R.string.importing_data_success);

				switch (result)
				{
				case ImportSucceeded:
					toastText = resources.getString(R.string.importing_data_success);
					break;
				case ImportFailedInvalidFileName:
					toastText = resources.getString(R.string.importing_data_fail_invalid_file_name);
					break;
				case ImportFailedInvalidFileStructure:
					toastText = resources.getString(R.string.importing_data_fail_invalid_structure);
					break;
				case ImportFailedNothingToImport:
					toastText = resources.getString(R.string.importing_data_fail_file_empty);
					break;
				case ImportFailedOtherError:
					toastText = resources.getString(R.string.importing_data_fail_other_error);
					break;
				}

				Toast toast = Toast.makeText(DatabaseExchange.this, toastText, Toast.LENGTH_SHORT);
				toast.show();

			}
		}
	}

}
