package com.west.bas.ui.map;

import android.os.Bundle; 
import android.util.Log;
import android.view.*; 
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.west.bas.R;

public class MapFragmentDual extends SupportMapFragment { 
	
	private static boolean sIsGoogleMap = false;
	private static GoogleMap sMap;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) { 
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
		if(status==ConnectionResult.SUCCESS){
			sIsGoogleMap = true;
			Log.d("Map","Found Google Play");
			Log.d("Map","GooglePlay version: "+GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);
			View v = super.onCreateView(inflater, viewGroup, savedInstanceState);
			return v;
		}else{
			Log.d("Map","No Google Play, set up drawing");
			View view = inflater.inflate(R.layout.fragment_draw, viewGroup, false); 
			MapViewDraw map = (MapViewDraw)(view.findViewById(R.id.mapView_drawMap));
			map.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Toast.makeText(getActivity().getBaseContext(), "Clicked the map!", Toast.LENGTH_SHORT).show();
				}});
			return view;
		}
		
	}

	public boolean isGoogleMap() {
		return sIsGoogleMap;
	} 

	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
//		/http://stackoverflow.com/questions/14047257/how-do-i-know-the-map-is-ready-to-get-used-when-using-the-supportmapfragment
		sMap=getMap();
		
		 LatLngBounds wy = new LatLngBounds(
		  new LatLng(41, -111.05), new LatLng(45, -104.05));

		sMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wy.getCenter(), 5));
		//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(wy, 0));
	}

	public GoogleMap getGoogleMap() {
		return sMap;
	}

	
}
