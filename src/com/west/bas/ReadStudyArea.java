package com.west.bas;

import java.io.File;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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
		if(!studyAreaSHP.exists()) return null;
		if(!mFilename.endsWith(".shp")){
			Log.d("ReadSHP", "Filename does have have .shp extension");
			return null;
		}

//		FileDataStore store;
//		try {
//			store = FileDataStoreFinder.getDataStore(studyAreaSHP);
//		} catch (IOException e) {
//			Log.d("ReadSHP",e.getMessage());
//			return false;
//		}
//		
//		try {
//			featureSource = store.getFeatureSource();
//			Log.d("ReadSHP","Read features!");
//			return true;
//		} catch (IOException e) {
//			Log.d("ReadSHP",e.getMessage());
//			return false;
//		}

//		try {
//		FeatureCollection<SimpleFeatureType, SimpleFeature> fc = featureSource.getFeatures();
//		FeatureType type = fc.getSchema();
//		SimpleFeatureIterator iter = (SimpleFeatureIterator) fc.features();
//		try {
//	        while( iter.hasNext() ){
//	            SimpleFeature feature = iter.next();
//	            // process feature
//	            Log.d("ReadSHP",feature.toString());
//	        }
//	    }
//	    finally {
//	        iter.close();
//	    }
//	} catch (IOException e) {
//		Log.d("ReadSHP",e.getMessage());
//		return false;
//	}

		StudyArea studyArea = new StudyArea(mFilename, mStudyName);
		Log.d("ReadStudyArea","Need to read shapefile!");
		return studyArea;
		
	}
	
	@Override
	public void onPostExecute(StudyArea studyArea){
		if(mCallback!=null) mCallback.onTaskComplete(studyArea);
	}

}

