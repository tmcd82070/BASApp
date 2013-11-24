package com.west.bas;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.sample.GenerateSample;
import com.west.bas.spatial.StudyArea;
import com.west.bas.ui.CreateBASCallback;
import com.west.bas.ui.CreateBASDialog;
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
	
	//TODO clean up on exit
	protected void onPause(){
		//http://stackoverflow.com/questions/18309958/activity-gets-crashed-with-fatal-signal-11-sigsegv-at-0x00000200-code-1
	}
	
	
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
		AlertDialog dialog = CreateBASDialog.getCreateBASDialog(
				this, 
				new CreateBASCallback() {
					@Override
					public void onTaskComplete(String studyName,
							final int nSamples, 
							final int nOversamples,
							String studyAreaFilename) {
						mCurrentStudyName = studyName;

						ReadStudyAreaTask reader = new ReadStudyAreaTask(
								studyAreaFilename, 
								studyName,
								new ReadFileCallback() {
									@Override
									public void onTaskComplete(
											StudyArea studyArea) {
										if (studyArea.isValid()) {
											generateSamplesForStudyArea(
													studyArea, nSamples,
													nOversamples);
										} else {
											displayToast(studyArea
													.getFailMessage());
										}
									}
								});
						reader.execute();

					}
				});
		
		dialog.show();				
	}

	protected void generateSamplesForStudyArea(StudyArea studyArea, int nSamples, int nOversamples){
		mCurrentStudy = studyArea;
		GenerateSample g = new GenerateSample(this,
				mCurrentStudy,nSamples,nOversamples,
				new RefreshCallback(){
					@Override
					public void onTaskComplete() {
						refreshMainDisplays();
					}});
		g.execute();	
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
