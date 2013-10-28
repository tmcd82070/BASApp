package west.sample.bas;

import west.sample.bas.SampleDatabaseHelper.SampleInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

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
		Log.d("click","selected: "+mAdapter.getItem(position));
	}

}
