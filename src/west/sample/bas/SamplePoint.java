package west.sample.bas;

import java.sql.Timestamp;
import java.util.Locale;

/** The SamplePoint class encapsulates all of the information associated with 
 * a single sample point.
 * 
 * @author M. Brittell, October 2013
 *
 */
public class SamplePoint {

	/** 
	 * Each generated point is labeled as either a necessary collection point 
	 * (SAMPLE) or is provided as an alternative to a necessary point that gets 
	 * rejected in the field (OVERSAMPLE)
	 */
	public static enum SampleType {
		SAMPLE,OVERSAMPLE;

		public String getString() {
			return "'"+this.toString().toLowerCase(Locale.US)+"'";
		}
	}
	
	/**
	 * Four mutually exclusive status classification describe the state of 
	 * each point.  Initially all points are either SAMPLE or OVERSAMPLE
	 * (which matches the SampleType).  Any point marked COMPLETE has been
	 * visited and data has been collected.  As collection occurs, some points may
	 * be rejected from the sample.  In such a case, that point is marked as
	 * REJECT and the next point in the sequence that is marked as OVERSAMPLE
	 * is changed to a SAMPLE point that must be included to complete the 
	 * data collection.
	 * 
	 * SAMPLE: 		a point that must be visited to complete the collection
	 * OVERSAMPLE: 	an additional point generated in case others in the 
	 * 				continuous block are rejected in the field
	 * REJECT: 		a point that has been rejected in the field (e.g., 
	 * 				inaccessible due to land ownership or hazardous conditions
	 * COMPLETE: 	a point that has been visited and data was collected
	 */
	public static enum Status {SAMPLE, OVERSAMPLE, REJECT, COLLECTED}
	
	/** Required attributes **/
	/* x,y coordinates of the point */
	private double x;
	private double y;
	
	/* the position of the point within the continuous sequence in the generated sample */
	private int number;
	
	/* Original type of the point (sample or oversample) */
	private SampleType type;
	
	/* Dynamic status of the point during collection in the field */
	private Status status;
	
	/** Optional Attributes **/
	/* The time that the point was visited (complete or reject) */
	private Timestamp timestamp;
	
	/* Narrative comments provided by the user */
	private String comments;
	
}
