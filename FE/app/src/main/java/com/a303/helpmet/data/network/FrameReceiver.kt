package com.a303.helpmet.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import okhttp3.*
import org.json.JSONObject

class FrameReceiver {

    private var webSocket: WebSocket? = null

    fun connect(serverUrl: String, onBitmapReceived: (Bitmap) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder().url(serverUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "연결됨")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    if (json.getString("type") == "frame") {
                        val base64 = json.getString("data")
                        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        onBitmapReceived(bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "프레임 처리 실패: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "연결 실패: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                Log.d("WebSocket", "연결 종료 중: $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "연결 종료됨: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "종료")
    }
}
