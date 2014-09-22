package com.west.bas.ui;

import com.west.bas.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Shows first, user may consent or withhold consent to plot location on the map
 * link to Google policy
 * statement of what data is collected and how it is used
 * @author mbrittell
 *
 */
public class PrivacyPolicyDialog extends AlertDialog{
	final static private String sPrivacyPolicy = 
			"This application displays the study area polygon and "
			+ "the generated sample locations on a Google Maps "
			+ "display.  It does not send any identifying "
			+ "information to Google. The polygon and markers are "
			+ "placed on the map as a group, but are posted "
			+ "anonymously.  By using the Balanced Acceptance "
			+ "Sampling (BAS) application, you consent to the use "
			+ "of Google Maps to display the locations and accept "
			+ "Google's Privacy Policy and Terms of Service as they "
			+ "apply to the BAS application.\n\n"
			+ "As an added feature, the BAS application can also "
			+ "display your current location on the map and "
			+ "highlight any sample locations that are nearby.  To "
			+ "do so, the BAS application will access the location "
			+ "of your Android device using the GPS service.";
	
	public PrivacyPolicyDialog(Context context, final PrivacyPolicyCallback callback){
		super(context);
		
		final View.OnClickListener continueClick = initLayoutWidgets(context,callback);
		
		setButton(BUTTON_POSITIVE, "Accept privacy policy", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {}
		});
		setButton(BUTTON_NEGATIVE, "Quit BAS", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callback.onTaskComplete(false, false);
			}
		});
		setOnShowListener(new OnShowListener(){
			@Override
			public void onShow(DialogInterface di) {
				Button createBtn = PrivacyPolicyDialog.this.getButton(AlertDialog.BUTTON_POSITIVE);
				createBtn.setOnClickListener(continueClick);
			}
		});	
	}
	
	@SuppressLint("InflateParams")
	// http://www.doubleencore.com/2013/05/layout-inflation-as-intended/
	// exception: placing view in dialog 
	private View.OnClickListener initLayoutWidgets(
			final Context context, 
			final PrivacyPolicyCallback callback) {
		
		// Inflate the layout from XML
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.dialog_privacy, null);
		setView(layout);
		
		// The text of the privacy policy (could move to xml..)
		final TextView policyText = 
				(TextView) layout.findViewById(R.id.textView_privacyPolicy);
		policyText.setText(sPrivacyPolicy);
		
		// checkbox to indicate whether or not the user gives consent to 
		// access device location
		final CheckBox consentGPS = 
				(CheckBox) layout.findViewById(R.id.checkBox_consentGPS);
		
		return new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				callback.onTaskComplete(true, consentGPS.isChecked());
				dismiss();
			}
		};
	}
}
