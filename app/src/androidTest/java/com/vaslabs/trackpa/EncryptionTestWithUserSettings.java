package com.vaslabs.trackpa;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;

import com.vaslabs.trackpa.EncryptionTest;
import com.vaslabs.trackpa.RsaManager;
import com.vaslabs.trackpa.SmsHandler;

import org.mockito.Mockito;

import java.security.PublicKey;

/**
 * Created by vnicolaou on 09/01/16.
 */
public class EncryptionTestWithUserSettings extends AndroidTestCase{

    private PublicKey remotePublicKey;

    @Override
    public void setUp() {
        RsaManager rsaManager = new RsaManager();
        System.setProperty("dexmaker.dexcache", this.mContext.getCacheDir().toString());
        try {
            remotePublicKey = rsaManager.getRemotePublicKey(this.getContext());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void test_send_sms() {
        Location location = Mockito.mock(Location.class);
        Mockito.when(location.getLatitude()).thenReturn(32.12321);
        Mockito.when(location.getLongitude()).thenReturn(-2.13121);
        String message = SmsHandler.generateMessage(this.getContext(), location);
        assertTrue(!message.contains("32.12321"));
        assertTrue(!message.contains("\n"));
        Log.i("TestEncryption", message);


        SmsHandler.sendLocationSms(this.getContext(), location);
    }
}
