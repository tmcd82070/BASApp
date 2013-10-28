package west.sample.bas;

import west.sample.bas.SampleDatabaseHelper.SampleInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class TableFragment extends ListFragment { 
	
	private DetailListAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		populateTable();
	}	
	
	public void onHiddenChanged(boolean b){
		super.onHiddenChanged(b);
		Toast.makeText(this.getActivity(), "table is showing", Toast.LENGTH_LONG).show();
	}
	
	private void populateTable() {
		// Connect to the database where the samples are stored
		SampleDatabaseHelper db = new SampleDatabaseHelper(getActivity()
				.getBaseContext());

		Cursor cursor = db.getStudyDetails(MainActivity.sCurrentStudy);
		
		//getActivity().getSupportLoaderManager();
		//.startManagingCursor(cursor);
		
		mAdapter = new DetailListAdapter(getActivity(), R.layout.row_details,
				cursor, new String[] { 
							SampleInfo._ID,
							SampleInfo.COLUMN_NAME_STATUS,
							SampleInfo.COLUMN_NAME_COMMENT }, 
						new int[] {
							R.id.text_sampleID, 
							R.id.text_status,
							R.id.text_comments },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		// DetailListAdapter adapter = new DetailListAdapter(inflater, result);
		// setListAdapter(adapter);
		
		setListAdapter(mAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("click","selected: "+mAdapter.getItem(position));
	}

}
