package west.sample.bas;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	// Layout objects that facilitate interaction
	private TabPagerAdapter mTabAdapter;
	private ViewPager mViewPager;
	private ActionBar mActionBar;

	// Static for now... figure out how to pass it around
	protected static String sCurrentStudy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initInteraction(savedInstanceState);
		
		// restore previous state
		if (savedInstanceState != null) {
			int currentTab = savedInstanceState.getInt("tab", 0);
			sCurrentStudy = savedInstanceState.getString("study");
			mActionBar.setSelectedNavigationItem(currentTab);
			mViewPager.setCurrentItem(currentTab);
		} else {
			mActionBar.setSelectedNavigationItem(0);
			mViewPager.setCurrentItem(0);
		}
		
		// refresh the two displays
		refreshMainDisplays();

	}

	/** Set up the main layout with two views: a map and a table
	 * The user navigates between these views using either a swipe
	 * gesture or by selecting the respective tabs. */
	private void initInteraction(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		mTabAdapter = new TabPagerAdapter(getSupportFragmentManager());
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
		outState.putString("study", sCurrentStudy);
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
				// possibly autocomplete (and change the "create" button to a "load" button?
				Log.d("study name","Check if the study name already exists");
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
					}
				});

		Button fileBrowseBtn = (Button) layout
				.findViewById(R.id.button_fileBrowser);
		fileBrowseBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				displayToast("Implement the file browser");
				studyNameLabel.setTextColor(getResources().getColor(
						android.R.color.black));
				filenameLabel.setTextColor(getResources().getColor(
						android.R.color.black));
				filenameTxt.setText("Selected!");
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
						int nSamples = getInt(numberSamplesTxt);
						int nOversamples = getInt(numberOversamplesTxt);
						String studyAreaFilename = filenameTxt.getText().toString();
						boolean isValid = true;
						
						// highlight missing values
						if(studyName.isEmpty()){
							displayToast("Empty study name or invalid characters");
							numberSamplesLabel.setTextColor(getResources().getColor(R.color.highlight));
							isValid = false;	
						}else{
							numberSamplesLabel.setTextColor(getResources().getColor(android.R.color.black));
						}

						if(nSamples<1){
							displayToast("New studies must have at least one sample");
							numberSamplesLabel.setTextColor(getResources().getColor(R.color.highlight));
							isValid = false;
						}else{
							numberSamplesLabel.setTextColor(getResources().getColor(android.R.color.black));
						}
						
						if(nOversamples<0){
							displayToast("A number of oversamples is required (may be zero)");
							numberOversamplesLabel.setTextColor(getResources().getColor(R.color.highlight));
							isValid = false;
						}else{
							numberOversamplesLabel.setTextColor(getResources().getColor(android.R.color.black));
						}
						
						if(studyAreaFilename == null || studyAreaFilename.isEmpty() ||
								studyAreaFilename.equals(getResources().getString(R.string.label_studyAreaFilename))){
							displayToast("A study area is required");
							filenameLabel.setTextColor(getResources().getColor(R.color.highlight));
							isValid = false;
						}else{
							filenameLabel.setTextColor(getResources().getColor(android.R.color.black));
						}
						
						if(isValid){
							GenerateSample g = new GenerateSample(getBaseContext(),
									studyName,studyAreaFilename,nSamples,nOversamples);
							g.execute();
							dialog.dismiss();
						}
					}});
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

	protected String getCleanString(EditText textField) {
		String raw = textField.getText().toString();
		// TODO strip non alpha characters
		return "clean" + raw;
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
						sCurrentStudy = (String) spinner.getSelectedItem();
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
		displayToast("Refresh the map and table");
		
		// refresh the map
		
		// refresh the table
		//this.getFragmentManager().
		//View myList = this.getWindow().findViewById(R.id.layout_table);
//		if(myList instanceOf TableFragment){
//			Log.d("table","yup, it's the thing I want.");
//		}
//		LoadSample ls = new LoadSample(new FragmentCallback() {
//
//            @Override
//            public void onTaskDone() {
//                methodThatDoesSomethingWhenTaskIsDone();
//				adapter.notifyDataSetChanged();
//            }
//        });
		
		//((ListView) findViewById(android.R.id.list)).getAdapter();
		// TODO refresh the current view (map or table)
		//displayToast("Selected "+ selectedItem);

	}
	
	private void displayToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
}
