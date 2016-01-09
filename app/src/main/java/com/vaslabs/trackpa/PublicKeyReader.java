package com.vaslabs.trackpa;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by vnicolaou on 12/12/15.
 */
public class PublicKeyReader {

        public static PublicKey get(InputStream inputStream)
                throws Exception {

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder sb = new StringBuilder(256);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String base64 = sb.toString();
            byte[] keyBytes = Base64.decode(base64, Base64.DEFAULT);
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }

}
