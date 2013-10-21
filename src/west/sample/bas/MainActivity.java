package west.sample.bas;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.*; 


public class MainActivity extends FragmentActivity {

	// Layout objects that facilitate interaction
	private TabPagerAdapter adapter; 
	private ViewPager viewPager; 
	private ActionBar actionBar;
	
	// Layout widgets
	private ListView table; 
	
	// State variables
	private String studyAreaFilename;

	
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		
		initInteraction(savedInstanceState);
		
		initWidgets();
	} 
	
	private void initInteraction(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main); 
		adapter = new TabPagerAdapter(getSupportFragmentManager()); 
		viewPager = (ViewPager)findViewById(R.id.pager_tabLayout); 
		viewPager.setAdapter(adapter); 
		viewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			@Override
			public void onPageSelected(int arg0) {
				actionBar.setSelectedNavigationItem(arg0);
				
			}});

		actionBar = getActionBar(); 
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); 
		android.app.ActionBar.TabListener tabListener = new ActionBar.TabListener(){

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
			};
			
			
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.label_map)).setTabListener(tabListener)); 
		actionBar.addTab(actionBar.newTab().setText(getString(R.string.label_table)).setTabListener(tabListener));
		
		if(savedInstanceState != null) { 
			int currentTab = savedInstanceState.getInt("tab", 0); 
			actionBar.setSelectedNavigationItem(currentTab); 
			viewPager.setCurrentItem(currentTab); 
		} else { 
			actionBar.setSelectedNavigationItem(0); 
			viewPager.setCurrentItem(0); 
		} 

	}

	/** 
	 * Store the view that was active on close 
	 */
	protected void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState); 
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
		//outState.putString("previousBAS",currentBASFilename);
	} 
	
	private void initWidgets() { 
		//mapLabel = (TextView)findViewById(R.id.textView_labelStudyArea);
		table = (ListView)findViewById(R.id.listView_table); 
	} 
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	
	public boolean onOptionsItemSelected(MenuItem item) { 
		switch(item.getItemId()) { 
		case R.id.action_create: 
			createBAS(); 
			return true; 
		case R.id.action_load: 
			loadBAS(); 
			return true; 
		} 
		return super.onOptionsItemSelected(item); 
	} 
	
	private void createBAS() { 
		LayoutInflater inflater = (LayoutInflater)getSystemService("layout_inflater"); 
		View layout = inflater.inflate(R.layout.dialog_create, (ViewGroup)findViewById(R.layout.activity_main)); 
		final EditText studyNameTxt = (EditText)layout.findViewById(R.id.editText_sampleName);
		final EditText numberSamplesTxt = (EditText)layout.findViewById(R.id.editText_sampleSize); 
		final EditText numberOversamplesTxt = (EditText)layout.findViewById(R.id.editText_oversampleSize);
		
		final TextView studyNameLabel = (TextView)layout.findViewById(R.id.textView_labelSampleName);
		final TextView filenameLabel = (TextView)layout.findViewById(R.id.textView_labelStudyArea);
		final TextView numberSamplesLabel = (TextView)layout.findViewById(R.id.textView_labelSampleSize);
		final TextView numberOversamplesLabel = (TextView)layout.findViewById(R.id.textView_label_oversampleSize);
		final TextView filenameTxt = (TextView)layout.findViewById(R.id.textView_studyAreaFilename);
		
		// Provide a recommendation for the number of oversamples
		numberOversamplesTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String text = numberSamplesTxt.getText().toString();
				if(text.length()>0){
					numberOversamplesTxt.setHint("Recommended: "+getInt(numberSamplesTxt));
				}
			}});
		
		Button fileBrowseBtn = (Button)layout.findViewById(R.id.button_fileBrowser); 
		fileBrowseBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				displayToast("Implement the file browser");
				studyAreaFilename = "selected";
				filenameLabel.setTextColor(getResources().getColor(android.R.color.black));
				filenameTxt.setText("Selected!");
			}});
		android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this); 
		builder.setView(layout); 
		builder.setPositiveButton("Create", null);
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}});
		
		final AlertDialog dialog = builder.create(); 
		dialog.show(); 
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// check the values
				String studyName = getCleanString(studyNameTxt);
				int nSamples = getInt(numberSamplesTxt);
				int nOversamples = getInt(numberOversamplesTxt);
				
				// highlight missing values 
				filenameLabel.setTextColor(getResources().getColor(android.R.color.black));
				if(studyName.isEmpty()){
					displayToast("Invalid characters in the study name");
					studyNameTxt.setText("");
					studyNameLabel.setTextColor(getResources().getColor(R.color.highlight));
				}if(nSamples<1){
					studyNameLabel.setTextColor(getResources().getColor(android.R.color.black));
					numberSamplesLabel.setTextColor(getResources().getColor(R.color.highlight));
				}else if(nOversamples<0){
					numberSamplesLabel.setTextColor(getResources().getColor(android.R.color.black));
					numberOversamplesLabel.setTextColor(getResources().getColor(R.color.highlight));
				}else if(studyAreaFilename != null){
					GenerateSample g = new GenerateSample(getBaseContext(),studyName, 
							studyAreaFilename,nSamples,nOversamples);
					g.execute();
					dialog.dismiss();
				}else{
					numberOversamplesLabel.setTextColor(getResources().getColor(android.R.color.black));
					filenameLabel.setTextColor(getResources().getColor(R.color.highlight));
				}
			}});
		} 
	
	protected int getInt(EditText textField) {
		String s = textField.getText().toString();
		// checks for length, but not for character types 
		// (EditText set as numeric in the xml; could use try/catch for more robust)
		if(s.length()>0) return Integer.valueOf(s);
		return -1;
	}
	
	protected String getCleanString(EditText textField){
		String raw = textField.getText().toString();
		// TODO strip non alpha characters 
		return "clean"+raw;
	}

	private boolean loadBAS() { 
		String filename = "Need to choose a filename!"; 
		return loadBAS(filename); 
	} 
	
	private boolean loadBAS(String filename) { 
		displayToast((new StringBuilder("Received request to load \"")).append(filename).append("\"").toString()); 
		return true; 
	} 
	
	protected void startActivity(String className) {
		Intent i = new Intent();
		i.setClassName(this, className);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	protected void displayToast(String message) { 
		Toast.makeText(this, message, 1).show(); 
	} 

}

