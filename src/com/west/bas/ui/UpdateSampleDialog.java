package com.west.bas.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
public class UpdateSampleDialog extends AlertDialog{
	
	public UpdateSampleDialog(Context context, UpdateSampleCallback callback){
		super(context);
		final View.OnClickListener checkFieldsOnClick = 
				initLayout(context,callback);
		
		OnClickListener typedNullListener = new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		};	
		setButton(BUTTON_POSITIVE, "Update", typedNullListener);
		setButton(BUTTON_NEGATIVE, "Cancel", typedNullListener);
		
		setOnShowListener(new OnShowListener(){
			@Override
			public void onShow(DialogInterface di) {
				Button createBtn = UpdateSampleDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
				createBtn.setOnClickListener(checkFieldsOnClick);
			}
		});
	}
		
	private View.OnClickListener initLayout(Context context, final UpdateSampleCallback callback){
		// Inflate the layout from XML
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.dialog_update,null);
		setView(layout);
		
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

		
		// Verify any values entered: if valid, generate samples 
		// (otherwise provide feedback to user about why invalid)
		View.OnClickListener checkFieldsOnClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// check the values
				Status status = Status.SAMPLE;
				if(toggleButtonComplete.isChecked()) status = Status.COLLECTED;
				if(toggleButtonReject.isChecked()) status = Status.REJECT;
				String comment = SampleDatabaseHelper.getCleanText(commentText.getText().toString());
				callback.onTaskComplete(status, comment);
				dismiss();			
			}};
		
		return checkFieldsOnClick;
	}	
}
