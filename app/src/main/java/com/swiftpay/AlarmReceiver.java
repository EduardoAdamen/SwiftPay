package com.swiftpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.swiftpay.util.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionStr = intent.getAction();
        if ("com.swiftpay.ACTION_SILENCE_ALARM".equals(actionStr)) {
            // Logic to silence or cancel repeating alarm
        } else {
            NotificationHelper.createNotificationChannel(context);
            NotificationHelper.sendNotification(context, "Recordatorio SwiftPay", "Revisa las novedades en el panel de control.", 1001);
            
            // Logic for fullscreen alert if app is foreground could go here or via EventBus
        }
    }
}