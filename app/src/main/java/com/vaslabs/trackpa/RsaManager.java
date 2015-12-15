package com.vaslabs.trackpa;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import javax.crypto.Cipher;


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
