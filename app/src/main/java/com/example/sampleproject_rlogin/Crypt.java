package com.example.sampleproject_rlogin;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Crypt {
    private static final String ALIAS = "app_key";
    
        private static SecretKey getKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if (!ks.containsAlias(ALIAS)) {
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            kg.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build());
            kg.generateKey();
        }
        return (SecretKey) ks.getKey(ALIAS, null);
    }
    
    public static String enc(String plain) {
        if (plain == null) return null;
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] iv = c.getIV();
            byte[] enc = c.doFinal(plain.getBytes("UTF-8"));
            return Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(enc, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return plain; // fallback
        }
    }
    
    public static String dec(String encStr) {
        if (encStr == null) return null;
        try {
            String[] parts = encStr.split(":");
            if (parts.length != 2) return encStr;
            byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] enc = Base64.decode(parts[1], Base64.NO_WRAP);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(128, iv));
            return new String(c.doFinal(enc), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return encStr; // fallback
        }
    }
}
