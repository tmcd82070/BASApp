package com.west.bas.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.SampleDatabaseHelper.Status;
import com.west.bas.sample.GenerateSample;

/**
 * 
 * <br/><br/>
 * 
 * West EcoSystems Technologies, Inc (2013)
 * 
 * @see GenerateSample
 */
public class UpdateSampleDialog{

	private static Context sContext=null;
	private static Activity sActivity=null;
	private static UpdateSampleCallback sCallback;
	
	// Disallow instantiation
	private UpdateSampleDialog(){}
	
	public static AlertDialog getUpdateSampleDialog(final int itemID){
		if(sContext ==null || sActivity==null || sCallback==null) return null;
		
		// Inflate the layout from XML
		LayoutInflater inflater = LayoutInflater.from(sContext);
		View layout = inflater.inflate(R.layout.dialog_update,null);

		// Buttons to indicate change in status
		final ToggleButton toggleButtonComplete = (ToggleButton) layout.findViewById(R.id.toggleButton_complete);
		final ToggleButton toggleButtonReject = (ToggleButton) layout.findViewById(R.id.toggleButton_reject);
		final EditText commentText = (EditText) layout.findViewById(R.id.editText_comments);
		
		// The button state gives feedback about which is selected, and
		// is then read when the "Update" button is clicked
		// Only one is selected at a time
		toggleButtonComplete.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				toggleButtonComplete.setChecked(true);
				toggleButtonReject.setChecked(false);
			}});
		
		toggleButtonReject.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				toggleButtonReject.setChecked(true);
				toggleButtonComplete.setChecked(false);
			}});

		AlertDialog.Builder builder = new android.app.AlertDialog.Builder(sActivity);
		builder.setView(layout);
		// default listener will close
		builder.setNegativeButton("Cancel",null);
		builder.setPositiveButton("Update",new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Status status = Status.SAMPLE;
				if(toggleButtonComplete.isChecked()) status = Status.COLLECTED;
				if(toggleButtonReject.isChecked()) status = Status.REJECT;
				String comment = SampleDatabaseHelper.getCleanText(commentText.getText().toString());
				sCallback.onTaskComplete(itemID, status, comment);
			}
		});
		return builder.create();
	}
	
	
	public static void initUpdateSampleDialog(
			Context context, 
			Activity activity,
			UpdateSampleCallback callback){
		sContext = context;
		sActivity = activity;
		sCallback = callback;
	}
}
