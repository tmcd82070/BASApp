package west.sample.bas;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class LoadSample extends AsyncTask<Void, Void, Boolean> {

	/* handle to the database from which samples will be read */
	private Context mContext;
	private String mStudyName;
	private ListView mTable;
	private DetailListAdapter mAdapter;
	
	public LoadSample(Context c, ListView table){
		this.mContext = c;
		this.mStudyName = MainActivity.sCurrentStudy;
		this.mTable = table;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// Connect to the database where the samples are stored
		SampleDatabaseHelper db = new SampleDatabaseHelper(mContext);

//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(c,
//		R.layout.row_details);

//		ArrayList<String> result = db.getStudyDetails(mStudyName);
//		for(String s: result){
//			Log.d("load",s);
//		}
//		if(result.isEmpty()) Log.d("load","Empty!!");
//		
//		//adapter = new DetailListAdapter(layoutInflater, result);
//		mTable.setAdapter(mAdapter);
//
//		return result != null;
		return null;
	}
	
	@Override
	protected void onPostExecute(Boolean success){
		if(success){
			mAdapter.notifyDataSetChanged();
		}else{
			Toast.makeText(mContext, "Error loading "+mStudyName+"!", Toast.LENGTH_LONG).show(); 
		}
	}

}
