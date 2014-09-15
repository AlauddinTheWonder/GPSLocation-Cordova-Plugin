package com.alauddin.cordova.gpslocation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class GPSLocation extends CordovaPlugin implements LocationListener {
  LocationManager locationManager;
  Location location;
  CallbackContext callback;
  String provider;
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

      checkGPS("Startup");

      /*cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          Toast toast = Toast.makeText(cordova.getActivity(), "My Toast Message", Toast.LENGTH_LONG);
          toast.show();
        }
      });*/
      
      return true;
    }
    else
    {
      callback.error("gpslocation." + action + " is not a supported function. Did you mean 'getLocation'?");
      return false;
    }
  }

  /* Request updates at startup */
  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    if(this.latitude == 0 || this.longitude == 0)
    {
      checkGPS("On Resume");
    }
  }

  /* Remove the locationlistener updates when Activity is paused */
  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    locationManager.removeUpdates(this);
  }

  @Override
  public void onProviderEnabled(String arg0) {
    
  }

  @Override
  public void onProviderDisabled(String arg0) {
    if(this.latitude == 0 || this.longitude == 0)
    {
      checkGPS("On Provider disabled");
    }
  }

  @Override
  public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    if(this.latitude == 0 || this.longitude == 0)
    {
      checkGPS("On Status Changed");
    }
  }

  @Override
  public void onLocationChanged(Location location) {
    statusOfGPS = true;

    callback.success(this.getLocationJSON(location));
    locationManager.removeUpdates(this); // Remove the locationlistener updates once location found
  }

  protected void showLocationSetting()
  {
    Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    cordova.getActivity().startActivity(viewIntent);
  }

  protected void checkGPS(String callFrom)
  {
    statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    if(statusOfGPS)
    {
      Criteria criteria = new Criteria();
      provider = locationManager.getBestProvider(criteria, false);

      location = locationManager.getLastKnownLocation(provider);

      if (location != null && (System.currentTimeMillis() - location.getTime()) <= max_age) {
        onLocationChanged(location);
      } else {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeout, 10, this);
      }
    }
    else
    {
      showAlertDialog("Enable Location Service (GPS) to access this service", "Alert", "showLocationSetting");
    }
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
}