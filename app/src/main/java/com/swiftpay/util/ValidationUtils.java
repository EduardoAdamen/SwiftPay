// com/swiftpay/util/ValidationUtils.java
package com.swiftpay.util;

import android.text.TextUtils;
import android.util.Patterns;

/**
 * Utilidades de validación para formularios del sistema SwiftPay.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    /**
     * Valida que la contraseña cumpla con los requisitos mínimos.
     * RNF 1.3: Mínimo 8 caracteres, debe contener letras y números.
     * @return mensaje de error o null si es válida
     */
    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "La contraseña es obligatoria";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos " + Constants.MIN_PASSWORD_LENGTH + " caracteres";
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return "La contraseña debe contener al menos una letra";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos un número";
        }
        return null; // válida
    }

    /**
     * Valida un nombre de usuario.
     * @return mensaje de error o null si es válido
     */
    public static String validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return "El nombre de usuario es obligatorio";
        }
        if (username.length() < 3) {
            return "El nombre de usuario debe tener al menos 3 caracteres";
        }
        if (username.length() > 30) {
            return "El nombre de usuario no debe exceder 30 caracteres";
        }
        if (!username.matches("^[a-zA-Z0-9._]+$")) {
            return "El nombre de usuario solo puede contener letras, números, puntos y guiones bajos";
        }
        return null;
    }

    /**
     * Valida un nombre completo.
     * @return mensaje de error o null si es válido
     */
    public static String validateFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "El nombre completo es obligatorio";
        }
        if (fullName.trim().length() < 3) {
            return "El nombre debe tener al menos 3 caracteres";
        }
        if (fullName.length() > 100) {
            return "El nombre no debe exceder 100 caracteres";
        }
        return null;
    }

    /**
     * Valida un correo electrónico.
     * @return mensaje de error o null si es válido
     */
    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) return null; // opcional
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "El correo electrónico no es válido";
        }
        return null;
    }

    /**
     * Valida un número de teléfono mexicano (10 dígitos).
     * @return mensaje de error o null si es válido
     */
    public static String validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) return null; // opcional
        if (!phone.matches("^\\d{10}$")) {
            return "El teléfono debe tener exactamente 10 dígitos";
        }
        return null;
    }

    /**
     * Valida un RFC mexicano.
     * @return mensaje de error o null si es válido
     */
    public static String validateRfc(String rfc) {
        if (TextUtils.isEmpty(rfc)) return null; // opcional
        if (!rfc.matches("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$")) {
            return "El RFC no tiene un formato válido";
        }
        return null;
    }

    /**
     * Valida un código SKU.
     * @return mensaje de error o null si es válido
     */
    public static String validateSku(String sku) {
        if (TextUtils.isEmpty(sku)) {
            return "El SKU es obligatorio";
        }
        if (sku.length() < 3 || sku.length() > 20) {
            return "El SKU debe tener entre 3 y 20 caracteres";
        }
        return null;
    }
}
