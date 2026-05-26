package com.swiftpay.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.swiftpay.AlarmReceiver;
import java.util.Calendar;

/**
 * Schedules user-configured recurring alarms (UX-D3).
 *
 * Stores configuration in the safe SharedPreferences file "swiftpay_ux_prefs"
 * so it can be read from a BroadcastReceiver without Room access.
 */
public final class AlarmScheduler {

    private AlarmScheduler() {}

    public static final String PREFS = "swiftpay_ux_prefs";
    public static final String KEY_ALARMS_ENABLED = "alarms_enabled";
    public static final String KEY_ALARM_HOUR = "alarm_hour";
    public static final String KEY_ALARM_MIN = "alarm_min";
    /** Bitmask 0..6 -> Mon..Sun. */
    public static final String KEY_ALARM_DAYS = "alarm_days_mask";

    private static final int REQ_BASE = 1000;

    public static void applyFromPrefs(Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ALARMS_ENABLED, false);
        int hour = prefs.getInt(KEY_ALARM_HOUR, 9);
        int min = prefs.getInt(KEY_ALARM_MIN, 0);
        int mask = prefs.getInt(KEY_ALARM_DAYS, 0b0111110); // default: Lun-Sáb
        scheduleOrCancel(context, enabled, hour, min, mask);
    }

    public static void scheduleOrCancel(Context context, boolean enabled, int hour, int min, int daysMask) {
        if (!enabled || daysMask == 0) {
            cancelAll(context);
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Schedule one weekly repeating alarm per selected weekday.
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            boolean selected = (daysMask & (1 << dayIndex)) != 0;
            PendingIntent pi = buildPendingIntent(context, dayIndex);
            if (!selected) {
                alarmManager.cancel(pi);
                continue;
            }

            long triggerAt = nextTriggerMillis(dayIndex, hour, min);
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    AlarmManager.INTERVAL_DAY * 7,
                    pi
            );
        }
    }

    public static void cancelAll(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            alarmManager.cancel(buildPendingIntent(context, dayIndex));
        }
    }

    private static PendingIntent buildPendingIntent(Context context, int dayIndex) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQ_BASE + dayIndex,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    /** dayIndex 0..6 -> Mon..Sun */
    private static long nextTriggerMillis(int dayIndex, int hour, int min) {
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);

        int targetDow = toCalendarDayOfWeek(dayIndex);
        int currentDow = cal.get(Calendar.DAY_OF_WEEK);
        int delta = targetDow - currentDow;
        if (delta < 0) delta += 7;
        cal.add(Calendar.DAY_OF_YEAR, delta);

        // If it's today but time already passed, schedule next week.
        if (!cal.after(now)) {
            cal.add(Calendar.DAY_OF_YEAR, 7);
        }
        return cal.getTimeInMillis();
    }

    private static int toCalendarDayOfWeek(int dayIndex) {
        switch (dayIndex) {
            case 0: return Calendar.MONDAY;
            case 1: return Calendar.TUESDAY;
            case 2: return Calendar.WEDNESDAY;
            case 3: return Calendar.THURSDAY;
            case 4: return Calendar.FRIDAY;
            case 5: return Calendar.SATURDAY;
            case 6: return Calendar.SUNDAY;
            default: return Calendar.MONDAY;
        }
    }
}

