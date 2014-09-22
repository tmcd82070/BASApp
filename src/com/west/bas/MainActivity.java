package com.west.bas;

import java.util.ArrayList;
import java.util.Vector;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
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

import com.west.bas.database.ExportDataAsyncTask;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.UpdateSampleAsyncTask;
import com.west.bas.sample.GenerateSample;
import com.west.bas.spatial.LastKnownLocation;
import com.west.bas.spatial.ReadStudyAreaAsyncTask;
import com.west.bas.spatial.ReadStudyAreaCallback;
import com.west.bas.spatial.StudyArea;
import com.west.bas.ui.ColorHelper;
import com.west.bas.ui.CreateBASCallback;
import com.west.bas.ui.CreateBASDialog;
import com.west.bas.ui.ExportCallback;
import com.west.bas.ui.ExportDialog;
import com.west.bas.ui.PrivacyPolicyCallback;
import com.west.bas.ui.PrivacyPolicyDialog;
import com.west.bas.ui.RefreshCallback;
import com.west.bas.ui.TabPagerAdapter;
import com.west.bas.ui.map.MapFragmentDual;
import com.west.bas.ui.table.DetailListAdapter;

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

	/** The name of the current study (selected by the user or recalled from saved state) */
	private String mStudyName = null;
	
	/** The tab that is 'selected' (by the user or recalled from saved state) */
	private int mCurrentTab = TabPagerAdapter.MAP_ITEM;

	/** A list of async tasks that can be cleaned up on close or pause */
	private Vector<AsyncTask<?,?,?>> taskList = new Vector<AsyncTask<?,?,?>>();
	
	
	/** Initialize the two layouts (table and map).  When the application 
	 * reopens, it reinstates the previous configuration, which includes
	 * loading a study and selecting between the two layouts. **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// restore previous state
		if (savedInstanceState != null) {
			// the name of the study displayed
			mStudyName = savedInstanceState.getString("study");
			String studyAreaFilename = SampleDatabaseHelper.getInstance(this).getSHPFilename(mStudyName);
			if(studyAreaFilename != null){
				// Try to read the study area KML file 
				ReadStudyAreaAsyncTask reader = new ReadStudyAreaAsyncTask(
						studyAreaFilename, 
						mStudyName,
						new ReadStudyAreaCallback() {
							@Override
							public void onTaskComplete(StudyArea studyArea) {
								if(studyArea==null)	clearCurrentStudyDetails();
							}
						});
				taskList.add(reader);
				reader.execute();
			}
			// Which tab is displayed (map or table)
			mCurrentTab = savedInstanceState.getInt("tab", TabPagerAdapter.MAP_ITEM);
		}
		
		AlertDialog dialog = new PrivacyPolicyDialog(
				this, 
				new PrivacyPolicyCallback(){
					@Override
					public void onTaskComplete(
							boolean hasAcceptedPrivacyPolicy,
							boolean hasProvidedConsentToLocation) {
						setUserConsent(hasAcceptedPrivacyPolicy, hasProvidedConsentToLocation);
					}});
		dialog.show();	
	}

	/** Store the view that was active and the study that is displayed 
	 * so that it can be automatically loaded when the application reopens. */
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
		outState.putString("study", mStudyName);
		super.onSaveInstanceState(outState);
	}

	
	/** When a response from the user has been received regarding agreement
	 * with the Google Maps terms and conditions and whether or not they
	 * consent to having their location accessed and plotted on the map, set 
	 * the application state and initialize the UI, or exit if declined.
	 * 
	 * @param hasAcceptedPrivacyPolicy
	 * @param hasProvidedConsentToLocation
	 */
	protected void setUserConsent(
			boolean hasAcceptedPrivacyPolicy, 
			boolean hasProvidedConsentToLocation) {
		
		// Currently acts as if the map is critical to the application
		// if the user does not accept the policy, quit the application
		if(!hasAcceptedPrivacyPolicy){
			finish();
            System.exit(0);
		}
		
		// provide the activity context to get the color resources
		ColorHelper.init(this);
		
		// initialize the UI
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
		
		// At this point, the user accepted the terms. Set whether or not
		// the user gave consent to access and map their location and provide 
		// feedback to confirm their selection.
		String message = "Privacy policy accepted.\nGPS location will ";
		if(hasProvidedConsentToLocation){
			LastKnownLocation.giveUserConsent(this.getApplicationContext(),new RefreshCallback(){

				@Override
				public void onTaskComplete(String toastMessage) {
					refreshDisplays(true);
					displayToast(toastMessage);
				}});
		}else{
			message += "NOT ";
		}
		message += "be shown.";
		displayToast(message);
		
		MapFragmentDual mapFragment = 
				(MapFragmentDual) ((TabPagerAdapter) mViewPager.getAdapter())
				.getItem(TabPagerAdapter.MAP_ITEM);
		mapFragment.setUserLocation(hasProvidedConsentToLocation);
		
		// Update the displays if there was saved state (which study and which view)
		refreshDisplays(true);
		mActionBar.setSelectedNavigationItem(mCurrentTab); 
		mViewPager.setCurrentItem(mCurrentTab); 
	}
	
	/** Clean up any sub-tasks and store state and data that may 
	 * have been modified or created during execution of the application
	 * (Not yet implemented) **/
	@Override
	protected void onPause(){
		for(AsyncTask<?,?,?> task : taskList){
			// TODO  !(task instanceof GenerateSample)
			if(task!=null){
				Status status = task.getStatus();
				if(status==Status.RUNNING){
					task.cancel(true);
				}
				task = null;
			}
		}
		super.onPause();
	}

	@Override
	protected void onDestroy(){
		//TODO clean up on exit
		// make sure all the cursors are closed
		//http://stackoverflow.com/questions/18309958/activity-gets-crashed-with-fatal-signal-11-sigsegv-at-0x00000200-code-1
		
		// check that the cursor got closed (but should be maintained because of the use of swap()
//		ListView tableListView = (ListView) this.getWindow().findViewById(android.R.id.list);
//		if(tableListView!=null){
//			DetailListAdapter adapter = (DetailListAdapter) tableListView.getAdapter();
//			if(adapter!=null){
//				Cursor c = adapter.getCursor();
//				if(c!=null) c.close();
//			}
//		}
		
		// Also doesn't seem to be it... map is null.
//		MapFragmentDual mapFragment = 
//				(MapFragmentDual) ((TabPagerAdapter) mViewPager.getAdapter())
//				.getItem(TabPagerAdapter.MAP_ITEM);
//		if(mapFragment!=null){
//			GoogleMap map = mapFragment.getGoogleMap();
//			if(map!=null) map.stopAnimation();
//		}
		super.onDestroy();
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
		AlertDialog dialog = new CreateBASDialog(
				this, 
				new CreateBASCallback() {
					@Override
					public void onTaskComplete(String studyName,
							int nSamples, 
							int nOversamples,
							String studyAreaFilename) {
						handleUserInputOfStudyDetails(studyName, nSamples,nOversamples,studyAreaFilename);
					}
				});
		dialog.show();		
	}

	private void handleUserInputOfStudyDetails(
			String studyName, 
			final int nSamples, 
			final int nOversamples, 
			String studyAreaFilename){
		// Store the user input 
		mStudyName = studyName;
		
		// Try to read the study area KML file 
		ReadStudyAreaAsyncTask reader = new ReadStudyAreaAsyncTask(
				studyAreaFilename, 
				studyName,
				new ReadStudyAreaCallback() {
					@Override
					public void onTaskComplete(StudyArea studyArea) {
						if(studyArea==null){
							displayToast("Error reading study area.");
							clearCurrentStudyDetails();
						}else if (studyArea.isValid()) {
							// last piece of required info needed to create the sample
							generateSamples(studyArea,nSamples,nOversamples);
						} else {
							displayToast(studyArea.getFailMessage());
							clearCurrentStudyDetails();
						}
					}
				});
		taskList.add(reader);
		reader.execute();
	}
	
	protected void clearCurrentStudyDetails() {
		mStudyName = null;
		mCurrentTab = TabPagerAdapter.MAP_ITEM;
	}

	private void generateSamples(StudyArea studyArea, int nSamples, int nOversamples){
		// Check that all four values were set
		if(mStudyName==null || 
				studyArea==null || !studyArea.isValid() ||
				nSamples==0 || nOversamples==0){
			displayToast("The four necessary values were not all "
					+ "initialized (required: study name, study area, "
					+ "# samples, # oversamples).  Please try again.");
			Log.d("userInput","Study name: "+mStudyName);
			Log.d("userInput","Study area: "+studyArea);
			Log.d("userInput","Study samples: "+nSamples);
			Log.d("userInput","Study oversample: "+nOversamples);
			clearCurrentStudyDetails();
		}else{
			GenerateSample g = new GenerateSample(MainActivity.this,
					studyArea, nSamples, nOversamples,new RefreshCallback(){
				@Override
				public void onTaskComplete(String message) {
					refreshDisplays(true);
					displayToast(message);
				}
			});
			taskList.add(g);
			g.execute();
		}
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
		SampleDatabaseHelper db = SampleDatabaseHelper.getInstance(getBaseContext());
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
						mStudyName = (String) spinner.getSelectedItem();
						loadStudyArea();
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

	protected void loadStudyArea() {
		String studyAreaFilename = SampleDatabaseHelper.getInstance(this).getSHPFilename(mStudyName);
		if(studyAreaFilename != null){
			// Try to read the study area KML file 
			ReadStudyAreaAsyncTask reader = new ReadStudyAreaAsyncTask(
					studyAreaFilename, 
					mStudyName,
					new ReadStudyAreaCallback() {
						@Override
						public void onTaskComplete(StudyArea studyArea) {
							if(studyArea==null)	clearCurrentStudyDetails();
							else refreshDisplays(true);
						}
					});
			taskList.add(reader);
			reader.execute();
		}
	}

	/** Currently Stubbed! (displays toast to indicate selection, but no functionality)
	 * Method that will export information about each BAS (the parameters
	 * used to generate the sample and the samples themselves) from the
	 * application's SQLite database to the SD card or to an external server.
	 */
	private void exportBAS(){
		new ExportDialog(this, mStudyName, new ExportCallback(){
			@Override
			public void onTaskComplete(boolean exportAll, String exportFilename) {
				writeData(exportAll,exportFilename);
			}}).show();
	}
	
	private void writeData(boolean b, String filename){
		ExportDataAsyncTask exporter = new ExportDataAsyncTask(this,filename,b,mStudyName);
		taskList.add(exporter);
		exporter.execute();
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
	 * study are queried from the database.
	 * 
	 * @see #sCurrentStudyName
	 */
	public void refreshDisplays(boolean resetMapDisplay){
		if(mStudyName!=null && !mStudyName.isEmpty()){
			SampleDatabaseHelper db = SampleDatabaseHelper.getInstance(getBaseContext());
			Cursor cursor = db.getSamplePointsForStudy(mStudyName);

			// refresh the map
			MapFragmentDual mapFragment = 
					(MapFragmentDual) ((TabPagerAdapter) mViewPager.getAdapter())
					.getItem(TabPagerAdapter.MAP_ITEM);
			if(resetMapDisplay) mapFragment.loadNewStudy();
			mapFragment.refresh(mStudyName, cursor);
		
			// refresh the table
			ListView tableListView = (ListView) this.getWindow().findViewById(android.R.id.list);
			if(tableListView!=null){
				DetailListAdapter adapter = (DetailListAdapter) tableListView.getAdapter();
				//TODO difference between change cursor and swap cursor
				adapter.swapCursor(cursor);
			}
			setTitle("Sample: "+mStudyName);
		}
	}

	/** A helper method to present narrative feedback to the user. All 
	 * message use the same length (LONG).
	 * @param message Narrative feedback to display
	 */
	private void displayToast(String message) {
		if(message!=null){
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
	
	/** static method for callback when the user has entered updates for a sample */
	public void updateSamplePoint(
			int itemID, 
			SampleDatabaseHelper.Status status, 
			String comment){
		UpdateSampleAsyncTask updater = new UpdateSampleAsyncTask(
				this, mStudyName, 
				itemID, status, comment, new RefreshCallback(){
					@Override
					public void onTaskComplete(String toastMessage) {
						refreshDisplays(false);
					}});
		taskList.add(updater);
		updater.execute();
	}
}
