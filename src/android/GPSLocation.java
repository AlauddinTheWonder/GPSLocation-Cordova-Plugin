package com.alauddin.cordova.gpslocation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class GPSLocation extends CordovaPlugin
{
	LocationManager locationManager;
	LocationListener locationListener;
	Location location;
	String locationProvider;
	
	CallbackContext callback;
	
	boolean statusOfGPS = false, dialogOpen = false, highAccuracy = false;
	int timeout = 120000, max_age = 60000;
	double latitude, longitude;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException 
	{
		callback = callbackContext;
		if (locationManager == null) {
			locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);
		}

		if(action.equals("getLocation"))
		{
			highAccuracy = args.getBoolean(0);
			timeout = args.getInt(1);
			max_age = args.getInt(2);

			this.processLocation();
			
			return true;
		}
		else
		{
			callback.error("gpslocation." + action + " is not a supported function. Did you mean 'getLocation'?");
			return false;
		}
	}

	private void processLocation()
	{
		locationManager = (LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE);

		if(this.highAccuracy)
		{
			locationProvider = LocationManager.GPS_PROVIDER;
		}
		else
		{
			locationProvider = LocationManager.NETWORK_PROVIDER;
		}

		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onLocationChanged(Location location) {
				updateLocation(location);
			}
		};

		this.getLocation();
	}

	private void getLocation()
	{
		Boolean isProvider = locationManager.isProviderEnabled(locationProvider);

		if(isProvider)
		{
			Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

			if(lastKnownLocation != null && (System.currentTimeMillis() - lastKnownLocation.getTime()) <= max_age)
			{
				this.updateLocation(lastKnownLocation);
			}
			else
			{
				locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
			}
		}
		else if(highAccuracy)
		{
			this.showAlertDialog("Enable Location Service (GPS) to access this service", "Alert", "showLocationSetting");
		}
		else
		{
			callback.error("Location Provider not found on this device!");
		}
	}

	private void updateLocation(Location location)
	{
		callback.success(this.getLocationJSON(location));
		locationManager.removeUpdates(locationListener); // Remove the locationlistener updates once location found
	}

	public void showAlertDialog(String msg, String title, final String callBackFunction)
	{
		if(!dialogOpen)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(cordova.getActivity());
			builder.setMessage(msg).setTitle(title);

			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) 
				{
					if(callBackFunction.equals("showLocationSetting"))
					{
						showLocationSetting();
					}

					dialogOpen = false;
				}
			});

			AlertDialog alert = builder.create();
			alert.setCancelable(false);
			alert.setCanceledOnTouchOutside(false);

			alert.show();

			dialogOpen = true;
		}
	}

	protected void showLocationSetting()
	{
		Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		cordova.getActivity().startActivity(viewIntent);
	}

	private JSONObject getLocationJSON(Location loc)
	{
		JSONObject myloc = new JSONObject();

		try {
			this.latitude = loc.getLatitude();
			this.longitude = loc.getLongitude();

			myloc.put("latitude", loc.getLatitude());
			myloc.put("longitude", loc.getLongitude());
			myloc.put("altitude", (loc.hasAltitude() ? loc.getAltitude() : null));
			myloc.put("accuracy", loc.getAccuracy());
			myloc.put("heading", (loc.hasBearing() ? (loc.hasSpeed() ? loc.getBearing() : null) : null));
			myloc.put("velocity", loc.getSpeed());
			myloc.put("timestamp", loc.getTime());
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}

		return myloc;
	}

	@Override
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		if(this.latitude == 0 || this.longitude == 0)
		{
			this.getLocation();
		}
	}

	@Override
	public void onPause(boolean multitasking) {
		super.onPause(multitasking);
		locationManager.removeUpdates(locationListener);
	}

}