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

/**
 * Balanced Acceptance Sampling - MainActivity
 *  
 * The BAS application implements the balanced acceptance sampling
 * technique (Robertson, 2013) on Android to support generation of 
 * sample points and to facilitate location and tracking of data
 * collection at each of those points.  
 * <br/><br/>
 * 
 * The application generates a set of sample points based on parameters 
 * given by the user (number of samples, number of over-samples, and 
 * the study area defined in a KML file).  Once the points have been 
 * generated they can be listed in a table or displayed on a map
 * (implemented using GoogleMaps).  
 * <br/><br/>
 * 
 * From either the table or map view, the user can select a point 
 * and annotate it with information about the collection status and
 * brief narrative comments.  
 * <br/><br/>
 * 
 * Parameters used to generate the sample and information about the 
 * sample points (location, status, comments) are stored in an
 * SQLite database on the Android enabled device.  Data can be 
 * exported to use on other devices.
 * <br/><br/>
 * 
 * <strong>Reference:</strong><br/>
 * B. L. Robertson, J. A. Brown, T. McDonald, and P. Jaksons (2013) 
 * <a href="http://onlinelibrary.wiley.com/doi/10.1111/biom.12059/abstract">BAS: Balanced Acceptance Sampling of Natural Resources</a>.  
 * Biometrics, 69(3):776-784.
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 */
public class MainActivity extends FragmentActivity {

	/** Pager that manages the two display layouts (table and map) **/
	protected ViewPager mViewPager;
	
	/** Bar along the top of the UI that displays "tabs" for the
	 * two displays: table and map. **/
	protected ActionBar mActionBar;

	/** The (database safe) name of the study currently displayed **/
	protected String mCurrentStudyName;
	
	/** A StudyArea object that contains the details of the study 
	 * that is currently displayed.  This object is used to store 
	 * details pulled from the database an used to refresh the display.
	 * There is the potential for the object to get out of sync with
	 * the current database contents; reduces the number of calls to
	 * the database. **/
	protected StudyArea mCurrentStudy;
	
	/** Initialize the two layouts (table and map).  When the application 
	 * reopens, it reinstates the previous configuration, which includes
	 * loading a study and selecting between the two layouts. **/
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
	 * gesture or by selecting the respective tabs. Although used just 
	 * once (during onCreate()), it organizes the code by containing 
	 * all of the widget initialization, e.g., connecting to the 
	 * widgets in the XML layout (excluding the menu).
	 * @see #onCreate(Bundle)
	 * @see #onCreateOptionsMenu(Menu)
	 */
	private void initInteraction(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		TabPagerAdapter mTabAdapter = new TabPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager_tabLayout);
		mViewPager.setAdapter(mTabAdapter);
		// make sure the "tabs" reflect the same layout selection
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
		// when navigating with tabs, ensure that the layouts match the selection
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

	/** Store the view that was active and the study that is displayed 
	 * so that it can be automatically loaded when the application reopens. */
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
		outState.putString("study", mCurrentStudyName);
	}
	
	/** Clean up any sub-tasks and store state and data that may 
	 * have been modified or created during execution of the application
	 * (Not yet implemented) **/
	@Override
	protected void onPause(){
		//TODO clean up on exit
		//http://stackoverflow.com/questions/18309958/activity-gets-crashed-with-fatal-signal-11-sigsegv-at-0x00000200-code-1
		super.onPause();
	}


	/** Specify the layout for the menu and inflate the menu 
	 * items that it contains.  (Functionality is attached in
	 * a separate method.) 
	 * @see #onOptionsItemSelected(MenuItem)
	 **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Initiate actions for each menu item defined in the XML.  
	 * Execution of the requested actions is mediated through 
	 * dialog displays. 
	 * @see #onCreateOptionsMenu(Menu)
	 * @see #createBAS()
	 * @see #loadBAS()
	 * @see #exportBAS()
	 **/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create:
			createBAS();
			return true;
		case R.id.action_load:
			loadBAS();
			return true;
		case R.id.action_export:
			exportBAS();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Present a dialog to solicit parameters for creating a new
	 * sample.  All values are required; checks to ensure that a
	 * value has been entered for each field.
	 * <br/><br/>
	 * 
	 * The study area is specified as a KML file.  The dialog
	 * presents a file browser, but the validity of the file is 
	 * not checked until attempting to open the file (i.e., after
	 * the user input has been accepted).	
	 * <br/><br/>
	 * 
	 * The processing to read the file and generate the BAS is
	 * conducted on a non-UI thread.
	 * 
	 * @see #onOptionsItemSelected(MenuItem)
	 * @see CreateBASDialog
	 * @see GenerateSample
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
										if(studyArea==null){
											displayToast("Error reading study area.");
										}else if (studyArea.isValid()) {
											mCurrentStudy = studyArea;
											GenerateSample g = new GenerateSample(MainActivity.this,
													mCurrentStudy,nSamples,nOversamples,new RefreshCallback(){
												@Override
												public void onTaskComplete(String message) {
													refreshMainDisplays();
													displayToast(message);
												}
											});
											g.execute();
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


	/** Present a list of studies (as found in the database) to the user
	 * for them to make a selection.  When selected, the display is refreshed.
	 */
	private void loadBAS() {
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
	}

	/** Currently Stubbed! (displays toast to indicate selection, but no functionality)
	 * Method that will export information about each BAS (the parameters
	 * used to generate the sample and the samples themselves) from the
	 * application's SQLite database to the SD card or to an external server.
	 */
	private void exportBAS(){
		displayToast("[MainActivity] Need to implement the export!");
	}
	

	/** The main display includes both a map and a table to present the 
	 * BAS samples.  These displays are populated based on the contents of
	 * the application's SQLite database.  When there have been changes
	 * to those contents (e.g, from generating a new sample of points 
	 * on the non-UI thread), the displays must be prompted to refresh.
	 * <br/><br/>
	 * 
	 * The application state includes a string representation of the 
	 * names of the current BAS in the display (null if no study has been 
	 * created or selected).  Using this name, all other details of the 
	 * study can be queried from the database.
	 * 
	 * @see #mCurrentStudyName
	 */
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
	
	/** A helper method to present narrative feedback to the user. All 
	 * message use the same length (LONG).
	 * @param message Narrative feedback to display
	 */
	private void displayToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
