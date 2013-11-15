package com.west.bas;

import java.io.File;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.sample.GenerateSample;
import com.west.bas.spatial.StudyArea;
import com.west.bas.ui.DetailListAdapter;
import com.west.bas.ui.MapFragmentDual;
import com.west.bas.ui.MapViewDraw;
import com.west.bas.ui.ReadFileCallback;
import com.west.bas.ui.RefreshCallback;
import com.west.bas.ui.TabPagerAdapter;

public class MainActivity extends FragmentActivity {

	// Layout objects that facilitate interaction
	protected ViewPager mViewPager;
	protected ActionBar mActionBar;

	protected String mCurrentStudyName;
	protected StudyArea mCurrentStudy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initInteraction(savedInstanceState);
		
		// restore previous state
		if (savedInstanceState != null) {
			int currentTab = savedInstanceState.getInt("tab", TabPagerAdapter.MAP_ITEM);
			mCurrentStudyName = savedInstanceState.getString("study");
			mActionBar.setSelectedNavigationItem(currentTab);
			mViewPager.setCurrentItem(currentTab);
		} else {
			mActionBar.setSelectedNavigationItem(TabPagerAdapter.MAP_ITEM);
			mViewPager.setCurrentItem(TabPagerAdapter.MAP_ITEM);
		}
		
