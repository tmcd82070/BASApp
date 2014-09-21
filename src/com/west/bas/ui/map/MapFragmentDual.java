package com.west.bas.ui.map;

import android.database.Cursor;
import android.location.Location;
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
	
	public static final float NEARBY_THRESHOLD = 100.0f;
	
	/** Reference to the map object */
	private GoogleMap mMap;
	
	/** Flag indicating whether or not the user gave consent to 
	 * show their location on the map.  If so, the user location
	 * should be drawn during map refresh and is used to highlight
	 * locations that are nearby.
	 */
	private boolean mShowUserLocation = false;
	
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
			mPolygonOptions.fillColor(0x802222AA);//R.color.project_fill);
			// Draw with red outline
			mPolygonOptions.strokeColor(0xFF2222AA);//R.color.highlight);
			mPolygonOptions.strokeWidth(1);
		}
		
		if(mPolygonOptions!=null){
			mMap.addPolygon(mPolygonOptions);
		}
	}
	
	
	public void onDestroy(){
		if(updater !=null){
			updater.cancel(true);
			updater = null;
		}
	}
	
	/** Initialize the map in onActivityCreate so that
	 * the map fragment has already been created.
	 */
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity().getBaseContext());
		if(status==ConnectionResult.SUCCESS){
			mMap=getMap();

			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wy.getCenter(), 5));
			//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(wy, 0));
			
			// Respond to user clicks within the GoogleMap
			mMap.setOnMarkerClickListener(new OnMarkerClickListener(){
				@Override
				public boolean onMarkerClick(Marker m) {
					// TODO check and only update if it is sample point (give toast otherwise)
					// use the title and the id attributes?
					int id = Integer.valueOf(m.getTitle());
					getSampleStatus(id);
					// consume the event (don't proceed to default action(s))
					return true;
				}});

		}
	}
	
	protected void getSampleStatus(final int id) {
		UpdateSampleDialog dialog = new UpdateSampleDialog(
				this.getActivity().getBaseContext(),
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
		mMap.setMyLocationEnabled(hasUserLocationConsent);
		mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
	        @Override
	        public void onMyLocationChange(Location location) {
	            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
	            updateUserMarker(latLng);
	        }
	    });
	}
	
	protected void updateUserMarker(LatLng loc) {
        if(mUserCircle == null || mUserMarker == null){
			if(loc==null) loc = wy.getCenter();

			// define "near by"
		    double radiusInMeters = NEARBY_THRESHOLD;
		    int strokeColor = 0xff0000cc; //red outline
		    int shadeColor = 0x440000cc; //opaque red fill

		    CircleOptions circleOptions = new CircleOptions().center(loc).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
		    mUserCircle = mMap.addCircle(circleOptions);
			
		    // user location
			MarkerOptions m = new MarkerOptions().position(loc)
					.title("")
					.draggable(false);
			m.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_location)); 
			mUserMarker = mMap.addMarker(m);
        }else{
            mUserCircle.setCenter(loc);
            mUserMarker.setPosition(loc);
        }
	}

	public void refresh(String studyName, Cursor cursor) {
		if(mMap==null) mMap=getMap();
		if(mMap==null) return;
		
		mMap.clear();
		
		if(ReadStudyAreaAsyncTask.hasRecentStudy()){
			// recenter and zoom the map
			mMap.moveCamera(
					CameraUpdateFactory.newLatLngBounds(
							ReadStudyAreaAsyncTask.getBounds(), 0));
			
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
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_sample)); 
					break;
				case OVERSAMPLE: 
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_oversample)); 
					break;
				case REJECT: 
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_reject)); 
					break;
				case COLLECTED: 
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_collected)); 
					break;
				}
				mMap.addMarker(marker);
				//m.set('isEditable',true);
				Log.d("checkPoints","x: "+x+" y: "+y+", "+typeLabel);
				cursor.moveToNext();
			}
				
			// plot the user's location on the map
			if(mShowUserLocation){
				updateUserMarker(LastKnownLocation.getLatLong());
			}
		}
	}
}
