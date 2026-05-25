// app/src/main/java/com/swiftpay/util/ValidationUtils.java
package com.swiftpay.util;

import android.util.Patterns;
import java.util.regex.Pattern;

/**
 * Centralized form validation for SwiftPay business rules.
 */
public final class ValidationUtils {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    private static final Pattern RFC_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isValidRFC(String rfc) {
        return rfc != null && RFC_PATTERN.matcher(rfc.trim().toUpperCase()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates temporary and permanent passwords: at least eight characters,
     * one letter and one digit.
     *
     * @param password password to validate
     * @return null when valid, otherwise a user-facing error
     */
    public static String validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            return "La contrasena debe tener minimo 8 caracteres, una letra y un numero";
        }
        return null;
    }
}
