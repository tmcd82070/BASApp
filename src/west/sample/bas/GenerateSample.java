package west.sample.bas;

import android.os.AsyncTask; 
import android.util.Log; 

import java.util.*; 

public class GenerateSample extends AsyncTask<Void, Void, String> { 
	
	private static Random rand;
	private ArrayList<Integer> inputX; 
	private ArrayList<Integer> inputY; 
	private ArrayList<float[]> sample; 
	
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
	public GenerateSample(String studyAreaFilename, int nSample, int nOversample) { 
		if(rand == null) rand = new Random(); 
		
		//TODO make this a spatial object and population from the given file
		areaStudy = 20;
		//TODO get the bounding box from the study area
		bb = new BoundingBox(0,0,5,5);
		
		this.nSample = nSample<0 ? 0 : nSample; 
		this.nOversample = nOversample<0 ? 0 : nOversample; 
		
		int nPoints = (int)((float)(nSample + nOversample) * (bb.getArea() / areaStudy)); 
		sample = new ArrayList<float[]>(nPoints); 
		int seed = rand.nextInt(); 
		int nDigits = (int)(Math.log(nPoints + seed) / Math.log(2D)); 
		inputX = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseX, inputX); 
		seed = rand.nextInt(); 
		nDigits = (int)(Math.log(nPoints + seed) / Math.log(3D)); 
		inputY = new ArrayList<Integer>(nDigits); 
		convertToBase(seed, baseY, inputY); 
	} 
	
	private void convertToBase(int n_10, int baseX, ArrayList<Integer> n_base) { 
		while(n_10>0){
			int q = n_10/baseX;
			int r = n_10-q*baseX;
			n_base.add(r);
			n_10=q;
		}		
	}
	
	private float[] nextPoint() { 
		float x = vanDerCorput(inputX, 2); 
		float y = vanDerCorput(inputY, 3); 
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
		while(sample.size()< nSample+nOversample){
			sample.add(bb.getSample(nextPoint()));
		}
		Log.d("generate", prettyPrint(sample)); 
		Log.d("generate", (new StringBuilder("Number of samples: ")).append(nSample).toString()); 
		Log.d("generate", (new StringBuilder("Number of oversamples: ")).append(nOversample).toString()); 
		return "stdout!";
	} 
	
	String prettyPrint(ArrayList<float[]> list) { 
		String result = ""; 
		for(float[] xy : list) { 
			result += "["+xy[0]+","+xy[1]+"] ";
		} 
		return result; 
	}

	

}
