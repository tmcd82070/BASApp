package com.west.bas.spatial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.vividsolutions.jts.geom.Coordinate;

import android.os.AsyncTask;
import android.util.Log;

/** A class to read and parse a KML file to determine the 
 * extent of the study area.  Reading the file takes place
 * on a non-UI thread and must be given a callback to indicate
 * to the application that it has finished reading the file
 * (along with indication of the success).
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class ReadStudyAreaAsyncTask extends AsyncTask<Void, Void, StudyArea> {

	private String mFilename;
	private String mStudyName;
	private ReadStudyAreaCallback mCallback;
	
	private static StudyArea sStudyArea;
	
	/**
	 * The ReadStudyAreaTask needs the filename to read and from which
	 * to parse the details of the study area geometry.  
	 * @param filename absolute path to the KML file
	 * @param studyName name that labels this study
	 * @param callback handle to a callback to use when processing is complete
	 */
	public ReadStudyAreaAsyncTask(String filename, String studyName, ReadStudyAreaCallback callback){
		mFilename = filename;
		mStudyName = studyName;
		mCallback = callback;
	}
	
	
	@Override
	protected StudyArea doInBackground(Void... params) {
		Log.d("ReadStudy","started read study async task");
		File studyAreaKML = new File(mFilename);
		
		//currently only handles KML files
		if(!mFilename.endsWith(".kml")){
			return new StudyArea("Filename must indicate a shapefile (extension .kml)");
		}
		
		// The file must exist on the file system
		if(!studyAreaKML.exists()){
			return new StudyArea("File doesn't exist on the device: "+mFilename);
		}
		
		// TODO parse the file to get the polygon points and style details
		KMLHandler handler = new KMLHandler();
		try {
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
			XMLReader parser = XMLReaderFactory.createXMLReader();
			parser.setContentHandler(handler);
			InputSource input = new InputSource(new FileInputStream(studyAreaKML));
			parser.parse(input);
		} catch (SAXException e) {
			Log.e("readSA",e.getMessage());
			return new StudyArea(e.getMessage());
		} catch (FileNotFoundException e) {
			Log.e("readSA",e.getMessage());
			return new StudyArea(e.getMessage());
		} catch (IOException e) {
			Log.e("readSA",e.getMessage());
			return new StudyArea(e.getMessage());
		}
		
		return new StudyArea(mStudyName, mFilename, handler.getPolygon());
	}
	
	@Override
	public void onPostExecute(StudyArea studyArea){
		sStudyArea = studyArea;
		if(mCallback!=null) mCallback.onTaskComplete(studyArea);
		Log.d("ReadStudy","completed read study async task");
	}

	public static boolean hasRecentStudy(){
		return sStudyArea!=null;
	}
	public static LatLngBounds getBounds(){
		if(sStudyArea==null) return null;
		
		double[] bb = sStudyArea.getBB();
		return  new LatLngBounds(
				  new LatLng(bb[1], bb[0]), 
				  new LatLng(bb[3], bb[2]));

	}
	
	public static PolygonOptions getPolygon(){
		if(sStudyArea==null) return null;
		
		PolygonOptions p = new PolygonOptions();
		
		// add the perimeter points
		Coordinate[] coords = sStudyArea.getBoundaryPoints();
		for(int i=0;i<coords.length;i++){
			p.add(new LatLng(coords[i].y, coords[i].x));
		}
		
		// Add the holes
		ArrayList<ArrayList<LatLng>> holes = sStudyArea.getHoles();
		for(ArrayList<LatLng> hole : holes) p.addHole(hole);
		
		return p;
	}
}


