package com.swiftpay.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilidades para formateo de moneda MXN.
 */
public final class CurrencyUtils {

    private CurrencyUtils() {}

    private static final Locale MEXICO_LOCALE = new Locale("es", "MX");

    public static String format(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(MEXICO_LOCALE);
        return nf.format(amount);
    }

    public static String formatSimple(double amount) {
        return String.format(MEXICO_LOCALE, "$%,.2f", amount);
    }
}
