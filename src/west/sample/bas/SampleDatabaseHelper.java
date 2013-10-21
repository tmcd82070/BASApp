package west.sample.bas;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Based on developer.android.com/training/basics/data-storage/databases.html
 * 
 */
public class SampleDatabaseHelper extends SQLiteOpenHelper {

	// V1: initial implementation
	// V2: updated to include timestamp when creating an entry
	// V3: created a field to indicate which study each point is associated with
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "BAS.db";
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	
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

	/** Data types in the sample data **/
	private static final String DATA_INT = " INTEGER";
	private static final String DATA_FLOAT = " FLOAT(7)";
	private static final String DATA_TEXT = " TEXT";
	private static final String DATA_ENUM_TYPE = " TEXT";	  	//" ENUM('sample','oversample')";
	private static final String DATA_ENUM_STATUS = " TEXT"; 	//" ENUM('sample','oversample','reject','complete')";
	private static final String DATA_DATE = " TIMESTAMP";
	private static final String REQUIRED = " NOT NULL";
	
	/** String that defines the structure of the table within a CREATE command **/
	private static final String CREATE_TABLE = 
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
	
	private static final String DELETE_TABLE = 
			"DROP TABLE IF EXISTS "+SampleInfo.TABLE_NAME;

	SQLiteDatabase db;
	
	public SampleDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	//TODO actually deal with changes in the database - don't just delete the old one
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// write the previous data to file
		//writeToFile(db);
		// remove all data from the database
		db.execSQL(DELETE_TABLE);
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
	public long addValue(String study, int number, SamplePoint.SampleType type, double x, double y){
		ContentValues data = new ContentValues();
		data.put(SampleInfo.COLUMN_NAME_STUDY, study);
		data.put(SampleInfo.COLUMN_NAME_NUMBER, number);
		data.put(SampleInfo.COLUMN_NAME_TYPE, type.getString());
		data.put(SampleInfo.COLUMN_NAME_X, x);
		data.put(SampleInfo.COLUMN_NAME_Y, y);
		data.put(SampleInfo.COLUMN_NAME_TIMESTAMP, format.format(new Date(System.currentTimeMillis())));
		// Initially the status is equal to the type
		data.put(SampleInfo.COLUMN_NAME_STATUS, type.getString());

		return db.insert(SampleInfo.TABLE_NAME, null, data);
	}
	
	public ArrayList<String> getListOfStudies(){
		Cursor cursor= db.rawQuery("SELECT DISTINCT "+SampleInfo.COLUMN_NAME_STUDY+
				" FROM "+SampleInfo.TABLE_NAME, null);
		int columnIndex = cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STUDY);
		int numStudies = cursor.getCount();
		if(numStudies>0){
			ArrayList<String> result = new ArrayList<String>(cursor.getCount());
			while(cursor.moveToNext()){
				result.add(cursor.getString(columnIndex));
			}
			return result;
		}else{
			return null;
		}
	}
	
	public String prettyPrint() {
		String result = ""; 
		Cursor cursor = db.rawQuery("SELECT * FROM "+SampleInfo.TABLE_NAME, null);
		while(cursor.moveToNext()){
			String commentString = cursor.getString(7);
			if(commentString==null) commentString = "[No comments]";
			result += cursor.getString(1)+","+	// study
					  cursor.getInt(2)+","+ 	// number
					  cursor.getString(3)+","+ 	// type	
					  cursor.getFloat(4)+","+	// x
					  cursor.getFloat(5)+","+	// y
					  cursor.getString(6)+","+	// status
					  commentString+","+		// comment
					  cursor.getString(8)+"\n"; // timestamp
		}
		return result; 
	}

//    public boolean updateStudent(int id, String name, String standard) {
//        ContentValues args = new ContentValues();
//        args.put(KEY_NAME, name);
//        args.put(KEY_GRADE, standard);
//        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
//    }
    

}
