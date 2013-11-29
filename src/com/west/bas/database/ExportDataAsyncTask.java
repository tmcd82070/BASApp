package com.west.bas.database;

import android.content.Context;
import android.os.AsyncTask;
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
	
	SampleDatabaseHelper mDbHelper;
	
	public ExportDataAsyncTask(Context c){
		mDbHelper = new SampleDatabaseHelper(c);
	}

	@Override
	protected Boolean doInBackground(String... params) {

		Log.d("dbDump",mDbHelper.prettyPrint());
		
		
		return null;
	}

}
