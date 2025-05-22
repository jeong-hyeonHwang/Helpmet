package com.a303.helpmet.framework.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannelManager {
    fun setupNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertChannel = NotificationChannel(
                "usage_alert_channel",
                "App Usage Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            val monitorChannel = NotificationChannel(
                "usage_monitor_channel",
                "App Usage Monitor Alerts",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(monitorChannel)
        }
    }
}