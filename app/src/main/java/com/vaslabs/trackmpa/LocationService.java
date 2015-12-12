package com.vaslabs.trackmpa;

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
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class LocationService extends Service implements LocationListener {

    private static final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public LocationManager locationManager;

    Intent intent = null;
    PendingIntent smsIntent = null;
    @Override
    public void onCreate()
    {
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
                locationManager = (LocationManager)(context.getSystemService(Context.LOCATION_SERVICE));
        if (locationManager == null) {
            Toast.makeText(context, "Please enable gps", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, TimeUnit.MILLISECONDS.convert(wakeUpTime, TimeUnit.MINUTES), 10f, this);

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
            sendLocationSms(location);
        } else {
            this.stopSelf();
        }
    }

    private void sendLocationSms(Location location) {
        String phoneNumber = getPhoneNumber();
        Log.i("LocationService", "Got phone number: " + phoneNumber);
        if ("".equals(phoneNumber))
            return;
        SmsManager smsManager = SmsManager.getDefault();
        String message = generateMessage(location);

        smsManager.sendTextMessage(phoneNumber, null, generateMessage(location), null, null);
    }

    private String generateMessage(Location location) {
        String message = String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude());
        if (requiresEncryption()) {
            message = encryptedMessage(message);
        }
        return message;
    }

    private String encryptedMessage(String message) {
        try {
            FileInputStream fis = this.openFileInput("rsa.pub");
            try {
                PublicKey pk = PublicKeyReader.get(fis);
                return new String(new RsaManager().encrypt(message, this));
            } catch (Exception e) {
                return message;
            }
        } catch (FileNotFoundException e) {
            return message;
        }
    }

    private boolean requiresEncryption() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("encrypted_switch", false);
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

    public String getPhoneNumber() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("phone_number", "");
    }
}
