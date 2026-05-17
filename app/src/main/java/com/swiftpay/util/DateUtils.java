package com.swiftpay.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilidades para formateo y conversión de fechas.
 */
public final class DateUtils {

    private DateUtils() {}

    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_SHORT, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static long now() {
        return System.currentTimeMillis();
    }
}
