package com.west.bas.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

// TODO accept a destination (string?  other...?) and write the database to it
// HTTP POST: http://stackoverflow.com/questions/15968165/uploading-files-and-other-data-from-android-through-http-post-request
// http://stackoverflow.com/questions/6042453/android-utility-to-send-sqlite-db-to-sever/6042762#6042762
// http://stackoverflow.com/questions/1610903/exporting-sqlite-data-from-an-android-application
// http://stackoverflow.com/questions/13261814/simple-import-export-option-of-a-sqlite-database

/** 
 * Data from the BAS sample can be exported to support analysis or review on
 * other devices.  
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class ExportDataAsyncTask extends AsyncTask<String, Void, Boolean> {
	
	private SampleDatabaseHelper mDbHelper;
	private String mFilename;
	private boolean mExportAll;
	private String mStudyName;
	
	public ExportDataAsyncTask(Context c, String filename, boolean b, String studyName){
		mDbHelper = new SampleDatabaseHelper(c);
		mFilename = filename;
		mExportAll = b;
		mStudyName = studyName;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		Log.d("Export","started export task");
		
		File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(sdPath+"/"+mFilename));
			if(mExportAll){
				writer.append(mDbHelper.allStudiesToString());
			}else{
				writer.append(mDbHelper.singleStudyToString(mStudyName));
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	@Override
	public void onPostExecute(Boolean b){
		Log.d("Export","completed export task");
	}

}
