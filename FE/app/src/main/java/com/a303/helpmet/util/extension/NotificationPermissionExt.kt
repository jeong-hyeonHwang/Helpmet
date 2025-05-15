package com.a303.helpmet.util.extension

import android.content.Context
import android.provider.Settings

fun Context.hasNotificationListenerPermission(): Boolean {
    val pkgName = packageName
    val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return enabledListeners?.contains(pkgName) == true
}