package com.swiftpay.data.db;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * TypeConverters para Room.
 * Los timestamps se almacenan como long (millis epoch) directamente en las entities.
 * Este converter proporciona conversión Date <-> Long para uso futuro si fuera necesario.
 */
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
