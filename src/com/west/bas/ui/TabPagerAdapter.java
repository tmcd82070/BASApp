package com.west.bas.ui;

import android.support.v4.app.*; 
import android.util.Log; 

/**
 * The TabPagerA
 * West EcoSystems Technologies, Inc (2013)
 */
public class TabPagerAdapter extends FragmentPagerAdapter { 

	public static final int MAP_ITEM = 0;

	public TabPagerAdapter(FragmentManager fm) { 
		super(fm); 
	} 
	
	public Fragment getItem(int tabIndex) { 
		switch(tabIndex) { 
		case MAP_ITEM:  
			return new MapFragmentDual(); 
		case 1:  
			return new TableFragment(); 
		} 
		Log.d("TabPagerAdapter", "Attempt to get fragment that has not been implemented"); 
		return null; 
	} 
	
	public int getCount() { return 2; } 
}
