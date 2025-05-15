package com.a303.helpmet.util.handler

import android.content.Context
import android.net.wifi.WifiManager
import java.util.Locale

fun getGatewayIp(context: Context): String? {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val dhcpInfo = wifiManager.dhcpInfo
    val ip = dhcpInfo.gateway
    return String.format(Locale.US,
        "%d.%d.%d.%d",
        (ip and 0xff),
        (ip shr 8 and 0xff),
        (ip shr 16 and 0xff),
        (ip shr 24 and 0xff)
    )
}
