package com.example.android.geofence;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TrackerService extends Service implements LocationListener {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started service");
        startLocationSampling();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        stopLocationSampling();
    }

    /** Helper method that starts Location sampling. */
    public void startLocationSampling() {
        try {
            // Check the flag
            if (isLocationOn)
                return;

            // Get the location manager
            locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Get one of the enabled location providers (ignore passive provider)
            List<String> enabledProviders = locationManager.getProviders(true);
            enabledProviders.remove(LocationManager.PASSIVE_PROVIDER);
            if (enabledProviders.isEmpty()) {
                throw new Exception("No location provider enabled");
            }

            // Add a location change listener for any one of the enabled providers
            // The provider used (GPS or network) depends on user's Location Settings
            locationManager.requestLocationUpdates(enabledProviders.get(0),
                    30 * 1000, // Min time: 30 seconds
                    0.0F,      // Min distance change: 0 meters
                    this);     // Location Listener to be called

            // Set the flag
            isLocationOn = true;
        } catch (Exception e) {
            // Log the exception
            Log.e(TAG, "Unable to start location", e);
            // Tell the user
            // createToast("Unable to start location: " + e.toString());
        }
    }

    /**
     * Helper method that stops Location sampling.
     */
    public void stopLocationSampling() {
        try {
            // Check the flag
            if (!isLocationOn)
                return;

            // Set the flag
            isLocationOn = false;

            // Remove the location change listener
            locationManager.removeUpdates(this);
        } catch (Exception e) {
            // Log the exception
            Log.e(TAG, "Unable to stop location", e);
            // Tell the user
            // createToast("Unable to stop location: " + e.toString());
        } finally {
            locationManager = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        // Get the location details
        long locationTime = location.getTime();
        String provider = location.getProvider();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        double altitude = (location.hasAltitude() ?location.getAltitude() : -1.0);
        float bearing = (location.hasBearing() ?location.getBearing() : -1.0f);
        float speed = (location.hasSpeed() ?location.getSpeed() : -1.0f);

        // Log the reading
        logLocationReading(locationTime,
                provider,
                latitude,
                longitude,
                accuracy,
                altitude,
                bearing,
                speed);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void logLocationReading(long locationTime ,
                                    String provider,
                                    double latitude,
                                    double longitude,
                                    float accuracy,
                                    double altitude,
                                    float bearing,
                                    float speed) {

        // Make a timestamp
        String timeStamp = mDateFormat.format(locationTime);

        // Location details
        StringBuilder sb = new StringBuilder();
        sb.append(provider).append(LOCATION_DELIMITER).
        append(latitude).append(LOCATION_DELIMITER).
        append(longitude).append(LOCATION_DELIMITER).
        append(accuracy).append(LOCATION_DELIMITER).
        append(altitude).append(LOCATION_DELIMITER).
        append(bearing).append(LOCATION_DELIMITER).
        append(speed);

        String text = sb.toString();

        // Get the current log file or create a new one, then log the activity
        LogFile.getInstance(getApplicationContext()).log(
                timeStamp + LOG_DELIMITER + text
        );
    }

    public class TrackerServiceBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    private final IBinder mBinder = new TrackerServiceBinder();

    private static final String LOG_DELIMITER = ";;";
    private static final String LOCATION_DELIMITER = ", ";
    private DateFormat mDateFormat = DateFormat.getDateInstance();

    /** Location Manager. */
    private LocationManager locationManager;

    /** Flag to indicate that location sensing is going on. */
    private boolean isLocationOn;

    /** DDMS Log Tag. */
    private static final String TAG = "TrackerService";
}
