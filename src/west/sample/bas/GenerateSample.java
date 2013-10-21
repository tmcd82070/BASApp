package west.sample.bas;

import java.util.ArrayList;
import java.util.Random;

import west.sample.bas.SamplePoint.SampleType;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class GenerateSample extends AsyncTask<Void, Void, String> { 
	
	private static Random rand;
	private static final int SUFFICIENTLY_LARGE_U = 50;
	
	/* handle to the database to which samples will be written */
	private SampleDatabaseHelper dbHelper;

	private ArrayList<Integer> inputX; 
	private ArrayList<Integer> inputY; 
	
	private static final int baseX = 2; 
	private static final int baseY = 3; 
	
	private BoundingBox bb; 
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
	public GenerateSample(Context c, String studyAreaFilename, int nSample, int nOversample) { 
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
		int seed = rand.nextInt(); 
		int nDigits = (int)(Math.log(nPoints + seed) / Math.log(2D)); 
		inputX = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseX, inputX); 
		seed = rand.nextInt(); 
		nDigits = (int)(Math.log(nPoints + seed) / Math.log(3D)); 
		inputY = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseY, inputY); 
		
		Log.d("GEN","Initial inputX: "+inputX);
		Log.d("GEN","Initial inputY: "+inputY);
	} 
	
	private void convertToBase(int n_10, int baseX, ArrayList<Integer> n_base) { 
		if(n_10==0) n_base.add(0);
		else{
			while(n_10>0){
				int q = n_10/baseX;
				int r = n_10-q*baseX;
				n_base.add(r);
				n_10=q;
			}
		}
		Log.d("GEN","Convert to base: "+n_base);
	}
	
	private float[] nextPoint() { 
		Log.d("GEN","[inputX]: "+inputX);
		Log.d("GEN","[inputY]: "+inputY);
		float x = vanDerCorput(inputX, 2); 
		float y = vanDerCorput(inputY, 3); 
		Log.d("GEN","[x,y]: "+x+","+y);
		return (new float[] { x, y }); 
	} 

	private float vanDerCorput(ArrayList<Integer> n_base, int base) { 
		float sum = 0.0F; 
		int denom = base; 
		int toAdd = 1; 
		for(int i = n_base.size() - 1; i >= 0; i--) { 
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
	protected String doInBackground(Void... params) {
		for(int i=0;i<nSample;i++){
			float[] coords = bb.getSample(nextPoint());
			if(-1==dbHelper.addValue(i,SampleType.SAMPLE,coords[0],coords[1])){
				Log.d("DBentry","Failed to insert row in the database");
				return null;
			}
		}
		for(int i=0;i<nOversample;i++){
			float[] coords = bb.getSample(nextPoint());
			if(-1==dbHelper.addValue(i+nSample,SampleType.OVERSAMPLE,coords[0],coords[1])){
				Log.d("DBentry","Failed to insert row in the database");
				return null;
			}
		}
		
		Log.d("generate", dbHelper.prettyPrint()); 
		return "stdout!";
	} 
	
}
