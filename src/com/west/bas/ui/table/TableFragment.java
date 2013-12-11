package com.west.bas.ui.table;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.west.bas.MainActivity;
import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.database.SampleDatabaseHelper.Status;
import com.west.bas.ui.UpdateSampleCallback;
import com.west.bas.ui.UpdateSampleDialog;

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
		item.close();
		new UpdateSampleDialog(getActivity(),new UpdateSampleCallback(){

			@Override
			public void onTaskComplete(Status status, String comment) {
				((MainActivity) getActivity()).updateSamplePoint(itemID,status,comment);
			}}).show();	
		Log.d("click","[TableFragment] selected: "+item);
	}



}
