package com.west.bas.spatial;

import java.io.File;

import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
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

	GeometryFactory f = new GeometryFactory();
	
	private Polygon studyAreaPolygon_sp;
	
	private String mStudyName;
	private String mFilename;
	private String mFail;
	
	private Matrix mTransform;
	
	public StudyArea(String failMessage){
		mFail = failMessage;
	}
	
	public StudyArea(String studyName, String filename, Polygon studyArea) {
		mStudyName = studyName;
		mFilename = filename;
		studyAreaPolygon_sp = studyArea;
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
		
		// for drawing on canvas (not updated)
//		studyAreaPolygon = new Path();
//		studyAreaPolygon.moveTo(x[0],y[0]);
//		for(int i=1;i<x.length;i++){
//			studyAreaPolygon.lineTo(x[i], y[i]);
//		}
		
		//studyAreaPolygon.computeBounds(mBB, true);
	}

	// for drawing on a canvas (not updated)
//	public void draw(Canvas c, Paint outline, Paint background){
//		c.drawRect(mAdjustedBB, background);
//		
//		c.drawPath(studyAreaPolygon,outline);
//		
//		studyAreaPolygon.transform(mTransform);
//	}

	public boolean isValid(){ return mFail==null; }
	public String getFailMessage(){ return mFail; }
	
	public String getName(){ return mStudyName; }
	
	public String getFilename() { return mFilename; }
	
	public LatLng getCenterLatLng(){
		Point center = studyAreaPolygon_sp.getCentroid();
		return new LatLng(center.getX(), center.getY());
	}

	public int estimateNumPointsNeeded(int nPointsInSample){
		Geometry boundingBox = studyAreaPolygon_sp.getEnvelope();
		return (int)((float)(nPointsInSample) * (boundingBox.getArea() / studyAreaPolygon_sp.getArea())); 
	}

	/** Convert a point in the unit square into a coordinate within
	 * the bounding box of the study area.  Determines whether or not
	 * that point lies within the study area.
	 * @param pointUnitSq
	 * @return point in the study area or null if input is outside the study area
	 */
	public float[] getSampleLocation(float[] pointUnitSq) {
		Envelope boundingBox = studyAreaPolygon_sp.getEnvelopeInternal();
//		http://www.vividsolutions.com/jts/javadoc/com/vividsolutions/jts/geom/Geometry.html#getEnvelope%28%29
//		Returns this Geometrys bounding box. 
//		If this Geometry is the empty geometry, returns an empty Point. 
//		If the Geometry is a point, returns a non-empty Point. 
//		Otherwise, returns a Polygon whose points are 
//		(minx, miny), (maxx, miny), (maxx, maxy), (minx, maxy), (minx, miny). 
		
		float x = (float) (boundingBox.getMinX() + (boundingBox.getMaxX()-boundingBox.getMinX()) * pointUnitSq[0]); 
		float y = (float) (boundingBox.getMinY() + (boundingBox.getMaxY()-boundingBox.getMinY()) * pointUnitSq[1]);
		Point p = f.createPoint(new Coordinate(x,y));
		
		//TODO return a point rather than float array?
		if(studyAreaPolygon_sp.contains(p)){
			return (new float[] { x, y });
		}
		return null;
	}
	
	// used in drawing (not updated)
//	public void scaleToFit(int screenWidth, int screenHeight) {
//		
//		// Create a transformation matrix
//		mTransform = new Matrix();
//		
//		// Scale the study area as needed
//		float scale = screenWidth/mBB.width();
//		if(mBB.height()*scale>screenHeight){
//			scale = (float) (screenHeight/mBB.height());
//		}
//		// just smaller than the size of the screen
//		scale*=0.9;
//		mTransform.preScale(scale, scale);
//		
//		// position in the center of the screen
//		mTransform.preTranslate(
//				(screenWidth-mBB.width()*scale)/2, 
//				(screenHeight-mBB.height()*scale)/2);
//		
//		
//		Log.d("scale","window width: "+screenWidth+", height: "+screenHeight);
//		Log.d("scale","bounding box: "+mBB.width()+", "+mBB.height());
//		Log.d("scale","scale: "+scale);
//		Log.d("scale","scaled box: "+mBB.width()*scale+", "+mBB.height()*scale);
//		
//		mAdjustedBB = new RectF();
//		mTransform.mapRect(mAdjustedBB,mBB);
//		
//		mAdjustedStudyAreaPolygon = new Path();
//		mAdjustedStudyAreaPolygon.set(studyAreaPolygon);
//		mAdjustedStudyAreaPolygon.transform(mTransform);
//	}
//
	// TODO update (not revised to use spatial polygon as study area))
	public android.graphics.Point transformToScreen(float ptX, float ptY) {
		float[] coords = new float[]{ptX, ptY};
		mTransform.mapPoints(coords);
		android.graphics.Point result = new android.graphics.Point();
		result.set((int)coords[0],(int)coords[1]);
		return result;
	}
}
