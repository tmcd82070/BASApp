package com.west.bas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.west.bas.spatial.KMLHandler;
import com.west.bas.spatial.StudyArea;
import com.west.bas.ui.ReadFileCallback;

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
public class ReadStudyAreaTask extends AsyncTask<Void, Void, StudyArea> {

	private String mFilename;
	private String mStudyName;
	private ReadFileCallback mCallback;
	
	/**
	 * The ReadStudyAreaTask needs the filename to read and from which
	 * to parse the details of the study area geometry.  
	 * @param filename absolute path to the KML file
	 * @param studyName name that labels this study
	 * @param callback handle to a callback to use when processing is complete
	 */
	public ReadStudyAreaTask(String filename, String studyName, ReadFileCallback callback){
		mFilename = filename;
		mStudyName = studyName;
		mCallback = callback;
	}
	
	
	@Override
	protected StudyArea doInBackground(Void... params) {
		
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
		if(mCallback!=null) mCallback.onTaskComplete(studyArea);
	}

}

