package com.a303.helpmet.data.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.a303.helpmet.MainActivity
import com.a303.helpmet.R

class AppUsageService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastPackage: String? = null
    private var lastStartTime: Long = 0L

    private val targetPackage = "com.dki.spb_android"

    override fun onCreate() {
        startForeground(1, createPersistentNotification())
        startTracking()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun startTracking() {
        handler.post(object : Runnable {
            override fun run() {
                val currentPackage = getForegroundApp()
                val now = System.currentTimeMillis()

                if (currentPackage != null && currentPackage == targetPackage) {
                    if (currentPackage == lastPackage) {
                        if (now - lastStartTime >= 15000) {
                            showAlertNotification(currentPackage)
                            lastStartTime = now
                        }
                    } else {
                        lastPackage = currentPackage
                        lastStartTime = now
                    }
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()

        val usageEvents = usageStatsManager.queryEvents(now - 16_000, now)
        val event = UsageEvents.Event()
        var lastForegroundApp: String? = null
        var lastEventTime = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND &&
                event.timeStamp > lastEventTime
            ) {
                lastForegroundApp = event.packageName
                lastEventTime = event.timeStamp
            }
        }

        return lastForegroundApp
    }

    private fun showAlertNotification(packageName: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(this, "usage_alert_channel")
            .setSmallIcon(R.drawable.ic_util_helmet)
            .setContentTitle("따릉이를 이용하시나요?")
            .setContentText("헬프멧과 함께 안전하고 편리하게 라이딩해보세요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1002, notification)
    }

    private fun createPersistentNotification(): Notification {

        return NotificationCompat.Builder(this, "usage_monitor_channel")
            .setSmallIcon(R.drawable.ic_util_helmet)
            .setContentTitle("헬프멧")
            .setContentText("따릉이를 기다리고 있어요!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
