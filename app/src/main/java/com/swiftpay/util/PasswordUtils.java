// com/swiftpay/util/PasswordUtils.java
package com.swiftpay.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidades para hashing y verificación de contraseñas con BCrypt.
 * RNF 1.2: Algoritmo de hash seguro para contraseñas.
 */
public final class PasswordUtils {

    private PasswordUtils() {}

    /**
     * Genera un hash BCrypt de la contraseña proporcionada.
     * @param password contraseña en texto plano
     * @return hash BCrypt
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt.
     * @param password contraseña en texto plano
     * @param hashedPassword hash BCrypt almacenado
     * @return true si coinciden
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
