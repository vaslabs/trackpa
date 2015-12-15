package com.vaslabs.trackpa;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static com.vaslabs.trackpa.SmsHandler.sendLocationSms;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class LocationService extends Service implements LocationListener {

    private static final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final String NETWORK_PROVIDER = LocationManager.NETWORK_PROVIDER;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public LocationManager locationManager;

    Intent intent = null;
    PendingIntent smsIntent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("LocationService", "created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initLocationManager(this);
        Log.i("LocationService", "started");
        return START_STICKY;
    }


    private void initLocationManager(Context context) {
        long wakeUpTime = getWakeUpTime(this);
        wakeUpTime = TimeUnit.MILLISECONDS.convert(wakeUpTime, TimeUnit.MINUTES);
        locationManager = (LocationManager) (context.getSystemService(Context.LOCATION_SERVICE));
        if (locationManager == null) {
            Toast.makeText(context, "Please enable gps", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, wakeUpTime, 10f, this);
        locationManager.requestLocationUpdates(NETWORK_PROVIDER,
                wakeUpTime, 10, this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (smsIntent != null)
            locationManager.removeUpdates(this);
        Log.i("LocationService", "destroyed");

    }

    private static long getWakeUpTime(Context context) {
        return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString("sync_frequency", SettingsActivity.DEFAULT_SYNC));

    }

    @Override
    public void onLocationChanged(Location location) {
        boolean isTrackingEnabled = readTrackingPreference();
        if (isTrackingEnabled) {
            sendLocationSms(this, location);
        } else {
            Log.i("LocationService", "Stopping...");
            this.stopSelf();
        }
    }

    private boolean readTrackingPreference() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("track_switch", false);
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

}
