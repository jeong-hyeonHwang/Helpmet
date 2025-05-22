package com.a303.helpmet.util.handler

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat

class WifiScanner(context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val receiver: BroadcastReceiver

    var onScanResult: ((List<ScanResult>) -> Unit)? = null

    init {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val hasLocation = ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasWifiState = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED

                    if (hasLocation && hasWifiState) {
                        try {
                            val results = wifiManager.scanResults
                            onScanResult?.invoke(results)
                        } catch (e: SecurityException) {
                            Log.e("WifiScanner", "SecurityException while accessing scanResults", e)
                        }
                    } else {
                        Log.w("WifiScanner", "Missing required permissions: FINE_LOCATION or WIFI_STATE")
                    }
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    fun scan(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return  // 권한 없으면 아무것도 안 함
        }

        try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(receiver)
    }
}