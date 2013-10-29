package west.sample.bas;

import west.sample.bas.SampleDatabaseHelper.SampleInfo;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class UpdateTask  extends AsyncTask<Void, Void, Integer>{

	public static final int SUCCESS = 0;
	public static final int UPDATE_ERROR = -1;
	public static final int INSUFFICIENT_SAMPLES_ERROR = -2;
	
	private Context mContext;
	private SampleDatabaseHelper mDbHelper;
	private int mSampleID;
	private SamplePoint.Status mStatus;
	private String mComment;
	private RefreshCallback mRefreshCallback;
	private String mStudyName;
	
	public UpdateTask(Context c, String studyName, int id, 
			SamplePoint.Status status, 
			String comment, RefreshCallback refreshCallback){
		mContext = c;
		mStudyName = studyName;
		mSampleID = id;
		mStatus = status;
		mComment = comment;
		mRefreshCallback = refreshCallback;
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		mDbHelper = new SampleDatabaseHelper(mContext);
		ContentValues values = new ContentValues();
		values.put(SampleInfo.COLUMN_NAME_STATUS, mStatus.toString());
		values.put(SampleInfo.COLUMN_NAME_COMMENT, mComment);
		if(mDbHelper.update(values,mSampleID)<0){
			return UPDATE_ERROR;
		}else{
			// if rejecting a sample, change an oversample to a sample
			if(mStatus==SamplePoint.Status.REJECT){
				return mDbHelper.makeSample(mStudyName);
			}else{
				return SUCCESS;
			}
		}
	}
	
	public void onPostExecute(Integer i){
		int length = Toast.LENGTH_LONG;
		String message = "";
		switch(i){
		case UPDATE_ERROR:
			message = "ERROR: Unable to update id "+mSampleID;
			break;
		case INSUFFICIENT_SAMPLES_ERROR:
			message = "ERROR: Insufficient number of oversamples to accommodate rejection";
			break;
		case SUCCESS:
			length = Toast.LENGTH_SHORT;
			message = "Success";
			break;
		}
		Toast.makeText(mContext, message, length).show();
		mRefreshCallback.onTaskComplete();
	}
	

}
