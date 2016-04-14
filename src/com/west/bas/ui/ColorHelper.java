package com.west.bas.ui;

import android.content.Context;

import com.west.bas.R;

public class ColorHelper {

	private static Context sContext;
	private static int sBlack;
	private static int sHighlight;
	private static int sWarning;
	
	public static void init(Context c){
		sContext = c;
	}
	
	public static int black(){
		if(sBlack==0){
			sBlack = sContext.getResources().getColor(android.R.color.black);
		}
		return sBlack;
	}
	
	public static int highlight(){
		if(sHighlight==0){
			sHighlight = sContext.getResources().getColor(R.color.highlight);
		}
		return sHighlight;
	}
	
	public static int warning(){
		if(sWarning==0){
			sWarning = sContext.getResources().getColor(R.color.warning);
		}
		return sWarning;
	}
}
