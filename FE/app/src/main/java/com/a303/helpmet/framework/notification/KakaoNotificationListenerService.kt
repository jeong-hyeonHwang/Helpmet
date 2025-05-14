package com.a303.helpmet.framework.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.content.Intent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class KakaoNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""

        // 카카오톡에서 온 알림이고, 제목에 '반납시간이 10분 남았습니다'가 포함되어 있을 때만 반응
        if (packageName.contains("com.kakao.talk") && title.contains("반납시간이 10분 남았습니다")) {
            Log.d("NotificationListener", "따릉이 반납 알림 감지됨")

            val intent = Intent("com.a303.helpmet.RETURN_ALERT_DETECTED")
            sendBroadcast(intent)
        }
    }

}
