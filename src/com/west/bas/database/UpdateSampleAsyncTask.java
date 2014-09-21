package com.west.bas.database;

import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.ui.RefreshCallback;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * This class was implemented as an AsyncTask because of the
 * interaction with the database that could potential 
 * delay other interaction with the UI.
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class UpdateSampleAsyncTask  extends AsyncTask<Void, Void, Integer>{

	/** Constant to indicate success when updating the database */
	public static final int SUCCESS = 0;
	
	/** Constant to indicate that there was an error updating the database */
	public static final int UPDATE_ERROR = -1;
	
	/** Constant to indicate that there were not enough samples to replace one that was rejected */
	public static final int INSUFFICIENT_SAMPLES_ERROR = -2;
	
	/** Application context (used to identify and get a handle to the database) */
	private Context mContext;
	
	/** Handle to the database helper class */
	private SampleDatabaseHelper mDbHelper;
	
	/** A unique reference to the sample that is being updated */
	private int mSampleID;
	
	/** The status to which the sample point should be updated */
	private SampleDatabaseHelper.Status mStatus;
	
	/** Comments to annotate the database modification (associated with
	 * the sample that is being modified. */
	private String mComment;
	
	/** A callback to request a display refresh on the UI thread */
	private RefreshCallback mRefreshCallback;
	
	/** The name of the study to which the sample that is being 
	 * updated belongs. */
	private String mStudyName;	
	
	//TODO reuse the UpdateTask by initializing with the study and then
	// providing parameters to each call to execute.
	/**
	 * Each update is currently conducted with a separate UpdateTask
	 * object.  The full details must be provided to initialize the task.
	 * @param c application context
	 * @param studyName name of the study that contains the sample to update
	 * @param id unique id of the sample
	 * @param status new status of the sample
	 * @param comment narrative to annotate the update
	 * @param refreshCallback callback to post a refresh on the UI thread
	 */
	public UpdateSampleAsyncTask(Context c, String studyName, int id, 
			SampleDatabaseHelper.Status status, 
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
		Log.d("Update","started update sample async task");
		mDbHelper = SampleDatabaseHelper.getInstance(mContext);
		ContentValues values = new ContentValues();
		values.put(SampleInfo.COLUMN_NAME_STATUS, mStatus.toString());
		values.put(SampleInfo.COLUMN_NAME_COMMENT, mComment);
		if(mDbHelper.update(values,mSampleID)<0){
			return UPDATE_ERROR;
		}else{
			// if rejecting a sample, change an oversample to a sample
			if(mStatus==SampleDatabaseHelper.Status.REJECT){
				return mDbHelper.makeSample(mStudyName);
			}else{
				return SUCCESS;
			}
		}
	}
	
	@Override
	public void onPostExecute(Integer i){
		String message = "";
		switch(i){
		case UPDATE_ERROR:
			message = "ERROR: Unable to update id "+mSampleID;
			break;
		case INSUFFICIENT_SAMPLES_ERROR:
			message = "ERROR: Insufficient number of oversamples to accommodate rejection";
			break;
		case SUCCESS:
			message = "Updated sample "+mSampleID;
			break;
		}
		if(mRefreshCallback!=null) mRefreshCallback.onTaskComplete(message);
		Log.d("Update","completed updated sample async task");
	}
	

}
