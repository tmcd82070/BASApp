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
		return studyAreaPolygon_sp.getExteriorRing().getCoordinates();
	}

	/** coordinates returned as y,x for plotting on gmap */
	public ArrayList<ArrayList<LatLng>> getHoles() {
		int nHoles = studyAreaPolygon_sp.getNumInteriorRing();
		ArrayList<ArrayList<LatLng>> holes = new ArrayList<ArrayList<LatLng>>();
		for(int i=0;i<nHoles;i++){
			ArrayList<LatLng> hole = new ArrayList<LatLng>();
			Coordinate[] points = studyAreaPolygon_sp.getInteriorRingN(i).getCoordinates();
			for(int j=0;j<points.length;j++){
				hole.add(new LatLng(points[j].y,points[j].x));
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

	public double[] getBB(){
		Coordinate[] coords = studyAreaPolygon_sp.getEnvelope().getCoordinates();
		double[] bb = new double[]{coords[0].x,coords[0].y,coords[0].x,coords[0].y};
		for(int i=1;i<coords.length;i++){
			if(coords[i].x<bb[0]) bb[0]=coords[i].x;
			else if(coords[i].x>bb[2]) bb[2]=coords[i].x;
			if(coords[i].y<bb[1]) bb[1]=coords[i].y;
			else if(coords[i].y>bb[3]) bb[3]=coords[i].y;
		}
		
		return bb;
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
		double sqSide = boundingBox.getWidth();
		double boxX = boundingBox.getMinX();
		double boxY = boundingBox.getMinY();
		// center the study are within a *square* bounding box
		if(boundingBox.getHeight() > boundingBox.getWidth()){
			sqSide = boundingBox.getHeight();
			boxX -= (sqSide-boundingBox.getWidth())/2;
		}else{
			boxY += (sqSide-boundingBox.getHeight())/2;
		}
		float x = (float) (boxX + sqSide * pointUnitSq[0]); 
		float y = (float) (boxY + sqSide * pointUnitSq[1]);
		Point p = f.createPoint(new Coordinate(x,y));
		
		//TODO return a point rather than float array?
		if(studyAreaPolygon_sp.contains(p)){
			return (new float[] { x, y });
		}
		return null;
	}
	
	// TODO update (not revised to use spatial polygon as study area))
	public android.graphics.Point transformToScreen(float ptX, float ptY) {
		float[] coords = new float[]{ptX, ptY};
		mTransform.mapPoints(coords);
		android.graphics.Point result = new android.graphics.Point();
		result.set((int)coords[0],(int)coords[1]);
		return result;
	}

}
