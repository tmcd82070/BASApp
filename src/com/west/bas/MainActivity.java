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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.vividsolutions.jts.geom.Coordinate;
import com.west.bas.database.ExportDataAsyncTask;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.database.UpdateSampleAsyncTask;
import com.west.bas.sample.GenerateSample;
import com.west.bas.spatial.ReadStudyAreaAsyncTask;
import com.west.bas.spatial.ReadStudyAreaCallback;
import com.west.bas.spatial.StudyArea;
import com.west.bas.ui.ColorHelper;
import com.west.bas.ui.CreateBASCallback;
import com.west.bas.ui.CreateBASDialog;
import com.west.bas.ui.ExportCallback;
import com.west.bas.ui.ExportDialog;
import com.west.bas.ui.RefreshCallback;
import com.west.bas.ui.TabPagerAdapter;
import com.west.bas.ui.UpdateSampleCallback;
import com.west.bas.ui.UpdateSampleDialog;
import com.west.bas.ui.map.MapFragmentDual;
import com.west.bas.ui.map.MapViewDraw;
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

	/** The (database safe) name of the study currently displayed **/
	protected String mCurrentStudyName;
	
	/** Temporary storage space for the number of samples to generate.  
	 * These values are stored as part of the activity while the study
	 * area KML file is read.  If successful, these values are used to
	 * create a new entry in the studies table of the database; if not,
	 * these values are discarded. **/
	protected int mCurrentStudySamples;
	
	/** Temporary storage space for the number of over samples to generate. 
	 * @see #mCurrentStudySamples */
	protected int mCurrentStudyOversamples;
	
	/** A StudyArea object that contains the details of the study 
	 * that is currently displayed.  This object is used to store 
	 * details pulled from the database an used to refresh the display.
	 * There is the potential for the object to get out of sync with
	 * the current database contents; reduces the number of calls to
	 * the database. **/
	protected StudyArea mCurrentStudyArea;
	
	/** The GoogleMaps polygon that represents the study area */
	private Polygon mStudyAreaPolygon;
	
	private RefreshCallback mRefreshCallback = new RefreshCallback(){
		@Override
		public void onTaskComplete(String message) {
			refreshMainDisplays();
			displayToast(message);
		}
	};
	
	private Vector<AsyncTask<?,?,?>> taskList = new Vector<AsyncTask<?,?,?>>();
	
	/** Initialize the two layouts (table and map).  When the application 
	 * reopens, it reinstates the previous configuration, which includes
	 * loading a study and selecting between the two layouts. **/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ColorHelper.init(this);
		
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
		for(AsyncTask<?,?,?> task : taskList){
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
//		if(mapFragment!=null && mapFragment.isGoogleMap()){
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
			int nSamples, 
			int nOversamples, 
			String studyAreaFilename){
		// Store the user input 
		mCurrentStudyName = studyName;
		mCurrentStudySamples = nSamples;
		mCurrentStudyOversamples = nOversamples;
		
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
							mCurrentStudyArea = studyArea;
							generateSamples();
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
		mCurrentStudyName = null;
		mCurrentStudyArea = null;
		mCurrentStudySamples = 0;
		mCurrentStudyOversamples = 0;
	}

	private void generateSamples(){
		// Check that all four values were set
		if(mCurrentStudyName==null || mCurrentStudyArea==null ||
				!mCurrentStudyArea.isValid() ||
				mCurrentStudySamples==0 || mCurrentStudyOversamples==0){
			displayToast("The four necessary values were not all "
					+ "initialized (required: study name, study area, "
					+ "# samples, # oversamples).  Please try again.");
			Log.d("userInput","Study name: "+mCurrentStudyName);
			Log.d("userInput","Study area: "+mCurrentStudyArea);
			Log.d("userInput","Study samples: "+mCurrentStudySamples);
			Log.d("userInput","Study oversample: "+mCurrentStudyOversamples);
			clearCurrentStudyDetails();
		}else{
			GenerateSample g = new GenerateSample(MainActivity.this,
					mCurrentStudyArea,mCurrentStudySamples,
					mCurrentStudyOversamples,mRefreshCallback);
			taskList.add(g);
			g.execute();
		}
	}

	
	protected void getSampleStatus(final int id) {
		UpdateSampleDialog dialog = new UpdateSampleDialog(
				this,
				new UpdateSampleCallback(){
					@Override
					public void onTaskComplete(
							SampleDatabaseHelper.Status status,
							String comment) {
						updateSamplePoint(id, status, comment);
					}
				});
		dialog.show();
		Log.d("click","[MainActivity] selected: "+id);
	}
	
	
	public void updateSamplePoint(int itemID, SampleDatabaseHelper.Status status, String comment){
		UpdateSampleAsyncTask updater = new UpdateSampleAsyncTask(
				getBaseContext(), mCurrentStudyName, 
				itemID, status, comment, mRefreshCallback);
		taskList.add(updater);
		updater.execute();
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
		new ExportDialog(this, mCurrentStudyName, new ExportCallback(){
			@Override
			public void onTaskComplete(boolean exportAll, String exportFilename) {
				writeData(exportAll,exportFilename);
			}}).show();
	}
	
	private void writeData(boolean b, String filename){
		ExportDataAsyncTask exporter = new ExportDataAsyncTask(this,filename,b,mCurrentStudyName);
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
	 * study can be queried from the database.
	 * 
	 * @see #mCurrentStudyName
	 */
	private void refreshMainDisplays(){
		if(mCurrentStudyName!=null && !mCurrentStudyName.isEmpty()){
			SampleDatabaseHelper db = new SampleDatabaseHelper(getBaseContext());
			
			// Determine whether or not the study has been retrieved from the database
			// if not, read in the study details (then try again)
			if(mCurrentStudyArea==null){
				ReadStudyAreaAsyncTask reader = new ReadStudyAreaAsyncTask(db.getSHPFilename(mCurrentStudyName),mCurrentStudyName, new ReadStudyAreaCallback(){
					@Override
					public void onTaskComplete(StudyArea studyArea) {
						mCurrentStudyArea = studyArea;
						if(mCurrentStudyArea==null){
							displayToast("Error loading a study: "+mCurrentStudyName);
						}else{
							refreshMainDisplays();
						}
					}});
				taskList.add(reader);
				reader.execute();
				return;
			}
			
			// If the study has already been read in, display its contents in the map and table
			Cursor cursor = db.getSamplePointsForStudy(mCurrentStudyName);
		
			// If the map is a GoogleMap, place markers at each sample location
			// Otherwise, draw points on a simple canvas
			MapFragmentDual mapFragment = 
					(MapFragmentDual) ((TabPagerAdapter) mViewPager.getAdapter())
					.getItem(TabPagerAdapter.MAP_ITEM);
			
			if(mapFragment.isGoogleMap()){
				GoogleMap map = mapFragment.getGoogleMap();
				if(map!=null && mCurrentStudyArea!=null){
					map.clear();

					// Respond to user clicks within the GoogleMap
					//TODO only set this up once...
					map.setOnMarkerClickListener(new OnMarkerClickListener(){
						@Override
						public boolean onMarkerClick(Marker m) {
							int id = Integer.valueOf(m.getTitle());
							getSampleStatus(id);
							// consume the event (don't proceed to default action(s))
							return true;
						}});

					map.moveCamera(CameraUpdateFactory.newLatLng(mCurrentStudyArea.getCenterLatLng()));
					
					// draw the study area bounds on the map
					Coordinate[] coords = mCurrentStudyArea.getBoundaryPoints();
					ArrayList<ArrayList<LatLng>> holes = mCurrentStudyArea.getHoles();
					if(mStudyAreaPolygon==null){
						// Instantiates a new Polygon object and adds points to define a rectangle
						PolygonOptions polygonOptions = new PolygonOptions();
						for(int i=0;i<coords.length;i++){
							polygonOptions.add(new LatLng(coords[i].x, coords[i].y));
						}
						for(ArrayList<LatLng> hole : holes) polygonOptions.addHole(hole);
						// Draw with transparent fill
						polygonOptions.fillColor(android.R.color.transparent);
						// Draw with red outline
						polygonOptions.strokeColor(R.color.highlight);
						// Add the polygon to the map and store a handle (to reuse the color settings)
						mStudyAreaPolygon = map.addPolygon(polygonOptions);
					}else{
						ArrayList<LatLng> points = new ArrayList<LatLng>();
						for(int i=0;i<coords.length;i++){
							points.add(new LatLng(coords[i].x, coords[i].y));
						}
						mStudyAreaPolygon.setPoints(points);
						mStudyAreaPolygon.setHoles(holes);
					}
					
					// draw the sample points on the map
					cursor.moveToFirst();
					while(!cursor.isAfterLast()){
						float x = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
						float y = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
						// ID is used to determine which row of the database to update
						int id = cursor.getInt(cursor.getColumnIndex(SampleInfo._ID));
						// status is used to color code the marker (i.e., select the icon)
						String typeLabel = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS));
						MarkerOptions marker = new MarkerOptions().position(new LatLng(y, x))
								.title(""+id)
								.draggable(false);
						switch(SampleDatabaseHelper.Status.getValueFromString(typeLabel)){
						case SAMPLE: 
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_sample)); 
							break;
						case OVERSAMPLE: 
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_oversample)); 
							break;
						case REJECT: 
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_reject)); 
							break;
						case COLLECTED: 
							marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_collected)); 
							break;
						}
						map.addMarker(marker);
						Log.d("checkPoints","x: "+x+" y: "+y+", "+typeLabel);
						cursor.moveToNext();
					}
				}else{
					displayToast("Google map is null");
				}
			}else{
				// refresh the map
				MapViewDraw mapView = (MapViewDraw) this.getWindow().findViewById(R.id.mapView_drawMap);
				
				mapView.initView(cursor,mapView.getWidth(),mapView.getHeight(), mCurrentStudyArea);
				
				// for drawing on a canvas (not updated)
//				public void draw(Canvas c, Paint outline, Paint background){
//					c.drawRect(mAdjustedBB, background);
//					
//					c.drawPath(studyAreaPolygon,outline);
//					
//					studyAreaPolygon.transform(mTransform);
//				}
//				mapView.initView(cursor,mapView.getWidth(),mapView.getHeight(), db.getStudyBounds(mCurrentStudyName));
				mapView.invalidate();
			}
		
			// refresh the table
			ListView tableListView = (ListView) this.getWindow().findViewById(android.R.id.list);
			if(tableListView!=null){
				DetailListAdapter adapter = (DetailListAdapter) tableListView.getAdapter();
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
