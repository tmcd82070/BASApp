package com.west.bas.ui.table;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.database.SampleDatabaseHelper.Status;

public class DetailListAdapter extends SimpleCursorAdapter{
	
	double gpsX = 0;
	double gpsY = 0;
	
	public DetailListAdapter(Context c, int layout, Cursor cursor, String[] from, int[] to, int flags){
		super(c, layout, cursor, from, to, flags);
	}

	@Override
	public void bindView(View view, Context c, Cursor cursor) {
		super.bindView(view, c, cursor);
		double x = cursor.getDouble(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
		double y = cursor.getDouble(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
		Log.d("details","determine if the user is close to the point: "+x+","+y+" (gps "+gpsX+","+gpsY+")");
//		if(x<1) view.setBackgroundColor(view.getResources().getColor(R.color.near_me));
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		View view = super.getView(position, convertView, parent);
		
		Cursor cursor = (Cursor) getItem(position);
		ColoredTextView statusTxt = (ColoredTextView) view.findViewById(R.id.text_status);
		String status = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS));
		statusTxt.setState(Status.getValueFromString(status));
		statusTxt.refreshDrawableState();
		return view;
	}

	public void setCurrentLocation(double x, double y){
		gpsX = x;
		gpsY = y;
	}
}
