package com.west.bas.database;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Definition of the database tables and basic interaction.
 * <br/><br/>
 * 
 * Based on developer.android.com/training/basics/data-storage/databases.html
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class SampleDatabaseHelper extends SQLiteOpenHelper {
	
	/** 
	 * Each generated point is labeled as either a necessary collection point 
	 * (SAMPLE) or is provided as an alternative to a necessary point that gets 
	 * rejected in the field (OVERSAMPLE)
	 */
	public static enum SampleType {
		SAMPLE,OVERSAMPLE;

		public String getString() {
			return this.toString();
		}
	}
	
	/**
	 * Four mutually exclusive status classifications describe the state of 
	 * each point.  Initially all points are either SAMPLE or OVERSAMPLE
	 * (which matches the SampleType).  Any point marked COMPLETE has been
	 * visited and data has been collected.  As collection occurs, some points may
	 * be rejected from the sample.  In such a case, that point is marked as
	 * REJECT and the next point in the sequence that is marked as OVERSAMPLE
	 * is changed to a SAMPLE point that must be included to complete the 
	 * data collection.
	 * 
	 * SAMPLE: 		a point that must be visited to complete the collection
	 * OVERSAMPLE: 	an additional point generated in case others in the 
	 * 				continuous block are rejected in the field
	 * REJECT: 		a point that has been rejected in the field (e.g., 
	 * 				inaccessible due to land ownership or hazardous conditions
	 * COMPLETE: 	a point that has been visited and data was collected
	 */
	public static enum Status {SAMPLE, OVERSAMPLE, REJECT, COLLECTED;

		public boolean matches(String status) {
			return status.equalsIgnoreCase(this.toString());
		}

		public static Status getValueFromString(String status) {
			if(status.equalsIgnoreCase("sample")) return SAMPLE;
			if(status.equalsIgnoreCase("oversample")) return OVERSAMPLE;
			if(status.equalsIgnoreCase("reject")) return REJECT;
			if(status.equalsIgnoreCase("collected")) return COLLECTED;
			return null;
		}
	}	
	
	
	/** Data types in the sample and study data **/
	private static final String DATA_INT = " INTEGER";
	private static final String DATA_FLOAT = " FLOAT(7)";
	private static final String DATA_TEXT = " TEXT";
	private static final String DATA_ENUM_TYPE = " TEXT";	  	//" ENUM('sample','oversample')";
	private static final String DATA_ENUM_STATUS = " TEXT"; 	//" ENUM('sample','oversample','reject','complete')";
	private static final String DATA_DATE = " TIMESTAMP";
	private static final String REQUIRED = " NOT NULL";
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	
	/** Description of the database columns **/
	public static abstract class SampleInfo implements BaseColumns{
		public static final String TABLE_NAME = "bas_sample";
		public static final String COLUMN_NAME_STUDY = "study";
		public static final String COLUMN_NAME_NUMBER = "number";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_X = "x";
		public static final String COLUMN_NAME_Y = "y";
		public static final String COLUMN_NAME_STATUS = "status";
		public static final String COLUMN_NAME_COMMENT = "comment";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
	}
	
	public static abstract class StudyInfo implements BaseColumns{
		public static final String TABLE_NAME = "bas_study";
		public static final String COLUMN_NAME_STUDYNAME = "studyNameID";
		public static final String COLUMN_NAME_SHAPEFILE = "studySHP";
		public static final String COLUMN_NAME_SEED_X = "seedX";
		public static final String COLUMN_NAME_SEED_Y = "seedY";
		public static final String COLUMN_NAME_AUTOREJECT = "numRejected";
	}

	/** String that defines the structure of the samples table within a CREATE command **/
	private static final String CREATE_TABLE_SAMPLES = 
			"CREATE TABLE "+SampleInfo.TABLE_NAME + " ("+
			SampleInfo._ID 					+ DATA_INT 			+ " PRIMARY KEY AUTOINCREMENT,"+
			SampleInfo.COLUMN_NAME_STUDY 	+ DATA_TEXT 		+ REQUIRED + "," + 
			SampleInfo.COLUMN_NAME_NUMBER 	+ DATA_INT 			+ REQUIRED + "," + 
			SampleInfo.COLUMN_NAME_TYPE 	+ DATA_ENUM_TYPE 	+ REQUIRED + "," + 
			SampleInfo.COLUMN_NAME_X 		+ DATA_FLOAT 		+ REQUIRED + "," + 
			SampleInfo.COLUMN_NAME_Y 		+ DATA_FLOAT 		+ REQUIRED + "," + 
			SampleInfo.COLUMN_NAME_STATUS 	+ DATA_ENUM_STATUS 	+ "," + 
			SampleInfo.COLUMN_NAME_COMMENT 	+ DATA_TEXT 		+ "," + 
			SampleInfo.COLUMN_NAME_TIMESTAMP + DATA_DATE 		+ ")";
	
	/** String that defines the structure of the studies table within a CREATE command **/
	private static final String CREATE_TABLE_STUDIES = 
			"CREATE TABLE "+StudyInfo.TABLE_NAME + " ("+
			StudyInfo.COLUMN_NAME_STUDYNAME 		+ DATA_TEXT 	+ " PRIMARY KEY, " + 
			StudyInfo.COLUMN_NAME_SHAPEFILE		 	+ DATA_TEXT 	+ REQUIRED + "," + 
			StudyInfo.COLUMN_NAME_SEED_X		 	+ DATA_FLOAT 	+ REQUIRED + "," + 
			StudyInfo.COLUMN_NAME_SEED_Y		 	+ DATA_FLOAT 	+ REQUIRED + "," + 
			StudyInfo.COLUMN_NAME_AUTOREJECT	 	+ DATA_INT	 	+ ")";
	
	private static final String DELETE_TABLE_SAMPLES = 
			"DROP TABLE IF EXISTS "+SampleInfo.TABLE_NAME;

	private static final String DELETE_TABLE_STUDIES = 
			"DROP TABLE IF EXISTS "+StudyInfo.TABLE_NAME;

	// V1: initial implementation
	// V2: updated to include time stamp when creating an entry
	// V3: created a field to indicate which study each point is associated with
	// V4: added a field to store the name of the shapefile
	// V5: created a second table to hold study information
	public static final int DATABASE_VERSION = 5;
	public static final String DATABASE_NAME = "BAS.db";
	
	private SQLiteDatabase mDatabase;
	
	/* ensure that the database helper is a singleton */
	private static SampleDatabaseHelper sDBHelper;
	private SampleDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mDatabase = getWritableDatabase();
	}
	public static SampleDatabaseHelper getInstance(Context context){
		if(sDBHelper==null){
			sDBHelper = new SampleDatabaseHelper(context.getApplicationContext());
		}
		return sDBHelper;
	}
	


	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_STUDIES);
		db.execSQL(CREATE_TABLE_SAMPLES);
	}

	//TODO actually deal with changes in the database - don't just delete the old one
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO write the previous data to file
		//writeToFile(db);
