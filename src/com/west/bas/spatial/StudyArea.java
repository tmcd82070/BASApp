package com.west.bas.spatial;

import java.io.File;

import com.google.android.gms.maps.model.LatLng;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
/**
 * An object that holds all of the details of a study. Ultimately won't be
 * necessary (all of that informatin can be queried from the database)
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class StudyArea {

	private String mStudyName;
	private String mFilename;
	private String mFail;
	
	private Matrix mTransform;
	
	private float mCenterX;
	private float mCenterY;
	
	private float mAreaOfStudy;
	
	private RectF mBB;
	private Path studyAreaPolygon;
	
	private RectF mAdjustedBB;
	private Path mAdjustedStudyAreaPolygon;
	
	public StudyArea(String failMessage){
		mFail = failMessage;
	}
	
	// TODO accept a spataial data type and a name (rather than two strings)
	public StudyArea(File studyAreaSHP, String shapefileName, String studyName){
		mFilename = shapefileName;
		mStudyName = studyName;
		
		// TODO try to read the shapefile
		// studyAreaSHP
		// if failure: set fail message
		
		// locations from
		//http://www.netstate.com/states/geography/wy_geography.htm
		float[] x = new float[]{-108,-106,-106,-108,-108};
		float[] y = new float[]{42,42,44,43,42};
		mAreaOfStudy = 10;
		
		studyAreaPolygon = new Path();
		studyAreaPolygon.moveTo(x[0],y[0]);
		for(int i=1;i<x.length;i++){
			studyAreaPolygon.lineTo(x[i], y[i]);
		}
		
		mCenterX = -107.6F;
		mCenterY = 43;
		
		// Determine the bounding box
		mBB = new RectF();
		float left = -111.05F;
		float right = -104.05F;
		float bottom = 41;
		float top = 45;
		mBB.set(left, top, right, bottom);
		//studyAreaPolygon.computeBounds(mBB, true);
	}

	public void draw(Canvas c, Paint outline, Paint background){
		c.drawRect(mAdjustedBB, background);
		
		c.drawPath(studyAreaPolygon,outline);
		
		studyAreaPolygon.transform(mTransform);
	}

	public boolean isValid(){ return mFail==null; }
	public String getFailMessage(){ return mFail; }
	
	public String getName(){ return mStudyName; }
	
	public String getFilename() { return mFilename; }
	
	public LatLng getCenter(){
		return new LatLng(mCenterY, mCenterX);
	}

	public int estimateNumPointsNeeded(int nPointsInSample){
		return (int)((float)(nPointsInSample) * (mBB.height()*mBB.width() / mAreaOfStudy)); 
	}

	/** Convert a point in the unit square into a coordinate within
	 * the bounding box of the study area.  Determines whether or not
	 * that point lies within the study area.
	 * @param pointUnitSq
	 * @return point in the study area or null if input is outside the study area
	 */
	public float[] getSampleLocation(float[] pointUnitSq) {
		float x_bb = mBB.left + mBB.width() * pointUnitSq[0]; 
		float y_bb = mBB.bottom - mBB.height() * pointUnitSq[1]; 
		if(contains(x_bb, y_bb)) return (new float[] { x_bb, y_bb });
		return null;
	}

	private boolean contains(float x, float y) {
		//studyAreaPolygon.contains...
		// TODO Determine whether or not the given point
		// lies within the study are (not just within the 
		// bounding box
		return true;
	}

	public void scaleToFit(int screenWidth, int screenHeight) {
		
		// Create a transformation matrix
		mTransform = new Matrix();
		
		// Scale the study area as needed
		float scale = screenWidth/mBB.width();
		if(mBB.height()*scale>screenHeight){
			scale = (float) (screenHeight/mBB.height());
		}
		// just smaller than the size of the screen
		scale*=0.9;
		mTransform.preScale(scale, scale);
		
		// position in the center of the screen
		mTransform.preTranslate(
				(screenWidth-mBB.width()*scale)/2, 
				(screenHeight-mBB.height()*scale)/2);
		
		
		Log.d("scale","window width: "+screenWidth+", height: "+screenHeight);
		Log.d("scale","bounding box: "+mBB.width()+", "+mBB.height());
		Log.d("scale","scale: "+scale);
		Log.d("scale","scaled box: "+mBB.width()*scale+", "+mBB.height()*scale);
		
		mAdjustedBB = new RectF();
		mTransform.mapRect(mAdjustedBB,mBB);
		
		mAdjustedStudyAreaPolygon = new Path();
		mAdjustedStudyAreaPolygon.set(studyAreaPolygon);
		mAdjustedStudyAreaPolygon.transform(mTransform);
	}

	public Point transformToScreen(float ptX, float ptY) {
		float[] coords = new float[]{ptX, ptY};
		mTransform.mapPoints(coords);
		Point result = new Point();
		result.set((int)coords[0],(int)coords[1]);
		return result;
	}
}
