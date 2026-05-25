// app/src/main/java/com/swiftpay/util/PasswordUtils.java
package com.swiftpay.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing utilities based exclusively on jbcrypt.
 */
public final class PasswordUtils {

    private static final int BCRYPT_COST = 12;

    private PasswordUtils() {
    }

    /**
     * Hashes a plain text password with jbcrypt.
     *
     * @param password plain text password
     * @return bcrypt hash
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("La contrasena no puede ser nula.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifies a password against a stored bcrypt hash in constant-time library code.
     *
     * @param password plain text password
     * @param hashedPassword stored bcrypt hash
     * @return true when both match
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        return password != null
                && hashedPassword != null
                && hashedPassword.startsWith("$2")
                && BCrypt.checkpw(password, hashedPassword);
    }
}
