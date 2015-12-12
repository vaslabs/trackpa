package com.vaslabs.trackmpa;

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class RsaManager {

    private final String savedPublicKey = "rsa.pub";


    public byte[] encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherData = cipher.doFinal(data.getBytes());
        return cipherData;
    }

    public void saveRemoteKey(String data, Context mContext) throws IOException {
        FileOutputStream fos = mContext.openFileOutput(savedPublicKey, Context.MODE_PRIVATE);
        byte[] bytes = data.getBytes();
        fos.write(bytes, 0, bytes.length);
        fos.close();
    }

    public PublicKey getRemotePublicKey(Context context) throws Exception {
        PublicKey publicKey = PublicKeyReader.get(context.openFileInput(savedPublicKey));
        return publicKey;
    }
}
