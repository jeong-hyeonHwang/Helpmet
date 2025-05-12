package com.a303.helpmet.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject


class WebSocketFrameReceiver {
    private var webSocket: WebSocket? = null

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

    fun disconnect() {
        webSocket?.close(1000, "Done")
    }
}
