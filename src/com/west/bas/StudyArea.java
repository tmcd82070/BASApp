package com.west.bas;

public class StudyArea {

	private String mStudyName;
	private String mFilename;
	
	//TODO determine area from shapefile
	private float mAreaOfStudy = 10;
	
	//TODO get the bounding box from the data!
	private BoundingBox mBB = new BoundingBox(0,0,5,5);
	
	// TODO accept a spataial data type and a name (rather than two strings)
	public StudyArea(String shapefileName, String studyName){
		mFilename = shapefileName;
		mStudyName = studyName;
	}

	public String getName(){ return mStudyName; }
	
	public String getFilename() { return mFilename; }

	public int estimateNumPointsNeeded(int nPointsInSample){
		return (int)((float)(nPointsInSample) * (mBB.getArea() / mAreaOfStudy)); 
	}

	/** Convert a point in the unit square into a coordinate within
	 * the bounding box of the study area.  Determines whether or not
	 * that point lies within the study area.
	 * @param pointUnitSq
	 * @return point in the study area or null if input is outside the study area
	 */
	public float[] getSampleLocation(float[] pointUnitSq) {
		float[] pointStudyBB = mBB.getSample(pointUnitSq);
		if(contains(pointStudyBB)) return pointStudyBB;
		return null;
	}

	private boolean contains(float[] pointStudyBB) {
		// TODO Determine whether or not the given point
		// lies within the study are (not just within the 
		// bounding box
		return true;
	}

	public BoundingBox getBoundingBox() {
		return mBB;
	}
}
