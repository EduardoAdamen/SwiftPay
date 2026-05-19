// com/swiftpay/util/PasswordUtils.java
package com.swiftpay.util;

import android.util.Base64;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades para hashing y verificacion de contrasenas.
 * RNF 1.2: Algoritmo de hash seguro para contrasenas usando PBKDF2WithHmacSHA256.
 */
public final class PasswordUtils {

    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private PasswordUtils() {}

    /**
     * Genera un hash PBKDF2 de la contrasena proporcionada con un salt aleatorio.
     * @param password contrasena en texto plano
     * @return hash en formato Base64 "salt:hash"
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = generateHash(password.toCharArray(), salt);
            
            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + 
                   Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear la contrasena", e);
        }
    }

    /**
     * Verifica si una contrasena en texto plano coincide con un hash PBKDF2.
     * @param password contrasena en texto plano
     * @param hashedPassword hash PBKDF2 almacenado (formato "salt:hash")
     * @return true si coinciden
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.contains(":")) {
            return false;
        }
        
        try {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) return false;
            
            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] expectedHash = Base64.decode(parts[1], Base64.NO_WRAP);
            byte[] actualHash = generateHash(password.toCharArray(), salt);
            
            if (expectedHash.length != actualHash.length) return false;
            
            int diff = 0;
            for (int i = 0; i < expectedHash.length; i++) {
                diff |= expectedHash[i] ^ actualHash[i];
            }
            return diff == 0;
            
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] generateHash(char[] password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}