//		new ExportDataAsyncTask(context);
		// remove all data from the database
		db.execSQL(DELETE_TABLE_SAMPLES);
		db.execSQL(CREATE_TABLE_SAMPLES);

		// Create the tables to hold new info
		db.execSQL(DELETE_TABLE_STUDIES);
		db.execSQL(CREATE_TABLE_STUDIES);
	}

 	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    
	/** New samples must specify their location, order, and type.  The initial
	 * status is implied from the type.
	 * @param order integer representing this points location within the generated sequence
	 * @param type indication of whether this point is in the sample set or provided as an oversample
	 * @param x x coordinate of the point
	 * @param y y coordinate of the point
	 * @return primary key value (_ID) of the new row
	 */
	public long addValue(String study, int number, SampleType type, double x, double y){
		ContentValues data = new ContentValues();
		data.put(SampleInfo.COLUMN_NAME_STUDY, study);
		data.put(SampleInfo.COLUMN_NAME_NUMBER, number);
		data.put(SampleInfo.COLUMN_NAME_TYPE, type.getString());
		data.put(SampleInfo.COLUMN_NAME_X, x);
		data.put(SampleInfo.COLUMN_NAME_Y, y);
		data.put(SampleInfo.COLUMN_NAME_TIMESTAMP, format.format(new Date(System.currentTimeMillis())));
		// Initially the status is equal to the type
		data.put(SampleInfo.COLUMN_NAME_STATUS, type.getString());

		return mDatabase.insert(SampleInfo.TABLE_NAME, null, data);
	}
	
	public long addStudy(String studyName, String filenameSHP, int seedX, int seedY){
		ContentValues data = new ContentValues();
		data.put(StudyInfo.COLUMN_NAME_STUDYNAME, studyName);
		data.put(StudyInfo.COLUMN_NAME_SHAPEFILE, filenameSHP);
		data.put(StudyInfo.COLUMN_NAME_SEED_X, seedX);
		data.put(StudyInfo.COLUMN_NAME_SEED_Y, seedY);
		return mDatabase.insert(StudyInfo.TABLE_NAME, null, data);
	}
	
	public long updateStudy(String studyName, int autoRejected){
		ContentValues values = new ContentValues();
		values.put(StudyInfo.COLUMN_NAME_AUTOREJECT, autoRejected);
		
		int result = 0;
		try{
			result = getWritableDatabase().update(StudyInfo.TABLE_NAME, values, 
					StudyInfo.COLUMN_NAME_STUDYNAME+"='"+studyName+"'",null);
		}catch(SQLiteException e){
			Log.d("database","Error getting writable database for update");
			return UpdateSampleAsyncTask.UPDATE_ERROR;
		}
		if(result!=1){
			Log.d("database","Error updating, "+result+" rows affected (expected 1)");
			return UpdateSampleAsyncTask.UPDATE_ERROR;
		}
		return UpdateSampleAsyncTask.SUCCESS;		
	}

	public ArrayList<String> getListOfStudies(){
		String query = "SELECT DISTINCT "+SampleInfo.COLUMN_NAME_STUDY+
				" FROM "+SampleInfo.TABLE_NAME;
		return convertCursorToStrings(query,SampleInfo.COLUMN_NAME_STUDY);
	}
	
	public Cursor getSamplePointsForStudy(String studyName){
		String query = "SELECT * FROM "+SampleInfo.TABLE_NAME+
				" WHERE "+SampleInfo.COLUMN_NAME_STUDY+"='"+studyName+"'";
		if(studyName==null) return null;
		return getReadableDatabase().rawQuery(query, null);
	}
	
	public String getSHPFilename(String studyName) {
		if(studyName==null) return null;
		String query = "SELECT "+StudyInfo.COLUMN_NAME_SHAPEFILE+" FROM "+StudyInfo.TABLE_NAME+
				" WHERE "+StudyInfo.COLUMN_NAME_STUDYNAME+"='"+studyName+"'";
		Cursor cursor = mDatabase.rawQuery(query, null);
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(StudyInfo.COLUMN_NAME_SHAPEFILE);
			String shapefileName = cursor.getString(columnIndex);
			cursor.close();
			return shapefileName;
		}else{
			cursor.close();
			Log.d("Database","No results from query: "+query);
			return null;
		}
	}


	
	private ArrayList<String> convertCursorToStrings(String query, String columnName){
		Cursor cursor = null;
		try{
			cursor = mDatabase.rawQuery(query, null);
			int numRows = cursor.getCount();
			ArrayList<String> result = new ArrayList<String>(numRows);
			if(numRows>0){
				int columnIndex = cursor.getColumnIndex(columnName);
				while(cursor.moveToNext()){
					result.add(cursor.getString(columnIndex));
				}
			}
			// present the study in alphabetical order
			Collections.sort(result);
			return result;
		}finally{
			if(cursor!=null) cursor.close();
		}
	}
	
	public String allStudiesToString() {
		String result = ""; 
		result += SampleInfo.COLUMN_NAME_STUDY+","+
				StudyInfo.COLUMN_NAME_SHAPEFILE+","+
				StudyInfo.COLUMN_NAME_SEED_X+","+
				StudyInfo.COLUMN_NAME_SEED_Y+","+
				StudyInfo.COLUMN_NAME_AUTOREJECT+","+
				SampleInfo._ID+","+
				SampleInfo.COLUMN_NAME_NUMBER+","+
				SampleInfo.COLUMN_NAME_TYPE+","+
				SampleInfo.COLUMN_NAME_X+","+
				SampleInfo.COLUMN_NAME_Y+","+
				SampleInfo.COLUMN_NAME_STATUS+","+
				SampleInfo.COLUMN_NAME_COMMENT+","+
				SampleInfo.COLUMN_NAME_TIMESTAMP+"\n";
		
		if(!mDatabase.isOpen()){
			
		}
		
		Cursor studyCursor = mDatabase.rawQuery("SELECT * FROM "+StudyInfo.TABLE_NAME, null);
		studyCursor.moveToFirst();
		
		while(studyCursor.getCount()>0 && !studyCursor.isAfterLast()){
			int columnIndex = studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_STUDYNAME);
			String studyName = studyCursor.getString(columnIndex);
			Cursor cursor = mDatabase.rawQuery("SELECT * FROM "+SampleInfo.TABLE_NAME+
					" WHERE "+SampleInfo.COLUMN_NAME_STUDY+"='"+studyName+"'", null);
			cursor.moveToFirst();
			while(cursor.getCount()>0 && !cursor.isAfterLast()){
				String commentString = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_COMMENT));
				if(commentString==null) commentString = "[No comments]";
				result +=
						cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STUDY))+","+
						studyCursor.getString(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SHAPEFILE))+","+
						studyCursor.getFloat(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SEED_X))+","+
						studyCursor.getFloat(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SEED_Y))+","+
						studyCursor.getInt(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_AUTOREJECT))+","+
						cursor.getInt(cursor.getColumnIndex(SampleInfo._ID))+","+
						cursor.getInt(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_NUMBER))+","+
						cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_TYPE))+","+
						cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X))+","+
						cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y))+","+
						cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS))+","+
						commentString+","+
						cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_TIMESTAMP))+"\n";
				cursor.moveToNext();
			}
			cursor.close();
			studyCursor.moveToNext();
		}
		studyCursor.close();
		return result; 
	}

	
	public String singleStudyToString(String studyName) {
		String result = ""; 
		
		Cursor studyCursor = mDatabase.rawQuery("SELECT * FROM "+StudyInfo.TABLE_NAME+
				" WHERE "+StudyInfo.COLUMN_NAME_STUDYNAME+"='"+studyName+"'", null);
		studyCursor.moveToFirst();
		
		Cursor cursor = mDatabase.rawQuery("SELECT * FROM "+SampleInfo.TABLE_NAME+
				" WHERE "+SampleInfo.COLUMN_NAME_STUDY+"='"+studyName+"'", null);
		cursor.moveToFirst();
		
		result += SampleInfo.COLUMN_NAME_STUDY+","+
				StudyInfo.COLUMN_NAME_SHAPEFILE+","+
				StudyInfo.COLUMN_NAME_SEED_X+","+
				StudyInfo.COLUMN_NAME_SEED_Y+","+
				StudyInfo.COLUMN_NAME_AUTOREJECT+","+
				SampleInfo._ID+","+
				SampleInfo.COLUMN_NAME_NUMBER+","+
				SampleInfo.COLUMN_NAME_TYPE+","+
				SampleInfo.COLUMN_NAME_X+","+
				SampleInfo.COLUMN_NAME_Y+","+
				SampleInfo.COLUMN_NAME_STATUS+","+
				SampleInfo.COLUMN_NAME_COMMENT+","+
				SampleInfo.COLUMN_NAME_TIMESTAMP+"\n";
		while(cursor.getCount()>0 && !cursor.isAfterLast()){
			String commentString = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_COMMENT));
			if(commentString==null) commentString = "[No comments]";
			result += cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STUDY))+","+
					  studyCursor.getString(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SHAPEFILE))+","+
					  studyCursor.getFloat(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SEED_X))+","+
					  studyCursor.getFloat(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_SEED_Y))+","+
					  studyCursor.getInt(studyCursor.getColumnIndex(StudyInfo.COLUMN_NAME_AUTOREJECT))+","+
					  cursor.getInt(cursor.getColumnIndex(SampleInfo._ID))+","+
					  cursor.getInt(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_NUMBER))+","+
					  cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_TYPE))+","+
					  cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X))+","+
					  cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y))+","+
					  cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS))+","+
					  commentString+","+
					  cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_TIMESTAMP))+"\n";
				cursor.moveToNext();
		}
		cursor.close();
		studyCursor.close();
		return result; 
	}

	// returns 0 if successful
	public int makeSample(String studyName) {
		Log.d("database",allStudiesToString());
		
		String innerQuery = "SELECT "+SampleInfo._ID+" FROM "+SampleInfo.TABLE_NAME+
				" WHERE "+SampleInfo.COLUMN_NAME_STATUS+"='"+
				Status.OVERSAMPLE.toString()+"'"+
				" AND "+SampleInfo.COLUMN_NAME_STUDY+"='"+studyName+"'";
		String query = "SELECT MIN("+SampleInfo._ID+") AS "+SampleInfo._ID+" FROM ("+innerQuery+")";
		Cursor cursor = mDatabase.rawQuery(query, null);
		if(cursor.getCount()==0){
			cursor.close();
			return UpdateSampleAsyncTask.INSUFFICIENT_SAMPLES_ERROR;
		}
		cursor.moveToFirst();
		int id = cursor.getInt(cursor.getColumnIndex(SampleInfo._ID));	
		cursor.close();
		ContentValues values = new ContentValues();
		values.put(SampleInfo.COLUMN_NAME_STATUS, Status.SAMPLE.toString());
		return update(values,id);
	}

	public int update(ContentValues values, int mSampleID) {
		Log.d("database",allStudiesToString());
		values.put(SampleInfo.COLUMN_NAME_TIMESTAMP, format.format(new Date(System.currentTimeMillis())));
		try{
			mDatabase.update(SampleInfo.TABLE_NAME, values, 
					"_ID='"+mSampleID+"'",null);
		}catch(SQLiteException e){
			Log.d("database","Error getting writable database for update");
			return UpdateSampleAsyncTask.UPDATE_ERROR;
		}
		return UpdateSampleAsyncTask.SUCCESS;
	}

	/** Strip any characters that are not alphanumeric, 
	 * underscore, space, or period.
	 * @param raw string that is raw user input
	 * @return modified string
	 */
	public static String getCleanText(String raw) {
		//accept alpha numeric, underscore, space, and period
		return raw.replaceAll("[^a-zA-Z0-9_ \\.]", "");
	}
	
	/**
	 * Given an input string return a string that contains
	 * only alpha-numeric characters and underscore
	 * @param textField UI field that contains the user input
	 * @return cleaned string
	 */
	public static String getCleanStudyNameString(String raw) {
		raw = getCleanText(raw);
		return raw.replaceAll(" ","_");
	}
}
