package west.sample.bas;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LoadSample extends AsyncTask<Void, Void, Boolean> {

	/* handle to the database from which samples will be read */
	private SampleDatabaseHelper db;
	
	
	public LoadSample(Context c, String studyName){
		// Connect to the database where the samples are stored
		db = new SampleDatabaseHelper(c);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		//db.getReadableDatabase();
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean success){
		if(success){
			//adapter.notifyDataSetChanged();
		}
	}

}
