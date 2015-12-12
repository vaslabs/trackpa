package com.vaslabs.trackmpa;

import android.location.Location;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;

import org.mockito.Mockito;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class EncryptionTest extends AndroidTestCase{
    RsaManager rsaManager;

    PublicKey remotePublicKey;
    PublicKey myPublicKey;

    PrivateKey myPrivateKey;
    PrivateKey remotePrivateKey;


    public void setUp() throws NoSuchAlgorithmException {
        System.setProperty("dexmaker.dexcache", this.mContext.getCacheDir().toString());
        rsaManager = new RsaManager();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        remotePrivateKey = kp.getPrivate();
        remotePublicKey = kp.getPublic();
    }


    private byte[] onEncryptDataWithRemotePublicKey(String data) throws Exception {
        return rsaManager.encrypt(data, remotePublicKey);
    }

    public void test_send_encrypted_message() {
        //before running this test, enable encryption
        Location location = Mockito.mock(Location.class);
        Mockito.when(location.getLatitude()).thenReturn(32.12321);
        Mockito.when(location.getLongitude()).thenReturn(-2.13121);
        String message = SmsHandler.generateMessage(this.getContext(), location);
        assertTrue(!message.contains("32.12321"));
        Log.i("TestEncryption", message);
        //SmsHandler.sendLocationSms(this.getContext(), location);
    }

}