//package west.sample.bas;
//
//import java.util.Locale;
//
//import android.app.ActionBar;
//import android.app.FragmentTransaction;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.app.NavUtils;
//import android.support.v4.view.ViewPager;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//public class MainActivity extends FragmentActivity implements
//		ActionBar.TabListener {
//
//	/**
//	 * The {@link android.support.v4.view.PagerAdapter} that will provide
//	 * fragments for each of the sections. We use a
//	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
//	 * will keep every loaded fragment in memory. If this becomes too memory
//	 * intensive, it may be best to switch to a
//	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
//	 */
//	SectionsPagerAdapter mSectionsPagerAdapter;
//
//	/**
//	 * The {@link ViewPager} that will host the section contents.
//	 */
//	ViewPager mViewPager;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		// Set up the action bar.
//		final ActionBar actionBar = getActionBar();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//		// Create the adapter that will return a fragment for each of the three
//		// primary sections of the app.
//		mSectionsPagerAdapter = new SectionsPagerAdapter(
//				getSupportFragmentManager());
//
//		// Set up the ViewPager with the sections adapter.
//		mViewPager = (ViewPager) findViewById(R.id.pager_tabLayout);
//		mViewPager.setAdapter(mSectionsPagerAdapter);
//
//		// When swiping between different sections, select the corresponding
//		// tab. We can also use ActionBar.Tab#select() to do this if we have
//		// a reference to the Tab.
//		mViewPager
//				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//					@Override
//					public void onPageSelected(int position) {
//						actionBar.setSelectedNavigationItem(position);
//					}
//				});
//
//		// For each of the sections in the app, add a tab to the action bar.
//		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//			// Create a tab with text corresponding to the page title defined by
//			// the adapter. Also specify this Activity object, which implements
//			// the TabListener interface, as the callback (listener) for when
//			// this tab is selected.
//			actionBar.addTab(actionBar.newTab()
//					.setText(mSectionsPagerAdapter.getPageTitle(i))
//					.setTabListener(this));
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public void onTabSelected(ActionBar.Tab tab,
//			FragmentTransaction fragmentTransaction) {
//		// When the given tab is selected, switch to the corresponding page in
//		// the ViewPager.
//		mViewPager.setCurrentItem(tab.getPosition());
//	}
//
//	@Override
//	public void onTabUnselected(ActionBar.Tab tab,
//			FragmentTransaction fragmentTransaction) {
//	}
//
//	@Override
//	public void onTabReselected(ActionBar.Tab tab,
//			FragmentTransaction fragmentTransaction) {
//	}
//
//	/**
//	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
//	 * one of the sections/tabs/pages.
//	 */
//	public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//		public SectionsPagerAdapter(FragmentManager fm) {
//			super(fm);
//		}
//
//		@Override
//		public Fragment getItem(int position) {
//			// getItem is called to instantiate the fragment for the given page.
//			// Return a DummySectionFragment (defined as a static inner class
//			// below) with the page number as its lone argument.
//			Fragment fragment = new DummySectionFragment();
//			Bundle args = new Bundle();
//			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//			fragment.setArguments(args);
//			return fragment;
//		}
//
//		@Override
//		public int getCount() {
//			// Show 3 total pages.
//			return 3;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int position) {
//			Locale l = Locale.getDefault();
//			switch (position) {
//			case 0:
//				return getString(R.string.title_section1).toUpperCase(l);
//			case 1:
//				return getString(R.string.title_section2).toUpperCase(l);
//			case 2:
//				return getString(R.string.title_section3).toUpperCase(l);
//			}
//			return null;
//		}
//	}
//
//	/**
//	 * A dummy fragment representing a section of the app, but that simply
//	 * displays dummy text.
//	 */
//	public static class DummySectionFragment extends Fragment {
//		/**
//		 * The fragment argument representing the section number for this
//		 * fragment.
//		 */
//		public static final String ARG_SECTION_NUMBER = "section_number";
//
//		public DummySectionFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main_dummy,
//					container, false);
//			TextView dummyTextView = (TextView) rootView
//					.findViewById(R.id.section_label);
//			dummyTextView.setText(Integer.toString(getArguments().getInt(
//					ARG_SECTION_NUMBER)));
//			return rootView;
//		}
//	}
//
// }
