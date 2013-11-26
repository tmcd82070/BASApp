package com.west.bas.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.west.bas.R;
import com.west.bas.UpdateTask;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.spatial.SamplePoint.Status;

public class TableFragment extends ListFragment { 
	
	private DetailListAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		populateTable();
	}	
	
	private void populateTable() {
		mAdapter = new DetailListAdapter(getActivity(), R.layout.row_details,
				null, new String[] { 
							SampleInfo.COLUMN_NAME_NUMBER,
							SampleInfo.COLUMN_NAME_STATUS,
							SampleInfo.COLUMN_NAME_COMMENT }, 
						new int[] {
							R.id.text_sampleNumber, 
							R.id.text_status,
							R.id.text_comments },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		setListAdapter(mAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor item = (Cursor) mAdapter.getItem(position);
		Status status = Status.getValueFromString(
				item.getString(item.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS)));
		if(status!=Status.SAMPLE){
			Toast.makeText(getActivity().getBaseContext(), "Actions only available for samples", Toast.LENGTH_SHORT).show();
			return;
		}
		
		final int itemID = item.getInt(item.getColumnIndex(SampleInfo._ID));
		final String studyName = item.getString(item.getColumnIndex(SampleInfo.COLUMN_NAME_STUDY));
		item.close();
		
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService("layout_inflater");
		View layout = inflater.inflate(R.layout.dialog_update,
				(ViewGroup) getActivity().findViewById(R.layout.activity_main));
		final ToggleButton tbtn_complete = (ToggleButton) layout.findViewById(R.id.toggleButton_complete);
		final ToggleButton tbtn_reject = (ToggleButton) layout.findViewById(R.id.toggleButton_reject);
		final EditText commentTxt = (EditText) layout.findViewById(R.id.editText_comments);
		
		tbtn_complete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(tbtn_complete.isChecked()) tbtn_reject.setChecked(false);
			}});
		
		tbtn_reject.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(tbtn_reject.isChecked()) tbtn_complete.setChecked(false);
			}});
		
		AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
		builder.setView(layout);
		builder.setPositiveButton("Update", 
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Status status = Status.SAMPLE;
						if(tbtn_complete.isChecked()) status = Status.COLLECTED;
						if(tbtn_reject.isChecked()) status = Status.REJECT;
						new UpdateTask(getActivity().getBaseContext(),
								studyName,itemID,
								status,getCleanText(commentTxt),
								new RefreshCallback(){
									@Override
									public void onTaskComplete(String message) {
										SampleDatabaseHelper db = new SampleDatabaseHelper(getActivity().getBaseContext());
										Cursor cursor = db.getStudyDetails(studyName);
										DetailListAdapter adapter = (DetailListAdapter) getListAdapter();
										adapter.swapCursor(cursor);
										Log.d("database",db.prettyPrint());
										if(message!=null) Toast.makeText(getActivity().getBaseContext(),message,Toast.LENGTH_LONG).show();
									}}).execute();
					}});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

		final AlertDialog dialog = builder.create();
		dialog.show();
		
		Log.d("click","selected: "+item);
	}

	protected String getCleanText(EditText commentTxt) {
		String comment = commentTxt.getText().toString();
		return comment.replaceAll("[^a-zA-Z0-9_ \\.]", "");
		
	}

}
