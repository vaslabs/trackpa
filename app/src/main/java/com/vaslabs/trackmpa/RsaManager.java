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

    private final String myPublicKey = "my_rsa.pub";
    private final String savedPublicKey = "rsa.pub";

    public PublicKey getMyPublicKey(Context context) throws Exception {
        PublicKey publicKey = readPublicKeyFromFile(context, myPublicKey);
        if (publicKey == null)
            generateKeys(context);
        publicKey = readPublicKeyFromFile(context, myPublicKey);
        if (publicKey == null)
            throw new Exception("Something went wrong when trying to generate public keys");
        return publicKey;

    }

    private PublicKey readPublicKeyFromFile(Context context, String location) throws IOException {
        PublicKey publicKey;
        FileInputStream rsaPubIO = null;
        try {
            rsaPubIO = context.openFileInput(location);
        } catch (FileNotFoundException e) {
            return null;
        }
        publicKey = readPublicKeyFromInputStream(rsaPubIO);
        return publicKey;
    }

    private void generateKeys(Context context) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.genKeyPair();
        save(context, kp);
    }

    private void save(Context context, KeyPair kp) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(),
                RSAPublicKeySpec.class);
        RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(),
                RSAPrivateKeySpec.class);

        saveToFile(context, "rsa.pub", pub.getModulus(),
                pub.getPublicExponent());
        saveToFile(context, "rsa", priv.getModulus(),
                priv.getPrivateExponent());
    }

    private void saveToFile(Context context, String fileName,
                           BigInteger mod, BigInteger exp) throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE)));
        try {
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (Exception e) {
            throw new IOException("Unexpected error", e);
        } finally {
            oout.close();
        }
    }

    private PublicKey readPublicKeyFromInputStream(InputStream in) throws IOException {

        ObjectInputStream oin =
                new ObjectInputStream(new BufferedInputStream(in));
        try {
            BigInteger m = (BigInteger) oin.readObject();
            BigInteger e = (BigInteger) oin.readObject();
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PublicKey pubKey = fact.generatePublic(keySpec);
            return pubKey;
        } catch (Exception e) {
            throw new RuntimeException("Serialisation error", e);
        } finally {
            oin.close();
        }
    }

    private PrivateKey readPrivateKeyFromInputStream(InputStream in) throws IOException {

        ObjectInputStream oin =
                new ObjectInputStream(new BufferedInputStream(in));
        try {
            BigInteger m = (BigInteger) oin.readObject();
            BigInteger e = (BigInteger) oin.readObject();
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = fact.generatePrivate(keySpec);
            return privateKey;
        } catch (Exception e) {
            throw new RuntimeException("Serialisation error", e);
        } finally {
            oin.close();
        }
    }

    public byte[] encrypt(String data, Context context) throws Exception {
        PublicKey pubKey = readPublicKeyFromFile(context, savedPublicKey);
        return encrypt(data, pubKey);
    }

    public byte[] encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherData = cipher.doFinal(data.getBytes());
        return cipherData;
    }

    public String decrypt(byte[] data, PrivateKey privateKey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] cipherData = cipher.doFinal(data);
        return new String(cipherData);
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
