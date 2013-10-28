package west.sample.bas;

import west.sample.bas.SampleDatabaseHelper.SampleInfo;
import west.sample.bas.SamplePoint.Status;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;

public class DetailListAdapter extends SimpleCursorAdapter{
	
	double gpsX = 0;
	double gpsY = 0;
	
	public DetailListAdapter(Context c, int layout, Cursor cursor, String[] from, int[] to, int flags){
		super(c, layout, cursor, from, to, flags);
	}

	@Override
	public void bindView(View view, Context c, Cursor cursor) {
		super.bindView(view, c, cursor);
		
		ColoredTextView statusTxt = (ColoredTextView) view.findViewById(R.id.text_status);
		String status = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS));
		statusTxt.setState(Status.getValueFromString(status));
		
		double x = cursor.getDouble(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
		double y = cursor.getDouble(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
		Log.d("details","determine if the user is close to the point: "+x+","+y+" (gps "+gpsX+","+gpsY+")");
//		if(x<1) view.setBackgroundColor(view.getResources().getColor(R.color.near_me));
	}

	public void setCurrentLocation(double x, double y){
		gpsX = x;
		gpsY = y;
	}
}
