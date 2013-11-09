package com.west.bas;

import java.io.File;

import android.os.AsyncTask;

public class ReadStudyArea extends AsyncTask<Void, Void, StudyArea> {

	private String mFilename;
	private String mStudyName;
	private ReadFileCallback mCallback;
	
	public ReadStudyArea(String filename, String studyName, ReadFileCallback callback){
		mFilename = filename;
		mStudyName = studyName;
		mCallback = callback;
	}
	
	
	@Override
	protected StudyArea doInBackground(Void... params) {
		
		File studyAreaSHP = new File(mFilename);
		if(studyAreaSHP.exists()){	
			if(mFilename.endsWith(".shp")){
				return new StudyArea(studyAreaSHP, mFilename, mStudyName);
			}else{
				return new StudyArea("Filename must indicate a shapefile (extension .shp)");
			}
		}else{
			return new StudyArea("File doesn't exist on the device: "+mFilename);
		}
	}
	
	@Override
	public void onPostExecute(StudyArea studyArea){
		if(mCallback!=null) mCallback.onTaskComplete(studyArea);
	}

}

