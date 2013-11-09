package com.west.bas.ui;

import android.os.Bundle; 
import android.support.v4.app.Fragment; 
import android.util.Log;
import android.view.*; 
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.west.bas.R;
import com.west.bas.R.id;
import com.west.bas.R.layout;
public class MapFragment extends Fragment { 
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) { 

		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
//		if(status==ConnectionResult.SUCCESS){
//			Log.d("Map","Found Google Play");
//			Log.d("Map","GooglePlay version: "+GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);
//			return inflater.inflate(R.layout.fragment_map, viewGroup, false); 
//		}else{
			Log.d("Map","No Google Play, set up drawing");
			View view = inflater.inflate(R.layout.fragment_draw, viewGroup, false); 
			MapView map = (MapView)(view.findViewById(R.id.mapView_drawMap));
			map.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity().getBaseContext(), "Clicked the map!", Toast.LENGTH_SHORT).show();
				}});
			return view;
//		}
		
		
	} 
}
