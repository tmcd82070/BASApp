package com.west.bas.spatial;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.west.bas.ui.RefreshCallback;
import com.west.bas.ui.map.MapFragmentDual;

public class LastKnownLocation {

	static boolean sHasUserConsent = false;
	
	private static Location sLocation;
	private LastKnownLocation(){}
	
	
	
	public static void giveUserConsent(final Context c, final RefreshCallback callback){
		sHasUserConsent = true;
		//TODO start the location service
		
		// Expected use in the field - favor GPS over network...
		//String locationProvider = LocationManager.NETWORK_PROVIDER;
		String locationProvider = LocationManager.GPS_PROVIDER;
		LocationManager locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

		//Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		      sLocation = location;
		      callback.onTaskComplete("changed location: "+location.getLatitude()+", "+location.getLongitude());
		    }

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);

	}
	
	public static boolean isNear(double x, double y){
		if(!sHasUserConsent) return false;
		
		if(sLocation==null) return false;

		Location sampleLoc = new Location(sLocation);
		sampleLoc.setLongitude(x);
		sampleLoc.setLatitude(y);
		float dist = sLocation.distanceTo(sampleLoc);
		return dist < MapFragmentDual.NEARBY_THRESHOLD;
	}

	public static LatLng getLatLong() {
		if(sLocation==null) return null;
		return new LatLng(sLocation.getLatitude(), sLocation.getLongitude());
	}
}
