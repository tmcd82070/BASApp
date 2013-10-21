package west.sample.bas;

import java.util.ArrayList;
import java.util.Random;

import west.sample.bas.SamplePoint.SampleType;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class GenerateSample extends AsyncTask<Void, Void, Boolean> { 
	
	private static Random rand;
	private static final int SUFFICIENTLY_LARGE_U = 32;
	
	/* handle to the database to which samples will be written */
	private SampleDatabaseHelper dbHelper;

	private ArrayList<Integer> inputX; 
	private ArrayList<Integer> inputY; 
	
	private static final int baseX = 2; 
	private static final int baseY = 3; 
	
	private BoundingBox bb; 
	private String studyName;
	private float areaStudy; 
	private int nSample; 
	private int nOversample; 

	
	/**
	 * negative values for the numbers of (over)samples indicate that the value was not set
	 * @param bb
	 * @param areaStudy
	 * @param nSample
	 * @param nOversample
	 */
	public GenerateSample(Context c, String studyName, String studyAreaFilename, int nSample, int nOversample) { 
		this.studyName = studyName;
		
		// Connect to the database where the samples will be stored
		dbHelper = new SampleDatabaseHelper(c);
		
		if(rand == null) rand = new Random(); 
		
		//TODO make this a spatial object and population from the given file
		areaStudy = 20;
		//TODO get the bounding box from the study area
		bb = new BoundingBox(0,0,5,5);
		
		this.nSample = nSample<0 ? 0 : nSample; 
		this.nOversample = nOversample<0 ? 0 : nOversample; 
		
		int nPoints = (int)((float)(nSample + nOversample) * (bb.getArea() / areaStudy)); 
		int seed = rand.nextInt(SUFFICIENTLY_LARGE_U); 
		int nDigits = (int)(Math.log(nPoints + seed) / Math.log(2D)); 
		inputX = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseX, inputX); 
		seed = rand.nextInt(SUFFICIENTLY_LARGE_U); 
		nDigits = (int)(Math.log(nPoints + seed) / Math.log(3D)); 
		inputY = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseY, inputY); 
	} 
	
	/**
	 * The least significant n-digit (e.g., bit) is listed in position 0
	 * @param n_10 number in base 10
	 * @param baseX desired base of the output
	 * @param n_base list of base 10 integers representing baseX digits
	 */
	private void convertToBase(int n_10, int baseX, ArrayList<Integer> n_base) { 
		Log.d("GEN","Input: "+n_10);
		if(n_10<0) n_10*=-1;
		while(n_10>0){
			int q = n_10/baseX;
			int r = n_10-q*baseX;
			n_base.add(r);
			n_10=q;
		}
		Log.d("GEN","Base "+baseX+": "+n_base);
	}
	
	private float[] nextPoint() { 
		float x = vanDerCorput(inputX, 2); 
		float y = vanDerCorput(inputY, 3); 
		Log.d("GEN","[x,y]: "+x+","+y);
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
	 * @param n_base the number for which the sum shoud be computed
	 * @param base the base in which n_base is represented also used as 
	 * the demonimator in the division) 
	 * @return decimal value [0,1]
	 */
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
	
    
	@Override
	protected Boolean doInBackground(Void... params) {
		for(int i=0;i<nSample;i++){
			float[] coords = bb.getSample(nextPoint());
			if(-1==dbHelper.addValue(studyName,i,SampleType.SAMPLE,coords[0],coords[1])){
				Log.d("DBentry","Failed to insert row in the database");
				return false;
			}
		}
		for(int i=0;i<nOversample;i++){
			float[] coords = bb.getSample(nextPoint());
			if(-1==dbHelper.addValue(studyName,i+nSample,SampleType.OVERSAMPLE,coords[0],coords[1])){
				Log.d("DBentry","Failed to insert row in the database");
				return false;
			}
		}
		
		//Log.d("generate", dbHelper.prettyPrint()); 
		return true;
	} 
	
}
