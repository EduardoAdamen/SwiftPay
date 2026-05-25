// app/src/main/java/com/swiftpay/AlarmReceiver.java
package com.swiftpay;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.swiftpay.util.NotificationHelper;

/**
 * BroadcastReceiver for scheduled SwiftPay alarms.
 * UX-D3: Handles recurring alarm broadcasts.
 * UX-D5: Supports a silence action that cancels the repeating alarm.
 * UX-D6: When the app is in the foreground, posts an event so the hosting
 *         Activity can show a full-screen DialogFragment instead of a
 *         system notification.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** Simple in-memory flag checked by the Activity to show a full-screen dialog. */
    private static volatile boolean pendingForegroundAlert = false;
    private static String pendingAlertTitle = "";
    private static String pendingAlertMessage = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // UX-D5: User tapped "Silence" on the notification — cancel the repeating alarm.
        if (NotificationHelper.ACTION_SILENCE.equals(action)) {
            cancelRepeatingAlarm(context);
            return;
        }

        String title = context.getString(R.string.notification_reminder_title);
        String message = context.getString(R.string.notification_reminder_message);

        // UX-D6: If the app is in the foreground, flag a pending alert for the Activity
        // to display a full-screen DialogFragment instead of a notification.
        if (isAppInForeground()) {
            pendingForegroundAlert = true;
            pendingAlertTitle = title;
            pendingAlertMessage = message;
            // The hosting Activity checks consumePendingAlert() on each onResume/tick.
        } else {
            NotificationHelper.sendNotification(
                    context, title, message, 1001,
                    R.drawable.ic_notifications, null);
        }
    }

    /**
     * Checks whether the application is currently in the foreground using
     * {@link ProcessLifecycleOwner}.
     */
    private boolean isAppInForeground() {
        try {
            Lifecycle.State state = ProcessLifecycleOwner.get().getLifecycle().getCurrentState();
            return state.isAtLeast(Lifecycle.State.STARTED);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cancels any repeating alarm scheduled under the standard PendingIntent.
     */
    private void cancelRepeatingAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * Consumes and resets the pending foreground alert.
     *
     * @return true if an alert was pending
     */
    public static boolean consumePendingAlert() {
        if (pendingForegroundAlert) {
            pendingForegroundAlert = false;
            return true;
        }
        return false;
    }

    /** Returns the title of the last pending foreground alert. */
    public static String getPendingAlertTitle() {
        return pendingAlertTitle;
    }

    /** Returns the message of the last pending foreground alert. */
    public static String getPendingAlertMessage() {
        return pendingAlertMessage;
    }
}