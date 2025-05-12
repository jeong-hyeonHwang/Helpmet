package com.a303.helpmet.framwork.usage.service

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

                if (currentPackage == targetPackage) {
                    if (currentPackage != lastPackage) {
                        lastPackage = currentPackage
                        lastStartTime = now
                    } else {
                        val elapsed = now - lastStartTime
                        if (elapsed in 15000..16000) {
                            showAlertNotification(currentPackage)
                        }
                    }
                } else {
                    // 앱을 나가면 타이머 초기화
                    if (lastPackage == targetPackage) {
                        lastPackage = null
                        lastStartTime = 0L
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
            .setContentTitle(this.getString(R.string.helpmet_alert_title))
            .setContentText(this.getString(R.string.helpmet_alert_content))
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
            .setContentTitle(this.getString(R.string.helpmet_monitor_title))
            .setContentText(this.getString(R.string.helpmet_monitor_content))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
