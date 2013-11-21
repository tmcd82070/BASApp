package com.west.bas.ui;

import java.io.File;
import java.io.FilenameFilter;

public class KMLFilter implements FilenameFilter {
	//TODO make this a spatial file filter? (.shp too?)
	public boolean accept(File f, String s){
		return new File(f.getAbsoluteFile()+"/"+s).isDirectory() || 
				s.endsWith(".kml");
	}

}
