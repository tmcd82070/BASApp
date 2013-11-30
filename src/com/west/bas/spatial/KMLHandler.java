package com.west.bas.spatial;


import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A handler for use with a SAX parser for reading KLM files.
 * The contents of the file will be returned
 * through the structure provided in the constructor.
 * 
 * The attribute levels are expected to be collected into an 
 * SVG layer (indicated by tag 'g') with unique attributes as
 * listed in the targets argument to the constructor.  The array
 * of targets is expected to be ordered from low to high.
 *  
 * @author MB, last updated June 2011
 * @see DefaultHandler
 */
public class KMLHandler extends DefaultHandler{

	// The implemented tags
	private static enum KMLTag {
		Polygon, 
		outerBoundaryIs, innerBoundaryIs, 
		LinearRing, coordinates,
		OTHER;

		// TODO replace with valueOf?
		public static KMLTag getTagFromString(String s) {
			if(s.equals("Polygon")) return Polygon;
			if(s.equals("outerBoundaryIs")) return outerBoundaryIs;
			if(s.equals("innerBoundaryIs")) return innerBoundaryIs;
			if(s.equals("LinearRing")) return LinearRing;
			if(s.equals("coordinates")) return coordinates;
			return OTHER;
		}

		public boolean isChildOf(KMLTag parent) {
			switch(this){
			case Polygon: return false;
			case outerBoundaryIs: return parent==Polygon;
			case innerBoundaryIs: return parent==Polygon;
			case LinearRing: 
				return parent== outerBoundaryIs || parent==innerBoundaryIs;
			case coordinates: return parent==LinearRing;
			case OTHER:
				return false;
			}
			return false;
		}
	}
	
	LinearRing boundary;
	LinearRing[] holes;
	
	// Track navigation through the hierarchy
	Vector<KMLTag> parent = new Vector<KMLTag>();
	
	Polygon mPolygon;
	
	// A factory used to create a MULTIPOINT from the list of Coordinates
	GeometryFactory f = new GeometryFactory();
	

	@Override
	public void startDocument() throws SAXException {
		Log.d("parseKML","Started parsing KML...");
	}
	
	@Override
	public void endDocument() throws SAXException {
		Log.d("parseKML","Finished parsing KML...");
	}
	
	@Override
	public void startElement(String uri, 
							 String localName, 
							 String qName, 
							 Attributes attributes) throws SAXException {
		KMLTag tag = KMLTag.getTagFromString(qName);
		if(tag==null || (parent.size()>0 && !tag.isChildOf(parent.lastElement()))){
			Log.d("parseKML","Skipping tag: "+qName);
		}
		// Extract the polygon ID
//		if(tag==KMLTag.Polygon){
//			for (int i=0; i< attributes.getLength();i++){
//				if(attributes.getQName(i).equals("id")){
//					polygonID = attributes.getValue(i);
//				}
//			}
//		}
		parent.add(tag);	
	}
	
	@Override
	public void endElement(String uri, 
						   String localName, 
						   String qName) throws SAXException {
		KMLTag tag = KMLTag.getTagFromString(localName);
		if(tag==KMLTag.Polygon){
			if(mPolygon!=null) Log.e("parseXML","More than one polygon found!");
			mPolygon = f.createPolygon(boundary,holes);
			// Called to create the bounding box within the Polygon object
			@SuppressWarnings("unused")
			Envelope env = mPolygon.getEnvelopeInternal();
		}
		if(tag!=parent.lastElement()){
			Log.e("parseKML","Error parsing: unmatched tag: "+tag);
			//TODO quit?
		}
		parent.remove(parent.size()-1);
	}
	

	@Override
	public void characters(char[] ch, 
						   int start, 
						   int length) throws SAXException {
		if(parent.lastElement()==KMLTag.coordinates){
			// A temporary list into which to accumulate coordinates
			String s = new String(ch, start, length);
			String[] latLonAlt = s.split(",");
			Coordinate[] coords = new Coordinate[latLonAlt.length/2+1];
			for(int i=0, j=0;i<latLonAlt.length;i+=2,j++){
				coords[j] = new Coordinate(Double.valueOf(latLonAlt[i]),Double.valueOf(latLonAlt[i+1]));
			}
			// make the coordinates into a linear ring
			coords[coords.length-1]=coords[0];
			LinearRing ring = f.createLinearRing(coords);
			
			if(parent.elementAt(parent.size()-3)==KMLTag.innerBoundaryIs){
				if(holes==null) holes=new LinearRing[1];
				else{
					LinearRing[] temp = new LinearRing[holes.length+1];
					for(int i=0;i<holes.length;i++) temp[i]=holes[i];
					holes=temp;
				}
				holes[holes.length-1]=ring;
			}
			if(parent.elementAt(parent.size()-3)==KMLTag.outerBoundaryIs){
				boundary = ring;
			}
		}
	}

	@Override
	public void error(SAXParseException e) throws SAXException{
		Log.d("parseKML","SAX Parse ERROR: "+e.getMessage());
	}
	
	@Override
	public void fatalError(SAXParseException e) throws SAXException{
		Log.d("parseKML","SAX Parse FATAL ERROR: "+e.getMessage());
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException{
		Log.d("parseKML","SAX Parse WARNING: " + e.getMessage());
	}
	
	public Polygon getPolygon(){
		return mPolygon;
	}
}
