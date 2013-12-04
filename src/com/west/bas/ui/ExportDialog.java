package com.west.bas.ui;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper;

public class ExportDialog extends AlertDialog {

	private static int sBlack;
	private static int sHighlight;
	private static int sWarning;
	

	public ExportDialog(Context context, String studyName, 
			ExportCallback callback) {
		super(context);
		initColors(context);
		final View.OnClickListener checkFieldsOnClick = 
				initLayout(context,studyName!=null,callback);
		
		OnClickListener typedNullListener = new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		};	
		setButton(BUTTON_POSITIVE, "Export", typedNullListener);
		setButton(BUTTON_NEGATIVE, "Cancel", typedNullListener);
		
		setOnShowListener(new OnShowListener(){
			@Override
			public void onShow(DialogInterface di) {
				Button createBtn = ExportDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
				createBtn.setOnClickListener(checkFieldsOnClick);
			}
		});
	}
	
	/** Initialize colors used to encode status in the graphical
	 * user interface.  Colors are initialized only once.
	 * @param c
	 */
	private static void initColors(Context c){
		if(sBlack==0 || sHighlight==0 || sWarning==0){
			sBlack = c.getResources().getColor(android.R.color.black);
			sHighlight = c.getResources().getColor(R.color.highlight);
			sWarning = c.getResources().getColor(R.color.warning);
		}
	}
	
	private View.OnClickListener initLayout(final Context context, 
			boolean hasSelectedStudy,
			final ExportCallback callback){
		// Inflate the layout from XML
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.dialog_export,null);
		setView(layout);
		
		
		// Labels (connected to facilitate color coding text)
		final TextView exportFilenameLabel = 
				(TextView) layout.findViewById(R.id.textView_exportLabel);
		
		// Text fields that accept text input
		final EditText exportFilenameTxt = 
				(EditText) layout.findViewById(R.id.editText_exportFilename);
		exportFilenameTxt.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) exportFilenameLabel.setTextColor(sBlack);
			}});
		
		// Buttons that select the data to export
		final ToggleButton allStudiesBtn = 
				(ToggleButton) layout.findViewById(R.id.toggleButton_allStudies);
		final ToggleButton currentStudiesBtn = 
				(ToggleButton) layout.findViewById(R.id.toggleButton_currentStudy);
		
		allStudiesBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// always have one selected (with option to select multiple times)
				allStudiesBtn.setChecked(true);
				currentStudiesBtn.setChecked(false);
			}});
		currentStudiesBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// always have one selected (with option to select multiple times)
				currentStudiesBtn.setChecked(true);
				allStudiesBtn.setChecked(false);
			}});
		if(hasSelectedStudy){
			currentStudiesBtn.setChecked(true);
		}else{
			allStudiesBtn.setChecked(true);
			currentStudiesBtn.setEnabled(false);
		}

		// Verify any values entered: if valid, generate samples 
		// (otherwise provide feedback to user about why invalid)
		View.OnClickListener checkFieldsOnClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// check the values
				String filename = SampleDatabaseHelper.getCleanText(exportFilenameTxt.getText().toString());
				if(filename.isEmpty()){
					exportFilenameLabel.setTextColor(sHighlight);
					Toast.makeText(context, 
							"A name for the output file is required", 
							Toast.LENGTH_SHORT).show();
				}else if(!filename.endsWith(".csv")){
					exportFilenameLabel.setTextColor(sHighlight);
					Toast.makeText(context, 
							"The output file must have extension .csv", 
							Toast.LENGTH_SHORT).show();					
				}else if(new File(Environment.getExternalStorageDirectory()+"/"+filename).exists()){
					exportFilenameLabel.setTextColor(sHighlight);
					Toast.makeText(context, 
							"A file with that name already exists on the SD card; "
							+ "enter a unique name (or delete the existing file)", 
							Toast.LENGTH_SHORT).show();					
				}else{
					callback.onTaskComplete(allStudiesBtn.isChecked(), filename);
					dismiss();
				}
			}};
		
		return checkFieldsOnClick;
	}

}
