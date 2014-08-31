package com.west.bas.spatial;

import java.util.ArrayList;

import android.graphics.Matrix;

import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
/**
 * An object that holds all of the details of a study. Ultimately won't be
 * necessary (all of that information can be queried from the database)
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
		if(studyName==null){
			if(mFail==null) mFail = "";
			mFail += "Missing study name. ";
		}else mStudyName = studyName;
		
		if(filename==null){
			if(mFail==null) mFail = "";
			mFail += "Missing filename. ";
		}else mFilename = filename;
		
		if(studyArea==null){
			if(mFail==null) mFail = "";
			mFail += "Missing study area. ";
		}else studyAreaPolygon_sp = studyArea;
	}

	public Coordinate[] getBoundaryPoints(){
		return studyAreaPolygon_sp.getBoundary().getCoordinates();
	}

	public ArrayList<ArrayList<LatLng>> getHoles() {
		int nHoles = studyAreaPolygon_sp.getNumInteriorRing();
		ArrayList<ArrayList<LatLng>> holes = new ArrayList<ArrayList<LatLng>>();
		for(int i=0;i<nHoles;i++){
			ArrayList<LatLng> hole = new ArrayList<LatLng>();
			Coordinate[] points = studyAreaPolygon_sp.getInteriorRingN(i).getCoordinates();
			for(int j=0;j<points.length;j++){
				hole.add(new LatLng(points[j].x,points[j].y));
			}
			holes.add(hole);
		}
		return holes;

	}

//	public Path getStudyAreaAsPath(){
//			
//	}
	
	
	public boolean isValid(){ return mFail==null; }
	public String getFailMessage(){ return mFail; }
	
	public String getStudyName(){ return mStudyName; }
	
	public String getFilename() { return mFilename; }
	
	public LatLng getCenterLatLng(){
		Point center = studyAreaPolygon_sp.getCentroid();
		return new LatLng(center.getY(), center.getX());
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
