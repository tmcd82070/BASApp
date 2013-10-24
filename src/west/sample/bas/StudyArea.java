package west.sample.bas;

import java.io.IOException;

import android.util.Log;


public class StudyArea {

	private String mShapefileName;
	
	private String mStudyName;
	private float mAreaOfStudy = 10;
	
	//TODO get the bounding box from the study area
	BoundingBox mBB = new BoundingBox(0,0,5,5);
			
	public StudyArea(String shapefileName, String studyName) throws IOException{
		if(!readShapefile(shapefileName)){
			throw new IOException();
		}
		mShapefileName = shapefileName;
		mStudyName = studyName;
	}
	
	private boolean readShapefile(String shapefileName) {
		Log.d("study area","Need to read the shapefile! "+ mShapefileName);
		return true;
	}

	public String getName(){ return mStudyName; }
	
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
}
