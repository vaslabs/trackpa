package com.vaslabs.trackmpa;

import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

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
        rsaManager = new RsaManager();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        remotePrivateKey = kp.getPrivate();
        remotePublicKey = kp.getPublic();
    }

    public void test_encryption() throws Exception {
        String someData = "Lat: 13923, Lng: 4329049";
        byte[] encryptedData = rsaManager.encrypt(someData, this.getContext());
        String encryptionString = new String(encryptedData);
        try {
            assertEquals(someData, encryptionString);
            fail(someData + " should not be equal to " + encryptionString);
        } catch (AssertionError assertionError)  {

        }
    }

    public void test_encryption_decryption_lifecycle() throws Exception {
        byte[] encryptedData = onEncryptDataWithRemotePublicKey("Hello world");
        assertTrue(!"Hello world".equals(encryptedData));
        String decryptedData = onDecrypringDataWithRemotePrivateKey(encryptedData);
        assertEquals("Hello world", decryptedData);
    }

    private String onDecrypringDataWithRemotePrivateKey(byte[] encryptedData) throws IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException {
        return rsaManager.decrypt(encryptedData, remotePrivateKey);
    }

    private byte[] onEncryptDataWithRemotePublicKey(String data) throws Exception {
        return rsaManager.encrypt(data, remotePublicKey);
    }

    public void test_save_remote_public_key() throws Exception {
        PublicKey publicKey = PublicKeyReader.get(getContext().getResources().openRawResource(R.raw.test));
        assertNotNull(publicKey);
        remotePublicKey = publicKey;
        byte[] encryptedData = onEncryptDataWithRemotePublicKey("Hello world");
        assertTrue(!"Hello world".equals(encryptedData));
    }

}
