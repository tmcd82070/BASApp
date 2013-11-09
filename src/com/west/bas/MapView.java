package com.west.bas;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.west.bas.BoundingBox.ScreenOffset;
import com.west.bas.SampleDatabaseHelper.SampleInfo;
import com.west.bas.SamplePoint.Status;

public class MapView extends View {

	private Rect boundingRectangle;
	private Point[] mPoints;
	private Paint[] mColors;
	private int mPointSize = 7;
	private Paint mGrey;
	private Paint mPaintSample;
	private Paint mPaintOversample;
	private Paint mPaintReject;
	private Paint mPaintCollected;
	
	public MapView(Context context) {
		super(context);
		createColors(context);
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createColors(context);
	}

	public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		createColors(context);
	}

	private void createColors(Context c) {
		mGrey = new Paint();
		mGrey.setColor(Color.LTGRAY);
		
		mPaintSample = new Paint();
		mPaintSample.setColor(c.getResources().getColor(R.color.sample));
		
		mPaintOversample = new Paint();
		mPaintOversample.setColor(c.getResources().getColor(R.color.oversample));
		
		mPaintReject = new Paint();
		mPaintReject.setColor(c.getResources().getColor(R.color.rejected));
				
		mPaintCollected = new Paint();
		mPaintCollected.setColor(c.getResources().getColor(R.color.collected));
	}

	public void onDraw(Canvas canvas){
		if(boundingRectangle!=null){
			canvas.drawRect(boundingRectangle, mGrey);
		}
		if(mPoints!=null){
			for(int i=0;i<mPoints.length;i++){
				if(mPoints[i]!=null){
				canvas.drawCircle(mPoints[i].x, mPoints[i].y, mPointSize, mColors[i]);
				}
			}
		}
	}
	
	public void initView(Cursor cursor, int screenWidth, int screenHeight, BoundingBox bb) {
		mPoints = new Point[cursor.getCount()];
		mColors = new Paint[cursor.getCount()];
		
		ScreenOffset scale = bb.scaleToFit(screenWidth, screenHeight);
		
		cursor.moveToFirst();
		for(int i=0;i<mPoints.length && !cursor.isAfterLast();i++){
			float ptX = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
			float ptY = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
			Point p = scale.transformToScreen(ptX,ptY);
			mPoints[i] = p;
			Status status = Status.getValueFromString(cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS)));
			switch(status){
			case SAMPLE: 
				mColors[i] = mPaintSample; 
				break;
			case OVERSAMPLE: 
				mColors[i] = mPaintOversample; 
				break;
			case COLLECTED:
				mColors[i] = mPaintCollected; 
				break;
			case REJECT:
				mColors[i] = mPaintReject; 
				break;
			}
			cursor.moveToNext();
			Log.d("points","x: "+mPoints[i].x+","+mPoints[i].y);
		}
		
		boundingRectangle = bb.getBoundingRectangle(scale);
		
	}
}
