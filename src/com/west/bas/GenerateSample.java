/** Webpage for GeoTools download: http://sourceforge.net/projects/geotools/files/GeoTools%2010%20Releases/10.1/
 * And another maps on android project: http://sourceforge.net/p/javashapefilere/code/HEAD/tree/
 */

package com.west.bas;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.west.bas.SamplePoint.SampleType;

public class GenerateSample extends AsyncTask<Void, Void, Integer> { 
	
	/** A random number generator to seed the random-start Halton sequences */
	private static Random sRand;
	/** Upper bound on the random seed */
	private static final int SUFFICIENTLY_LARGE_U = 10000000;
	
	/** A handle to the database to which samples will be written */
	private SampleDatabaseHelper mDbHelper;

	/** A baseX representation of the seed for the Halton sequence 
	 * that determines the X coordinate
	 * @see #sBaseX
	 */
	private ArrayList<Integer> mInputX; 
	
	/** Base used in generating the Halton sequence for the X coordinate */
	private static final int sBaseX = 2; 
	
	/** A baseY representation of the seed for the Halton sequence 
	 * that determines the Y coordinate
	 * @see #sBaseY
	 */
	private ArrayList<Integer> mInputY; 
	
	/** Base used in generating the Halton sequence for the Y coordinate */
	private static final int sBaseY = 3; 
	
//	FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
	
	private StudyArea mStudyArea;
	private int mNumberSamples; 
	private int mNumberOversamples;
	
	private RefreshCallback callback;

	
	/**
	 * negative values for the numbers of (over)samples indicate that the value was not set
	 * @param c
	 * @param studyName
	 * @param studyArea
	 * @param nSample
	 * @param nOversample
	 * @param refreshCallback 
	 */
	public GenerateSample(Context c, StudyArea studyArea, int nSample, int nOversample, RefreshCallback refreshCallback) {
		mStudyArea = studyArea;
		
		callback = refreshCallback;
		
		// Connect to the database where the samples will be stored
		mDbHelper = new SampleDatabaseHelper(c);
		
		if(sRand == null) sRand = new Random(); 
		
		this.mNumberSamples = nSample<0 ? 0 : nSample; 
		this.mNumberOversamples = nOversample<0 ? 0 : nOversample; 
	}
	
	/** Initialize a buffer to hold the digits of the seed (in the 
	 * given base) to be used when generating the Halton sequence.
	 * (If the number of points is too large, excessive space may 
	 * be allocated.  If the number is too small, insufficient space
	 * may be allocated leading to an additional cost to expand the
	 * ArrayList.)
	 * 
	 * @see #convertToBase
	 * 
	 * @param nPoints number of points that will need to be generated 
	 * @param base number system for which the Halton sequence will be generated
	 * @return list of digits (in given base) for the seed of the Halton sequence
	 */
	private ArrayList<Integer> initSeedBuffer(int nPoints, int base){
		int seed = sRand.nextInt(SUFFICIENTLY_LARGE_U);
		int estMaxSeedValue = seed+nPoints;
		int nDigits = (int)(Math.log(estMaxSeedValue) / Math.log(base)); 
		ArrayList<Integer> buffer = new ArrayList<Integer>(nDigits);
		convertToBase(seed, base, buffer);
		return buffer;
	}
	
	
	/** Compute the next point in the 2-dimensional Halton sequence
	 * @return ordered (x,y) coordinates within the unit square
	 * @see #vanDerCorput
	 */
	private float[] nextPoint() { 
		float x = vanDerCorput(mInputX, sBaseX); 
		float y = vanDerCorput(mInputY, sBaseY); 
		return (new float[] { x, y }); 
	} 

	/**
	 * Steps from the least significant bit (in position 0) to the most 
	 * significant bit computing the sum = the next element in the 
	 * van der Corput sequence
	 * 
	 * A stateful method that increments the number (n_base) preparing
	 * for the next call to vanDerCorput()
	 * 
	 * The base is assumed to be non-zero
	 * 
	 * @param n_10 number in base 10
	 * @param baseX desired base of the output
	 * @param n_base list of base 10 integers representing baseX digits
	 */
	private void convertToBase(int n_10, int baseX, ArrayList<Integer> n_base) { 
		if(n_10<0) n_10*=-1;
		while(n_10>0){
			int q = n_10/baseX;
			int r = n_10-q*baseX;
			n_base.add(r);
			n_10=q;
		}
	}

	private float vanDerCorput(ArrayList<Integer> n_base, int base) { 
		float sum = 0.0F; 
		int denom = base; 
		int toAdd = 1; 
		for(int i = 0; i <n_base.size(); i++) { 
			int digit = ((Integer)n_base.get(i)).intValue(); 
			sum += (float)digit / (float)denom; 
			denom *= base; 
			
			if(toAdd == 1) { 
				digit += toAdd; 
				if(digit == base) { 
					n_base.set(i, Integer.valueOf(0)); 
				} else { 
					n_base.set(i, Integer.valueOf(digit)); 
					toAdd = 0; 
				} 
			} 
		} 
		return sum; 
	} 
	
   	/** Generate a balanced acceptance sample (BAS)
	 * @return number of rejected samples or -1 on failure
	 */
	@Override
	protected Integer doInBackground(Void... params) {
		
		SampleType sampleType = SampleType.SAMPLE;
		int rejectedCount = 0;
		
		// Get ready to generate samples
		// Initialize the seed buffers (storage space and values)
		// The number of points should reflect both the size of the 
		// sample and the likelihood that a point would like outside
		// the study area.
		if(mStudyArea==null){
			Log.d("Generate","study area is null");
			return -1;
		}
		int nPoints = mStudyArea.estimateNumPointsNeeded(mNumberSamples + mNumberOversamples);
		mInputX = initSeedBuffer(nPoints, sBaseX);
		mInputY = initSeedBuffer(nPoints, sBaseY);

		// Generate samples
		for(int i=0;i<(mNumberSamples+mNumberOversamples);i++){
			float[] coords = mStudyArea.getSampleLocation(nextPoint());
			if(coords == null){
				// if the point isn't within the study area, reject it now
				rejectedCount++;
				i--;
			}else{
				// if the point is in the study area, add it to the database
				// use a 1-up counter (for display to users)
				if(-1==mDbHelper.addValue(mStudyArea.getName(),mStudyArea.getFilename(),i+1,sampleType,coords[0],coords[1])){
					Log.d("DBentry","Failed to insert row in the database");
					return -1;
				}
				if(i==mNumberSamples-1) sampleType = SampleType.OVERSAMPLE;
			}
		}
		//Log.d("generate", dbHelper.prettyPrint()); 
		return rejectedCount;
	} 

	@Override
	public void onPostExecute(Integer i){
		if(i<0){
			Log.d("generate","Error! "+i);
			// tell the user what happened!
			// GENERATE_SAMPLE_FILEIO_ERROR
			// GENERATE_SAMPLE_DATABASE_ERROR
		}else{
			// TODO record the number of rejected points?
			Log.d("generate", "Number of rejected points: "+i);
			if(callback!=null) callback.onTaskComplete();
		}
	}
}