		// refresh the two displays
		refreshMainDisplays();

	}

	/** Set up the main layout with two views: a map and a table
	 * The user navigates between these views using either a swipe
	 * gesture or by selecting the respective tabs. */
	private void initInteraction(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		TabPagerAdapter mTabAdapter = new TabPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager_tabLayout);
		mViewPager.setAdapter(mTabAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}

			@Override
			public void onPageSelected(int arg0) {
				mActionBar.setSelectedNavigationItem(arg0);

			}
		});

		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		android.app.ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
		};

		mActionBar.addTab(mActionBar.newTab()
				.setText(getString(R.string.label_map))
				.setTabListener(tabListener));
		mActionBar.addTab(mActionBar.newTab()
				.setText(getString(R.string.label_table))
				.setTabListener(tabListener));
		
		if(savedInstanceState != null) { 
			int currentTab = savedInstanceState.getInt("tab", 0); 
			mActionBar.setSelectedNavigationItem(currentTab); 
			mViewPager.setCurrentItem(currentTab); 
		} else { 
			mActionBar.setSelectedNavigationItem(0); 
			mViewPager.setCurrentItem(0); 
		} 

	}

	/** Store the view that was active and the study that is displayed */
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
		outState.putString("study", mCurrentStudyName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create:
			createBAS();
			return true;
		case R.id.action_load:
			loadBAS();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Present a dialog to solicit parameters for creating a new
	 * sample.  All values are requried; checks to ensure that a
	 * value has been entered for each field.
	 * 
	 * The study area is specified as a shapefile.  The dialog
	 * accepts a filename, but the existence and validity of the
	 * file is not checked until attempting to create the BAS.
	 * 
	 * A recommendation is provided for the number of oversamples.
	 */	
	private void createBAS() {
		final int black = getResources().getColor(android.R.color.black);
		final int highlight = getResources().getColor(R.color.highlight);
		final int warning = getResources().getColor(R.color.warning);
		
		SampleDatabaseHelper db = new SampleDatabaseHelper(getBaseContext());
		final ArrayList<String> studyList = db.getListOfStudies();
				
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		View layout = inflater.inflate(R.layout.dialog_create,
				(ViewGroup) findViewById(R.layout.activity_main));
		final EditText studyNameTxt = (EditText) layout
				.findViewById(R.id.editText_sampleName);
		final EditText numberSamplesTxt = (EditText) layout
				.findViewById(R.id.editText_sampleSize);
		final EditText numberOversamplesTxt = (EditText) layout
				.findViewById(R.id.editText_oversampleSize);

		final TextView studyNameLabel = (TextView) layout
				.findViewById(R.id.textView_labelSampleName);
		final TextView filenameLabel = (TextView) layout
				.findViewById(R.id.textView_labelStudyArea);
		final TextView numberSamplesLabel = (TextView) layout
				.findViewById(R.id.textView_labelSampleSize);
		final TextView numberOversamplesLabel = (TextView) layout
				.findViewById(R.id.textView_label_oversampleSize);
		final TextView filenameTxt = (TextView) layout
				.findViewById(R.id.textView_studyAreaFilename);

		// Notify the user if a study name already exists
		studyNameTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!studyList.contains(studyNameTxt.getText().toString())){
					studyNameLabel.setTextColor(black);
				}
			}});
		
		numberSamplesTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String raw = numberSamplesTxt.getText().toString();
					if(raw.length()>0){
						numberSamplesLabel.setTextColor(black);
					}
				}
			}});
		
		// Provide a recommendation for the number of oversamples
		numberOversamplesTxt
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						String text = numberSamplesTxt.getText().toString();
						if (text.length() > 0) {
							numberOversamplesTxt.setHint("Recommended: "
									+ getInt(numberSamplesTxt)*2);
						}
						if(!hasFocus){
							String raw = numberOversamplesTxt.getText().toString();
							if(raw.length()>0){
								numberOversamplesLabel.setTextColor(black);
							}
						} 
					}
				});

		Button fileBrowseBtn = (Button) layout
				.findViewById(R.id.button_fileBrowser);
		fileBrowseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				displayToast("Implement the file browser");
				filenameLabel.setTextColor(black);
				filenameTxt.setText("polygon.shp");
			}
		});
		
		AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setPositiveButton("Create", null);
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		final AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// check the values
						String studyName = getCleanString(studyNameTxt);
						final int nSamples = getInt(numberSamplesTxt);
						final int nOversamples = getInt(numberOversamplesTxt);
						String studyAreaFilename = filenameTxt.getText().toString();
						
						boolean isValid = true;
						// Only display one toast per check
						boolean displayedToast = false;
						
						// highlight missing values
						if(studyName.isEmpty()){
							displayToast("Empty study name or invalid characters");
							displayedToast=true;
							studyNameLabel.setTextColor(highlight);
							isValid = false;	
						}else{
							if(studyList.contains(studyName)){
								displayToast("Study "+studyName+" already exists.  Load existing BAS or select a unique name.");
								displayedToast=true;
								studyNameLabel.setTextColor(highlight);
								isValid = false;	
							}else if(!studyName.equals(studyNameTxt.getText().toString())){
								studyNameTxt.setText(studyName);
								studyNameLabel.setTextColor(warning);
								displayToast("Revised study name to contain only\nalpha-numeric characters and underscore");
								displayedToast = true;
								isValid=false;
							}else{
								studyNameLabel.setTextColor(black);
							}
						}

						if(nSamples<1){
							if(!displayedToast){
								displayToast("New studies must have at least one sample");
								displayedToast = true;
							}
							numberSamplesLabel.setTextColor(highlight);
							isValid = false;
						}else{
							numberSamplesLabel.setTextColor(black);
						}
						
						if(nOversamples<0){
							if(!displayedToast){
								displayToast("A number of oversamples is required (may be zero)");
								displayedToast = true;
							}
							numberOversamplesLabel.setTextColor(highlight);
							isValid = false;
						}else{
							numberOversamplesLabel.setTextColor(black);
						}
						
						if(studyAreaFilename == null || studyAreaFilename.isEmpty() ||
								studyAreaFilename.equals(getResources().getString(R.string.label_studyAreaFilename))){
							if(!displayedToast){
								displayToast("A study area is required");
								displayedToast = true;
							}
							filenameLabel.setTextColor(highlight);
							isValid = false;
						}else{
							filenameLabel.setTextColor(black);
						}
						
						if(isValid){
							mCurrentStudyName = studyName;
							
							File extDir = Environment.getExternalStorageDirectory();
							Log.d("mainActivity","external directory "+extDir.getAbsolutePath());
							File fileDir = getBaseContext().getFilesDir();
							
							ReadStudyAreaTask reader = new ReadStudyAreaTask(fileDir+"/"+studyAreaFilename,studyName, new ReadFileCallback(){
								@Override
								public void onTaskComplete(StudyArea studyArea) {
									if(studyArea.isValid()){
										generateSamplesForStudyArea(studyArea,nSamples,nOversamples);
									}else{
										displayToast(studyArea.getFailMessage());
									}
								}});
							reader.execute();
							dialog.dismiss();
						}
					}});
	}

	protected void generateSamplesForStudyArea(StudyArea studyArea, int nSamples, int nOversamples){
		mCurrentStudy = studyArea;
		GenerateSample g = new GenerateSample(getBaseContext(),
				mCurrentStudy,nSamples,nOversamples,
				new RefreshCallback(){
					@Override
					public void onTaskComplete() {
						refreshMainDisplays();
					}});
		g.execute();	
	}
	
	protected int getInt(EditText textField) {
		String s = textField.getText().toString();
		// checks for length, but not for character types
		// (EditText set as numeric in the xml; could use try/catch for more
		// robust)
		if (s.length() > 0)
			return Integer.valueOf(s);
		return -1;
	}

	/**
	 * Given an input field return a string that contains
	 * only alpha-numeric characters and underscore
	 * @param textField UI field that contains the user input
	 * @return cleaned string
	 */
	protected String getCleanString(EditText textField) {
		String raw = textField.getText().toString();
		raw = raw.replaceAll(" ","_");
		return raw.replaceAll("[^a-zA-Z0-9_]", "");
	}

	/** Present a list of studies (as found in the database) to the user
	 * for them to make a selection.  When selected, the display is refreshed.
	 * @return
	 */
	private boolean loadBAS() {
		LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
		View layout = inflater.inflate(R.layout.dialog_load,
				(ViewGroup) findViewById(R.layout.activity_main));

		// populate the spinner with the names of tables available in the database
		final Spinner spinner = (Spinner) layout
				.findViewById(R.id.spinner_sampleNames);
		SampleDatabaseHelper db = new SampleDatabaseHelper(getBaseContext());
		ArrayList<String> studies = db.getListOfStudies();
		if (studies == null)
			studies = new ArrayList<String>(1);
		boolean isEnabled = true;
		if (studies.isEmpty()) {
			studies.add(getString(R.string.hint_noSampleName));
			spinner.setEnabled(false);
			isEnabled = false;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getBaseContext(), android.R.layout.simple_spinner_item, studies);
		spinner.setAdapter(adapter);

		AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setPositiveButton("Load",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Object selected = spinner.getSelectedItem();
					if (selected instanceof String) {
						mCurrentStudyName = (String) spinner.getSelectedItem();
						// refresh the display to reflect the change
						refreshMainDisplays();
					} else {
						Log.e("INPUT","Spinner input: Expected a string but received "
										+ selected.getClass());
					}

				}
			});
		// let the default listener take care of closing the dialog
		builder.setNegativeButton("Cancel",null);

		final AlertDialog dialog = builder.create();
		dialog.show();
		// Turn off the load button if there are no exiting studies
		if (!isEnabled) {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		}
		return true;
	}

	private void refreshMainDisplays(){
		if(mCurrentStudyName!=null && !mCurrentStudyName.isEmpty()){
			SampleDatabaseHelper db = new SampleDatabaseHelper(getBaseContext());
			
			// Determine whether or not the study has been retrieved from the database
			// if not, read in the study details (then try again)
			if(mCurrentStudy==null){
				ReadStudyAreaTask reader = new ReadStudyAreaTask(db.getSHPFilename(mCurrentStudyName),mCurrentStudyName, new ReadFileCallback(){
					@Override
					public void onTaskComplete(StudyArea studyArea) {
						mCurrentStudy = studyArea;
						if(mCurrentStudy==null){
							displayToast("Error loading a study: "+mCurrentStudyName);
						}else{
							refreshMainDisplays();
						}
					}});
				reader.execute();
				return;
			}
			
			// If the study has already been read in, display its contents in the map and table
			Cursor cursor = db.getStudyDetails(mCurrentStudyName);
		
			// If the map is a GoogleMap, place markers at each sample location
			// Otherwise, draw points on a simple canvas
			MapFragmentDual mapFragment = (MapFragmentDual) ((TabPagerAdapter) mViewPager.getAdapter()).getItem(TabPagerAdapter.MAP_ITEM);
			if(mapFragment.isGoogleMap()){
				GoogleMap map = mapFragment.getGoogleMap();
				if(map!=null){
//					GoogleMap map = mapFragment.getMap();
					map.moveCamera(CameraUpdateFactory.newLatLng(db.getStudyCenter(mCurrentStudyName)));
					// draw the study area bounds on the map
					
					// draw the sample points on the map
					cursor.moveToFirst();
					while(!cursor.isAfterLast()){
						float x = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
						float y = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
						String typeLabel = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS));
						String comment = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_COMMENT));
						MarkerOptions marker = new MarkerOptions().position(new LatLng(y, x))
								.title(typeLabel)
								.snippet(comment);
						// TODO change color of icon based on type?
						map.addMarker(marker);
						Log.d("checkPoints","x: "+x+" y: "+y+", "+typeLabel);
						cursor.moveToNext();
					}
					displayToast("Plotted sample points");
				}else{
					displayToast("Google map is null");
				}
			}else{
				// refresh the map
				MapViewDraw mapView = (MapViewDraw) this.getWindow().findViewById(R.id.mapView_drawMap);
				
				mapView.initView(cursor,mapView.getWidth(),mapView.getHeight(), mCurrentStudy);
				mapView.invalidate();
			}
		
		// refresh the table
		ListView myList = (ListView) this.getWindow().findViewById(android.R.id.list);
		if(myList!=null){
			DetailListAdapter adapter = (DetailListAdapter) myList.getAdapter();
			//TODO difference between change cursor and swap cursor
			adapter.swapCursor(cursor);
		}
			setTitle("Sample: "+mCurrentStudyName);
		}
	}
	
	private void displayToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
