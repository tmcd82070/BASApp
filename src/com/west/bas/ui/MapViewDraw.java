package com.west.bas.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.spatial.SamplePoint.Status;
import com.west.bas.spatial.StudyArea;

public class MapViewDraw extends View {

	private StudyArea studyArea;
	private Point[] mPoints;
	private Paint[] mColors;
	private int mPointSize = 7;
	private Paint mBlack, mGrey;
	private Paint mPaintSample;
	private Paint mPaintOversample;
	private Paint mPaintReject;
	private Paint mPaintCollected;
	
	public MapViewDraw(Context context) {
		super(context);
		createColors(context);
	}

	public MapViewDraw(Context context, AttributeSet attrs) {
		super(context, attrs);
		createColors(context);
	}

	public MapViewDraw(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		createColors(context);
	}

	private void createColors(Context c) {
		mBlack = new Paint();
		mBlack.setColor(Color.BLACK);
		mBlack.setStyle(Style.STROKE);
		
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
		if(studyArea!=null){
			//TODO update the drawing?
			//studyArea.draw(canvas,mBlack,mGrey);
		}
		if(mPoints!=null){
			for(int i=0;i<mPoints.length;i++){
				if(mPoints[i]!=null){
				canvas.drawCircle(mPoints[i].x, mPoints[i].y, mPointSize, mColors[i]);
				}
			}
		}
	}
	
	public void initView(Cursor cursor, int screenWidth, int screenHeight, StudyArea studyArea) {
		mPoints = new Point[cursor.getCount()];
		mColors = new Paint[cursor.getCount()];
		
		// TODO handle scaling
//		studyArea.scaleToFit(screenWidth,screenHeight);
		
		cursor.moveToFirst();
		for(int i=0;i<mPoints.length && !cursor.isAfterLast();i++){
			float ptX = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
			float ptY = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
			Point p = studyArea.transformToScreen(ptX,ptY);
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

	}

}
