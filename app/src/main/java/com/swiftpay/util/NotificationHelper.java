// app/src/main/java/com/swiftpay/util/NotificationHelper.java
package com.swiftpay.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.swiftpay.AlarmReceiver;
import com.swiftpay.R;
import com.swiftpay.util.AlarmScheduler;

/**
 * Centralised helper for SwiftPay notification management.
 * UX-D1: Vibration and sound alerts.
 * UX-D2: Configurable notification sound.
 * UX-D4: Contextual icons per event type.
 * UX-D5: Silence action embedded in the notification.
 */
public final class NotificationHelper {

    public static final String CHANNEL_ID = "swiftpay_alerts";
    public static final String ACTION_SILENCE = "com.swiftpay.ACTION_SILENCE_ALARM";

    private NotificationHelper() {
    }

    /**
     * Creates the high-importance notification channel.
     * Must be called from {@link com.swiftpay.SwiftPayApplication#onCreate()}.
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            // UX-D2: apply user-selected sound to the channel when available.
            Uri soundUri = null;
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences(AlarmScheduler.PREFS, Context.MODE_PRIVATE);
                String sound = prefs.getString("notification_sound", null);
                if (sound != null && !sound.trim().isEmpty()) {
                    soundUri = Uri.parse(sound);
                }
            } catch (Exception ignored) {}
            if (soundUri != null) {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                channel.setSound(soundUri, attrs);
            }

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * On Android O+ the sound is controlled by the NotificationChannel.
     * Recreates the channel so the user's sound preference takes effect.
     */
    public static void recreateNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                try {
                    manager.deleteNotificationChannel(CHANNEL_ID);
                } catch (Exception ignored) {}
            }
        }
        createNotificationChannel(context);
    }

    /**
     * Sends a high-priority notification with the default icon and vibration.
     *
     * @param context        application context
     * @param title          notification title
     * @param message        notification body
     * @param notificationId unique identifier to update/cancel this notification
     */
    public static void sendNotification(Context context, String title, String message, int notificationId) {
        sendNotification(context, title, message, notificationId, R.drawable.ic_notifications, null);
    }

    /**
     * Sends a notification with a contextual icon, optional custom sound,
     * and a silence action button (UX-D5).
     *
     * @param context        application context
     * @param title          notification title
     * @param message        notification body
     * @param notificationId unique identifier
     * @param iconResId      contextual drawable resource for the small icon
     * @param soundUri       optional custom tone URI from user preferences; null for default
     */
    public static void sendNotification(Context context, String title, String message,
                                        int notificationId, @DrawableRes int iconResId,
                                        Uri soundUri) {

        // UX-D5: Build a PendingIntent that the user can tap to silence the alarm.
        Intent silenceIntent = new Intent(context, AlarmReceiver.class);
        silenceIntent.setAction(ACTION_SILENCE);
        PendingIntent silencePending = PendingIntent.getBroadcast(
                context, notificationId, silenceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setAutoCancel(true)
                .addAction(R.drawable.ic_notifications, context.getString(R.string.notification_silence), silencePending);

        if (soundUri != null) {
            builder.setSound(soundUri);
        }

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS permission not granted on Android 13+.
            // Handled gracefully — the notification simply won't show.
            e.printStackTrace();
        }
    }

    /**
     * Returns the appropriate drawable icon for a given system event type.
     * UX-D4: Notifications include a contextual icon.
     *
     * @param eventType the event type string (e.g. NEW_SALE, NEW_CLIENT, CASH_DIFFERENCE)
     * @return drawable resource id for the contextual icon
     */
    @DrawableRes
    public static int getIconForEventType(String eventType) {
        if (eventType == null) {
            return R.drawable.ic_notifications;
        }
        switch (eventType) {
            case Constants.EVENT_NEW_SALE:
                return R.drawable.ic_point_of_sale;
            case Constants.EVENT_NEW_CLIENT:
                return R.drawable.ic_people;
            case Constants.EVENT_CASH_DIFFERENCE:
                return R.drawable.ic_payments;
            default:
                return R.drawable.ic_notifications;
        }
    }
}
