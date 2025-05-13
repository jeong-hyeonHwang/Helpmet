package com.a303.helpmet.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject


class WebSocketFrameReceiver {
    private var webSocket: WebSocket? = null
    private var lastSentTime = 0L

    fun connect(url: String, onFrameReceived: (Bitmap) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()


        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(ws: WebSocket, text: String) {
                val json = JSONObject(text)
                if (json.getString("type") == "frame") {
                    val base64 = json.getString("data")
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    onFrameReceived(bitmap)
                }
            }
        })
    }

    fun send(msg: JsonObject) {
        val currentTime = System.currentTimeMillis()
        val debounceInterval = 1000L // 1초 제한

        if (currentTime - lastSentTime >= debounceInterval) {
            try {
                val jsonString = msg.toString()
                val success = webSocket?.send(jsonString) ?: false
                if (success) {
                    lastSentTime = currentTime
                    Log.d("WebSocket", "메시지 전송 성공: $jsonString")
                } else {
                    Log.e("WebSocket", "메시지 전송 실패 (send=false)")
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "send 중 예외 발생", e)
            }
        } else {
            Log.d("WebSocket", "쿨타임 중, 메시지 전송 건너뜀")
        }
    }

    fun disconnect() {
        Log.d("Websocket", "연결 종료")
        webSocket?.close(1000, "Done")
    }
}
