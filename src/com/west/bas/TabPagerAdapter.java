package com.west.bas;

import android.support.v4.app.*; 
import android.util.Log; 
// Referenced classes of package west.bas: 
// MapFragment, TableFragment 

public class TabPagerAdapter extends FragmentPagerAdapter { 
	public TabPagerAdapter(FragmentManager fm) { 
		super(fm); 
	} 
	
	public Fragment getItem(int tabIndex) { 
		switch(tabIndex) { 
		case 0:  
			return new MapFragment(); 
		case 1:  
			return new TableFragment(); 
		} 
		Log.d("TabPagerAdapter", "Attempt to get fragment that has not been implemented"); 
		return null; 
	} 
	
	public int getCount() { return 2; } 
}
