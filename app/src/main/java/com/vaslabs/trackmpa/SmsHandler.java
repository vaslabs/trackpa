package com.vaslabs.trackmpa;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class SmsHandler {

    protected static void sendLocationSms(Context context, Location location) {
        String phoneNumber = getPhoneNumber(context);
        Log.i("LocationService", "Got phone number: " + phoneNumber);
        if ("".equals(phoneNumber))
            return;
        SmsManager smsManager = SmsManager.getDefault();
        String message = generateMessage(context, location);
        if (message.length() > 160) {
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }

    protected static String getPhoneNumber(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("phone_number", "");
    }

    protected static String generateMessage(Context context, Location location) {
        String message = String.format("Lat: %s, Lng: %s", location.getLatitude(), location.getLongitude());
        if (requiresEncryption(context)) {
            try {
                message = Base64.encodeToString(encryptedMessage(context, message), Base64.DEFAULT);
                message = message.replaceAll("\n", "");
                message = URLEncoder.encode(message, "UTF-8");

            } catch (Exception e) {
                Log.i("Encryption", "Error encrypting message: " + e.toString());
                Toast.makeText(context, "Encryption failed: sending raw message.", Toast.LENGTH_SHORT).show();
            }
        }
        return message;
    }


    protected static byte[] encryptedMessage(Context context, String message) throws Exception {
        Log.i("LocationService", "Encrypting message: " + message);
        FileInputStream fis = context.openFileInput("rsa.pub");
        PublicKey pk = PublicKeyReader.get(fis);
        return new RsaManager().encrypt(message, pk);


    }

    protected static boolean requiresEncryption(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("encrypt_switch", false);
    }


}
