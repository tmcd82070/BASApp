package com.west.bas.ui;

import java.util.ArrayList;
import java.util.Vector;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.sample.GenerateSample;

/**
 * 
 * A balanced acceptance sample is parameterized by the number of
 * samples (and oversamples) and the extent of the study area.  The
 * CreateBASDialog collects these parameters from the user.  The 
 * values can then be sent to the GenerateSample method
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 * 
 * @see GenerateSample
 */
public class CreateBASDialog extends AlertDialog{

	/** Recommendations for the number of oversamples is based on 
	 * a scalar multiple of the number of samples. */
	public static final int RATIO_OVERSAMPLES_TO_SAMPLES = 2;
	
	// Disallow instantiation
	public CreateBASDialog(Context context, CreateBASCallback callback){
		super(context);
		final View.OnClickListener checkFieldsOnClick = 
				initLayoutWidgets(context,callback);
		
		OnClickListener typedNullListener = new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		};	
		setButton(BUTTON_POSITIVE, "Create", typedNullListener);
		setButton(BUTTON_NEGATIVE, "Cancel", typedNullListener);

		setOnShowListener(new OnShowListener(){
			@Override
			public void onShow(DialogInterface di) {
				Button createBtn = CreateBASDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
				createBtn.setOnClickListener(checkFieldsOnClick);
			}
		});	
	}

	/**
	 * Initialize listeners to accept user input and provide feedback.
	 * 
	 * The context is required to execute a query against the database
	 * for a list of names of existing studies (to compare against the
	 * name entered by the user; new names but be distinct from 
	 * existing names).
	 * 
	 * The context is also required to determine the root directory
	 * for the application (i.e., root of the file browser)
	 * 
	 * @param layout inflated view that contains dialog widgets
	 * @param context application context to locate database
	 * @param callback 
	 */
	@SuppressLint("InflateParams")
	// http://www.doubleencore.com/2013/05/layout-inflation-as-intended/
	// exception: placing view in dialog 
	private View.OnClickListener initLayoutWidgets(
			final Context context, 
			final CreateBASCallback callback) {
		
		// Inflate the layout from XML
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.dialog_create, null);
		setView(layout);
		
		// Labels (connected to facilitate color coding text)
		final TextView studyNameLabel = 
				(TextView) layout.findViewById(R.id.textView_labelSampleName);
		final TextView filenameLabel = 
				(TextView) layout.findViewById(R.id.textView_labelStudyArea);
		final TextView numberSamplesLabel = 
				(TextView) layout.findViewById(R.id.textView_labelSampleSize);
		final TextView numberOversamplesLabel = 
				(TextView) layout.findViewById(R.id.textView_label_oversampleSize);
		final TextView filenameDisplay = 
				(TextView) layout.findViewById(R.id.textView_studyAreaFilename);
		
		// Text fields that accept text input
		// Study name: must be unique for this installation (checks against
		// entries in the database
		final EditText studyNameTxt = 
				(EditText) layout.findViewById(R.id.editText_sampleName);
		// Get a list of studies from the database
		// (check against the list when the user enters a name)
		SampleDatabaseHelper db = SampleDatabaseHelper.getInstance(context);
		final ArrayList<String> studyList = db.getListOfStudies();
		studyNameTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// reset the text color when the user has made changes
				// so that the specified name is not already in the list
				if(!studyList.contains(studyNameTxt.getText().toString())){
					studyNameLabel.setTextColor(ColorHelper.black());
				}
			}});
		final EditText numberSamplesTxt = 
				(EditText) layout.findViewById(R.id.editText_sampleSize);
		numberSamplesTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					// Removes color highlighting, but doesn't check yet
					String raw = numberSamplesTxt.getText().toString();
					if(raw.length()>0){
						numberSamplesLabel.setTextColor(ColorHelper.black());
					}
				}
			}});
		final EditText numberOversamplesTxt = 
				(EditText) layout.findViewById(R.id.editText_oversampleSize);	
		numberOversamplesTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// Provide a recommendation for the number of over-samples
				String text = numberSamplesTxt.getText().toString();
				if (text.length() > 0) {
					numberOversamplesTxt.setHint("Recommended: "
							+ getInt(numberSamplesTxt)*RATIO_OVERSAMPLES_TO_SAMPLES);
				}
				if(!hasFocus){
					String raw = numberOversamplesTxt.getText().toString();
					if(raw.length()>0){
						numberOversamplesLabel.setTextColor(ColorHelper.black());
					}
				} 
			}
		});

		
		// Buttons that select the root of the browser display
		final ListView browser = 
				(ListView) layout.findViewById(R.id.listView_fileNames);
		final ToggleButton appFolderBtn = 
				(ToggleButton) layout.findViewById(R.id.toggleButton_appFolder);
		final ToggleButton sdCardBtn = 
				(ToggleButton) layout.findViewById(R.id.toggleButton_SD);
		
		appFolderBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				BrowserAdapter adapter = (BrowserAdapter) browser.getAdapter();
				adapter.ascendToRoot(false);
				// always have one selected (with option to select multiple times)
				appFolderBtn.setChecked(true);
				sdCardBtn.setChecked(false);
			}});
		sdCardBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				BrowserAdapter adapter = (BrowserAdapter) browser.getAdapter();
				adapter.ascendToRoot(true);
				// always have one selected (with option to select multiple times)
				sdCardBtn.setChecked(true);
				appFolderBtn.setChecked(false);
			}});

		// List to display the filenames
		browser.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> listView, View parentView,
					int position, long positionL) {
				String selectedString=(String) listView.getItemAtPosition(position);
				boolean isFile = ((BrowserAdapter) listView.getAdapter()).handleSelection(selectedString);
				if(isFile){
					filenameDisplay.setText(selectedString);
					filenameLabel.setTextColor(ColorHelper.black());
				}
			}});
		
		BrowserAdapter adapter = new BrowserAdapter(
				context,
				R.id.listView_fileNames,
				new Vector<String>());
		browser.setAdapter(adapter);
		
		// start in the application folder
		appFolderBtn.setChecked(true);
		adapter.ascendToRoot(false);
		
		// Verify any values entered: if valid, generate samples 
		// (otherwise provide feedback to user about why invalid)
		View.OnClickListener checkFieldsOnClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// check the values
				String studyName = SampleDatabaseHelper.getCleanStudyNameString(studyNameTxt.getText().toString());
				final int nSamples = getInt(numberSamplesTxt);
				final int nOversamples = getInt(numberOversamplesTxt);
				String studyAreaFilename = filenameDisplay.getText().toString();
				
				boolean isValid = true;
				// Only display one toast per check
				boolean displayedToast = false;
				
				// highlight missing values
				if(studyName.isEmpty()){
					Toast.makeText(context, 
							"Empty study name or invalid characters", 
							Toast.LENGTH_SHORT).show();
					displayedToast=true;
					studyNameLabel.setTextColor(ColorHelper.highlight());
					isValid = false;	
				}else{
					if(studyList.contains(studyName)){
						Toast.makeText(context, 
								"Study "+studyName+" already exists.  "+
								"Load existing BAS or select a unique name.", 
								Toast.LENGTH_SHORT).show();
						displayedToast=true;
						studyNameLabel.setTextColor(ColorHelper.highlight());
						isValid = false;	
					}else if(!studyName.equals(studyNameTxt.getText().toString())){
						studyNameTxt.setText(studyName);
						studyNameLabel.setTextColor(ColorHelper.warning());
						Toast.makeText(context, 
								"Revised study name to contain only\n"+
								"alpha-numeric characters and underscore", 
								Toast.LENGTH_SHORT).show();
						displayedToast = true;
						isValid=false;
					}else{
						studyNameLabel.setTextColor(ColorHelper.black());
					}
				}

				if(nSamples<1){
					if(!displayedToast){
						Toast.makeText(context, 
								"New studies must have at least one sample", 
								Toast.LENGTH_SHORT).show();
						displayedToast = true;
					}
					numberSamplesLabel.setTextColor(ColorHelper.highlight());
					isValid = false;
				}else{
					numberSamplesLabel.setTextColor(ColorHelper.black());
				}
				
				if(studyAreaFilename == null || studyAreaFilename.isEmpty() ||
						studyAreaFilename.equals(context.getResources().
								getString(R.string.label_studyAreaFilename))){
					if(!displayedToast){
						Toast.makeText(context, "A study area is required", 
								Toast.LENGTH_SHORT).show();
						displayedToast = true;
					}
					filenameLabel.setTextColor(ColorHelper.highlight());
					isValid = false;
				}else{
					filenameLabel.setTextColor(ColorHelper.black());
				}
				
				// check oversamples last because it could be left blank
				if(nOversamples<0){
					if(!displayedToast){
						Toast.makeText(context, 
								"Using standard number of oversamples", 
								Toast.LENGTH_SHORT).show();
						// don't consider this the toast (still show help if there are  other issues)
					}
					numberOversamplesLabel.setTextColor(ColorHelper.highlight());
				}else{
					numberOversamplesLabel.setTextColor(ColorHelper.black());
				}
				if(isValid){
					int nOversampleWithStd = nOversamples<0 ? nSamples*2 : nOversamples;
					callback.onTaskComplete(
							studyName, 			// database safe string identifier
							nSamples, 			// number of samples
							nOversampleWithStd, 		// number of over samples
							studyAreaFilename); // absolute path to file containing study area
					dismiss();
				}
			}

		};
		return checkFieldsOnClick;
	}


	/**
	 * Helper function to extract an integer value from user input
	 * into a text field
	 * @param textField the widget that collected user input
	 * @return integer or -1 on failure
	 */
	protected static int getInt(EditText textField) {
		String s = textField.getText().toString();
		int result = -1;
		// EditText set as numeric in the xml; could omit try/catch to improve speed)
		if (s.length() > 0){
			try{
				result = Integer.valueOf(s);
			}catch(NumberFormatException e){
				Log.d("create","[CreateBASDialog] Non-numeric input for number of samples");
			}
		}
		return result;
	}

}
