package com.swiftpay.util;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidRFC(String rfc) {
        if (rfc == null || rfc.isEmpty()) return false;
        return Pattern.compile("^[A-Z\u00D1&]{3,4}[0-9]{6}[A-Z0-9]{3}$").matcher(rfc.toUpperCase()).matches();
    }

    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) return "Password must be at least 8 characters long";
        return null;
    }
}
