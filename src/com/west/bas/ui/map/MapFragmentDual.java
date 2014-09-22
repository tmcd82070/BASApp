package com.west.bas.ui.map;

import android.database.Cursor;
import android.os.Bundle; 
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.west.bas.MainActivity;
import com.west.bas.R;
import com.west.bas.database.SampleDatabaseHelper;
import com.west.bas.database.UpdateSampleAsyncTask;
import com.west.bas.database.SampleDatabaseHelper.SampleInfo;
import com.west.bas.spatial.LastKnownLocation;
import com.west.bas.spatial.ReadStudyAreaAsyncTask;
import com.west.bas.ui.UpdateSampleCallback;
import com.west.bas.ui.UpdateSampleDialog;

public class MapFragmentDual extends SupportMapFragment { 
	
	// distance in meters (10km)
	public static final float NEARBY_THRESHOLD = 10000.0f;
	
	/** Reference to the map object */
	private static GoogleMap sMap;
	
	/** Reposition the map on load; flag to remember whether or not it is on load */
	private static boolean sIsFirstZoom = true;
	public void loadNewStudy(){
		sIsFirstZoom = true;
	}
	
	/** Flag indicating whether or not the user gave consent to 
	 * show their location on the map.  If so, the user location
	 * should be drawn during map refresh and is used to highlight
	 * locations that are nearby.
	 */
	private static boolean sShowUserLocation = false;
	
	/** Class level reference to the async task (for cleanup) */
	private UpdateSampleAsyncTask updater;
	
	/** A set of configuration options for a marker to show the 
	 * user location
	 */
	private Marker mUserMarker;
	private Circle mUserCircle;
	
	/** Default location for the display */
	private LatLngBounds wy = new LatLngBounds(
			  new LatLng(41, -111.05), new LatLng(45, -104.05));

	/** The study location that is drawn on the map for reference */
	private PolygonOptions mPolygonOptions;
	
	private void drawStudyArea(){
		if(mPolygonOptions==null && ReadStudyAreaAsyncTask.hasRecentStudy()){
			mPolygonOptions = ReadStudyAreaAsyncTask.getPolygon();
			
			// Draw with transparent fill
			//polygonOptions.fillColor(android.R.color.transparent);
			mPolygonOptions.fillColor(0x402222AA);//R.color.project_fill);
			// Draw with red outline
			mPolygonOptions.strokeColor(0xFF2222AA);//R.color.highlight);
			mPolygonOptions.strokeWidth(1);
		}
		
		if(mPolygonOptions!=null){
			sMap.addPolygon(mPolygonOptions);
		}
	}
	
	
	public void onDestroy(){
		if(updater !=null){
			updater.cancel(true);
			updater = null;
		}
		super.onDestroy();
	}
	
	/** Initialize the map in onActivityCreate so that
	 * the map fragment has already been created.
	 */
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
		if(status==ConnectionResult.SUCCESS){
			sMap=getMap();

			sMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wy.getCenter(), 5));
			//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(wy, 0));
			
			// Respond to user clicks within the GoogleMap
			sMap.setOnMarkerClickListener(new OnMarkerClickListener(){
				@Override
				public boolean onMarkerClick(Marker m) {
					// TODO check and only update if it is sample point (give toast otherwise)
					// use the title and the id attributes?
					String title = m.getTitle();
					int id = Integer.valueOf(title);
					getSampleStatus(id);
					// consume the event (don't proceed to default action(s))
					return true;
				}});

		}
	}
	
	protected void getSampleStatus(final int id) {
		UpdateSampleDialog dialog = new UpdateSampleDialog(
				(MainActivity) getActivity(),
				new UpdateSampleCallback(){
					@Override
					public void onTaskComplete(
							SampleDatabaseHelper.Status status,
							String comment) {
						((MainActivity) getActivity()).updateSamplePoint(id,status,comment);
					}
				});
		dialog.show();
		Log.d("click","[MainActivity] selected: "+id);
	}


	
	public void setUserLocation(boolean hasUserLocationConsent){
		sShowUserLocation = hasUserLocationConsent;
	}
	
	protected void updateUserMarker(LatLng loc) {
//		if(loc==null) return;
        if(mUserCircle == null || mUserMarker == null){
			if(loc==null) loc = wy.getCenter();

			// define "near by"
		    double radiusInMeters = NEARBY_THRESHOLD;
		    int strokeColor = 0xffffb600; //red outline
		    int shadeColor = 0x44ffb600; //opaque red fill

		    CircleOptions circleOptions = new CircleOptions().center(loc).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
		    mUserCircle = sMap.addCircle(circleOptions);
			
		    // user location
			MarkerOptions m = new MarkerOptions().position(loc)
					.title("")
					.draggable(false);
			m.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location)); 
			mUserMarker = sMap.addMarker(m);
        }else{
            mUserCircle.setCenter(loc);
            mUserMarker.setPosition(loc);
        }
	}

	public void refresh(String studyName, Cursor cursor) {
		if(sMap==null) return;
		
		sMap.clear();
		
		if(ReadStudyAreaAsyncTask.hasRecentStudy()){
			// if the user hasn't zoomed in to level...
			//float zoom = sMap.getCameraPosition().zoom;
			if(sIsFirstZoom){
				// recenter and zoom the map
				sMap.moveCamera(
						CameraUpdateFactory.newLatLngBounds(
								ReadStudyAreaAsyncTask.getBounds(), 0));
				sIsFirstZoom = false;
			}
			
			// draw the study area
			drawStudyArea();
			
			// draw the sample points on the map
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				float x = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_X));
				float y = cursor.getFloat(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_Y));
				// ID is used to determine which row of the database to update
				int id = cursor.getInt(cursor.getColumnIndex(SampleInfo._ID));
				// status is used to color code the marker (i.e., select the icon)
				String typeLabel = cursor.getString(cursor.getColumnIndex(SampleInfo.COLUMN_NAME_STATUS));
				MarkerOptions marker = new MarkerOptions().position(new LatLng(y, x))
						.title(""+id)
						.draggable(false);
				switch(SampleDatabaseHelper.Status.getValueFromString(typeLabel)){
				case SAMPLE: 
					if(LastKnownLocation.isNear(x,y)) marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_samplenearby));
					else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_sample)); 
					break;
				case OVERSAMPLE: 
					if(LastKnownLocation.isNear(x,y)) marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_oversamplenearby));
					else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_oversample)); 
					break;
				case REJECT: 
					if(LastKnownLocation.isNear(x,y)) marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_rejectnearby));
					else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_reject)); 
					break;
				case COLLECTED: 
					if(LastKnownLocation.isNear(x,y)) marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_collectednearby));
					else marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_collected)); 
					break;
				}
				sMap.addMarker(marker);
				//m.set('isEditable',true);
				Log.d("checkPoints","x: "+x+" y: "+y+", "+typeLabel);
				cursor.moveToNext();
			}
				
			// plot the user's location on the map
			if(sShowUserLocation){
				updateUserMarker(LastKnownLocation.getLatLong());
			}
		}
	}
}
